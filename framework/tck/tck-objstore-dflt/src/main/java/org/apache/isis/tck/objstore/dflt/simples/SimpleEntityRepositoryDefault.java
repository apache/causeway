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

package org.apache.isis.tck.objstore.dflt.simples;

import org.apache.isis.tck.dom.AbstractEntityRepository;
import org.apache.isis.tck.dom.simples.SimpleEntity;
import org.apache.isis.tck.dom.simples.SimpleEntityRepository;

public class SimpleEntityRepositoryDefault extends AbstractEntityRepository<SimpleEntity> implements SimpleEntityRepository {

    public SimpleEntityRepositoryDefault() {
        super(SimpleEntity.class);
    }

    @Override
    public String getId() {
        return "simples";
    }

    @Override
    public SimpleEntity newTransientEntity() {
        return newTransientInstance(SimpleEntity.class);
    }

    @Override
    public SimpleEntity newPersistentEntity(String name, Boolean flag) {
        SimpleEntity entity = newTransientEntity();
        entity.setName(name);
        entity.setFlag(flag);
        getContainer().persist(entity);
        return entity;
    }

    @Override
    public void toggle(SimpleEntity object) {
        object.setFlag(!object.getFlag());
    }
    

}
