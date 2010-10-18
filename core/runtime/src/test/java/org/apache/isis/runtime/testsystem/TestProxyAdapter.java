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


package org.apache.isis.runtime.testsystem;

import java.util.Hashtable;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.exceptions.NotYetImplementedException;
import org.apache.isis.commons.lang.ToString;
import org.apache.isis.metamodel.adapter.Instance;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.Version;
import org.apache.isis.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Specification;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtime.persistence.ConcurrencyException;


public class TestProxyAdapter implements ObjectAdapter {
    private final Hashtable fieldContents = new Hashtable();
    private Object object;
    private Oid oid;
    private ObjectSpecification spec;
    private ResolveState state;
    private String titleString = "default title string";
    private Version version;
    private static int next;
    private final int id = next++;
    private String iconName;

    public void checkLock(final Version version) {
        if (this.version.different(version)) {
            throw new ConcurrencyException("", getOid());
        }
    }

    public ObjectAdapter getField(final ObjectAssociation field) {
        return (ObjectAdapter) fieldContents.get(field.getId());
    }

    public String getIconName() {
        return iconName;
    }

    public Object getObject() {
        return object;
    }

    public Oid getOid() {
        return oid;
    }

    public ResolveState getResolveState() {
        return state;
    }


	public boolean isPersistent() {
		return getResolveState().isPersistent();
	}

	public boolean isTransient() {
		return getResolveState().isTransient();
	}

    public ObjectSpecification getSpecification() {
        return spec;
    }

    public Version getVersion() {
        return version;
    }

    public void setOptimisticLock(final Version version) {
        this.version = version;
    }

    public void setupFieldValue(final String name, final ObjectAdapter field) {
        this.fieldContents.put(name, field);
    }

    public void setupIconName(final String iconName) {
        this.iconName = iconName;
    }

    public void setupObject(final Object object) {
        if (object instanceof ObjectAdapter) {
            throw new IsisException("can't create a [[NAME]] for a [[NAME]]: " + object.toString());
        }
        this.object = object;
    }

    public void setupOid(final Oid oid) {
        this.oid = oid;
    }

    public void setupResolveState(final ResolveState state) {
        this.state = state;
    }

    public void setupSpecification(final ObjectSpecification spec) {
        this.spec = spec;
    }

    public void setupTitleString(final String titleString) {
        this.titleString = titleString;
    }

    public void setupVersion(final Version version) {
        this.version = version;
    }

    public void setValue(final OneToOneAssociation field, final Object object) {}

    public String titleString() {
        return titleString;
    }

    @Override
    public String toString() {
        final ToString str = new ToString(this, id);
        str.append("title", titleString);
        str.append("oid", oid);
        str.append("pojo", object);
        return str.toString();
    }

    public void changeState(final ResolveState state) {
        this.state.isValidToChangeTo(state);
        this.state = state;
    }

    public void replacePojo(final Object pojo) {
        throw new NotYetImplementedException();
    }

    public void fireChangedEvent() {}

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

}
