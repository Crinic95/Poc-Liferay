package it.dedagroup.microservice.proxy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SupplyFilterParser {

    private static final Pattern STATO_EQ = Pattern.compile(
            "(?i)statoContratto\\s+eq\\s+'([^']*)'");

    private static final Pattern UBICAZIONE_EQ = Pattern.compile(
            "(?i)ubicazione\\s+eq\\s+'([^']*)'");

    private SupplyFilterParser() {
    }

    public static SupplyFilterCriteria parse(String filter) {
        if (filter == null || filter.isBlank()) {
            return new SupplyFilterCriteria(null, null);
        }

        String stato = null;
        String ubicazione = null;

        Matcher m1 = STATO_EQ.matcher(filter);
        if (m1.find()) {
            stato = normalize(m1.group(1));
        }

        Matcher m2 = UBICAZIONE_EQ.matcher(filter);
        if (m2.find()) {
            ubicazione = normalize(m2.group(1));
        }

        return new SupplyFilterCriteria(stato, ubicazione);
    }

    private static String normalize(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }
}