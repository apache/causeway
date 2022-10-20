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
package org.apache.causeway.core.metamodel.facets.object.ignore.annotation;

import javax.inject.Inject;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MethodExcludeMarker;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;

import lombok.val;

public class RemoveAnnotatedMethodsFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public RemoveAnnotatedMethodsFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {

        val policy = getMetaModelContext().getConfiguration().getCore().getMetaModel().getIntrospector().getPolicy();
        switch (policy) {
            case ENCAPSULATION_ENABLED:
                getClassCache().streamPublicOrDeclaredMethods(processClassContext.getCls())
                        .forEach(method -> {
                            if (!ProgrammingModelConstants.MethodIncludeMarker.anyMatchOn(method)) {
                                processClassContext.removeMethod(method);
                            }
                        });
                break;

            case ANNOTATION_REQUIRED:
                // TODO: this could probably be more precise and insist on @Domain.Include for members.

            case ANNOTATION_OPTIONAL:

                getClassCache()
                        .streamPublicMethods(processClassContext.getCls())
                        .forEach(method->{
                            if(MethodExcludeMarker.anyMatchOn(method)) {
                                processClassContext.removeMethod(method);
                            }
                        });

                break;
        }


    }

}
