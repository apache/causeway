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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.Navigable;
import org.apache.causeway.applib.annotation.ObjectSupport;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.schema.metamodel.v2.Annotation;
import org.apache.causeway.schema.metamodel.v2.FacetHolder;
import org.apache.causeway.schema.metamodel.v2.MetamodelElement;

import lombok.Setter;

@XmlSeeAlso({
    AnnotationNode.class,
    MemberNode.class,
    ActionNode.class,
    CollectionNode.class,
    FacetAttrNode.class,
    FacetGroupNode.class,
    FacetNode.class,
    ParameterNode.class,
    PropertyNode.class,
    TypeNode.class,})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class MMNode {

    @PropertyLayout(navigable=Navigable.PARENT, hidden=Where.EVERYWHERE)
    public abstract MMNode getParentNode();

    @Collection
    @Setter protected List<MMNode> childNodes;
    public final List<MMNode> getChildNodes() {
        if(childNodes==null) {
            setChildNodes(
                    Stream.concat(
                            streamAnnotationNodes(),
                            streamChildNodes())
                    .collect(Collectors.toList()));
        }
        return childNodes;
    }

    protected abstract MetamodelElement metamodelElement();
    protected abstract Stream<MMNode> streamChildNodes();

    protected final Stream<MMNode> streamAnnotationNodes() {
        return streamAnnotations()
                .filter(notNameOf("@title"))
                .map(a->MMNodeFactory.annotation(a, this));
    }

    protected String title;

    //FIXME[CAUSEWAY-2774] should be picked up also when declared abstract, yet we work around that
    @ObjectSupport public final String title() {
        return title==null
                ? title = createTitle()
                : title;
    }
    @Programmatic
    protected abstract String createTitle();

    protected abstract String iconSuffix();

    @ObjectSupport public final String iconName() {
        return iconSuffix();
    }

    protected String simpleName(final String name) {
        return _Strings.splitThenStream(""+name, ".")
        .reduce((first, second) -> second) // get the last
        .orElse("null");
    }

    protected final Optional<Annotation> lookupTitleAnnotation() {
        return lookupAnnotationByName("@title");
    }

    protected final Stream<Annotation> streamAnnotations() {
        return Optional.ofNullable(metamodelElement())
                .map(MetamodelElement::getAnnotations)
                .map(FacetHolder.Annotations::getAsList)
                .<Stream<Annotation>>map(List::stream)
                .orElseGet(Stream::empty);
    }

    protected final Optional<Annotation> lookupAnnotationByName(final String annotationName) {
        return streamAnnotations()
                .filter(nameOf(annotationName))
                .findFirst();
    }

    static Predicate<Annotation> nameOf(final String annotationName) {
        return annot->Objects.equals(annotationName, annot.getName());
    }

    static Predicate<Annotation> notNameOf(final String annotationName) {
        return nameOf(annotationName).negate();
    }


}
