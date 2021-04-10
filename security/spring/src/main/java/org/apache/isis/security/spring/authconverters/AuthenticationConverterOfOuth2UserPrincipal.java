package org.apache.isis.security.spring.authconverters;

import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.val;

/**
 * Interpret {@link Authentication} as containing an OAuth2 principal.
 *
 * <p>
 * Requires the following dependency to be added:
 * </p>
 *
 * <pre>
 * &lt;dependency&gt;
 *     &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
 *     &lt;artifactId&gt;spring-boot-starter-oauth2-client&lt;/artifactId&gt;
 *     &lt;exclusions&gt;
 *         &lt;exclusion&gt;
 *             &lt;groupId&gt;org.springframework.boot&lt;/groupId&gt;
 *             &lt;artifactId&gt;spring-boot-starter-logging&lt;/artifactId&gt;
 *         &lt;/exclusion&gt;
 *     &lt;/exclusions&gt;
 * &lt;/dependency&gt;
 * </pre>
 */
@Component
@Order(OrderPrecedence.LATE - 150)
public class AuthenticationConverterOfOuth2UserPrincipal implements AuthenticationConverter {

    @Override
    public UserMemento convert(Authentication authentication) {
        val principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            val oAuth2User = (OAuth2User) principal;
            final Object loginAttr = oAuth2User.getAttributes().get("login");
            val principalIdentity =
                    loginAttr instanceof CharSequence
                            ? ((CharSequence) loginAttr).toString()
                            : oAuth2User.getName();
            return UserMemento.ofNameAndRoleNames(principalIdentity);
        }
        return null;
    }
}
