package org.apache.isis.config.beans;

import java.io.Serializable;

import org.springframework.core.io.AbstractResource;

import org.apache.isis.commons.internal.resources._Resources;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class WebAppConfigBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final String MenubarsLayoutResourceName = "menubars.layout.xml";
    
    @Getter @Setter private String applicationName;
    @Getter @Setter private String applicationVersion;
    @Getter @Setter private String aboutMessage;
    @Getter @Setter private String welcomeMessage;
    @Getter @Setter private String faviconContentType;
    
    // URL *not* sensitive to context path
    @Getter @Setter private String applicationCss;
    @Getter @Setter private String applicationJs;

    // URL sensitive to context path
    @Setter private String faviconUrl;
    @Setter private String brandLogoHeader;
    @Setter private String brandLogoSignin;
    
    @Getter @Setter private AbstractResource menubarsLayoutXml;
    
    public String getFaviconUrl() {
        return honorContextPath(faviconUrl);
    }

    public String getBrandLogoHeader() {
        return honorContextPath(brandLogoHeader);
    }

    public String getBrandLogoSignin() {
        return honorContextPath(brandLogoSignin);
    }

    // -- HELPER
    
    private String honorContextPath(String url) {
        return _Resources.prependContextPathIfRequired(url);
    }
    
    
}
