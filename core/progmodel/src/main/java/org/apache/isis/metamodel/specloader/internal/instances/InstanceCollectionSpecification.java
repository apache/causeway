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


package org.apache.isis.metamodel.specloader.internal.instances;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectList;
import org.apache.isis.core.metamodel.facetdecorator.FacetDecoratorSet;
import org.apache.isis.core.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.core.metamodel.runtimecontext.spec.IntrospectableSpecificationAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionType;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacetDefaultToObject;


public class InstanceCollectionSpecification extends IntrospectableSpecificationAbstract {

    private final SpecificationLoader specificationLoader;

	public InstanceCollectionSpecification(
    		final SpecificationLoader specificationLoader,
    		final RuntimeContext runtimeContext) {
		super(runtimeContext);
		this.specificationLoader = specificationLoader;
	}


	public void markAsService() {}

    public void introspect(final FacetDecoratorSet decorator) {
        fullName = ObjectList.class.getName();
        identifier = Identifier.classIdentifier(fullName);
        superClassSpecification = specificationLoader.loadSpecification(Object.class);
        superClassSpecification.addSubclass(this);
        fields = new ObjectAssociation[0];

        addFacet(new InstancesCollectionFacet(this));
        addFacet(new TypeOfFacetDefaultToObject(this, specificationLoader) {
        });

        setIntrospected(true);
    }

    public ObjectAssociation getAssociation(final String name) {
        return null;
    }

    @Override
    public ObjectAction[] getServiceActionsFor(final ObjectActionType... type) {
        return new ObjectAction[0];
    }

    @Override
    public ObjectAction[] getObjectActions(final ObjectActionType... type) {
        return new ObjectAction[0];
    }

    public ObjectAction getObjectAction(
            final ObjectActionType type,
            final String id,
            final ObjectSpecification[] parameters) {
        return null;
    }

    public ObjectAction getObjectAction(final ObjectActionType type, final String id) {
        return null;
    }

    public String getSingularName() {
        return "Instances";
    }

    public String getPluralName() {
        return "Instances";
    }

    public String getShortName() {
        return "Instances";
    }

    public String getTitle(final ObjectAdapter object) {
        return ((ObjectList) object.getObject()).titleString();
    }

    @Override
    public String getIconName(final ObjectAdapter object) {
        return "instances";
    }

    public String getDescription() {
        return "Typed instances";
    }

    @Override
    public boolean isCollectionOrIsAggregated() {
        return true;
    }

}

