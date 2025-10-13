package it.dedagroup.invoices.filter;

import com.liferay.info.filter.InfoFilter;
import com.liferay.info.type.Keyed;
import java.util.Locale;

public class InvoiceStatusFilter implements InfoFilter, Keyed {

    private final String status;

    public InvoiceStatusFilter(String raw) {
        this.status = normalize(raw);
    }

    public String getStatus() {
        return status;
    }

    @Override public String getKey() {
        return "invoiceStatus";
    }

    @Override public String getFilterTypeName() {
        return "select";
    }

    private static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String filter = raw.trim().toLowerCase(Locale.ITALY);

        if (filter.equals("aperte")) filter = "aperta";
        if (filter.equals("chiuse")) filter = "chiusa";

        if (filter.equals("aperta")) return "Aperta";
        if (filter.equals("chiusa")) return "Chiusa";

        return null;
    }
}
