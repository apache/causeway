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
import java.util.UUID;

import org.apache.isis.applib.annotation.CommandExecuteIn;
import org.apache.isis.applib.annotation.CommandPersistence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;

public class CommandDefault implements Command {

    // -- constructor

    public CommandDefault() {
        this.executor = Executor.OTHER;
        this.uniqueId = UUID.randomUUID();
    }

    // -- actionIdentifier (property)

    private String actionIdentifier;
    @Override
    public String getMemberIdentifier() {
        return actionIdentifier;
    }

    // -- targetClass (property)

    private String targetClass;
    @Override
    public String getTargetClass() {
        return targetClass;
    }

    // -- targetAction (property)

    private String targetAction;
    @Override
    public String getTargetAction() {
        return targetAction;
    }

    // -- arguments (property)

    private String arguments;
    @Override
    public String getArguments() {
        return arguments;
    }

    // -- memento (property)

    private String memento;

    @Override
    public String getMemento() {
        return memento;
    }

    // -- target (property)

    private Bookmark target;
    @Override
    public Bookmark getTarget() {
        return target;
    }

    // -- timestamp (property)

    private Timestamp timestamp;
    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    // -- startedAt (property)

    private Timestamp startedAt;
    @Override
    public Timestamp getStartedAt() {
        return startedAt;
    }

    // -- completedAt (property)

    private Timestamp completedAt;

    @Override
    public Timestamp getCompletedAt() {
        return completedAt;
    }

    // -- user (property)

    private String user;
    @Override
    public String getUser() {
        return user;
    }

    // -- executor (property)

    private Executor executor;

    @Override
    public Executor getExecutor() {
        return executor;
    }

    // -- executionType (property)

    private CommandExecuteIn executionType;

    @Override
    public CommandExecuteIn getExecuteIn() {
        return executionType;
    }

    // -- parent (property)

    private Command parent;

    @Override
    public Command getParent() {
        return parent;
    }

    // -- result (property)

    private Bookmark result;

    @Override
    public Bookmark getResult() {
        return result;
    }

    // -- exceptionStackTrace (property)

    private String exceptionStackTrace;

    @Override
    public String getException() {
        return exceptionStackTrace;
    }

    // -- transactionId (property)

    private UUID uniqueId;

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    // -- persistence

    private CommandPersistence persistence;

    @Override
    public CommandPersistence getPersistence() {
        return persistence;
    }

    // -- persistHint

    private boolean persistHint;

    @Override
    public boolean isPersistHint() {
        return persistHint;
    }

    // -- toString

    private final static ToString<CommandDefault> toString = ObjectContracts
            .toString("startedAt", CommandDefault::getStartedAt)
            .thenToString("user", CommandDefault::getUser)
            .thenToString("memberIdentifier", CommandDefault::getMemberIdentifier)
            .thenToString("target", CommandDefault::getTarget)
            .thenToString("transactionId", CommandDefault::getUniqueId);

    @Override
    public String toString() {
        return toString.toString(this);
    }

    
    // -- FRAMEWORK INTERNATA
    
    private final Command.Internal INTERNAL = new Command.Internal() {
        @Override
        public void setMemberIdentifier(String actionIdentifier) {
            CommandDefault.this.actionIdentifier = actionIdentifier;
        }
        @Override
        public void setTargetClass(String targetClass) {
            CommandDefault.this.targetClass = targetClass;
        }
        @Override
        public void setTargetAction(String targetAction) {
            CommandDefault.this.targetAction = targetAction;
        }
        @Override
        public void setArguments(String arguments) {
            CommandDefault.this.arguments = arguments;
        }
        @Override
        public void setMemento(String memento) {
            CommandDefault.this.memento = memento;
        }
        @Override
        public void setTarget(Bookmark target) {
            CommandDefault.this.target = target;
        }
        @Override
        public void setTimestamp(Timestamp timestamp) {
            CommandDefault.this.timestamp = timestamp;
        }
        @Override
        public void setStartedAt(Timestamp startedAt) {
            CommandDefault.this.startedAt = startedAt;
        }
        @Override
        public void setCompletedAt(final Timestamp completed) {
            CommandDefault.this.completedAt = completed;
        }
        @Override
        public void setUser(String user) {
            CommandDefault.this.user = user;
        }
        @Override
        public void setParent(Command parent) {
            CommandDefault.this.parent = parent;
        }
        @Override
        public void setResult(final Bookmark result) {
            CommandDefault.this.result = result;
        }
        @Override
        public void setException(final String exceptionStackTrace) {
            CommandDefault.this.exceptionStackTrace = exceptionStackTrace;
        }
        @Override
        public void setPersistence(CommandPersistence persistence) {
            CommandDefault.this.persistence = persistence;
        }
        @Override
        public void setPersistHint(boolean persistHint) {
            CommandDefault.this.persistHint = persistHint;
        }
        @Override
        public void setExecutor(Executor executor) {
            CommandDefault.this.executor = executor;
        }
    };
    
    @Override
    public Command.Internal internal() {
        return INTERNAL;
    }



}
