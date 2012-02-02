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
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SimpleClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.SqlDataClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.EmptyInterface;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyInterface;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyInterfaceImplA;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyInterfaceImplB;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolySelfRefClass;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolySubClassOne;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolySubClassThree;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolySubClassTwo;
import org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism.PolyTestClass;

/**
 * @author Kevin
 * 
 */
public class SqlDataClassFactory extends AbstractFactoryAndRepository {
    // {{ Persistor actions
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

    // }}

    // {{ SQL Data type tests
    public List<SqlDataClass> allDataClasses() {
        return allInstances(SqlDataClass.class);
    }

    public SqlDataClass newDataClass() {
        final SqlDataClass object = newTransientInstance(SqlDataClass.class);
        return object;
    }

    // SimpleClass
    public SimpleClass newSimpleClass() {
        final SimpleClass object = newTransientInstance(SimpleClass.class);
        return object;
    }

    public List<SimpleClass> allSimpleClasses() {
        return allInstances(SimpleClass.class);
    }

    public List<SimpleClass> allSimpleClassesThatMatch(final SimpleClass simpleClassMatch) {
        return allMatches(SimpleClass.class, simpleClassMatch);
    }

    // SimpleClassTwo
    public List<SimpleClassTwo> allSimpleClassTwos() {
        return allInstances(SimpleClassTwo.class);
    }

    public SimpleClassTwo newSimpleClassTwo() {
        final SimpleClassTwo object = newTransientInstance(SimpleClassTwo.class);
        return object;
    }

    // NumericTestClass
    public List<NumericTestClass> allNumericTestClasses() {
        return allInstances(NumericTestClass.class);
    }

    public NumericTestClass newNumericTestClass() {
        final NumericTestClass object = newTransientInstance(NumericTestClass.class);
        return object;
    }

    public List<NumericTestClass> allNumericTestClassesThatMatch(final NumericTestClass match) {
        return allMatches(NumericTestClass.class, match);
    }

    // }}

    // {{ For polymorphism tests
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

    public PolySubClassThree newPolySubClassThree() {
        final PolySubClassThree object = newTransientInstance(PolySubClassThree.class);
        return object;
    }

    public PolySubClassTwo newPolySubClassTwo() {
        final PolySubClassTwo object = newTransientInstance(PolySubClassTwo.class);
        return object;
    }

    public PolyInterfaceImplA newPolyInterfaceImplA() {
        final PolyInterfaceImplA object = newTransientInstance(PolyInterfaceImplA.class);
        return object;
    }

    public PolyInterfaceImplB newPolyInterfaceImplB() {
        final PolyInterfaceImplB object = newTransientInstance(PolyInterfaceImplB.class);
        return object;
    }

    public PolySelfRefClass newPolySelfRefClass() {
        final PolySelfRefClass object = newTransientInstance(PolySelfRefClass.class);
        return object;
    }

    public List<PolyInterface> allPolyInterfaces() {
        return allInstances(PolyInterface.class);
    }

    public List<PolyInterface> queryPolyInterfaces(final PolyInterface query) {
        return allMatches(PolyInterface.class, query);
    }

    public List<EmptyInterface> allEmptyInterfacesThatMatch(final EmptyInterface match) {
        return allMatches(EmptyInterface.class, match);
    }

    // }}

}
