package it.dedagroup.panelapp.log.configuration;

import aQute.bnd.annotation.metatype.Meta;
import com.liferay.portal.configuration.metatype.annotations.ExtendedObjectClassDefinition;

@ExtendedObjectClassDefinition(category = "logger-configuration", scope = ExtendedObjectClassDefinition.Scope.COMPANY)
@Meta.OCD(
        id = "it.dedagroup.panelapp.log.configuration.PanelappLogPortletConfiguration",
        localization = "content/Language",
        name = "log-panelapp-configuration-name"
)public interface PanelappLogPortletConfiguration {
    @Meta.AD(required = false, description = "Inserire il percorso della directory dei file di log.")
    String configurationPath();

    @Meta.AD(required = false, description = "Inserire i ruoli che hanno accesso al pannello dei log")
    String[] roles();

    @Meta.AD(required = false, description = "Inserire la dimensione predefinita dei file di log (MB)")
    String fileSizeLimit();
}