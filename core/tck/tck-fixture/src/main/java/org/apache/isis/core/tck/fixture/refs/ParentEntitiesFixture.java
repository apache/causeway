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

package org.apache.isis.core.tck.fixture.refs;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.core.tck.dom.refs.ParentEntity;
import org.apache.isis.core.tck.dom.refs.ParentEntityRepository;

public class ParentEntitiesFixture extends AbstractFixture {

    @Override
    public void install() {
        createEntity("parent 1");
        createEntity("parent 2");
        createEntity("parent 3");
        createEntity("parent 4");
        createEntity("parent 5");
    }

    private ParentEntity createEntity(final String name) {
        final ParentEntity parent = parentEntityRepository.newEntity(name);
        parent.newChild("child 1");
        parent.newChild("child 2");
        return parent;
    }

    private ParentEntityRepository parentEntityRepository;

    public void setParentEntityRepository(final ParentEntityRepository parentEntityRepository) {
        this.parentEntityRepository = parentEntityRepository;
    }

}
