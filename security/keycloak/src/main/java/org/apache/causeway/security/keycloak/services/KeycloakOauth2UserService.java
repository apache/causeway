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
package org.apache.causeway.security.keycloak.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.springframework.lang.Nullable;
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

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.config.CausewayConfiguration;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class KeycloakOauth2UserService extends OidcUserService {

    private final static OAuth2Error INVALID_REQUEST = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);

    final JwtDecoder jwtDecoder;
    final GrantedAuthoritiesMapper authoritiesMapper;
    final CausewayConfiguration causewayConfiguration;

    /**
     * Augments {@link OidcUserService#loadUser(OidcUserRequest)} to add authorities
     * provided by Keycloak.
     * <p>
     * Needed because {@link OidcUserService#loadUser(OidcUserRequest)} (currently)
     * does not provide a hook for adding custom authorities from a
     * {@link OidcUserRequest}.
     */
    @Override
    public OidcUser loadUser(final OidcUserRequest userRequest) throws OAuth2AuthenticationException {

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
    private Collection<? extends GrantedAuthority> extractKeycloakAuthorities(final OidcUserRequest userRequest) {

        Jwt token = parseJwt(userRequest.getAccessToken().getTokenValue());

        final List<String> combinedRoles = new ArrayList<>();

        if(causewayConfiguration.getSecurity().getKeycloak().isExtractClientRoles()) {
            // attempt to parse out 'resource_access.${client_id}.roles'
            val rolePrefix = Optional.ofNullable(causewayConfiguration.getSecurity().getKeycloak().getClientRolePrefix()).orElse("");
            asNonEmptyMap(token.getClaims().get("resource_access"))
            .ifPresent(resourceMap->{
                final String clientId = userRequest.getClientRegistration().getClientId();
                asNonEmptyMap(resourceMap.get(clientId))
                .flatMap(clientResource->asNonEmptyCollection(clientResource.get("roles")))
                .ifPresent(clientRoles->
                    forEachNonNullIn(clientRoles, clientRole -> combinedRoles.add(rolePrefix + clientRole))
                );
            });
        }

        if (causewayConfiguration.getSecurity().getKeycloak().isExtractRealmRoles()) {
            // attempt to parse out 'realm_access.roles'
            val rolePrefix = Optional.ofNullable(causewayConfiguration.getSecurity().getKeycloak().getRealmRolePrefix()).orElse("");
            asNonEmptyMap(token.getClaims().get("realm_access"))
            .ifPresent(realmAccessMap->{
                asNonEmptyCollection(realmAccessMap.get("roles"))
                .ifPresent(realmRoles->{
                    forEachNonNullIn(realmRoles, realmRole -> combinedRoles.add(rolePrefix + realmRole));
                });
            });
        }

        if (causewayConfiguration.getSecurity().getKeycloak().isExtractRoles()) {
            // attempt to parse out 'roles'
            val rolePrefix = Optional.ofNullable(causewayConfiguration.getSecurity().getKeycloak().getRolePrefix()).orElse("");
            asNonEmptyCollection(token.getClaims().get("roles"))
            .ifPresent(roles->{
                forEachNonNullIn(roles, role -> combinedRoles.add(rolePrefix + role));
            });
        }

        Collection<? extends GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(combinedRoles.toArray(new String[]{}));
        return authoritiesMapper == null
                ? authorities
                : authoritiesMapper.mapAuthorities(authorities);

    }

    // -- HELPER

    @SuppressWarnings("rawtypes")
    private Optional<Map> asNonEmptyMap(final @Nullable Object x){
        return _Casts.castTo(Map.class, x)
                .filter(Predicate.not(_NullSafe::isEmpty));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void forEachNonNullIn(final @NonNull Collection x, final @NonNull Consumer<Object> _do){
        x.stream()
                .filter(Objects::nonNull)
                .forEach(_do);
    }

    @SuppressWarnings("rawtypes")
    private Optional<Collection> asNonEmptyCollection(final @Nullable Object x){
        return _Casts.castTo(Collection.class, x)
                .filter(Predicate.not(_NullSafe::isEmpty));
    }


    private Jwt parseJwt(final String accessTokenValue) {
        try {
            // Token is already verified by spring security infrastructure
            return jwtDecoder.decode(accessTokenValue);
        } catch (JwtException e) {
            throw new OAuth2AuthenticationException(INVALID_REQUEST, e);
        }
    }
}
