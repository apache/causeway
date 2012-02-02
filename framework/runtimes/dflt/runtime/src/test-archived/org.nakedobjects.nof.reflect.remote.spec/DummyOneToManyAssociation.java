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


package org.apache.isis.nof.reflect.remote.spec;

import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.adapter.ObjectAdapterReference;
import org.apache.isis.noa.facets.Facet;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.noa.reflect.ObjectField;
import org.apache.isis.noa.spec.ObjectSpecification;
import org.apache.isis.nof.core.reflect.Allow;
import org.apache.isis.nof.reflect.peer.OneToManyPeer;


public class DummyOneToManyAssociation implements ObjectField {

    private final OneToManyPeer fieldPeer;

    public DummyOneToManyAssociation(final OneToManyPeer fieldPeer) {
        this.fieldPeer = fieldPeer;
    }
    
    public String getBusinessKeyName() {
        return null;
    }

    public OneToManyPeer getPeer() {
        return fieldPeer;
    }
    
    public ObjectSpecification getSpecification() {
        return null;
    }

    public boolean isCollection() {
        return false;
    }

    public boolean isPersisted() {
        return true;
    }

    public boolean isEmpty(final ObjectAdapter adapter) {
        return false;
    }

    public boolean isObject() {
        return false;
    }

    public boolean isValue() {
        return false;
    }

    public boolean isMandatory() {
        return false;
    }

    public boolean isOptionEnabled() {
        return false;
    }
    
    public ObjectAdapter get(final ObjectAdapter fromObject) {
        return null;
    }

    public Object getDefault(
            ObjectAdapter adapter) {
        return null;
    }
    
    public void toDefault(ObjectAdapter target) {
    }
    
    public Facet getFacet(final Class cls) {
        return null;
    }
    
     public Class[] getFacetTypes() {
        return null;
    }
    
	public Facet[] getFacets(Facet.Filter filter) {
		return null;
	}

	public void addFacet(Facet facet) {
	}

	public void removeFacet(Facet facet) {
	}


    public String getName() {
        return null;
    }

    public String getId() {
        return fieldPeer.getIdentifier().getName();
    }

    public String getDescription() {
        return null;
    }

    public ObjectAdapter[] getOptions(final ObjectAdapter target) {
        return null;
    }
    
    public Consent isUsable() {
        Consent usableDeclaratively = isUsableDeclaratively();
        if (usableDeclaratively.isVetoed()) {
            return usableDeclaratively;
        }
        return isUsableForSession();
    }

    public Consent isUsableDeclaratively() {
        return Allow.DEFAULT;
    }

    public Consent isUsableForSession() {
        return Allow.DEFAULT;
    }

    public Consent isUsable(final ObjectAdapterReference target) {
        return null;
    }

    public boolean isVisible() {
        return isVisibleDeclaratively() && isVisibleForSession();
    }

    public boolean isVisibleDeclaratively() {
        return false;
    }

    public boolean isVisibleForSession() {
        return false;
    }

    public boolean isVisible(final ObjectAdapterReference target) {
        return true;
    }


    public String getHelp() {
        return null;
    }

    public String debugData() {
        return "";
    }



}
