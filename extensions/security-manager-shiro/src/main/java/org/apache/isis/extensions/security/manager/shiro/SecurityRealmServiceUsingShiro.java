package org.apache.isis.extensions.security.manager.shiro;

import org.apache.isis.extensions.security.manager.api.SecurityRealm;
import org.apache.isis.extensions.security.manager.api.SecurityRealmService;
import org.springframework.stereotype.Service;

@Service
public class SecurityRealmServiceUsingShiro implements SecurityRealmService {

	@Override
	public SecurityRealm getCurrentRealm() {
		return ShiroUtils.getIsisModuleSecurityRealm();
	}

}
