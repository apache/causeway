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


package org.apache.isis.core.progmodel.specloader.internal.instances;

import java.util.Collections;
import java.util.List;

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
import org.apache.isis.core.progmodel.facets.actcoll.typeof.TypeOfFacetDefaultToObject;


public class InstanceCollectionSpecification extends IntrospectableSpecificationAbstract {

    private final SpecificationLoader specificationLoader;

	public InstanceCollectionSpecification(
    		final SpecificationLoader specificationLoader,
    		final RuntimeContext runtimeContext) {
		super(runtimeContext);
		this.specificationLoader = specificationLoader;
	}


	@Override
    public void markAsService() {}

    @Override
    public void introspect(final FacetDecoratorSet decorator) {
        fullName = ObjectList.class.getName();
        identifier = Identifier.classIdentifier(fullName);
        superClassSpecification = specificationLoader.loadSpecification(Object.class);
        superClassSpecification.addSubclass(this);
        fields = Collections.emptyList();

        addFacet(new InstancesCollectionFacet(this));
        addFacet(new TypeOfFacetDefaultToObject(this, specificationLoader) {
        });

        setIntrospected(true);
    }

    @Override
    public ObjectAssociation getAssociation(final String name) {
        return null;
    }

    @Override
    public List<ObjectAction> getServiceActionsFor(final ObjectActionType... type) {
        return Collections.emptyList();
    }

    @Override
    public List<ObjectAction> getObjectActions(final ObjectActionType... type) {
        return Collections.emptyList();
    }

    @Override
    public ObjectAction getObjectAction(
            final ObjectActionType type,
            final String id,
            final List<ObjectSpecification> parameters) {
        return null;
    }

    @Override
    public ObjectAction getObjectAction(final ObjectActionType type, final String id) {
        return null;
    }

    @Override
    public String getSingularName() {
        return "Instances";
    }

    @Override
    public String getPluralName() {
        return "Instances";
    }

    @Override
    public String getShortName() {
        return "Instances";
    }

    @Override
    public String getTitle(final ObjectAdapter object) {
        return ((ObjectList) object.getObject()).titleString();
    }

    @Override
    public String getIconName(final ObjectAdapter object) {
        return "instances";
    }

    @Override
    public String getDescription() {
        return "Typed instances";
    }

    @Override
    public boolean isCollectionOrIsAggregated() {
        return true;
    }

}

