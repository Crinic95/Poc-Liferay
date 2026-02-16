package it.dedagroup.invoices.contributor;

import com.liferay.portal.kernel.template.TemplateContextContributor;
import it.dedagroup.invoices.collection.InvoicesCollectionProvider;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Component(
        property = {"type=" + TemplateContextContributor.TYPE_GLOBAL},
        service = TemplateContextContributor.class
)
public class InvoicesProviderContextContributor
        implements TemplateContextContributor {

    @Reference
    private InvoicesCollectionProvider _invoicesCountProvider;

    @Override
    public void prepare(Map<String, Object> contextObjects, HttpServletRequest request) {
        contextObjects.put("invoicesCollectionProvider", _invoicesCountProvider);
    }
}