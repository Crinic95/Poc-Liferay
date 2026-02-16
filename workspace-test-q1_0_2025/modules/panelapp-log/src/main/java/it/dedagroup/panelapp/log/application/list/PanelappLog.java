package it.dedagroup.panelapp.log.application.list;

import com.liferay.application.list.BasePanelApp;
import com.liferay.application.list.PanelApp;
import com.liferay.portal.kernel.model.Portlet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate = true, property = {
        "panel.app.order:Integer=100",
        "panel.category.key=" + com.liferay.application.list.constants.PanelCategoryKeys.CONTROL_PANEL_CONFIGURATION
}, service = {PanelApp.class})
public class PanelappLog extends BasePanelApp {
    private Portlet portlet;

    public String getPortletId() {
        return "it_dedagroup_logger_LoggerPortlet";
    }

    @Reference(target = "(javax.portlet.name=it_dedagroup_logger_LoggerPortlet)", unbind = "-")
    public void setPortlet(Portlet portlet) {
        this.portlet = portlet;
    }

    public Portlet getPortlet() {
        return this.portlet;
    }
}