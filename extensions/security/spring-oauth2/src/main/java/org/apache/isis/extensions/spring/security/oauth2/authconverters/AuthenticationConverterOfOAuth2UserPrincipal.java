package org.apache.isis.extensions.spring.security.oauth2.authconverters;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.user.UserMemento;
import org.apache.isis.security.spring.authconverters.AuthenticationConverter;

import lombok.val;
import lombok.var;

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
@javax.annotation.Priority(PriorityPrecedence.LATE - 150)
public class AuthenticationConverterOfOAuth2UserPrincipal implements AuthenticationConverter {

    @Override
    public UserMemento convert(Authentication authentication) {
        val principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            val oAuth2User = (OAuth2User) principal;
            val username = usernameFrom(oAuth2User);
            var userMemento = UserMemento.ofNameAndRoleNames(username);
            userMemento = userMemento.withAvatarUrl(avatarUrlFrom(oAuth2User));
            userMemento = userMemento.withRealName(realNameFrom(oAuth2User));
            return userMemento;
        }
        return null;
    }

    protected static String usernameFrom(OAuth2User oAuth2User) {
        val loginAttr = oAuth2User.getAttributes().get("login");
        return loginAttr instanceof CharSequence
                ? ((CharSequence) loginAttr).toString()
                : oAuth2User.getName();
    }

    protected static URL avatarUrlFrom(OAuth2User oAuth2User) {
        final Object avatarUrlObj = oAuth2User.getAttributes().get("avatar_url");
        if(avatarUrlObj instanceof String) {
            try {
                return new URL((String)avatarUrlObj);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    protected static String realNameFrom(OAuth2User oAuth2User) {
        final Object nameAttr = oAuth2User.getAttributes().get("name");
        if(nameAttr instanceof String) {
            return (String)nameAttr;
        }
        return null;
    }

}
