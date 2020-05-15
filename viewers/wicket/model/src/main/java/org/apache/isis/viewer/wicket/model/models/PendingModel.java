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

import java.util.stream.Collectors;

import org.apache.wicket.model.Model;

import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.collections._Collections;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.webapp.context.IsisWebAppCommonContext;
import org.apache.isis.core.webapp.context.memento.ObjectMemento;
import org.apache.isis.core.webapp.context.memento.ObjectMementoService;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
final class PendingModel extends Model<ObjectMemento> {
    private static final long serialVersionUID = 1L;

    @NonNull private final ManagedObjectModel pendingValueModel;

    /**
     * Whether pending has been set (could have been set to null)
     */
    private boolean hasPending;
    /**
     * The new value (could be set to null; hasPending is used to distinguish).
     */
    private ObjectMemento pending;

    @Override
    public ObjectMemento getObject() {
        if (hasPending) {
            return pending;
        }
        
        if(pendingValueModel.memento()!=null) {
            return pendingValueModel.memento();
        }
        
        //XXX [a.huber] as I don't understand the big picture here, given newly introduced branch above,
        // there might be a slight chance, that this is dead code anyway ...
        val adapter = pendingValueModel.getObject();
        val pojo = adapter.getPojo();
        if(pojo!=null && _Collections.isCollectionOrArrayOrCanType(pojo.getClass())) {
            val specId = pendingValueModel.getTypeOfSpecification().getSpecId();
            log.warn("potentially a bug, wild guess fix for non-scalar %s", specId);
            val pojos = _NullSafe.streamAutodetect(pojo)
                    .collect(Collectors.<Object>toList());
            return getMementoService().mementoForPojos(pojos, specId);
        }
        return getMementoService().mementoForObject(adapter);
    }

    @Override
    public void setObject(final ObjectMemento adapterMemento) {
        pending = adapterMemento;
        hasPending = true;
    }

    public void clearPending() {
        this.hasPending = false;
        this.pending = null;
    }

    public ManagedObject getPendingElseCurrentAdapter() {
        return hasPending ? getPendingAdapter() : pendingValueModel.getObject();
    }
    
    ManagedObject getPendingAdapter() {
        val memento = getObject();
        return getCommonContext().reconstructObject(memento);
    }

    ObjectMemento getPendingMemento() {
        return pending;
    }

    void setPendingMemento(ObjectMemento selectedAdapterMemento) {
        this.pending = selectedAdapterMemento;
        hasPending=true;
    }
    
    private IsisWebAppCommonContext getCommonContext() {
        return pendingValueModel.getCommonContext();
    }
    
    private ObjectMementoService getMementoService() {
        return pendingValueModel.getMementoService();
    }
    
    
}