/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.services.command;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.spi.CommandService;
import org.apache.isis.applib.util.ObjectContracts;

public class CommandDefault implements Command {

    public CommandDefault() {
        setNature(Command.Nature.OTHER);
    }
    
    // //////////////////////////////////////
    // actionIdentifier (property)
    // //////////////////////////////////////

    private String actionIdentifier;
    public String getActionIdentifier() {
        return actionIdentifier;
    }
    @Override
    public void setActionIdentifier(String actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }

    // //////////////////////////////////////
    // targetClass (property)
    // //////////////////////////////////////

    private String targetClass;
    public String getTargetClass() {
        return targetClass;
    }

    @Override
    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    // //////////////////////////////////////
    // targetAction (property)
    // //////////////////////////////////////
    
    private String targetAction;
    public String getTargetAction() {
        return targetAction;
    }
    
    @Override
    public void setTargetAction(String targetAction) {
        this.targetAction = targetAction;
    }
    
    // //////////////////////////////////////
    // arguments (property)
    // //////////////////////////////////////
    
    private String arguments;
    public String getArguments() {
        return arguments;
    }
    
    @Override
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
    
    
    // //////////////////////////////////////
    // memento (property)
    // //////////////////////////////////////
    
    private String memento;
    
    @Override
    public String getMemento() {
        return memento;
    }
    @Override
    public void setMemento(String memento) {
        this.memento = memento;
    }
    

    // //////////////////////////////////////
    // target (property)
    // //////////////////////////////////////
    
    private Bookmark target;
    public Bookmark getTarget() {
        return target;
    }
    @Override
    public void setTarget(Bookmark target) {
        this.target = target;
    }


    // //////////////////////////////////////
    // timestamp (property)
    // //////////////////////////////////////

    private Timestamp timestamp;
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Timestamp startedAt) {
        this.timestamp = startedAt;
    }

    
    // //////////////////////////////////////
    // startedAt (property)
    // //////////////////////////////////////
    
    
    private Timestamp startedAt;
    @Override
    public Timestamp getStartedAt() {
        return startedAt;
    }
    @Override
    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

    
    // //////////////////////////////////////
    // completedAt (property)
    // //////////////////////////////////////
    
    private Timestamp completedAt;

    @Override
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    @Override
    public void setCompletedAt(final Timestamp completed) {
        this.completedAt = completed;
    }

    // //////////////////////////////////////
    // user (property)
    // //////////////////////////////////////

    private String user;
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }


    // //////////////////////////////////////
    // nature (property)
    // //////////////////////////////////////

    private Nature nature;

    @Override
    public Nature getNature() {
        return nature;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     * 
     * <p>
     * Implementation notes: populated by the viewer as hint to {@link CommandService} implementation.
     */
    @Override
    public void setNature(Nature nature) {
        this.nature = nature;
    }

    
    // //////////////////////////////////////
    // parent (property)
    // //////////////////////////////////////

    private Command parent;
    
    @Override
    public Command getParent() {
        return parent;
    }

    @Override
    public void setParent(Command parent) {
        this.parent = parent;
    }

    
    // //////////////////////////////////////
    // result (property)
    // //////////////////////////////////////
    
    private Bookmark result;
    
    @Override
    public Bookmark getResult() {
        return result;
    }
    @Override
    public void setResult(final Bookmark result) {
        this.result = result;
    }


    // //////////////////////////////////////
    // exceptionStackTrace (property)
    // //////////////////////////////////////

    private String exceptionStackTrace;
    
    @Override
    public String getException() {
        return exceptionStackTrace;
    }
    @Override
    public void setException(final String exceptionStackTrace) {
        this.exceptionStackTrace = exceptionStackTrace;
    }
    
    // //////////////////////////////////////

    @Override
    public String toString() {
        return ObjectContracts.toString(this, "startedAt,user,actionIdentifier,target,guid");
    }
    
    
    // //////////////////////////////////////
    
    private UUID transactionId;
    
    @Override
    public UUID getTransactionId() {
        return transactionId;
    }
    @Override
    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }
    
    
    // //////////////////////////////////////

    private final Map<String, AtomicInteger> sequenceByName = Maps.newHashMap();

    @Override
    public int next(String sequenceName) {
        AtomicInteger next = sequenceByName.get(sequenceName);
        if(next == null) {
            next = new AtomicInteger(0);
            sequenceByName.put(sequenceName, next);
        } else {
            next.incrementAndGet();
        }
        return next.get();
    }

    
    // //////////////////////////////////////
    
    private boolean persistHint;
    
    public boolean isPersistHint() {
        return persistHint;
    }
    
    public void setPersistHint(boolean persistHint) {
        this.persistHint = persistHint;
    }
    
    
}
