package org.apache.isis.viewer.common.model.header;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.DomainServiceLayout.MenuBar;
import org.apache.isis.viewer.common.model.branding.BrandingUiModelProvider;
import org.apache.isis.viewer.common.model.menu.MenuUiModelProvider;
import org.apache.isis.viewer.common.model.userprofile.UserProfileUiModelProvider;

@Service
public class HeaderUiModelProvider {
    
    @Inject private BrandingUiModelProvider brandingUiModelProvider;
    @Inject private UserProfileUiModelProvider userProfileUiModelProvider;
    @Inject private MenuUiModelProvider menuUiModelProvider;

    public HeaderUiModel getHeader() {
        return HeaderUiModel.of(
                brandingUiModelProvider.getHeaderBranding(),
                userProfileUiModelProvider.getUserProfile(),
                menuUiModelProvider.getMenu(MenuBar.PRIMARY),
                menuUiModelProvider.getMenu(MenuBar.SECONDARY),
                menuUiModelProvider.getMenu(MenuBar.TERTIARY));
    }
    
}
