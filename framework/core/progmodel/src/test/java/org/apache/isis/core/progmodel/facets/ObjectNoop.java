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


package org.apache.isis.core.progmodel.facets;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.spec.ElementSpecificationProvider;
import org.apache.isis.core.metamodel.spec.Instance;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;


/**
 * Has no functionality but makes it easier to write tests that require an instance of an {@link ObjectAdapter}.
 */
public class ObjectNoop implements ObjectAdapter {

    @Override
    public void changeState(final ResolveState newState) {}

    @Override
    public void checkLock(final Version version) {}

    @Override
    public void fireChangedEvent() {}

    @Override
    public String getIconName() {
        return null;
    }

    @Override
    public Object getObject() {
        return null;
    }

    @Override
    public Oid getOid() {
        return null;
    }

    @Override
    public ResolveState getResolveState() {
        return null;
    }

    @Override
    public ObjectSpecification getSpecification() {
        return null;
    }

    @Override
    public Version getVersion() {
        return null;
    }

    @Override
    public void replacePojo(final Object pojo) {}

    @Override
    public void setOptimisticLock(final Version version) {}

    @Override
    public String titleString() {
        return null;
    }

    @Override
    public ObjectSpecification getElementSpecification() {
        return null;
    }

    @Override
    public void setElementSpecificationProvider(ElementSpecificationProvider elementSpecificationProvider) {
    }


    @Override
    public ObjectAdapter getOwner() {
        return null;
    }

    @Override
    public Instance getInstance(Specification specification) {
        return null;
    }

    @Override
    public boolean isAggregated() {
        return false;
    }

	@Override
    public boolean isPersistent() {
		return false;
	}

	@Override
    public boolean isTransient() {
		return false;
	}

    @Override
    public ObjectAdapter getAggregateRoot() {
        return null;
    }


}

