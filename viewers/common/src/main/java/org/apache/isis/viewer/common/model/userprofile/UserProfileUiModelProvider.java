package org.apache.isis.viewer.common.model.userprofile;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.userprof.UserProfileService;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionTracker;

@Service
public class UserProfileUiModelProvider implements UserProfileService {

    @Inject private AuthenticationSessionTracker authenticationSessionTracker;
    
    @Override
    public String userProfileName() {
        return authenticationSessionTracker
                .currentAuthenticationSession()
                .map(AuthenticationSession::getUserName)
                .orElse("<Anonymous>");
    }

    public UserProfileUiModel getUserProfile() {
        return UserProfileUiModel.of(userProfileName());
    }
    
}
