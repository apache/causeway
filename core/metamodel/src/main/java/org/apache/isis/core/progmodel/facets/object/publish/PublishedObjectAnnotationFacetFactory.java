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

package org.apache.isis.core.progmodel.facets.object.publish;

import org.apache.isis.applib.annotation.PublishedObject;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.publish.PublishedObjectFacet;

public class PublishedObjectAnnotationFacetFactory extends FacetFactoryAbstract {

    public PublishedObjectAnnotationFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(ProcessClassContext processClassContext) {
        super.process(processClassContext);
        final PublishedObject annotation = Annotations.getAnnotation(processClassContext.getCls(), PublishedObject.class);
        FacetUtil.addFacet(create(annotation, processClassContext.getFacetHolder()));
    }

    private PublishedObjectFacet create(final PublishedObject annotation, final FacetHolder holder) {
        return annotation == null ? null : new PublishedObjectFacetAnnotation(newEventCanonicalizer(annotation.canonicalizeWith()), holder);
    }

    private static PublishedObject.EventCanonicalizer newEventCanonicalizer(final Class<? extends PublishedObject.EventCanonicalizer> value) {
        if(value == null) {
            return null;
        }
        try {
            return (PublishedObject.EventCanonicalizer) value.newInstance();
        } catch (final InstantiationException e) {
            return null;
        } catch (final IllegalAccessException e) {
            return null;
        }
    }

}
