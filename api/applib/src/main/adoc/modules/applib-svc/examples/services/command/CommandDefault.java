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
package org.apache.isis.applib.services.command;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.isis.applib.annotation.CommandExecuteIn;
import org.apache.isis.applib.annotation.CommandPersistence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;

import lombok.Getter;

public class CommandDefault implements Command {

    public CommandDefault() {
        this.executor = Executor.OTHER;
        this.uniqueId = UUID.randomUUID();
    }

    @Getter
    private String memberIdentifier;

    @Getter
    private String targetClass;

    @Getter
    private String targetAction;

    @Getter
    private String arguments;

    @Getter
    private String memento;

    @Getter
    private Bookmark target;

    @Getter
    private Timestamp timestamp;

    @Getter
    private Timestamp startedAt;

    @Getter
    private Timestamp completedAt;

    @Getter
    private String user;

    @Getter
    private Executor executor;

    @Getter
    private CommandExecuteIn executeIn;

    @Getter
    private Command parent;

    @Getter
    private Bookmark result;

    @Getter
    private String exception;

    @Getter
    private UUID uniqueId;

    @Getter
    private CommandPersistence persistence;

    @Getter
    private boolean persistHint;

    // -- toString

    private static final ToString<CommandDefault> toString = ObjectContracts
            .toString("startedAt", CommandDefault::getStartedAt)
            .thenToString("user", CommandDefault::getUser)
            .thenToString("memberIdentifier", CommandDefault::getMemberIdentifier)
            .thenToString("target", CommandDefault::getTarget)
            .thenToString("transactionId", CommandDefault::getUniqueId);

    @Override
    public String toString() {
        return toString.toString(this);
    }


    // -- FRAMEWORK INTERNAL

    private final Command.Internal INTERNAL = new Command.Internal() {
        @Override
        public void setMemberIdentifier(String actionIdentifier) {
            CommandDefault.this.memberIdentifier = actionIdentifier;
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
            CommandDefault.this.exception = exceptionStackTrace;
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
