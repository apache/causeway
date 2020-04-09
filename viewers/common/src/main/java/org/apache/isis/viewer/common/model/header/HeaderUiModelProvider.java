package org.apache.isis.viewer.common.model.header;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.viewer.common.model.branding.BrandingUiModelProvider;
import org.apache.isis.viewer.common.model.userprofile.UserProfileUiModelProvider;

@Service
public class HeaderUiModelProvider {
    
    @Inject private BrandingUiModelProvider brandingUiModelProvider;
    @Inject private UserProfileUiModelProvider userProfileUiModelProvider;

    public HeaderUiModel getHeader() {
        return HeaderUiModel.of(
                brandingUiModelProvider.getHeaderBranding(),
                userProfileUiModelProvider.getUserProfile());
    }
    
}
