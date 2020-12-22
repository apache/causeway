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
package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.memento.ObjectMemento;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class PendingModel extends Model<ObjectMemento> {
    private static final long serialVersionUID = 1L;

    @NonNull private final ManagedObjectModel ownerModel;

    /**
     * Whether pending has been set (could have been set to null)
     */
    private boolean hasPending;
    
    /**
     * The new value (could be set to null; hasPending is used to distinguish).
     */
    private ObjectMemento pendingMemento;

    @Override
    public ObjectMemento getObject() {
        return pendingMemento;
    }

    @Override
    public void setObject(final ObjectMemento pendingMemento) {
        this.pendingMemento = pendingMemento;
        this.hasPending = true;
    }

    public void clearPending() {
        this.hasPending = false;
        this.pendingMemento = null;
    }

    public ManagedObject getPendingElseCurrentAdapter() {
        return hasPending 
                ? getCommonContext().reconstructObject(pendingMemento) 
                : ownerModel.getObject();
    }

    
    // -- HELPER
    
    private IsisAppCommonContext getCommonContext() {
        return ownerModel.getCommonContext();
    }
    
    
}