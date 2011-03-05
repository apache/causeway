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


package org.apache.isis.runtimes.dflt.runtime.testspec;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.adapters.Localization;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyAdapter;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestSpecification;
import org.apache.isis.core.testsupport.testdomain.Person;



class PersonNameField extends ValueFieldTest {

    @Override
    public void clearAssociation(final ObjectAdapter inObject) {
        getPerson(inObject).setName("");
    }

    @Override
    public String debugData() {
        return "";
    }

    @Override
    public ObjectAdapter get(final ObjectAdapter fromObject) {
        final TestProxyAdapter adapter = new TestProxyAdapter();
        adapter.setupObject(getPerson(fromObject).getName());
        return adapter;
    }

    @Override
    public String getId() {
        return "name";
    }

    @Override
    public String getName() {
        return "Name";
    }

    private Person getPerson(final ObjectAdapter inObject) {
        return (Person) inObject.getObject();
    }

    @Override
    public ObjectSpecification getSpecification() {
        return new TestSpecification("java.lang.String");
    }

    @Override
    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter association) {
        getPerson(inObject).setName((String) association.getObject());
    }

    @Override
    public Consent isAssociationValid(final ObjectAdapter inObject, final ObjectAdapter association) {
        return Allow.DEFAULT;
    }

    @Override
    public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter association) {
        getPerson(inObject).setName((String) association.getObject());
    }

    @Override
    public void set(ObjectAdapter owner, ObjectAdapter newValue) {
        setAssociation(owner, newValue);
    }

    @Override
    public FeatureType getFeatureType() {
        return FeatureType.PROPERTY;
    }

}

public class PersonSpecification extends TestProxySpecification {

    public PersonSpecification() {
        super(Person.class);
        fields = Arrays.asList( (ObjectAssociation)new PersonNameField() );
    }

    @Override
    public String getFullIdentifier() {
        return Person.class.getName();
    }

    @Override
    public ObjectAction getObjectAction(final ActionType type, final String name) {
        return null;
    }

    @Override
    public ObjectAction getObjectAction(
            final ActionType type,
            final String name,
            final List<ObjectSpecification> parameters) {
        return null;
    }

    @Override
    public List<ObjectAction> getObjectActions(final ActionType... type) {
        return null;
    }

    @Override
    public String getPluralName() {
        return "People";
    }

    @Override
    public String getShortIdentifier() {
        return "person";
    }

    @Override
    public String getSingularName() {
        return "Person";
    }

    @Override
    public String getTitle(final ObjectAdapter adapter, Localization localization) {
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


