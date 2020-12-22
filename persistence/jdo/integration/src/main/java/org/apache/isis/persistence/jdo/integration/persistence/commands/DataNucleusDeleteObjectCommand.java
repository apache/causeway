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
package org.apache.isis.persistence.jdo.integration.persistence.commands;

import javax.jdo.PersistenceManager;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.persistence.jdo.integration.persistence.command.DestroyObjectCommand;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DataNucleusDeleteObjectCommand 
extends AbstractDataNucleusObjectCommand 
implements DestroyObjectCommand {

    public DataNucleusDeleteObjectCommand(ManagedObject adapter, PersistenceManager persistenceManager) {
        super(adapter, persistenceManager);
    }

    @Override
    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("destroy object - executing command for {}", onManagedObject());
        }
        getPersistenceManager().deletePersistent(onManagedObject().getPojo());
    }

    @Override
    public String toString() {
        return "DestroyObjectCommand [adapter=" + onManagedObject() + "]";
    }

}
