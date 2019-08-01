/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.testdomain.shiro;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.inject.Inject;

import org.apache.isis.extensions.secman.api.SecurityModuleConfig;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.encryption.jbcrypt.IsisBootSecmanEncryptionJbcrypt;
import org.apache.isis.extensions.secman.jdo.IsisBootSecmanPersistenceJdo;
import org.apache.isis.extensions.secman.model.IsisBootSecmanModel;
import org.apache.isis.extensions.secman.shiro.IsisBootSecmanRealmShiro;
import org.apache.isis.testdomain.jdo.JdoTestDomainModule_withShiro;
import org.apache.isis.testdomain.ldap.LdapEmbeddedServer;
import org.apache.isis.testdomain.ldap.LdapServerService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import lombok.val;

@SpringBootTest(
		classes = { 
				JdoTestDomainModule_withShiro.class, 
		}, 
		properties = {
				"logging.config=log4j2-test.xml",
				"smoketest.withShiro=true", // enable shiro specific config to be picked up by Spring 
		})
@Import({
	
	LdapServerService.class,
	
    // Security Manager Extension (secman)
    IsisBootSecmanModel.class,
    IsisBootSecmanRealmShiro.class,
    IsisBootSecmanPersistenceJdo.class,
    IsisBootSecmanEncryptionJbcrypt.class,
})
class ShiroSecmanLdapTest extends AbstractShiroTest {

	@Inject LdapServerService ldapServerService;
	@Inject ApplicationUserRepository applicationUserRepository;
	@Inject ApplicationRoleRepository applicationRoleRepository;
	@Inject SecurityModuleConfig securityConfig;
	
	@BeforeAll
	static void beforeClass() {
		//    Build and set the SecurityManager used to build Subject instances used in your tests
		//    This typically only needs to be done once per class if your shiro.ini doesn't change,
		//    otherwise, you'll need to do this logic in each test that is different
		val factory = new IniSecurityManagerFactory("classpath:shiro-secman-ldap.ini");
		setSecurityManager(factory.getInstance());
	}

	@AfterAll
	static void afterClass() {
		tearDownShiro();
	}
	
	private ApplicationUser setupSvenInDb() {
		val regularUserRoleName = securityConfig.getRegularUserRoleName();
		val regularUserRole = applicationRoleRepository.findByName(regularUserRoleName);
		val enabled = true;
		val svenUser = applicationUserRepository
				.newDelegateUser(LdapEmbeddedServer.SVEN_PRINCIPAL, regularUserRole, enabled);
		return svenUser;
	}
	
	
	@Test
	void loginLogoutRoundtrip() {
		
		setupSvenInDb();
		
		val secMan = SecurityUtils.getSecurityManager();
		assertNotNull(secMan);

		val subject = SecurityUtils.getSubject(); 
		assertNotNull(subject);
		assertFalse(subject.isAuthenticated());

		val token = (AuthenticationToken) new UsernamePasswordToken(
				LdapEmbeddedServer.SVEN_PRINCIPAL,
				"pass");

		subject.login(token);
		assertTrue(subject.isAuthenticated());

		subject.logout();
		assertFalse(subject.isAuthenticated());

	}
	
	@Test
	void login_withAccount_thatOnlyExistsWithLdap() {

		val secMan = getSecurityManager();
		assertNotNull(secMan);
		
		val subject = SecurityUtils.getSubject(); 
		assertNotNull(subject);
		assertFalse(subject.isAuthenticated());

		val token = (AuthenticationToken) new UsernamePasswordToken(
				LdapEmbeddedServer.OLAF_PRINCIPAL,
				"pass");

		// default behavior is to create the account within the DB but leave it disabled 
		assertThrows(DisabledAccountException.class, ()->{
			subject.login(token);
		});
		
	}

	@Test
	void login_withInvalidPassword() {

		val secMan = SecurityUtils.getSecurityManager();
		assertNotNull(secMan);

		val subject = SecurityUtils.getSubject(); 
		assertNotNull(subject);
		assertFalse(subject.isAuthenticated());

		val token = (AuthenticationToken) new UsernamePasswordToken(
				LdapEmbeddedServer.SVEN_PRINCIPAL,
				"invalid-pass");
		
		assertThrows(Exception.class, ()->{
			subject.login(token);
		});
		
		assertFalse(subject.isAuthenticated());

	}
	
	@Test
	void login_withNonExistentUser() {

		val secMan = SecurityUtils.getSecurityManager();
		assertNotNull(secMan);

		val subject = SecurityUtils.getSubject(); 
		assertNotNull(subject);
		assertFalse(subject.isAuthenticated());

		val token = (AuthenticationToken) new UsernamePasswordToken(
				"non-existent-user",
				"invalid-pass");
		
		assertThrows(Exception.class, ()->{
			subject.login(token);
		});
		
		assertFalse(subject.isAuthenticated());
		

	}


}
