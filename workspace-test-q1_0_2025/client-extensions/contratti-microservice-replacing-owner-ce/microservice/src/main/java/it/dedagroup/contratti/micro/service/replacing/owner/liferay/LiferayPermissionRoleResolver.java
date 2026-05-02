package it.dedagroup.contratti.micro.service.replacing.owner.liferay;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "liferay.permissions")
public class LiferayPermissionRoleResolver {

    /**
     * Mappa diretta CF -> nome ruolo.
     * Esempio:
     * RSSMRA80A01H501U -> FORNITURE_MARIO
     */
    private Map<String, String> rolesByTaxCode = new HashMap<>();

    /**
     * Pattern opzionale fallback per userId.
     * Esempio: FORNITURE_USER_%d
     */
    private String roleNamePatternByUserId;

    public String resolveRoleName(String taxCode, Long userId) {
        if (taxCode != null) {
            String role = rolesByTaxCode.get(taxCode.trim().toUpperCase());
            if (role != null && !role.isBlank()) {
                return role;
            }
        }

        if (userId != null && roleNamePatternByUserId != null && !roleNamePatternByUserId.isBlank()) {
            return roleNamePatternByUserId.formatted(userId);
        }

        return null;
    }

    public Map<String, String> getRolesByTaxCode() {
        return rolesByTaxCode;
    }

    public void setRolesByTaxCode(Map<String, String> rolesByTaxCode) {
        Map<String, String> normalized = new HashMap<>();
        if (rolesByTaxCode != null) {
            rolesByTaxCode.forEach((k, v) -> {
                if (k != null) {
                    normalized.put(k.trim().toUpperCase(), v);
                }
            });
        }
        this.rolesByTaxCode = normalized;
    }

    public String getRoleNamePatternByUserId() {
        return roleNamePatternByUserId;
    }

    public void setRoleNamePatternByUserId(String roleNamePatternByUserId) {
        this.roleNamePatternByUserId = roleNamePatternByUserId;
    }
}