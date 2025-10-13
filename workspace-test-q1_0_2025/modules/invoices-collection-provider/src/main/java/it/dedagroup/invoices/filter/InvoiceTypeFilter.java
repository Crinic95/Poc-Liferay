package it.dedagroup.invoices.filter;

import com.liferay.info.filter.InfoFilter;
import com.liferay.info.type.Keyed;
import java.util.Locale;

public class InvoiceTypeFilter implements InfoFilter, Keyed {

    private final String type;

    public InvoiceTypeFilter(String raw) {
        this.type = normalize(raw);
    }

    public String getType() {
        return type;
    }

    @Override public String getKey() {
        return "invoiceType";
    }

    @Override public String getFilterTypeName() {
        return "select";
    }

    private static String normalize(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String filter = raw.trim().toLowerCase(Locale.ITALY);

        if (filter.equals("pagate"))  filter = "pagata";
        if (filter.equals("scadute")) filter = "scaduta";

        if (filter.equals("pagata"))  return "Pagata";
        if (filter.equals("scaduta")) return "Scaduta";

        return null;
    }
}