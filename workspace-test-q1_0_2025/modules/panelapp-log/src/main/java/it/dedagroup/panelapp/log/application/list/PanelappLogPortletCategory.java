package it.dedagroup.panelapp.log.application.list;

import com.liferay.application.list.BasePanelCategory;
import com.liferay.application.list.PanelCategory;
import com.liferay.portal.configuration.module.configuration.ConfigurationProvider;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.util.ResourceBundleUtil;
import it.dedagroup.panelapp.log.configuration.PanelappLogPortletConfiguration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Locale;
import java.util.ResourceBundle;

@Component(configurationPid = {"it.dedagroup.panelapp.log.configuration.PanelappLogPortletConfiguration"}, immediate = true, property = {"panel.category.key=control_panel", "panel.category.order:Integer=100"}, service = {PanelCategory.class})
public class PanelappLogPortletCategory extends BasePanelCategory {
    @Reference
    private ConfigurationProvider _configurationProvider;

    public String getKey() {
        return "Logger";
    }

    public String getLabel(Locale locale) {
        ResourceBundle resourceBundle = ResourceBundleUtil.getBundle("content.Language", locale,
                getClass());
        return LanguageUtil.get(resourceBundle, "Log");
    }

    public boolean isShow(PermissionChecker permissionChecker, Group group) throws PortalException {
        PanelappLogPortletConfiguration configuration = (PanelappLogPortletConfiguration)this._configurationProvider.getSystemConfiguration(PanelappLogPortletConfiguration.class);
        String[] roles = configuration.roles();
        for (String role : roles) {
            boolean hasAuth = RoleLocalServiceUtil.hasUserRole(permissionChecker
                    .getUserId(), group.getCompanyId(), role, true);
            if (hasAuth)
                return true;
        }
        return false;
    }
}