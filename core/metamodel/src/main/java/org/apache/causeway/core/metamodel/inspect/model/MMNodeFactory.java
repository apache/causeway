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

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.Specification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.schema.metamodel.v2.Annotation;
import org.apache.causeway.schema.metamodel.v2.FacetAttr;

import lombok.experimental.UtilityClass;

@UtilityClass
class MMNodeFactory {

    MMNode annotation(final Annotation annotation, final MMNode parentNode) {
        var node = new AnnotationNode(annotation);
        node.setParentNode(parentNode);
        return node;
    }

    TypeNode type(final ObjectSpecification objSpec) {
        return new TypeNode(objSpec.logicalTypeName());
    }

    MMNode facet(final Facet facet, final FacetGroupNode parentNode) {
        var node = new FacetNode(facet);
        node.setParentNode(parentNode);
        return node;
    }

    MMNode facetAttr(final String name, final String value, final FacetNode parentNode) {
        var facetAttr = new FacetAttr();
        facetAttr.setName(name);
        facetAttr.setValue(value);
        var node = new FacetAttrNode(facetAttr);
        node.setParentNode(parentNode);
        return node;
    }

    MMNode action(final ObjectAction action, final TypeNode parentNode) {
        var node = new ActionNode(action);
        node.setParentNode(parentNode);
        node.setMixedIn(action.isMixedIn());
        return node;
    }

    MMNode property(final OneToOneAssociation prop, final TypeNode parentNode) {
        var node = new PropertyNode(prop);
        node.setParentNode(parentNode);
        node.setMixedIn(prop.isMixedIn());
        return node;
    }

    MMNode collection(final OneToManyAssociation coll, final TypeNode parentNode) {
        var node = new CollectionNode(coll);
        node.setParentNode(parentNode);
        node.setMixedIn(coll.isMixedIn());
        return node;
    }

    MMNode param(final ObjectActionParameter param, final ActionNode parentNode) {
        var node = new ParameterNode(param);
        node.setParentNode(parentNode);
        return node;
    }

    MMNode facetGroup(final Stream<Facet> stream, final MMNode parentNode) {
        var node = new FacetGroupNode(Can.ofStream(stream));
        node.setParentNode(parentNode);
        return node;
    }

    //--

    String simpleName(final String name) {
        return _Strings.splitThenStream(""+name, ".")
            .reduce((first, second) -> second) // get the last
            .orElse("null");
    }

    Optional<Annotation> lookupTitleAnnotation(final Facet facet) {
        // TODO Auto-generated method stub
        return Optional.empty();
    }

    Optional<Annotation> lookupTitleAnnotation(@Nullable final Specification metamodelElement) {
        return lookupAnnotationByName(metamodelElement, "@title");
    }

    Optional<Annotation> lookupAnnotationByName(
        @Nullable final Specification metamodelElement, final String annotationName) {
        return streamAnnotations(metamodelElement)
            .filter(nameOf(annotationName))
            .findFirst();
    }

    Stream<Annotation> streamAnnotations(@Nullable final Specification metamodelElement) {
        //TODO[causeway-core-metamodel-CAUSEWAY-3838] annotations
        return Stream.empty();

//        return Optional.ofNullable(metamodelElement)
//            .map(MetamodelElement::getAnnotations)
//            .map(FacetHolder.Annotations::getAsList)
//            .<Stream<Annotation>>map(List::stream)
//            .orElseGet(Stream::empty);
    }

    static Predicate<Annotation> nameOf(final String annotationName) {
        return annot->Objects.equals(annotationName, annot.getName());
    }



}
