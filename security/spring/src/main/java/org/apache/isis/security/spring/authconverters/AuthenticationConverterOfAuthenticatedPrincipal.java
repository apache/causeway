package org.apache.isis.security.spring.authconverters;

import org.springframework.core.annotation.Order;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.val;

@Component
@Order(OrderPrecedence.LATE - 100)
public class AuthenticationConverterOfAuthenticatedPrincipal implements AuthenticationConverter {

    @Override
    public UserMemento convert(Authentication authentication) {
        val principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedPrincipal) {
            val authenticatedPrincipal = (AuthenticatedPrincipal) principal;
            return UserMemento.ofNameAndRoleNames(authenticatedPrincipal.getName());
        }
        return null;
    }
}
