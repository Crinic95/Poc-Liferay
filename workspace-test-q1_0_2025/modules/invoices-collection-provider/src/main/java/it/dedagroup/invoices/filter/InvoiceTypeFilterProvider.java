package it.dedagroup.invoices.filter;

import com.liferay.info.filter.InfoFilterProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

@Component(
        service = InfoFilterProvider.class,
        property = {
                "item.class.name=it.dedagroup.invoices.model.Invoice",
                "key=invoiceType"
        }
)
public class InvoiceTypeFilterProvider implements InfoFilterProvider<InvoiceTypeFilter> {

    @Override
    public InvoiceTypeFilter create(Map<String, String[]> values) {
        String value = null;
        if (values != null) {
            String[] raw = values.get("invoiceType");
            if (raw != null && raw.length > 0) {
                value = raw[0];
            }
        }
        return new InvoiceTypeFilter(value);
    }

    @Override
    public String getKey() {
        return "invoiceType";
    }
}