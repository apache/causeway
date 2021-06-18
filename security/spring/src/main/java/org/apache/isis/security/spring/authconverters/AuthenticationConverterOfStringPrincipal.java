package org.apache.isis.security.spring.authconverters;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.services.user.UserMemento;

import lombok.val;

@Component
@javax.annotation.Priority(PriorityPrecedence.LATE + 100)
public class AuthenticationConverterOfStringPrincipal implements AuthenticationConverter {

    @Override
    public UserMemento convert(Authentication authentication) {
        val principal = authentication.getPrincipal();
        if (principal instanceof String) {
            val name = (String) principal;
            return UserMemento.ofNameAndRoleNames(name);
        } else {
            return null;
        }
    }
}
