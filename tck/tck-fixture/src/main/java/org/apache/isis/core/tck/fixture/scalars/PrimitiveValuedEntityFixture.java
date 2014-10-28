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

package org.apache.isis.core.tck.fixture.scalars;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntityRepository;

public class PrimitiveValuedEntityFixture extends AbstractFixture {

    @Override
    public void install() {
        createEntity();
        createEntity();
        createEntity();
        createEntity();
        createEntity();
    }

    private PrimitiveValuedEntity createEntity() {
        final PrimitiveValuedEntity pve = primitiveValuesEntityRepository.newEntity();
        pve.setBooleanProperty(true);
        pve.setByteProperty((byte)123);
        pve.setShortProperty((short)32123);
        pve.setCharProperty('a');
        pve.setIntProperty(987654321);
        pve.setLongProperty(2345678901234567890L);
        pve.setFloatProperty(12345678901234567890.1234567890F);
        pve.setDoubleProperty(12345678901234567890.1234567890);
        return pve;
    }

    private PrimitiveValuedEntityRepository primitiveValuesEntityRepository;

    public void setPrimitiveValuesEntityRepository(final PrimitiveValuedEntityRepository primitiveValuesEntityRepository) {
        this.primitiveValuesEntityRepository = primitiveValuesEntityRepository;
    }

}
