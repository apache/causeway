package org.apache.isis.core.runtime.services.userprof;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.userprof.UserProfileService;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;

@DomainService(nature = NatureOfService.DOMAIN)
public class UserProfileServiceDefault implements UserProfileService {

    @Programmatic
    @Override
    public String userProfileName() {
        return authenticationSessionProvider.getAuthenticationSession().getUserName();
    }

    @Inject
    AuthenticationSessionProvider authenticationSessionProvider;
}
