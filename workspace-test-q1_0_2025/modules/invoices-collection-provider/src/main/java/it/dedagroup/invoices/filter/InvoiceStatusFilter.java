package it.dedagroup.invoices.filter;

import com.liferay.info.filter.InfoFilter;
import com.liferay.info.type.Keyed;

public class InvoiceStatusFilter implements InfoFilter, Keyed {

    private final String status;

    public InvoiceStatusFilter(String status) {
        this.status = (status == null || status.isBlank()) ? null : status.trim();
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String getKey() {
        return "invoiceStatus";
    }

    @Override
    public String getFilterTypeName() {
        return "select";
    }
}