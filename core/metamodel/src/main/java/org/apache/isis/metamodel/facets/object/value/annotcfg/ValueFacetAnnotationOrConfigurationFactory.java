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

package org.apache.isis.metamodel.facets.object.value.annotcfg;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.annotation.Value;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.isis.metamodel.facets.object.parseable.ParseableFacet;
import org.apache.isis.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.metamodel.facets.object.value.EqualByContentFacet;
import org.apache.isis.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.metamodel.facets.object.value.vsp.ValueSemanticsProviderUtil;

/**
 * Processes the {@link Value} annotation.
 *
 * <p>
 * As a result, will always install the following facets:
 * <ul>
 * <li> {@link TitleFacet} - based on the <tt>title()</tt> method if present,
 * otherwise uses <tt>toString()</tt></li>
 * <li> {@link IconFacet} - based on the <tt>iconName()</tt> method if present,
 * otherwise derived from the class name</li>
 * </ul>
 * <p>
 * In addition, the following facets may be installed:
 * <ul>
 * <li> {@link ParseableFacet} - if a {@link Parser} has been specified
 * explicitly in the annotation (or is picked up through an external
 * configuration file)</li>
 * <li> {@link EncodableFacet} - if an {@link EncoderDecoder} has been specified
 * explicitly in the annotation (or is picked up through an external
 * configuration file)</li>
 * <li> {@link ImmutableFacet} - if specified explicitly in the annotation
 * <li> {@link EqualByContentFacet} - if specified explicitly in the annotation
 * </ul>
 * <p>
 * Note that {@link ParentedCollectionFacet} is <i>not</i> installed.
 */
public class ValueFacetAnnotationOrConfigurationFactory extends FacetFactoryAbstract  {


    public ValueFacetAnnotationOrConfigurationFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        FacetUtil.addFacet(create(processClassContaxt.getCls(), processClassContaxt.getFacetHolder()));
    }

    /**
     * Returns a {@link ValueFacet} implementation.
     */
    private ValueFacet create(final Class<?> cls, final FacetHolder holder) {

        // create from annotation, if present
        final Value annotation = Annotations.getAnnotation(cls, Value.class);
        if (annotation != null) {
            final ValueFacetAnnotation facet = new ValueFacetAnnotation(cls, holder);
            if (facet.isValid()) {
                return facet;
            }
        }

        // otherwise, try to create from configuration, if present
        final String semanticsProviderName = ValueSemanticsProviderUtil.semanticsProviderNameFromConfiguration(cls);
        if (!_Strings.isNullOrEmpty(semanticsProviderName)) {
            final ValueFacetFromConfiguration facet = new ValueFacetFromConfiguration(semanticsProviderName, holder);
            if (facet.isValid()) {
                return facet;
            }
        }

        // otherwise, no value semantic
        return null;
    }


}
