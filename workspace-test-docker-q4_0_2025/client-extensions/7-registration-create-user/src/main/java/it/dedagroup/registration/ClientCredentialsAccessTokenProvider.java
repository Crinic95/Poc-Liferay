package it.dedagroup.registration;

import it.dedagroup.registration.dto.TokenResponse;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

@Component
public class ClientCredentialsAccessTokenProvider implements AccessTokenProvider {

    private static final List<String> CONFIG_DIR_CANDIDATES = List.of(
            "/opt/liferay/routes",
            "/opt/liferay/osgi/client-extensions"
    );

    private static final String TARGET_PREFIX = "registration-create-user-oahs.oauth2";

    private final RestClient restClient;
    private final Environment environment;

    private volatile String cachedAccessToken;
    private volatile Instant expiresAt;

    public ClientCredentialsAccessTokenProvider(
            RestClient.Builder builder, Environment environment) {

        this.restClient = builder.build();
        this.environment = environment;
    }

    @Override
    public synchronized String getAccessToken() {
        if (_isValid()) {
            return cachedAccessToken;
        }

        OAuthConfig config = _resolveOAuthConfig();

        TokenResponse tokenResponse = _fetchToken(config);

        cachedAccessToken = tokenResponse.accessToken();

        long expiresIn = tokenResponse.expiresIn() != null ? tokenResponse.expiresIn() : 300L;
        expiresAt = Instant.now().plusSeconds(Math.max(30, expiresIn - 30));

        return cachedAccessToken;
    }

    private boolean _isValid() {
        return cachedAccessToken != null &&
                expiresAt != null &&
                Instant.now().isBefore(expiresAt);
    }

    private OAuthConfig _resolveOAuthConfig() {
        Map<String, String> allProps = new LinkedHashMap<>();

        _loadFromSpringEnvironment(allProps);
        _loadFromSystemEnv(allProps);
        _loadFromSystemProperties(allProps);
        _loadFromMountedDirectories(allProps);

        _debugOAuthSummary(allProps);

        String liferayBaseUrl = _firstNonBlank(
                allProps.get("liferay.base-url"),
                environment.getProperty("liferay.base-url"),
                System.getenv("LIFERAY_BASE_URL"),
                "http://liferay:8080"
        );

        String clientId = _resolveBySuffix(
                allProps,
                TARGET_PREFIX + ".headless.server.client.id",
                ".oauth2.headless.server.client.id",
                ".client.id"
        );

        String clientSecret = _resolveBySuffix(
                allProps,
                TARGET_PREFIX + ".headless.server.client.secret",
                ".oauth2.headless.server.client.secret",
                ".client.secret"
        );

        String tokenUri = _resolveBySuffix(
                allProps,
                TARGET_PREFIX + ".token.uri",
                ".oauth2.token.uri",
                ".token.uri"
        );

        System.out.println("Resolved liferayBaseUrl = " + liferayBaseUrl);
        System.out.println("Resolved clientId present = " + _hasText(clientId));
        System.out.println("Resolved clientSecret present = " + _hasText(clientSecret));
        System.out.println("Resolved tokenUri = " + tokenUri);

        if (!_hasText(clientId)) {
            throw new IllegalStateException(
                    "Unable to resolve OAuth client id dynamically for prefix " + TARGET_PREFIX);
        }

        if (!_hasText(clientSecret)) {
            throw new IllegalStateException(
                    "Unable to resolve OAuth client secret dynamically for prefix " + TARGET_PREFIX);
        }

        if (!_hasText(tokenUri)) {
            throw new IllegalStateException(
                    "Unable to resolve OAuth token URI dynamically for prefix " + TARGET_PREFIX);
        }

        return new OAuthConfig(liferayBaseUrl, clientId, clientSecret, tokenUri);
    }

    private void _debugOAuthSummary(Map<String, String> props) {

        boolean clientIdFound = props.keySet().stream()
                .anyMatch(k -> _normalizeKey(k).endsWith(".client.id"));

        boolean clientSecretFound = props.keySet().stream()
                .anyMatch(k -> _normalizeKey(k).endsWith(".client.secret"));

        boolean tokenUriFound = props.keySet().stream()
                .anyMatch(k -> _normalizeKey(k).endsWith(".token.uri"));

        System.out.println("OAuth config detected:");
        System.out.println(" - clientId present: " + clientIdFound);
        System.out.println(" - clientSecret present: " + clientSecretFound);
        System.out.println(" - tokenUri present: " + tokenUriFound);
    }

    private void _loadFromSpringEnvironment(Map<String, String> target) {
        if (!(environment instanceof ConfigurableEnvironment configurableEnvironment)) {
            return;
        }

        for (org.springframework.core.env.PropertySource<?> propertySource :
                configurableEnvironment.getPropertySources()) {

            if (propertySource instanceof EnumerablePropertySource<?> enumerablePropertySource) {
                for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                    Object value = enumerablePropertySource.getProperty(propertyName);

                    if (value != null) {
                        target.putIfAbsent(propertyName, String.valueOf(value));
                    }
                }
            }
        }
    }

    private void _loadFromSystemEnv(Map<String, String> target) {
        System.getenv().forEach((key, value) -> {
            if (_hasText(key) && _hasText(value)) {
                target.putIfAbsent(key, value);
            }
        });
    }

    private void _loadFromSystemProperties(Map<String, String> target) {
        Properties properties = System.getProperties();

        for (String name : properties.stringPropertyNames()) {
            String value = properties.getProperty(name);

            if (_hasText(name) && _hasText(value)) {
                target.putIfAbsent(name, value);
            }
        }
    }

    private void _loadFromMountedDirectories(Map<String, String> target) {
        for (String dir : CONFIG_DIR_CANDIDATES) {
            System.out.println("Trying config dir = " + dir);
            _loadDirectory(target, dir);
        }
    }

    private void _loadDirectory(Map<String, String> target, String dir) {
        if (!_hasText(dir)) {
            return;
        }

        Path basePath = Paths.get(dir);

        if (!Files.isDirectory(basePath)) {
            System.out.println("Directory not found -> " + dir);
            return;
        }

        try (Stream<Path> stream = Files.walk(basePath)) {
            stream.filter(Files::isRegularFile).forEach(path -> _loadFile(target, path));
        }
        catch (IOException ioException) {
            throw new IllegalStateException("Unable to read config directory " + dir, ioException);
        }
    }

    private void _loadFile(Map<String, String> target, Path path) {
        String fileName = path.getFileName().toString();
        String lower = fileName.toLowerCase(Locale.ROOT);

        try {
            if (_isBinaryFile(lower)) {
                System.out.println("Skipped binary file -> " + path);
                return;
            }

            if (lower.endsWith(".properties")) {
                Properties properties = new Properties();

                try (var inputStream = Files.newInputStream(path)) {
                    properties.load(inputStream);
                }

                for (String name : properties.stringPropertyNames()) {
                    target.putIfAbsent(name, properties.getProperty(name));
                }
                return;
            }

            if (lower.endsWith(".yaml") || lower.endsWith(".yml")) {
                YamlPropertiesFactoryBean factoryBean = new YamlPropertiesFactoryBean();
                factoryBean.setResources(new FileSystemResource(path.toFile()));

                Properties properties = factoryBean.getObject();

                if (properties != null) {
                    for (String name : properties.stringPropertyNames()) {
                        target.putIfAbsent(name, properties.getProperty(name));
                    }
                }

                System.out.println("Loaded yaml file -> " + path);
                return;
            }

            String value = Files.readString(path, StandardCharsets.UTF_8).trim();

            if (!value.isBlank()) {
                target.putIfAbsent(fileName, value);
                if (_normalizeKey(fileName).contains(TARGET_PREFIX)) {
                    System.out.println("Loaded OAuth config key -> " + fileName);
                }
            }
        }
        catch (MalformedInputException exception) {
            System.out.println("Skipped non-utf8 file -> " + path);
        }
        catch (Exception exception) {
            throw new IllegalStateException("Unable to load config file " + path, exception);
        }
    }

    private String _resolveBySuffix(Map<String, String> props, String... preferredSuffixes) {
        for (String suffix : preferredSuffixes) {
            String normalizedSuffix = _normalizeKey(suffix);

            for (Map.Entry<String, String> entry : props.entrySet()) {
                String rawKey = entry.getKey();
                String value = entry.getValue();

                if (!_hasText(rawKey) || !_hasText(value)) {
                    continue;
                }

                String normalizedKey = _normalizeKey(rawKey);

                if (normalizedKey.endsWith(normalizedSuffix)) {
                    System.out.println("Resolved key " + rawKey + " -> normalized as " + normalizedKey);
                    return value.trim();
                }
            }
        }

        return null;
    }

    private boolean _isBinaryFile(String lowerFileName) {
        return lowerFileName.endsWith(".zip")
                || lowerFileName.endsWith(".jar")
                || lowerFileName.endsWith(".war")
                || lowerFileName.endsWith(".class")
                || lowerFileName.endsWith(".png")
                || lowerFileName.endsWith(".jpg")
                || lowerFileName.endsWith(".jpeg")
                || lowerFileName.endsWith(".gif")
                || lowerFileName.endsWith(".pdf");
    }

    private boolean _isSecretKey(String key) {
        return _normalizeKey(key).contains("secret");
    }

    private String _normalizeKey(String key) {
        if (key == null) {
            return "";
        }

        String normalized = key.trim().toLowerCase(Locale.ROOT)
                .replace('_', '.')
                .replace('-', '.');

        while (normalized.contains("..")) {
            normalized = normalized.replace("..", ".");
        }

        return normalized;
    }

    private TokenResponse _fetchToken(OAuthConfig config) {
        String basicAuth = Base64.getEncoder().encodeToString(
                (config.clientId() + ":" + config.clientSecret()).getBytes(StandardCharsets.UTF_8)
        );

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        String resolvedTokenUri = config.tokenUri().startsWith("http")
                ? config.tokenUri()
                : config.liferayBaseUrl() + config.tokenUri();

        System.out.println("Fetching token from " + resolvedTokenUri);

        return restClient.post()
                .uri(resolvedTokenUri)
                .header(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(TokenResponse.class);
    }

    private boolean _hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String _firstNonBlank(String... values) {
        for (String value : values) {
            if (_hasText(value)) {
                return value.trim();
            }
        }

        return null;
    }

    private record OAuthConfig(
            String liferayBaseUrl,
            String clientId,
            String clientSecret,
            String tokenUri) {
    }
}