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
package org.apache.isis.applib.services.interaction;

import java.sql.Timestamp;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.util.ObjectContracts;

public class InteractionDefault implements Interaction {

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
    // startedAt (property)
    // //////////////////////////////////////

    private Timestamp startedAt;
    public Timestamp getStartedAt() {
        return startedAt;
    }

    @Override
    public void setStartedAt(Timestamp startedAt) {
        this.startedAt = startedAt;
    }

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

    private String guid;
    public String getGuid() {
        return guid;
    }

    @Override
    public void setGuid(String guid) {
        this.guid = guid;
    }


    // //////////////////////////////////////

    @Override
    public String toString() {
        return ObjectContracts.toString(this, "startedAt,user,actionIdentifier,target,guid");
    }
    
}
