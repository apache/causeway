package org.apache.isis.extensions.secman.api;

import lombok.Builder;
import lombok.Getter;

@Builder
public class SecurityModuleConfig {
	
	@Getter @Builder.Default 
	final String regularUserRoleName = "isis-module-security-regular-user";
	
}
