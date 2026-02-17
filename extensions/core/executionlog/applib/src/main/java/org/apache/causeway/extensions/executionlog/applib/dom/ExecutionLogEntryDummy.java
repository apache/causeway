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
package org.apache.causeway.extensions.executionlog.applib.dom;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.apache.causeway.applib.annotation.Domain;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.schema.ixn.v2.InteractionDto;

/**
 * Just a marker, to have the static-weaving processor pickup inherited abstract classes.
 * @deprecated not required in 4.x (as abstract classes were refactored into interfaces) 
 */
@Deprecated
@Domain.Exclude
@Entity
public class ExecutionLogEntryDummy extends ExecutionLogEntry {

	@Id
    @GeneratedValue
    private Long id;
	
	@Override
	public UUID getInteractionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInteractionId(UUID interactionId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSequence(int sequence) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ExecutionLogEntryType getExecutionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExecutionType(ExecutionLogEntryType executionType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setUsername(String userName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.sql.Timestamp getTimestamp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTimestamp(java.sql.Timestamp timestamp) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bookmark getTarget() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setTarget(Bookmark target) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getLogicalMemberIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLogicalMemberIdentifier(String logicalMemberIdentifier) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InteractionDto getInteractionDto() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInteractionDto(InteractionDto commandDto) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.sql.Timestamp getStartedAt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStartedAt(java.sql.Timestamp startedAt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public java.sql.Timestamp getCompletedAt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCompletedAt(java.sql.Timestamp completedAt) {
		// TODO Auto-generated method stub
		
	}

}
