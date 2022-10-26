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
package org.apache.causeway.core.metamodel.inspect.model;

import java.util.stream.Stream;

import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.Introspection;
import org.apache.causeway.applib.annotation.Nature;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.schema.metamodel.v2.Annotation;
import org.apache.causeway.schema.metamodel.v2.Facet;
import org.apache.causeway.schema.metamodel.v2.FacetAttr;
import org.apache.causeway.schema.metamodel.v2.MetamodelElement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

@Named(FacetNode.LOGICAL_TYPE_NAME)
@DomainObject(
        nature=Nature.VIEW_MODEL,
        introspection = Introspection.ANNOTATION_REQUIRED
)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ToString
public class FacetNode extends MMNode {

    public static final String LOGICAL_TYPE_NAME = CausewayModuleApplib.NAMESPACE + ".FacetNode";

    @Property(hidden = Where.EVERYWHERE)
    @Getter @Setter private Facet facet;

    @Override
    public String createTitle() {
        val title = lookupTitleAnnotation().map(Annotation::getValue)
                .orElseGet(()->
                String.format("%s: %s", simpleName(facet.getId()), facet.getFqcn()));
        return title;
    }

    @Override
    protected String iconSuffix() {
        return "";
    }

    @Override
    protected MetamodelElement metamodelElement() {
        return facet;
    }

    @Getter @Setter
    private boolean shadowed = false;

    // -- TREE NODE STUFF

    @Getter @Setter @XmlTransient
    private MMNode parentNode;

    @Override
    public Stream<MMNode> streamChildNodes() {
        return streamFacetAttributes()
                    .map(facetAttr->MMNodeFactory.facetAttr(facetAttr, this));
    }

    private Stream<FacetAttr> streamFacetAttributes() {
        return _NullSafe.stream(facet.getAttr());
    }

}

