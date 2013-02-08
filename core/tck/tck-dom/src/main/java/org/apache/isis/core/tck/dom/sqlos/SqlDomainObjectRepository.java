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
package org.apache.isis.core.tck.dom.sqlos;

import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.core.tck.dom.poly.Empty;
import org.apache.isis.core.tck.dom.poly.ReferencingPolyTypesEntity;
import org.apache.isis.core.tck.dom.poly.SelfReferencingEntity;
import org.apache.isis.core.tck.dom.poly.StringBaseEntitySub;
import org.apache.isis.core.tck.dom.poly.StringBaseEntitySubThree;
import org.apache.isis.core.tck.dom.poly.StringBaseEntitySubTwo;
import org.apache.isis.core.tck.dom.poly.Stringable;
import org.apache.isis.core.tck.dom.poly.StringableEntityWithOwnProperties;
import org.apache.isis.core.tck.dom.poly.StringableEntityWithOwnProperty;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.core.tck.dom.sqlos.data.SimpleClass;
import org.apache.isis.core.tck.dom.sqlos.data.SimpleClassTwo;
import org.apache.isis.core.tck.dom.sqlos.data.SqlDataClass;

/**
 * @author Kevin
 * 
 */
public class SqlDomainObjectRepository extends AbstractFactoryAndRepository {

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

    // SqlDataClass
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

    // PrimitiveValuedEntity
    public List<PrimitiveValuedEntity> allPrimitiveValueEntities() {
        return allInstances(PrimitiveValuedEntity.class);
    }

    public PrimitiveValuedEntity newPrimitiveValuedEntity() {
        return newTransientInstance(PrimitiveValuedEntity.class);
    }

    public List<PrimitiveValuedEntity> allPrimitiveValuedEntitiesThatMatch(final PrimitiveValuedEntity match) {
        return allMatches(PrimitiveValuedEntity.class, match);
    }

    
    // PolyTestClass
    public ReferencingPolyTypesEntity newPolyTestClass() {
        final ReferencingPolyTypesEntity object = newTransientInstance(ReferencingPolyTypesEntity.class);
        return object;
    }

    public List<ReferencingPolyTypesEntity> allPolyTestClasses() {
        return allInstances(ReferencingPolyTypesEntity.class);
    }

    public StringBaseEntitySub newPolySubClassOne() {
        final StringBaseEntitySub object = newTransientInstance(StringBaseEntitySub.class);
        return object;
    }

    public StringBaseEntitySubThree newPolySubClassThree() {
        final StringBaseEntitySubThree object = newTransientInstance(StringBaseEntitySubThree.class);
        return object;
    }

    public StringBaseEntitySubTwo newPolySubClassTwo() {
        final StringBaseEntitySubTwo object = newTransientInstance(StringBaseEntitySubTwo.class);
        return object;
    }

    public StringableEntityWithOwnProperty newPolyInterfaceImplA() {
        final StringableEntityWithOwnProperty object = newTransientInstance(StringableEntityWithOwnProperty.class);
        return object;
    }

    public StringableEntityWithOwnProperties newPolyInterfaceImplB() {
        final StringableEntityWithOwnProperties object = newTransientInstance(StringableEntityWithOwnProperties.class);
        return object;
    }

    public SelfReferencingEntity newPolySelfRefClass() {
        final SelfReferencingEntity object = newTransientInstance(SelfReferencingEntity.class);
        return object;
    }

    public List<Stringable> allPolyInterfaces() {
        return allInstances(Stringable.class);
    }

    public List<Stringable> queryPolyInterfaces(final Stringable query) {
        return allMatches(Stringable.class, query);
    }

    public List<Empty> allEmptyInterfacesThatMatch(final Empty match) {
        return allMatches(Empty.class, match);
    }

    public List<SimpleClass> someSimpleClasses(final long startIndex, final long rowCount) {
        return allInstances(SimpleClass.class, startIndex, rowCount);
    }

    // }}

}
