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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.QueryOnly;
import org.apache.isis.tck.dom.simples.SimpleEntity;
import org.apache.isis.tck.dom.simples.SimpleEntityRepository;

public class SimpleEntityRepositoryDefault extends AbstractFactoryAndRepository implements SimpleEntityRepository {

    @Override
    public String getId() {
        return "simples";
    }

    @Override
    public int count() {
        return allInstances(SimpleEntity.class).size();
    }

    @Override
    public SimpleEntity newTransientEntity() {
        return newTransientInstance(SimpleEntity.class);
    }

    @Override
    public SimpleEntity newPersistentEntity(final String name, final Boolean flag) {
        final SimpleEntity entity = newTransientEntity();
        entity.setName(name);
        entity.setFlag(flag);
        getContainer().persist(entity);
        return entity;
    }

    @Override
    public void toggle(final SimpleEntity object) {
        object.setFlag(!object.getFlag());
    }

    @Override
    public SimpleEntity update(final SimpleEntity object, final String name, final boolean flag, final Boolean anotherBoolean, final int anInt, final Integer anotherInt, final long aLong, final Long anotherLong, final double aDouble, final Double anotherDouble, final BigInteger aBigInteger,
            final BigDecimal aBigDecimal) {

        object.setName(name);
        object.setFlag(flag);
        object.setAnotherBoolean(anotherBoolean);
        object.setAnInt(anInt);
        object.setAnotherInt(anotherInt);
        object.setALong(aLong);
        object.setAnotherLong(anotherLong);
        object.setADouble(aDouble);
        object.setAnotherDouble(anotherDouble);
        object.setABigInteger(aBigInteger);
        object.setABigDecimal(aBigDecimal);

        return object;
    }

    @Override
    @QueryOnly
    public List<SimpleEntity> list() {
        return allInstances(SimpleEntity.class);
    }

}
