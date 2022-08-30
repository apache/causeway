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
package org.apache.isis.security.keycloak.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.util.CollectionUtils;

import org.apache.isis.core.config.IsisConfiguration;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class KeycloakOauth2UserService extends OidcUserService {

    private final static OAuth2Error INVALID_REQUEST = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);

    final JwtDecoder jwtDecoder;
    final GrantedAuthoritiesMapper authoritiesMapper;
    final IsisConfiguration isisConfiguration;

    /**
     * Augments {@link OidcUserService#loadUser(OidcUserRequest)} to add authorities
     * provided by Keycloak.
     * <p>
     * Needed because {@link OidcUserService#loadUser(OidcUserRequest)} (currently)
     * does not provide a hook for adding custom authorities from a
     * {@link OidcUserRequest}.
     */
    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        OidcUser user = super.loadUser(userRequest);

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.addAll(user.getAuthorities());
        authorities.addAll(extractKeycloakAuthorities(userRequest));

        return new DefaultOidcUser(authorities, userRequest.getIdToken(), user.getUserInfo(), "preferred_username");
    }

    /**
     * Extracts {@link GrantedAuthority GrantedAuthorities} from the AccessToken in
     * the {@link OidcUserRequest}.
     *
     * @param userRequest
     * @return
     */
    private Collection<? extends GrantedAuthority> extractKeycloakAuthorities(OidcUserRequest userRequest) {

        Jwt token = parseJwt(userRequest.getAccessToken().getTokenValue());

        List<String> combinedRoles = new ArrayList<>();

        if(isisConfiguration.getSecurity().getKeycloak().isExtractClientRoles()) {

            // attempt to parse out 'resource_access.${client_id}.roles'

            val resourceObj = token.getClaims().get("resource_access");
            if (resourceObj instanceof Map) {
                @SuppressWarnings("rawtypes")
                val resourceMap = (Map) resourceObj;

                val clientId = userRequest.getClientRegistration().getClientId();
                val clientResourceObj = resourceMap.get(clientId);
                if(clientResourceObj instanceof Map) {
                    @SuppressWarnings("rawtypes")
                    val clientResource = (Map) clientResourceObj;
                    if (!CollectionUtils.isEmpty(clientResource)) {
                        val clientRolesObj = clientResource.get("roles");
                        if (clientResourceObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            val clientRoles = (List<Object>) clientRolesObj;
                            if (!CollectionUtils.isEmpty(clientRoles)) {
                                val prefix = Optional.ofNullable(isisConfiguration.getSecurity().getKeycloak().getClientRolePrefix()).orElse("");
                                clientRoles.stream()
                                        .filter(Objects::nonNull)
                                        .map(clientRole -> prefix + clientRole)
                                        .forEach(combinedRoles::add);
                            }
                        }
                    }
                }
            }
        }

        if (isisConfiguration.getSecurity().getKeycloak().isExtractRealmRoles()) {
            // attempt to parse out 'realm_access.roles'
            val realmAccessObj = token.getClaims().get("realm_access");
            if (realmAccessObj instanceof Map) {
                @SuppressWarnings("rawtypes")
                val realmAccessMap = (Map)realmAccessObj;
                Object realmRolesObj = realmAccessMap.get("roles");
                if (realmRolesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    val realmRoles = (List<Object>) realmRolesObj;
                    val prefix = Optional.ofNullable(isisConfiguration.getSecurity().getKeycloak().getRealmRolePrefix()).orElse("");
                    realmRoles.stream()
                            .filter(Objects::nonNull)
                            .map(realmRole -> prefix + realmRole)
                            .forEach(combinedRoles::add);
                }
            }
        }

        if (isisConfiguration.getSecurity().getKeycloak().isExtractRoles()) {
            // attempt to parse out 'roles'
            val rolesObj = token.getClaims().get("roles");
            if (rolesObj instanceof List) {
                @SuppressWarnings("unchecked")
                val roles = (List<Object>) rolesObj;
                val prefix = Optional.ofNullable(isisConfiguration.getSecurity().getKeycloak().getRolePrefix()).orElse("");
                roles.stream()
                        .filter(Objects::nonNull)
                        .map(role -> prefix + role)
                        .forEach(combinedRoles::add);
            }
        }

        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(combinedRoles.toArray(new String[]{}));
        return authoritiesMapper == null
                ? authorities
                : authoritiesMapper.mapAuthorities(authorities);

    }

    private Jwt parseJwt(String accessTokenValue) {
        try {
            // Token is already verified by spring security infrastructure
            return jwtDecoder.decode(accessTokenValue);
        } catch (JwtException e) {
            throw new OAuth2AuthenticationException(INVALID_REQUEST, e);
        }
    }
}
