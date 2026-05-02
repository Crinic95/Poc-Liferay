package it.dedagroup.registration;

import it.dedagroup.registration.dto.ObjectActionPayload;
import it.dedagroup.registration.dto.UserAccountRequest;
import it.dedagroup.registration.dto.UserAccountResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegistrationService {

    private final LiferayClient liferayClient;
    private final AccessTokenProvider accessTokenProvider;

    public RegistrationService(
            LiferayClient liferayClient,
            AccessTokenProvider accessTokenProvider) {

        this.liferayClient = liferayClient;
        this.accessTokenProvider = accessTokenProvider;
    }

    // 1. estraggo l'ID dell'object entry dal payload
    // 2. leggo C_RegistrationRequest via headless API
    // 3. controllo status == NEW
    // 4. aggiorno status = PROCESSING
    // 5. costruisco payload UserAccount
    // 6. chiamo POST /o/headless-admin-user/v1.0/user-accounts
    // 7. aggiorno C_RegistrationRequest a DONE oppure ERROR

    public void handleObjectAction(ObjectActionPayload payload) {
        Long objectEntryId = payload.classPK();

        System.out.println("objectEntryId = " + objectEntryId);

        if (objectEntryId == null) {
            throw new IllegalArgumentException("classPK mancante nel payload");
        }

        ObjectActionPayload.Values values =
                payload.objectEntry() != null ? payload.objectEntry().values() : null;

        if (values == null) {
            throw new IllegalArgumentException("objectEntry.values mancante nel payload");
        }

        if (!"NEW".equalsIgnoreCase(_safe(values.statusRegistration()))) {
            System.out.println("Skipping entry " + objectEntryId + " because statusRegistration is not NEW");
            return;
        }

        String accessToken = accessTokenProvider.getAccessToken();

        try {
            liferayClient.updateRegistrationRequest(
                    accessToken,
                    objectEntryId,
                    Map.of("statusRegistration", "PROCESSING")
            );

            UserAccountRequest userRequest = new UserAccountRequest(
                    _required(values.email(), "email"),
                    _required(values.givenName(), "givenName"),
                    _required(values.familyName(), "familyName"),
                    _buildAlternateName(values.email())
            );

            System.out.println("Creating user from registration request " + objectEntryId);

            UserAccountResponse createdUser =
                    liferayClient.createUser(accessToken, userRequest);

            System.out.println("CREATED USER ID = " + createdUser.id());

            Map<String, Object> successBody = new HashMap<>();
            successBody.put("statusRegistration", "DONE");
            successBody.put("createdUserId", createdUser.id());
            successBody.put("errorMessage", "");

            liferayClient.updateRegistrationRequest(
                    accessToken,
                    objectEntryId,
                    successBody
            );
        }
        catch (RestClientResponseException ex) {
            System.out.println(
                    "CREATE USER FAILED: HTTP " + ex.getStatusCode().value() +
                            " - " + ex.getResponseBodyAsString()
            );

            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("statusRegistration", "ERROR");
            errorBody.put(
                    "errorMessage",
                    _truncate(
                            "HTTP " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString(),
                            4000
                    )
            );

            _safeUpdate(accessToken, objectEntryId, errorBody);

            throw ex;
        }
        catch (Exception ex) {
            System.out.println("CREATE USER FAILED: " + ex.getMessage());

            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("statusRegistration", "ERROR");
            errorBody.put("errorMessage", _truncate(ex.getMessage(), 4000));

            _safeUpdate(accessToken, objectEntryId, errorBody);

            throw ex;
        }
    }

    private void _safeUpdate(String accessToken, Long objectEntryId, Map<String, Object> body) {
        try {
            liferayClient.updateRegistrationRequest(accessToken, objectEntryId, body);
        }
        catch (Exception ignored) {
            System.out.println(
                    "Failed to update registration request with ID " +
                            objectEntryId + ": " + ignored.getMessage()
            );
        }
    }

    private String _buildAlternateName(String email) {
        String safeEmail = _required(email, "email");
        int atIndex = safeEmail.indexOf('@');

        if (atIndex > 0) {
            return safeEmail.substring(0, atIndex);
        }

        return safeEmail;
    }

    private String _required(String value, String fieldName) {
        if (!_hasText(value)) {
            throw new IllegalArgumentException("Campo obbligatorio mancante: " + fieldName);
        }

        return value.trim();
    }

    private boolean _hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String _safe(String value) {
        return value == null ? "" : value;
    }

    private String _truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}