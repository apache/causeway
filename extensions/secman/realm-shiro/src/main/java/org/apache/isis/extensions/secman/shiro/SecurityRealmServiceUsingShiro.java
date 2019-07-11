package org.apache.isis.extensions.secman.shiro;

import org.apache.isis.extensions.secman.api.SecurityRealm;
import org.apache.isis.extensions.secman.api.SecurityRealmService;
import org.springframework.stereotype.Service;

@Service
public class SecurityRealmServiceUsingShiro implements SecurityRealmService {

	@Override
	public SecurityRealm getCurrentRealm() {
		return ShiroUtils.getIsisModuleSecurityRealm();
	}

}
