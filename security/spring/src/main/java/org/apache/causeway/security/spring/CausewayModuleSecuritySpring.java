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
package org.apache.causeway.security.spring;

import org.jspecify.annotations.Nullable;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter.Directive;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.core.security.authentication.manager.AuthenticationManager;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverterOfAuthenticatedPrincipal;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverterOfStringPrincipal;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverterOfUserDetailsPrincipal;
import org.apache.causeway.security.spring.authentication.AuthenticatorSpring;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration Bean to support authentication using Spring Security.
 *
 * @since 2.0 {@index}
 */
@Configuration(proxyBeanMethods = false)
@Import({
        // modules
        CausewayModuleCoreRuntimeServices.class,

        // @Component's
        AuthenticationConverterOfAuthenticatedPrincipal.class,
        AuthenticationConverterOfStringPrincipal.class,
        AuthenticationConverterOfUserDetailsPrincipal.class,

        // @Service's
        AuthenticatorSpring.class,

})
@EnableWebSecurity
@Slf4j
public class CausewayModuleSecuritySpring {

    public static final String NAMESPACE = "causeway.security.spring";

    @Bean
    SecurityFilterChain securityFilterChain(
    		final HttpSecurity http, final CausewayConfiguration causewayConfiguration) {
        http
            .csrf(csrf->{
            	if(!causewayConfiguration.security().spring().allowCsrfFilters()) {
            		log.info("disabling CsrfFilter");
            		csrf.disable();
            	}
            })
            .authorizeHttpRequests(requests -> requests
                .requestMatchers("/login", "/logout", "/error").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                    .loginPage("/login")           // Unified entry point
                    .loginProcessingUrl("/authenticate")  // Actual POST target
                    .successHandler(new SavedRequestAwareAuthenticationSuccessHandler()) // Auto-redirects to original URL
                    //.failureHandler((request, response, exception) -> {
//	                    System.err.println("failure: %s".formatted(exception));
//	                    exception.printStackTrace();
//	                    })
                    .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                //.deleteCookies("JSESSIONID", "rememberMe")
                //.invalidateHttpSession(true)
                .addLogoutHandler(new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter(Directive.ALL)))
            );
        return http.build();
    }

    @Controller
    public class LoginController {

        @GetMapping("/login")
        @ResponseBody
        String login(final @Nullable CsrfToken csrfToken) {
            return """
                <!DOCTYPE html>
                <html>
                    <body>
                        <h1>Login</h1>
                        <p>Access to this App</p>
                        <form action="/authenticate" method="post">
                            <p><input type="text" name="username" autocomplete="username" placeholder="User" required autofocus value="sven" /></p>
                            <p><input type="password" name="password" autocomplete="current-password" placeholder="Password" required /></p>
                            <p><input type="checkbox" name="rememberMe" />&nbsp;Remember Me</p>
                            %s
                            <p><input type="submit" value="Login"/></p>
                        </form>
                    </body>
                </html>"""
                .formatted(csrfToken!=null
                    ? """
                        <p><input type="hidden" name="%s" value="%s"></p>"""
                        .formatted(csrfToken.getParameterName(), csrfToken.getToken())
                    : "");
        }

    }

    @Component
    public class CausewayAuthenticationProvider implements AuthenticationProvider {

        private final AuthenticationManager authenticationManager;

        public CausewayAuthenticationProvider(final AuthenticationManager authenticationManager) {
            this.authenticationManager = authenticationManager;
        }

        @Override
        public Authentication authenticate(final Authentication auth) throws AuthenticationException {
            String username = auth.getName();
            String password = (String) auth.getCredentials();

            // Delegate to Causeway's core authentication logic
            var request = new AuthenticationRequestPassword(username, password);
            request.addRole(UserMemento.AUTHORIZED_USER_ROLE);

            var interactionContext = authenticationManager.authenticate(request);
            if (interactionContext == null)
				throw new BadCredentialsException("Invalid credentials");

            // Return a Spring-compatible token with Causeway's user details
            return new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    auth.getPrincipal(),
                    null,
                    auth.getAuthorities() // or map roles from interactionContext.getUser().roles()
            );
        }

        @Override
        public boolean supports(final Class<?> authentication) {
            return org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
        }
    }

}
