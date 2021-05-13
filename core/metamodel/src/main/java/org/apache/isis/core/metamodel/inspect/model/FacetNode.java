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
package org.apache.isis.core.metamodel.inspect.model;

import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.schema.metamodel.v2.Facet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@DomainObject(
        nature=Nature.VIEW_MODEL,
        objectType = FacetNode.OBJECT_TYPE
)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ToString
public class FacetNode extends MMNode {

    public static final String OBJECT_TYPE = IsisModuleApplib.NAMESPACE + ".FacetNode";

    @Property(hidden = Where.EVERYWHERE)
    @Getter @Setter private Facet facet;

    @Override
    public String createTitle() {
        return String.format("%s: %s", simpleName(facet.getId()), abbreviate(facet.getFqcn()));
    }

    @Override
    public String iconName() {
        return "";
    }

    // -- TREE NODE STUFF

    @Getter @Setter @XmlTransient
    private MMNode parentNode;

    @Override
    public Stream<MMNode> streamChildNodes() {
        return _NullSafe.stream(facet.getAttr())
                .map(facetAttr->MMNodeFactory.facetAttr(facetAttr, this));
    }


}

