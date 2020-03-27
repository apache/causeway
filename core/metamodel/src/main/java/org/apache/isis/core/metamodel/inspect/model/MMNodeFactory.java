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

import org.apache.isis.schema.metamodel.v2.Action;
import org.apache.isis.schema.metamodel.v2.Collection;
import org.apache.isis.schema.metamodel.v2.DomainClassDto;
import org.apache.isis.schema.metamodel.v2.Facet;
import org.apache.isis.schema.metamodel.v2.FacetAttr;
import org.apache.isis.schema.metamodel.v2.Param;
import org.apache.isis.schema.metamodel.v2.Property;

import lombok.val;

public class MMNodeFactory {
    
    public static MMNode type(DomainClassDto domainClassDto, MMNode parentNode) {
        val node = new TypeNode();
        node.setDomainClassDto(domainClassDto);
        node.setParentNode(parentNode);
        return node;
    }
    
    public static MMNode facet(Facet facet, MMNode parentNode) {
        val node = new FacetNode();
        node.setFacet(facet);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode facetAttr(FacetAttr facetAttr, FacetNode parentNode) {
        val node = new FacetAttrNode();
        node.setFacetAttr(facetAttr);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode action(Action action, TypeNode parentNode) {
        val node = new ActionNode();
        node.setAction(action);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode property(Property property, TypeNode parentNode) {
        val node = new PropertyNode();
        node.setProperty(property);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode collection(Collection collection, TypeNode parentNode) {
        val node = new CollectionNode();
        node.setCollection(collection);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode param(Param param, ActionNode parentNode) {
        val node = new ParameterNode();
        node.setParameter(param);
        node.setParentNode(parentNode);
        return node;
    }
    
    
}
