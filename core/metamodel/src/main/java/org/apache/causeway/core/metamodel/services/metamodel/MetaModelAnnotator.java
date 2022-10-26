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
package org.apache.causeway.core.metamodel.services.metamodel;

import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.schema.metamodel.v2.Action;
import org.apache.causeway.schema.metamodel.v2.Collection;
import org.apache.causeway.schema.metamodel.v2.DomainClassDto;
import org.apache.causeway.schema.metamodel.v2.Facet;
import org.apache.causeway.schema.metamodel.v2.MetamodelElement;
import org.apache.causeway.schema.metamodel.v2.MetamodelElement.Annotations;
import org.apache.causeway.schema.metamodel.v2.Param;
import org.apache.causeway.schema.metamodel.v2.Property;

import lombok.val;

/**
 * SPI that allows to add arbitrary meta data as
 * {@link org.apache.causeway.schema.metamodel.v2.Annotation}s
 * to the metamodel schema. Like eg. node titles for rendering of the metamodel tree structure.
 * <p>
 * Particularly useful for metamodel export tools.
 *
 * @since 2.0 {@index}
 */
public interface MetaModelAnnotator {

    ExporterConfig config();

    void annotate(Facet facetType, org.apache.causeway.core.metamodel.facetapi.Facet facet);

    void annotate(DomainClassDto domainClass, ObjectSpecification specification);

    void annotate(Action actionType, ObjectAction action);

    void annotate(Param parameterType, ObjectActionParameter parameter);

    void annotate(Property propertyType, OneToOneAssociation property);

    void annotate(Collection collectionType, OneToManyAssociation collection);

    /**
     * creates and adds to its parent
     */
    default <T extends MetamodelElement> T createAnnotation(final T t, final String name, final String value) {
        val titleAnnot = new org.apache.causeway.schema.metamodel.v2.Annotation();
        titleAnnot.setName(name);
        titleAnnot.setValue(value);
        val annots = Optional.ofNullable(t.getAnnotations()).orElseGet(Annotations::new);
        t.setAnnotations(annots);
        annots.getAsList().add(titleAnnot);
        return t;
    }

    public interface ExporterConfig {

        default String abbrev(final @Nullable Class<?> cls) {
            if(cls==null) { return ""; }
            return abbreviate(cls.getName());
        }

        default String simpleName(final @Nullable Class<?> cls) {
            if(cls==null) { return ""; }
            return simpleName(cls.getName());
        }

        // -- DEFAULTS

        static String abbreviate(final String input) {
            return (""+input)
                    .replace("org.apache.causeway.core.metamodel.facets.", "».c.m.f.")
                    .replace("org.apache.causeway.core.metamodel.", "».c.m.")
                    .replace("org.apache.causeway.core.", "».c.")
                    .replace("org.apache.causeway.applib.", "».a.")
                    .replace("org.apache.causeway.", "».")
                    .replace("java.lang.", "");
        }

        static String simpleName(final String name) {
            return _Strings.splitThenStream(""+name, ".")
            .reduce((first, second) -> second) // get the last
            .orElse("null");
        }
    }

}
