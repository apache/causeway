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
package org.apache.causeway.security.keycloak;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.JwsAlgorithms;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.MappedJwtClaimSetConverter;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.util.Assert;

import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.core.security.authentication.login.LoginSuccessHandlerUNUSED;
import org.apache.causeway.core.webapp.CausewayModuleCoreWebapp;
import org.apache.causeway.security.keycloak.handler.LogoutHandlerForKeycloak;
import org.apache.causeway.security.keycloak.services.KeycloakOauth2UserService;
import org.apache.causeway.security.spring.CausewayModuleSecuritySpring;

/**
 * Configuration Bean to support Causeway Security using Keycloak.
 *
 * @since 2.0 {@index}
 */
@Configuration
@Import({
        // modules
        CausewayModuleCoreRuntimeServices.class,
        CausewayModuleCoreWebapp.class,

        // services
        LogoutHandlerForKeycloak.class,

        // builds on top of Spring
        CausewayModuleSecuritySpring.class,

})
@EnableWebSecurity
public class CausewayModuleSecurityKeycloak {

    @Bean
    public SecurityFilterChain filterChain(
            final HttpSecurity http,
            final CausewayConfiguration causewayConfiguration,
            final KeycloakOauth2UserService keycloakOidcUserService,
            final List<LoginSuccessHandlerUNUSED> loginSuccessHandlersUNUSED,
            final List<LogoutHandler> logoutHandlers) throws Exception {

        var successUrl = causewayConfiguration.getSecurity().getKeycloak().getLoginSuccessUrl();
        var realm = causewayConfiguration.getSecurity().getKeycloak().getRealm();
        var loginPage = OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
                + "/" + realm;

        return http
                .sessionManagement(t->t.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(t->t.anyRequest().authenticated())
                // responsibility to propagate logout to Keycloak is performed by
                // LogoutHandlerForKeycloak (called by Causeway' LogoutMenu, not by Spring)
                // this is to ensure that Causeway can invalidate the http session eagerly and not preserve it in
                // the SecurityContextPersistenceFilter (which uses http session to do its work)
                .logout(t->{
                    var x = t.logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher("/logout"));
                    logoutHandlers.forEach(x::addLogoutHandler);   
                })
                // This is the point where OAuth2 login of Spring gets enabled
                .oauth2Login(t->t
                    .defaultSuccessUrl(successUrl, true)
//                        .successHandler(new AuthSuccessHandler(loginSuccessHandlers))
                    .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())
                    .userInfoEndpoint(c->c.oidcUserService(keycloakOidcUserService))
                    .loginPage(loginPage))
                .build();

        
// Spring 6 Legacy        
//        var httpSecurityLogoutConfigurer =
//            http
//                .sessionManagement()
//                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
//                .and()
//
//                .authorizeHttpRequests()
//                    .anyRequest().authenticated()
//                .and()
//
//                // responsibility to propagate logout to Keycloak is performed by
//                // LogoutHandlerForKeycloak (called by Causeway' LogoutMenu, not by Spring)
//                // this is to ensure that Causeway can invalidate the http session eagerly and not preserve it in
//                // the SecurityContextPersistenceFilter (which uses http session to do its work)
//                .logout()
//                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
//
//        logoutHandlers.forEach(httpSecurityLogoutConfigurer::addLogoutHandler);
//
//        httpSecurityLogoutConfigurer
//                .and()
//
//                // This is the point where OAuth2 login of Spring 5 gets enabled
//                .oauth2Login()
//                    .defaultSuccessUrl(successUrl, true)
////                        .successHandler(new AuthSuccessHandler(loginSuccessHandlers))
//                    .successHandler(new SavedRequestAwareAuthenticationSuccessHandler())
//                    .userInfoEndpoint()
//                        .oidcUserService(keycloakOidcUserService)
//                .and()
//
//                .loginPage(loginPage);
//
//        return http.build();
    }

    @Bean
    KeycloakOauth2UserService keycloakOidcUserService(final OAuth2ClientProperties oauth2ClientProperties, final CausewayConfiguration causewayConfiguration) {

        var jwtDecoder = createNimbusJwtDecoder(
                oauth2ClientProperties.getProvider().get("keycloak").getJwkSetUri(),
                JwsAlgorithms.RS256);

        var authoritiesMapper = new SimpleAuthorityMapper();
        authoritiesMapper.setConvertToUpperCase(true);

        return new KeycloakOauth2UserService(jwtDecoder, authoritiesMapper, causewayConfiguration);
    }

    // -- HELPER

    private static NimbusJwtDecoder createNimbusJwtDecoder(final String jwkSetUrl, final String jwsAlgorithms) {
        Assert.hasText(jwkSetUrl, "jwkSetUrl cannot be empty");

        final OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefault();
        final Converter<Map<String, Object>, Map<String, Object>> claimSetConverter =
                MappedJwtClaimSetConverter
                    .withDefaults(Collections.emptyMap());

        final NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUrl)
                .jwsAlgorithm(SignatureAlgorithm.from(jwsAlgorithms))
                .build();
        decoder.setClaimSetConverter(claimSetConverter);
        decoder.setJwtValidator(jwtValidator);
        return decoder;
    }

}
