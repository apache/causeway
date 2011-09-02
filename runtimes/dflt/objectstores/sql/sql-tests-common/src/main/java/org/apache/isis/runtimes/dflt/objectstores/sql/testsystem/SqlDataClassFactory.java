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

/**
 * 
 */
package org.apache.isis.runtimes.dflt.objectstores.sql.testsystem;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.NumericTestClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.PolySubClassOne;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.PolySubClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.PolyTestClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SqlDataClass;

/**
 * @author Kevin
 * 
 */
public class SqlDataClassFactory extends AbstractFactoryAndRepository {
    public List<SqlDataClass> allDataClasses() {
        return allInstances(SqlDataClass.class);
    }

    public SqlDataClass newDataClass() {
        final SqlDataClass object = newTransientInstance(SqlDataClass.class);
        return object;
    }

    // SimpleClass
    public List<SimpleClass> allSimpleClasses() {
        return allInstances(SimpleClass.class);
    }

    public SimpleClass newSimpleClass() {
        final SimpleClass object = newTransientInstance(SimpleClass.class);
        return object;
    }

    // SimpleClassTwo
    public List<SimpleClassTwo> allSimpleClassTwos() {
        return allInstances(SimpleClassTwo.class);
    }

    public SimpleClassTwo newSimpleClassTwo() {
        final SimpleClassTwo object = newTransientInstance(SimpleClassTwo.class);
        return object;
    }

    public NumericTestClass newNumericTestClass() {
        final NumericTestClass object = newTransientInstance(NumericTestClass.class);
        return object;
    }

    public void save(final Object sqlDataClass) {
        persistIfNotAlready(sqlDataClass);
    }

    public void delete(final Object sqlDataClass) {
        remove(sqlDataClass);
    }

    public void update(final Object object) {
        getContainer().objectChanged(object);
    }

    public void resolve(final Object domainObject) {
        getContainer().resolve(domainObject);
    }

    public PolyTestClass newPolyTestClass() {
        final PolyTestClass object = newTransientInstance(PolyTestClass.class);
        return object;
    }

    public List<PolyTestClass> allPolyTestClasses() {
        return allInstances(PolyTestClass.class);
    }

    public PolySubClassOne newPolySubClassOne() {
        final PolySubClassOne object = newTransientInstance(PolySubClassOne.class);
        return object;
    }

    public PolySubClassTwo newPolySubClassTwo() {
        final PolySubClassTwo object = newTransientInstance(PolySubClassTwo.class);
        return object;
    }

}
