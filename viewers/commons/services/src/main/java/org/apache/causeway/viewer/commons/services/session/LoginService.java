/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.commons.services.session;

import java.time.ZoneId;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.applib.services.keyvaluestore.KeyValueSessionStore;
import org.apache.causeway.applib.services.user.UserCurrentSessionTimeZoneHandler;
import org.apache.causeway.applib.value.semantics.TimeZoneChoiceProvider;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Common.Signin;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Common.Signin.RememberMe;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.viewer.commons.model.session.LoginFormModel;

@Service
public record LoginService(
		CausewayConfiguration causewayConfiguration,
		AuthenticationManager authenticationManager,
		TranslationService translationService,
		@Qualifier("Login")
		Optional<TimeZoneChoiceProvider> timeZoneChoiceProviderOpt,
		Optional<KeyValueSessionStore> keyValueSessionStoreOpt) {

	public Signin signinConf() {
		return causewayConfiguration().viewer().common().signin();
	}

	public String rememberMeDescription() {
		return translationService.translate(
				TranslationContext.named("Login"),
				signinConf().rememberMe().description());
	}

	public TimeZoneChoiceProvider timeZoneChoiceProvider() {
		return timeZoneChoiceProviderOpt.orElseGet(TimeZoneChoiceProvider::fallback);
	}

	public void initModel(final LoginFormModel loginFormModel, final CookieHandler cookieHandler) {
		loginFormModel.setUsername(new UsernameCookieHandler(signinConf().rememberMe(), cookieHandler).recoverUsername()
				.orElse(null));
		loginFormModel.setTimezone(new TimezoneCookieHandler(signinConf().rememberMe(), cookieHandler).recoverTimezone()
				.orElse(null));
	}

	public boolean login(final LoginFormModel loginFormModel, final CookieHandler cookieHandler, @Nullable final LoginSuccessCallback loginSuccessListener) {
		var authenticationRequest = loginFormModel.toAuthenticationRequest();
		var interactionContext = authenticationManager.authenticate(authenticationRequest);
		var success = interactionContext!=null;
		if(!success)
			return false;

		if(loginFormModel.isRememberMe()) {
            if (StringUtils.hasLength(loginFormModel.getUsername())) {
            	// remember latest username
            	new UsernameCookieHandler(signinConf().rememberMe(), cookieHandler)
            		.rememberUsername(loginFormModel.getUsername());
            }
            if (loginFormModel.getTimezone()!=null) {
            	// remember time-zone selection
            	new TimezoneCookieHandler(signinConf().rememberMe(), cookieHandler)
            		.rememberTimezone(loginFormModel.getTimezone());
            }
		}
		if (loginFormModel.getTimezone()!=null) {
			new UserCurrentSessionTimeZoneHandler(keyValueSessionStoreOpt).setUserTimeZone(loginFormModel.getTimezone());
			interactionContext = interactionContext.withTimeZone(loginFormModel.getTimezone());
		} else {
			new UserCurrentSessionTimeZoneHandler(keyValueSessionStoreOpt).clearUserTimeZone();
		}
		loginSuccessListener.onLoginSuccess(authenticationRequest, interactionContext);
		return success;
	}

	// -- HELPER

    private record UsernameCookieHandler(String cookieName, CookieHandler cookieHandler) {
    	UsernameCookieHandler(final RememberMe conf, final CookieHandler cookieHandler) {
    		this(conf.cookieKey() + "_usr", cookieHandler);
    	}
    	void rememberUsername(final String username) {
    		cookieHandler.put(cookieName, username);
    	}
    	Optional<String> recoverUsername() {
    		return cookieHandler.lookup(cookieName)
					.map(_Strings::emptyToNull);
    	}
    }

    private record TimezoneCookieHandler(String cookieName, CookieHandler cookieHandler) {
    	TimezoneCookieHandler(final RememberMe conf, final CookieHandler cookieHandler) {
    		this(conf.cookieKey() + "_tz", cookieHandler);
    	}
    	void rememberTimezone(final ZoneId zoneId) {
    		cookieHandler.put(cookieName, zoneId.getId());
    	}
    	Optional<ZoneId> recoverTimezone() {
    		try {
    			return cookieHandler.lookup(cookieName)
    					.map(_Strings::emptyToNull)
    					.map(ZoneId::of);
    		} catch (Exception e) {
    			// silently ignore recovery errors
    			return Optional.empty();
    		}
    	}
    }

}
