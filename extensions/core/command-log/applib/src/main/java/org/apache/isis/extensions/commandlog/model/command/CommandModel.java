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
package org.apache.isis.extensions.commandlog.model.command;

import java.sql.Timestamp;
import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.command.CommandOutcomeHandler;
import org.apache.isis.applib.services.commanddto.HasCommandDto;
import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.extensions.commandlog.model.IsisModuleExtCommandLogApplib;

public interface CommandModel 
extends 
    HasCommandDto, 
    Comparable<CommandModel> {
    
    public static class TitleUiEvent extends IsisModuleExtCommandLogApplib.TitleUiEvent<CommandModel> { }
    public static class IconUiEvent extends IsisModuleExtCommandLogApplib.IconUiEvent<CommandModel> { }
    public static class CssClassUiEvent extends IsisModuleExtCommandLogApplib.CssClassUiEvent<CommandModel> { }
    public static class LayoutUiEvent extends IsisModuleExtCommandLogApplib.LayoutUiEvent<CommandModel> { }

    public static abstract class PropertyDomainEvent<T> extends IsisModuleExtCommandLogApplib.PropertyDomainEvent<CommandModel, T> { }
    public static abstract class CollectionDomainEvent<T> extends IsisModuleExtCommandLogApplib.CollectionDomainEvent<CommandModel, T> { }
    public static abstract class ActionDomainEvent extends IsisModuleExtCommandLogApplib.ActionDomainEvent<CommandModel> { }

    
    Bookmark getResult();
    
    String getException();

    Timestamp getStartedAt();
    Timestamp getCompletedAt();
    Timestamp getTimestamp();

    Bookmark getTarget();
    
    String getLogicalMemberIdentifier();
    
    String getUsername();

    void saveAnalysis(String analysis);

    UUID getInteractionId();
    
    ReplayState getReplayState();

    CommandOutcomeHandler outcomeHandler();

    void setReplayState(ReplayState excluded);
    
    default String toFriendlyString() {
        return ObjectContracts
                .toString("interactionId", CommandModel::getInteractionId)
                .thenToString("username", CommandModel::getUsername)
                .thenToString("timestamp", CommandModel::getTimestamp)
                .thenToString("target", CommandModel::getTarget)
                .thenToString("logicalMemberIdentifier", CommandModel::getLogicalMemberIdentifier)
                .thenToStringOmitIfAbsent("startedAt", CommandModel::getStartedAt)
                .thenToStringOmitIfAbsent("completedAt", CommandModel::getCompletedAt)
                .toString(this);
    }
    
    @Override
    default int compareTo(final CommandModel other) {
        return this.getTimestamp().compareTo(other.getTimestamp());
    }

}
