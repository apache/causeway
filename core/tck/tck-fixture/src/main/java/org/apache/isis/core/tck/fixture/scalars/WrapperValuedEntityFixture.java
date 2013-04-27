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
import org.apache.isis.core.tck.dom.scalars.WrapperValuedEntity;
import org.apache.isis.core.tck.dom.scalars.WrapperValuedEntityRepository;

public class WrapperValuedEntityFixture extends AbstractFixture {

    @Override
    public void install() {
        createEntity();
        createEntity();
        createEntity();
        createEntity();
        createEntity();
    }

    private WrapperValuedEntity createEntity() {
        final WrapperValuedEntity wve = wrapperValuesEntityRepository.newEntity();
        wve.setBooleanProperty(true);
        wve.setByteProperty((byte)123);
        wve.setShortProperty((short)32123);
        wve.setCharacterProperty('a');
        wve.setIntegerProperty(987654321);
        wve.setLongProperty(2345678901234567890L);
        wve.setFloatProperty(12345678901234567890.1234567890F);
        wve.setDoubleProperty(12345678901234567890.1234567890);

        return wve;
    }

    private WrapperValuedEntityRepository wrapperValuesEntityRepository;

    public void setPrimitiveValuesEntityRepository(final WrapperValuedEntityRepository wrapperValuesEntityRepository) {
        this.wrapperValuesEntityRepository = wrapperValuesEntityRepository;
    }

}
