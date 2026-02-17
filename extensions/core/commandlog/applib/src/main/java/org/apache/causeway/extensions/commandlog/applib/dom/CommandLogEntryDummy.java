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
package org.apache.causeway.extensions.commandlog.applib.dom;

import java.util.UUID;

import javax.persistence.Entity;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.schema.cmd.v2.CommandDto;

/**
 * Just a marker, to have the static-weaving processor pickup inherited abstract classes.
 * @deprecated not required in 4.x (as abstract classes were refactored into interfaces) 
 */
@Deprecated
@Entity
public class CommandLogEntryDummy extends CommandLogEntry {

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
	public org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn getExecuteIn() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setExecuteIn(org.apache.causeway.extensions.commandlog.applib.dom.ExecuteIn replayState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public UUID getParentInteractionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setParentInteractionId(UUID parentInteractionId) {
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
	public CommandDto getCommandDto() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCommandDto(CommandDto commandDto) {
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

	@Override
	public Bookmark getResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setResult(Bookmark result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getException() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setException(String exception) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public org.apache.causeway.extensions.commandlog.applib.dom.ReplayState getReplayState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReplayState(org.apache.causeway.extensions.commandlog.applib.dom.ReplayState replayState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getReplayStateFailureReason() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setReplayStateFailureReason(String replayStateFailureReason) {
		// TODO Auto-generated method stub
		
	}
    
}

