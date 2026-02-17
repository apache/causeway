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
package org.apache.causeway.extensions.secman.applib.user.dom;

import java.util.Set;

import javax.persistence.Entity;

import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;

/**
 * Just a marker, to have the static-weaving processor pickup inherited abstract classes.
 * @deprecated not required in 4.x (as abstract classes were refactored into interfaces) 
 */
@Deprecated
@Entity
public class ApplicationUserDummy extends ApplicationUser {

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUsername(String username) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFamilyName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFamilyName(String familyName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getGivenName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGivenName(String givenName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getKnownAs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setKnownAs(String knownAs) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getEmailAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEmailAddress(String emailAddress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPhoneNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPhoneNumber(String phoneNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getFaxNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFaxNumber(String faxNumber) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.util.Locale getLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLanguage(java.util.Locale locale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.util.Locale getNumberFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setNumberFormat(java.util.Locale locale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.util.Locale getTimeFormat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTimeFormat(java.util.Locale locale) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAtPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAtPath(String atPath) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public org.apache.causeway.extensions.secman.applib.user.dom.AccountType getAccountType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAccountType(org.apache.causeway.extensions.secman.applib.user.dom.AccountType accountType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ApplicationUserStatus getStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(ApplicationUserStatus disabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getEncryptedPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEncryptedPassword(String encryptedPassword) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<ApplicationRole> getRoles() {
		// TODO Auto-generated method stub
		return null;
	}

}
