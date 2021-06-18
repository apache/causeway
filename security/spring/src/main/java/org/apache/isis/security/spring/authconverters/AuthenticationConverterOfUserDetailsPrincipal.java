package org.apache.isis.security.spring.authconverters;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.val;

@Component
@javax.annotation.Priority(PriorityPrecedence.LATE - 200)
public class AuthenticationConverterOfUserDetailsPrincipal implements AuthenticationConverter {

    @Override
    public UserMemento convert(Authentication authentication) {
        val principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            val userDetails = (UserDetails) principal;
            return UserMemento.ofNameAndRoleNames(userDetails.getUsername());
        } else {
            return null;
        }
    }
}
