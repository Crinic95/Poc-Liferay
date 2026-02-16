package it.dedagroup.panelapp.log.portlet;

import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.module.configuration.ConfigurationException;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import it.dedagroup.panelapp.log.bean.PanelappLogPortletBundle;
import it.dedagroup.panelapp.log.configuration.PanelappLogPortletConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.portlet.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component(configurationPid = {"it.dedagroup.panelapp.log.configuration.PanelappLogPortletConfiguration"}, immediate = true, property = {"com.liferay.portlet.add-default-resource=true", "com.liferay.portlet.display-category=category.sample", "com.liferay.portlet.header-portlet-css=/css/main.css", "com.liferay.portlet.layout-cacheable=true", "com.liferay.portlet.private-request-attributes=false", "com.liferay.portlet.private-session-attributes=false", "com.liferay.portlet.render-weight=50", "com.liferay.portlet.use-default-template=true", "com.liferay.portlet.instanceable=false", "javax.portlet.display-name=Log Control Dedagroup", "javax.portlet.expiration-cache=0", "javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp", "javax.portlet.name=it_dedagroup_logger_LoggerPortlet", "javax.portlet.resource-bundle=content.Language", "javax.portlet.security-role-ref=power-user,user"}, service = {Portlet.class})
public class PanelappLogPortlet extends MVCPortlet {
    private static final Log log = LogFactoryUtil.getLog(PanelappLogPortlet.class);

    @Reference
    private ConfigurationProvider _configurationProvider;

    public void doView(RenderRequest renderRequest, RenderResponse renderResponse) throws IOException, PortletException {
        try {
            _checkOmniadmin();
        } catch (PortletException|PortalException e) {
            log.warn("Lo user non ha i permessi per vedere la portlet", e);
            renderRequest.setAttribute("errorMessage", "Non hai il permesso d'accesso.");
            include("/customError/customError.jsp", renderRequest, renderResponse);
            return;
        }
        double defaultMaxSizeInMB = 2.0D;
        try {
            double fileSizeLimit;
            PanelappLogPortletConfiguration config = (PanelappLogPortletConfiguration)this._configurationProvider.getSystemConfiguration(PanelappLogPortletConfiguration.class);
            String fileSizeLimitStr = config.fileSizeLimit();
            if (fileSizeLimitStr == null || fileSizeLimitStr.isEmpty()) {
                fileSizeLimit = 10.0D;
            } else {
                fileSizeLimit = Double.parseDouble(fileSizeLimitStr);
            }
            renderRequest.setAttribute("sizeLog", Double.valueOf(fileSizeLimit));
            log.info("File size limit from configuration: " + fileSizeLimit);
        } catch (ConfigurationException e) {
            log.error("Error fetching configuration", (Throwable)e);
            renderRequest.setAttribute("errorMessage", "Errore nel recupero della configurazione.");
        }
        renderRequest.setAttribute("logs", getLogs(renderRequest, defaultMaxSizeInMB));
        List<PanelappLogPortletBundle> bundles = new ArrayList<>();
        Collections.reverse(bundles);
        renderRequest.setAttribute("bundles", bundles);
        super.doView(renderRequest, renderResponse);
    }

    private List<String> getLogs(RenderRequest renderRequest, double maxSizeInMB) {
        List<String> logs = new ArrayList<>();
        File folder = new File(getPathLogs());
        File[] files = folder.listFiles();
        if (files != null) {
            LocalDate currentDate = LocalDate.now();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".log")) {
                    double fileSizeInMB = file.length() / 1048576.0D;
                    LocalDate fileLastModifiedDate = getFileLastModifiedDate(file);
                    if (fileSizeInMB <= maxSizeInMB && fileLastModifiedDate != null &&
                            !fileLastModifiedDate.isBefore(currentDate.minus(6L, ChronoUnit.DAYS))) {
                        String fileEntry = file.getName() + " (" + String.format("%.2f", new Object[] { Double.valueOf(fileSizeInMB) }) + " MB)";
                        logs.add(fileEntry);
                    }
                }
            }
        }
        Collections.reverse(logs);
        return logs;
    }

    private List<String> getLogs(ResourceRequest resourceRequest, double maxSizeInMB, boolean lastWeek, boolean allFiles, LocalDate startDate, LocalDate endDate) {
        List<String> logs = new ArrayList<>();
        File folder = new File(getPathLogs());
        File[] files = folder.listFiles();
        if (files != null) {
            LocalDate currentDate = LocalDate.now();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".log")) {
                    double fileSizeInMB = file.length() / 1048576.0D;
                    LocalDate fileLastModifiedDate = getFileLastModifiedDate(file);
                    if (allFiles && fileSizeInMB <= maxSizeInMB) {
                        String fileEntry = file.getName() + " (" + String.format("%.2f", new Object[] { Double.valueOf(fileSizeInMB) }) + " MB)";
                        logs.add(fileEntry);
                    } else if (!allFiles && startDate == null && endDate == null && fileSizeInMB <= maxSizeInMB && fileLastModifiedDate != null && !fileLastModifiedDate.isBefore(currentDate.minus(6L, ChronoUnit.DAYS))) {
                        String fileEntry = file.getName() + " (" + String.format("%.2f", new Object[] { Double.valueOf(fileSizeInMB) }) + " MB)";
                        logs.add(fileEntry);
                    } else if (startDate != null && endDate != null && fileSizeInMB <= maxSizeInMB && fileLastModifiedDate != null && (fileLastModifiedDate

                            .isEqual(startDate) || fileLastModifiedDate.isEqual(endDate) || (
                            !fileLastModifiedDate.isBefore(startDate) && !fileLastModifiedDate.isAfter(endDate)))) {
                        String fileEntry = file.getName() + " (" + String.format("%.2f", new Object[] { Double.valueOf(fileSizeInMB) }) + " MB)";
                        logs.add(fileEntry);
                    }
                }
            }
        }
        Collections.reverse(logs);
        return logs;
    }

    private LocalDate getFileLastModifiedDate(File file) {
        try {
            Path filePath = Paths.get(file.getAbsolutePath(), new String[0]);
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class, new java.nio.file.LinkOption[0]);
            return attrs.lastModifiedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (IOException e) {
            log.warn("Impossibile ottenere la data di ultima modifica per il file: " + file.getName(), e);
            return null;
        }
    }

    public void serveResource(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {
        log.debug("serveResource AlmavivaLoggerPortlet");
        String type = ParamUtil.getString((PortletRequest)resourceRequest, "type");
        if ("download".equals(type)) {
            download(resourceRequest, resourceResponse);
        } else if ("log".equals(type)) {
            log(resourceRequest, resourceResponse);
        } else if ("getLogsList".equals(type)) {
            double maxSizeInMB = ParamUtil.getDouble((PortletRequest)resourceRequest, "maxSize");
            boolean lastWeek = ParamUtil.getBoolean((PortletRequest)resourceRequest, "lastWeek");
            boolean allFiles = ParamUtil.getBoolean((PortletRequest)resourceRequest, "allFiles");
            String startDateStr = ParamUtil.getString((PortletRequest)resourceRequest, "startDate");
            String endDateStr = ParamUtil.getString((PortletRequest)resourceRequest, "endDate");
            LocalDate startDate = null;
            LocalDate endDate = null;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (!startDateStr.isEmpty() && !endDateStr.isEmpty()) {
                startDate = LocalDate.parse(startDateStr, formatter);
                endDate = LocalDate.parse(endDateStr, formatter);
            }
            getLogsList(resourceRequest, resourceResponse, maxSizeInMB, lastWeek, allFiles, startDate, endDate);
        }
    }

    private void getLogsList(ResourceRequest resourceRequest, ResourceResponse resourceResponse, double maxSizeInMB, boolean lastWeek, boolean allFiles, LocalDate startDate, LocalDate endDate) throws IOException {
        log.debug("serveResource getLogsList");
        List<String> logs = getLogs(resourceRequest, maxSizeInMB, lastWeek, allFiles, startDate, endDate);
        JSONArray jsonArray = JSONFactoryUtil.createJSONArray();
        for (String logName : logs)
            jsonArray.put(logName);
        resourceResponse.setContentType("application/json");
        PrintWriter writer = resourceResponse.getWriter();
        writer.write(jsonArray.toString());
    }

    private void download(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException, PortletException {
        log.debug("serveResource download");
        try {
            _checkOmniadmin();
        } catch (PortletException|PortalException e) {
            log.warn("Lo user non ha i permessi per vedere la portlet", e);
            return;
        }
        String logName = ParamUtil.getString((PortletRequest)resourceRequest, "file");
        if (logName.contains(" ("))
            logName = logName.substring(0, logName.indexOf(" ("));
        String path = getPathLogs() + System.getProperty("file.separator") + logName;
        File file = new File(path);
        if (!file.exists())
            throw new PortletException("Log file not found.");
        String fileName = file.getName();
        String contentType = "application/octet-stream";
        resourceResponse.setContentType(contentType);
        resourceResponse.setProperty("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        OutputStream outputStream = resourceResponse.getPortletOutputStream();
        byte[] b = Files.readAllBytes(Paths.get(path, new String[0]));
        outputStream.write(b, 0, b.length);
    }

    private void log(ResourceRequest resourceRequest, ResourceResponse resourceResponse) throws IOException {
        log.debug("log");
        int limit = ParamUtil.getInteger((PortletRequest)resourceRequest, "limit");
        String path = getPath();
        String fileContent = FileUtil.read(path);
        resourceResponse.setContentType("text/plain");
        PrintWriter writer = resourceResponse.getWriter();
        writer.print(readLastLinesFromString(fileContent, limit));
    }

    private String getPath() {
        String separator = System.getProperty("file.separator");
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("'liferay.'yyyy-MM-dd'.log'");
        String logFileName = currentDate.format(formatter);
        String liferayHome = PropsUtil.get("liferay.home");
        String path = liferayHome + separator + "logs" + separator + logFileName;
        return path;
    }

    private String getPathLogs() {
        try {
            PanelappLogPortletConfiguration configuration = (PanelappLogPortletConfiguration)this._configurationProvider.getSystemConfiguration(PanelappLogPortletConfiguration.class);
            if (configuration.configurationPath() == null || configuration.configurationPath().isEmpty()) {
                String separator = System.getProperty("file.separator");
                String liferayHome = PropsUtil.get("liferay.home");
                String path = liferayHome + separator + "logs";
                return path;
            }
            return configuration.configurationPath();
        } catch (ConfigurationException e) {
            log.error((Throwable)e);
            return null;
        }
    }

    private String readLastLinesFromString(String text, int numLines) {
        StringBuilder lastLines = new StringBuilder();
        String[] lines = text.split("\n");
        int startLine = Math.max(lines.length - numLines, 0);
        for (int i = startLine; i < lines.length; i++) {
            lastLines.append(lines[i]);
            lastLines.append("\n");
        }
        return lastLines.toString();
    }

    private void _checkOmniadmin() throws PortletException, PortalException {
        PermissionChecker permissionChecker = PermissionThreadLocal.getPermissionChecker();
        PanelappLogPortletConfiguration configuration = (PanelappLogPortletConfiguration)this._configurationProvider.getSystemConfiguration(PanelappLogPortletConfiguration.class);
        String[] roles = configuration.roles();
        for (String role : roles) {
            boolean hasAuth = RoleLocalServiceUtil.hasUserRole(permissionChecker
                    .getUserId(), permissionChecker.getCompanyId(), role, true);
            if (hasAuth)
                return;
        }
        throw new PortletException("Lo User deve avere un ruolo autorizzato");
    }
}