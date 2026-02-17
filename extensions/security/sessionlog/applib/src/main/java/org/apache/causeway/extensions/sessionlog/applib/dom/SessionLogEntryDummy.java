/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.sessionlog.applib.dom;

import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Just a marker, to have the static-weaving processor pickup inherited abstract classes.
 * @deprecated not required in 4.x (as abstract classes were refactored into interfaces) 
 */
@Deprecated
@Entity
public class SessionLogEntryDummy extends SessionLogEntry {

	protected SessionLogEntryDummy(UUID sessionGuid, String httpSessionId, String username,
			org.apache.causeway.applib.services.session.SessionSubscriber.CausedBy causedBy, Timestamp loginTimestamp) {
		super(sessionGuid, httpSessionId, username, causedBy, loginTimestamp);
	}
	
	@Id
    @GeneratedValue
    private Long id;

	@Override
	public UUID getSessionGuid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSessionGuid(UUID sessionGuid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getHttpSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHttpSessionId(String httpSessionId) {
		// TODO Auto-generated method stub
		
	}

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
	public Timestamp getLoginTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLoginTimestamp(Timestamp loginTimestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Timestamp getLogoutTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogoutTimestamp(Timestamp logoutTimestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public org.apache.causeway.applib.services.session.SessionSubscriber.CausedBy getCausedBy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCausedBy(org.apache.causeway.applib.services.session.SessionSubscriber.CausedBy causedBy) {
		// TODO Auto-generated method stub
		
	}

	

}
