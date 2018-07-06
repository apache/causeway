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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.isis.applib.annotation.CommandExecuteIn;
import org.apache.isis.applib.annotation.CommandPersistence;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;

public class CommandDefault implements Command {

    // -- constructor

    public CommandDefault() {
        setExecutor(Executor.OTHER);
    }



    // -- actionIdentifier (property)

    private String actionIdentifier;
    @Override
    public String getMemberIdentifier() {
        return actionIdentifier;
    }

    @Override
    public void setMemberIdentifier(String actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }



    // -- targetClass (property)

    private String targetClass;
    @Override
    public String getTargetClass() {
        return targetClass;
    }

    @Override
    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }



    // -- targetAction (property)

    private String targetAction;
    @Override
    public String getTargetAction() {
        return targetAction;
    }

    @Override
    public void setTargetAction(String targetAction) {
        this.targetAction = targetAction;
    }



    // -- arguments (property)

    private String arguments;
    @Override
    public String getArguments() {
        return arguments;
    }

    @Override
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }



    // -- memento (property)

    private String memento;

    @Override
    public String getMemento() {
        return memento;
    }
    @Override
    public void setMemento(String memento) {
        this.memento = memento;
    }



    // -- target (property)

    private Bookmark target;
    @Override
    public Bookmark getTarget() {
        return target;
    }
    @Override
    public void setTarget(Bookmark target) {
        this.target = target;
    }



    // -- timestamp (property)

    private Timestamp timestamp;
    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }



    // -- startedAt (property)

    private Timestamp startedAt;
    @Override
    public Timestamp getStartedAt() {
        return startedAt;
    }
    @Override
    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }



    // -- completedAt (property)

    private Timestamp completedAt;

    @Override
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    @Override
    public void setCompletedAt(final Timestamp completed) {
        this.completedAt = completed;
    }



    // -- user (property)

    private String user;
    @Override
    public String getUser() {
        return user;
    }

    @Override
    public void setUser(String user) {
        this.user = user;
    }



    // -- actionDomainEvent (peek/pop/flush)

    private final LinkedList<ActionDomainEvent<?>> actionDomainEvents = _Lists.newLinkedList();

    @Override
    public ActionDomainEvent<?> peekActionDomainEvent() {
        return actionDomainEvents.isEmpty()? null: actionDomainEvents.getLast();
    }

    @Override
    public void pushActionDomainEvent(ActionDomainEvent<?> event) {
        if(peekActionDomainEvent() == event) {
            return;
        }
        this.actionDomainEvents.add(event);
    }

    @Override
    public ActionDomainEvent<?> popActionDomainEvent() {
        return !actionDomainEvents.isEmpty() ? actionDomainEvents.removeLast() : null;
    }

    @Override
    @Programmatic
    public List<ActionDomainEvent<?>> flushActionDomainEvents() {
        final List<ActionDomainEvent<?>> events =
                Collections.unmodifiableList(_Lists.newArrayList(actionDomainEvents));
        actionDomainEvents.clear();
        return events;
    }



    // -- executor (property)

    private Executor executor;

    @Override
    public Executor getExecutor() {
        return executor;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    @Override
    public void setExecutor(Executor nature) {
        this.executor = nature;
    }



    // -- executionType (property)

    private CommandExecuteIn executionType;

    @Override
    public CommandExecuteIn getExecuteIn() {
        return executionType;
    }

    /**
     * <b>NOT API</b>: intended to be called only by the framework.
     */
    @Override
    public void setExecuteIn(CommandExecuteIn executionType) {
        this.executionType = executionType;
    }




    // -- parent (property)

    private Command parent;

    @Override
    public Command getParent() {
        return parent;
    }

    @Override
    public void setParent(Command parent) {
        this.parent = parent;
    }




    // -- result (property)

    private Bookmark result;

    @Override
    public Bookmark getResult() {
        return result;
    }
    @Override
    public void setResult(final Bookmark result) {
        this.result = result;
    }



    // -- exceptionStackTrace (property)

    private String exceptionStackTrace;

    @Override
    public String getException() {
        return exceptionStackTrace;
    }
    @Override
    public void setException(final String exceptionStackTrace) {
        this.exceptionStackTrace = exceptionStackTrace;
    }



    // -- transactionId (property)

    private UUID transactionId;

    @Override
    public UUID getTransactionId() {
        return transactionId;
    }
    @Override
    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }




    // -- persistence

    private CommandPersistence persistence;

    @Override
    public CommandPersistence getPersistence() {
        return persistence;
    }

    @Override
    public void setPersistence(CommandPersistence persistence) {
        this.persistence = persistence;
    }



    // -- persistHint

    private boolean persistHint;

    @Override
    public boolean isPersistHint() {
        return persistHint;
    }

    @Override
    public void setPersistHint(boolean persistHint) {
        this.persistHint = persistHint;
    }




    // -- next

    private final Map<String, AtomicInteger> sequenceByName = _Maps.newHashMap();

    @Deprecated
    @Override
    public int next(String sequenceAbbr) {
        AtomicInteger next = sequenceByName.get(sequenceAbbr);
        if(next == null) {
            next = new AtomicInteger(0);
            sequenceByName.put(sequenceAbbr, next);
        } else {
            next.incrementAndGet();
        }
        return next.get();
    }



    // -- toString

    private final static ToString<CommandDefault> toString = ObjectContracts
            .toString("startedAt", CommandDefault::getStartedAt)
            .thenToString("user", CommandDefault::getUser)
            .thenToString("memberIdentifier", CommandDefault::getMemberIdentifier)
            .thenToString("target", CommandDefault::getTarget)
            .thenToString("transactionId", CommandDefault::getTransactionId);


    @Override
    public String toString() {
        return toString.toString(this);
    }




}
