package org.apache.isis.security.spring.authconverters;

import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.val;

@Component
@javax.annotation.Priority(OrderPrecedence.LATE - 200)
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
