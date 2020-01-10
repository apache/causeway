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
package org.apache.isis.persistence.jdo.datanucleus5.datanucleus.persistence.commands;

import javax.jdo.PersistenceManager;

import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.persistence.jdo.datanucleus5.persistence.command.CreateObjectCommand;
import org.apache.isis.runtime.persistence.session.PersistenceSession;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DataNucleusCreateObjectCommand 
extends AbstractDataNucleusObjectCommand 
implements CreateObjectCommand {

    public DataNucleusCreateObjectCommand(ManagedObject adapter, PersistenceManager persistenceManager) {
        super(adapter, persistenceManager);
    }


    @Override
    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("create object - executing command for: {}", onManagedObject());
        }
        
        val domainObject = onManagedObject().getPojo();
        if(!isDetached(domainObject)) {
            // this could happen if DN's persistence-by-reachability has already caused the domainobject
            // to be persisted.  It's Isis adapter will have been updated as a result of the postStore
            // lifecycle callback, so in essence there's nothing to be done.
            return;
        }

        getPersistenceManager().makePersistent(domainObject);
    }

    @Override
    public String toString() {
        return "CreateObjectCommand [adapter=" + onManagedObject() + "]";
    }
    
    // -- HELPER

    private boolean isDetached(Object pojo) {
        val ps = PersistenceSession.current(PersistenceSession.class)
        .getFirst()
        .get();
        
        val state = ps.getEntityState(pojo);
        return state.isDetached();
    }
}
