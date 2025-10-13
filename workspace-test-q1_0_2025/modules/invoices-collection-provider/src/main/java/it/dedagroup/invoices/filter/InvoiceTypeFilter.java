package it.dedagroup.invoices.filter;

import com.liferay.info.filter.InfoFilter;
import com.liferay.info.type.Keyed;

public class InvoiceTypeFilter implements InfoFilter, Keyed {

    private final String type;

    public InvoiceTypeFilter(String type) {
        this.type = (type == null || type.isBlank()) ? null : type.trim();
    }

    public String getType() {
        return type;
    }

    @Override
    public String getKey() {
        return "invoiceType";
    }

    @Override
    public String getFilterTypeName() {
        return "select";
    }
}
