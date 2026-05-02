/**
 * SPDX-FileCopyrightText: (c) 2000 Liferay, Inc. https://liferay.com
 * SPDX-License-Identifier: LGPL-2.1-or-later OR LicenseRef-Liferay-DXP-EULA-2.0.0-2023-06
 */

package it.dedagroup.registration;

import com.liferay.client.extension.util.spring.boot3.BaseRestController;

import it.dedagroup.registration.dto.ObjectActionPayload;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Raymond Augé
 * @author Gregory Amerson
 * @author Brian Wing Shun Chan
 */
@RequestMapping("/registration")
@RestController
public class RegistrationObjectActionController extends BaseRestController {

    private final RegistrationService _registrationService;

    public RegistrationObjectActionController(RegistrationService registrationService) {
        this._registrationService = registrationService;
    }

    @PostMapping("/create-user")
    public ResponseEntity<Void> createUser(@RequestBody ObjectActionPayload payload) {
        _registrationService.handleObjectAction(payload);
        return ResponseEntity.ok().build();
    }
}