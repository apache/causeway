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
package org.apache.isis.core.tck.fixture.busrules;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.core.tck.dom.busrules.BusRulesEntity;
import org.apache.isis.core.tck.dom.busrules.BusRulesEntityRepository;

public class BusRulesEntityFixture extends AbstractFixture {

    @Override
    public void install() {
        createEntity(1);
        createEntity(2);
        createEntity(3);
        createEntity(4);
        createEntity(5);
    }

    private BusRulesEntity createEntity(Integer id) {
        final BusRulesEntity entity = repository.newEntity();
        entity.setId(id);
        return entity;
    }

    // {{ injected: BusRulesEntityRepository
    private BusRulesEntityRepository repository;

    public void setBusRulesEntityRepository(final BusRulesEntityRepository repository) {
        this.repository = repository;
    }
    // }}

}
