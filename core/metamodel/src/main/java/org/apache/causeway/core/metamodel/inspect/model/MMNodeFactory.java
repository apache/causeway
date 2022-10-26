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

import org.apache.causeway.schema.metamodel.v2.Action;
import org.apache.causeway.schema.metamodel.v2.Annotation;
import org.apache.causeway.schema.metamodel.v2.Collection;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.schema.metamodel.v2.Facet;
import org.apache.causeway.schema.metamodel.v2.FacetAttr;
import org.apache.causeway.schema.metamodel.v2.FacetHolder.Facets;
import org.apache.causeway.schema.metamodel.v2.Param;
import org.apache.causeway.schema.metamodel.v2.Property;

import lombok.val;

public class MMNodeFactory {


    public static MMNode annotation(final Annotation annotation, final MMNode parentNode) {
        val node = new AnnotationNode();
        node.setAnnotation(annotation);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode type(final DomainClassDto domainClassDto, final MMNode parentNode) {
        val node = new TypeNode();
        node.setDomainClassDto(domainClassDto);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode facet(final Facet facet, final FacetGroupNode parentNode) {
        val node = new FacetNode();
        node.setFacet(facet);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode facetAttr(final FacetAttr facetAttr, final FacetNode parentNode) {
        val node = new FacetAttrNode();
        node.setFacetAttr(facetAttr);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode action(final Action action, final TypeNode parentNode) {
        val node = new ActionNode();
        node.setAction(action);
        node.setParentNode(parentNode);
        node.setMixedIn(action.isMixedIn());
        return node;
    }

    public static MMNode property(final Property property, final TypeNode parentNode) {
        val node = new PropertyNode();
        node.setProperty(property);
        node.setParentNode(parentNode);
        node.setMixedIn(property.isMixedIn());
        return node;
    }

    public static MMNode collection(final Collection collection, final TypeNode parentNode) {
        val node = new CollectionNode();
        node.setCollection(collection);
        node.setParentNode(parentNode);
        node.setMixedIn(collection.isMixedIn());
        return node;
    }

    public static MMNode param(final Param param, final ActionNode parentNode) {
        val node = new ParameterNode();
        node.setParameter(param);
        node.setParentNode(parentNode);
        return node;
    }

    public static MMNode facetGroup(final Facets facets, final MMNode parentNode) {
        val node = new FacetGroupNode();
        node.setFacets(facets);
        node.setParentNode(parentNode);
        return node;
    }


}
