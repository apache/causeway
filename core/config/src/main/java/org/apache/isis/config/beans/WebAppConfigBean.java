package org.apache.isis.config.beans;

import java.io.Serializable;

import org.springframework.core.io.AbstractResource;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WebAppConfigBean implements Serializable {

    private static final long serialVersionUID = 1L;
    
    public static final String MenubarsLayoutResourceName = "menubars.layout.xml";
    
    private String applicationName;
    private String applicationVersion;
    private String aboutMessage;
    private String welcomeMessage;
    private String faviconUrl;
    private String faviconContentType;
    private String brandLogoHeader;
    private String brandLogoSignin;
    private String applicationCss;
    private String applicationJs;
    
    private AbstractResource menubarsLayoutXml;
    
}
