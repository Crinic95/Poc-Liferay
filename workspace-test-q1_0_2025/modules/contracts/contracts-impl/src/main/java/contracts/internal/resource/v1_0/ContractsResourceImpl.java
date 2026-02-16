package contracts.internal.resource.v1_0;

import contracts.resource.v1_0.ContractsResource;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * @author NCLCST95H
 */
@Component(
        properties = "OSGI-INF/liferay/rest/v1_0/contracts.properties",
        scope = ServiceScope.PROTOTYPE,
        service = ContractsResource.class
)
public class ContractsResourceImpl extends BaseContractsResourceImpl {

    @Override
    public String getLabel() {
        return "Testo di prova - API";
    }
}