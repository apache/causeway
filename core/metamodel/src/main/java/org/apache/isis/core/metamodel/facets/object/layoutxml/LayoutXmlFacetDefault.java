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
package org.apache.isis.core.metamodel.facets.object.layoutxml;


import java.util.List;

import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetAbstract;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.applib.layout.v1_0.DomainObject;
import org.apache.isis.applib.layout.v1_0.TabGroup;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

public class LayoutXmlFacetDefault
            extends FacetAbstract
            implements LayoutXmlFacet {

    private final DomainObject metadata;

    public static Class<? extends Facet> type() {
        return LayoutXmlFacet.class;
    }


    public static LayoutXmlFacet create(
            final FacetHolder facetHolder,
            final DomainObject domainObject) {
        if(domainObject == null) {
            return null;
        }
        return new LayoutXmlFacetDefault(facetHolder, domainObject);
    }

    private LayoutXmlFacetDefault(
            final FacetHolder facetHolder,
            final DomainObject metadata) {
        super(LayoutXmlFacetDefault.type(), facetHolder, Derivation.NOT_DERIVED);
        this.metadata = metadata;
    }


    private boolean fleshedOut;

    public DomainObject getLayoutMetadata() {
        return fleshedOut? metadata : fleshOut(metadata);
    }

    private  DomainObject fleshOut(final DomainObject metadata) {
        synchronized (metadata) {
            doFleshOut(metadata);
            fleshedOut = true;
        }
        return metadata;
    }

    private void doFleshOut(final DomainObject metadata) {
        ObjectSpecification objectSpec = (ObjectSpecification) getFacetHolder();
        List<OneToOneAssociation> properties = getProperties(objectSpec);
        List<TabGroup> tabses = metadata.getTabGroups();
    }

    private List getProperties(final ObjectSpecification objectSpec) {
        return objectSpec
                .getAssociations(Contributed.INCLUDED, OneToOneAssociation.Filters.PROPERTIES);
    }
}
