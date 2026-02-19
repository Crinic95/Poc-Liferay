package it.dedagroup.microservice.proxy.liferay;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
public class TaxCodeExtractor {

    public String extractTaxCode(JSONObject myUserAccountJson) {
        if (myUserAccountJson == null) return null;

        Object cfObj = myUserAccountJson.opt("customFields");
        if (!(cfObj instanceof JSONArray customFields)) {
            if (cfObj instanceof JSONObject customFieldsObj) {
                String v = customFieldsObj.optString("taxCode", null);
                return (v == null || v.isBlank()) ? null : v.trim();
            }
            return null;
        }

        for (int i = 0; i < customFields.length(); i++) {
            JSONObject cf = customFields.optJSONObject(i);
            if (cf == null) continue;

            String name = cf.optString("name", null);
            if (!"taxCode".equalsIgnoreCase(name)) continue;

            JSONObject customValue = cf.optJSONObject("customValue");
            if (customValue == null) return null;

            String taxCode = customValue.optString("data", null);
            if (taxCode == null || taxCode.isBlank()) return null;

            return taxCode.trim();
        }

        return null;
    }
}