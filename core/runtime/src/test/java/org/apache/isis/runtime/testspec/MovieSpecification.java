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
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.consent.Allow;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.metamodel.interactions.InteractionContext;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.testdomain.Movie;
import org.apache.isis.runtime.testdomain.Person;
import org.apache.isis.runtime.testsystem.TestProxyAdapter;



class MovieDirectorField extends OneToOneAssociationTest {

    @Override
    public void clearAssociation(final ObjectAdapter inObject) {
        getMovie(inObject).setDirector(null);
    }

    public String debugData() {
        return "";
    }

    public ObjectAdapter get(final ObjectAdapter fromObject) {
        final Person director = getMovie(fromObject).getDirector();
        if (director == null) {
            return null;
        } else {
            return getAdapterManager().adapterFor(director);
        }
    }

    public String getId() {
        return "director";
    }

    private Movie getMovie(final ObjectAdapter inObject) {
        return (Movie) inObject.getObject();
    }

    public String getName() {
        return "Director";
    }

    public ObjectSpecification getSpecification() {
        return IsisContext.getSpecificationLoader().loadSpecification(Person.class);
    }

    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        getMovie(inObject).setDirector(associate == null ? null : (Person) associate.getObject());
    }

    public Consent isAssociationValid(final ObjectAdapter inObject, final ObjectAdapter associate) {
        return Allow.DEFAULT;
    }

    public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
        getMovie(inObject).setDirector((Person) associate.getObject());
    }

    public void set(ObjectAdapter owner, ObjectAdapter newValue) {
        setAssociation(owner, newValue);
    }

}

class MovieNameField extends ValueFieldTest {
    
    public boolean isOneToManyAssociation() {
        return false;
    }
    public void clearAssociation(final ObjectAdapter inObject) {
        getMovie(inObject).setName("");
    }

    public String debugData() {
        return "";
    }

    public ObjectAdapter get(final ObjectAdapter fromObject) {
        final TestProxyAdapter adapter = new TestProxyAdapter();
        final String object = getMovie(fromObject).getName();
        adapter.setupObject(object);
        return adapter;
    }

    public String getId() {
        return "name";
    }

    private Movie getMovie(final ObjectAdapter inObject) {
        return (Movie) inObject.getObject();
    }

    public String getName() {
        return "Name";
    }

    public ObjectSpecification getSpecification() {
        return new TestProxySpecification("java.lang.String");
    }

    public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter association) {
        getMovie(inObject).setName((String) association.getObject());
    }

    public Consent isAssociationValid(final ObjectAdapter inObject, final ObjectAdapter association) {
        return Allow.DEFAULT;
    }

    public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter association) {
        getMovie(inObject).setName((String) association.getObject());
    }

    public void set(ObjectAdapter owner, ObjectAdapter newValue) {
        setAssociation(owner, newValue);
    }

}


public class MovieSpecification extends TestProxySpecification {

    public MovieSpecification() {
        super(Movie.class);
        fields = new ObjectAssociation[] { new MovieNameField(), new MovieDirectorField(),
        // new MovieRolesField()
        };
    }

    @Override
    public ObjectAction getClassAction(
            final ObjectActionType type,
            final String name,
            final ObjectSpecification[] parameters) {
        return null;
    }

    @Override
    public String getFullName() {
        return Movie.class.getName();
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
        return "Movies";
    }

    @Override
    public String getShortName() {
        return "movie";
    }

    @Override
    public String getSingularName() {
        return "Movie";
    }

    @Override
    public String getTitle(final ObjectAdapter adapter) {
        return ((Movie) adapter.getObject()).title();
    }

    @Override
    public boolean isNotCollection() {
        return true;
    }

    @Override
    public Object newInstance() {
        return new Movie();
    }

    public InteractionContext createVisibleInteractionContext(
            final AuthenticationSession session,
            final ObjectAdapter target,
            final InteractionInvocationMethod invocationMethod) {
        return null;
    }

    public InteractionContext createUsableInteractionContext(
            final AuthenticationSession session,
            final ObjectAdapter target,
            final InteractionInvocationMethod invocationMethod) {
        return null;
    }

    
}
