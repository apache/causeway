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


package org.apache.isis.core.metamodel.adapter;

import org.apache.isis.core.metamodel.adapter.Instance;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Specification;


/**
 * Has no functionality but makes it easier to write tests that require an instance of an {@link ObjectAdapter}.
 */
public class ObjectNoop implements ObjectAdapter {

    public void changeState(final ResolveState newState) {}

    public void checkLock(final Version version) {}

    public void fireChangedEvent() {}

    public String getIconName() {
        return null;
    }

    public Object getObject() {
        return null;
    }

    public Oid getOid() {
        return null;
    }

    public ResolveState getResolveState() {
        return null;
    }

    public ObjectSpecification getSpecification() {
        return null;
    }

    public Version getVersion() {
        return null;
    }

    public void replacePojo(final Object pojo) {}

    public void setOptimisticLock(final Version version) {}

    public String titleString() {
        return null;
    }

    public TypeOfFacet getTypeOfFacet() {
        return null;
    }

    public void setTypeOfFacet(final TypeOfFacet typeOfFacet) {}

    public ObjectAdapter getOwner() {
        return null;
    }

    public Instance getInstance(Specification specification) {
        return null;
    }

    public boolean isAggregated() {
        return false;
    }

	public boolean isPersistent() {
		return false;
	}

	public boolean isTransient() {
		return false;
	}



}

