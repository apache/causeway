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
package org.apache.isis.persistence.jdo.integration.persistence.command;

import javax.jdo.PersistenceManager;

import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RequiredArgsConstructor
@Log4j2
public class DeleteObjectCommand implements PersistenceCommand {

    //private final PersistenceManager persistenceManager;
    @Getter private final ManagedObject entity;
    
    @Override
    public void execute(PersistenceManager persistenceManager) {
        if (log.isDebugEnabled()) {
            log.debug("destroy object - executing command for {}", entity);
        }
        persistenceManager.deletePersistent(entity.getPojo());
    }

    @Override
    public String toString() {
        return "DeleteObjectCommand [entity=" + entity + "]";
    }

}
