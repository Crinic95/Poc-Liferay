/**
 * SPDX-FileCopyrightText: (c) 2023 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package com.clarityvisionsolutions.distributor.mgmt.actions;

import com.liferay.client.extension.util.spring.boot.BaseRestController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author Mateus Santana
 */
@RequestMapping("/object/validation/rule/taxid")
@RestController
public class DistributorTaxIdValidationRestController extends BaseRestController {

    private static final Log _log = LogFactory.getLog(DistributorTaxIdValidationRestController.class);

    @PostMapping
    public ResponseEntity<String> post(@AuthenticationPrincipal Jwt jwt, @RequestBody String json) {
        log(jwt, _log, json);

        JSONObject payload = new JSONObject(json);
        JSONObject entryDTO = payload.optJSONObject("entryDTO");
        String taxId = null;

        if (entryDTO != null) {
            taxId = entryDTO.optString("businessTaxIDNumber", null);
        }
        if (taxId == null) {
            taxId = payload.optString("businessTaxIDNumber", null);
        }

        // Obbligatorio - 11 cifre
        boolean ok = taxId != null && taxId.matches("^[0-9]{11}$");

        payload.put("validationCriteriaMet", ok);

        if (!ok) {
            payload.put("validationErrorMessage", "Business Tax ID Number must be exactly 11 digits.");
        }
        return new ResponseEntity<>(payload.toString(), HttpStatus.OK);
    }
}