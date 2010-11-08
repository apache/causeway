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


package org.apache.isis.runtime.testspec;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.runtime.testdomain.Person;
import org.apache.isis.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.runtime.testsystem.TestSpecification;



class PersonNameField extends ValueFieldTest {

    public void clearAssociation(final ObjectAdapter inObject) {
        getPerson(inObject).setName("");
    }

    public String debugData() {
        return "";
    }

    public ObjectAdapter get(final ObjectAdapter fromObject) {
        final TestProxyAdapter adapter = new TestProxyAdapter();
        adapter.setupObject(getPerson(fromObject).getName());
        return adapter;
    }

    public String getId() {
        return "name";
    }

    public String getName() {
        return "Name";
    }

    private Person getPerson(final ObjectAdapter inObject) {
        return (Person) inObject.getObject();
    }

    public ObjectSpecification getSpecification() {
        return new TestSpecification("java.lang.String");
    }

    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter association) {
        getPerson(inObject).setName((String) association.getObject());
    }

    public Consent isAssociationValid(final ObjectAdapter inObject, final ObjectAdapter association) {
        return Allow.DEFAULT;
    }

    public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter association) {
        getPerson(inObject).setName((String) association.getObject());
    }

    public void set(ObjectAdapter owner, ObjectAdapter newValue) {
        setAssociation(owner, newValue);
    }

}

public class PersonSpecification extends TestProxySpecification {

    public PersonSpecification() {
        super(Person.class);
        fields = new ObjectAssociation[] { new PersonNameField(), };
    }

    @Override
    public String getFullName() {
        return Person.class.getName();
    }

    @Override
    public ObjectAction getObjectAction(final ObjectActionType type, final String name) {
        return null;
    }

    @Override
    public ObjectAction getObjectAction(
            final ObjectActionType type,
            final String name,
            final ObjectSpecification[] parameters) {
        return null;
    }

    @Override
    public ObjectAction[] getObjectActions(final ObjectActionType... type) {
        return null;
    }

    @Override
    public String getPluralName() {
        return "People";
    }

    @Override
    public String getShortName() {
        return "person";
    }

    @Override
    public String getSingularName() {
        return "Person";
    }

    @Override
    public String getTitle(final ObjectAdapter adapter) {
        return ((Person) adapter.getObject()).title();
    }

    @Override
    public boolean isNotCollection() {
        return true;
    }

    @Override
    public Object newInstance() {
        return new Person();
    }
}


