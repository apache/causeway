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
package org.apache.isis.core.metamodel.facets.object.title.methods;

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.fallback.FallbackFacetFactory;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;
import org.apache.isis.core.metamodel.methods.MethodFinderUtils;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants.ObjectSupportMethod;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import static org.apache.isis.core.metamodel.methods.MethodLiteralConstants.TO_STRING;

import lombok.val;

/**
 * @implNote removes the {@link Object#toString()} method as action candidate,
 * regardless of whether this method is used for the domain-object's title or not
 */
public class TitleFacetViaMethodsFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    private static final ObjectSupportMethod SUPPORT_METHOD = ObjectSupportMethod.TITLE;

    @Inject
    public TitleFacetViaMethodsFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY, OrphanValidation.VALIDATE,
                SUPPORT_METHOD.getMethodNames());
    }

    /**
     * If no title or toString can be used then will use Facets provided by
     * {@link FallbackFacetFactory} instead.
     */
    @Override
    public void process(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();

        // priming 'toString()' into Precedence.INFERRED rank
        inferTitleFromToString(processClassContext);

        SUPPORT_METHOD.getMethodNames()
        .forEach(methodName->{

            val titleMethod = MethodFinderUtils.findMethod_returningText(
                    MethodFinderOptions
                    .objectSupport(processClassContext.getIntrospectionPolicy()),
                    cls,
                    methodName,
                    NO_ARG);
            addFacetIfPresent(TitleFacetViaTitleMethod.create(titleMethod, facetHolder));
            processClassContext.removeMethod(titleMethod);
        });
    }

    // -- HELPER

    private void inferTitleFromToString(final ProcessClassContext processClassContext) {
        val cls = processClassContext.getCls();
        val facetHolder = processClassContext.getFacetHolder();
        val toStringMethod = MethodFinderUtils.findMethod(
                MethodFinderOptions.publicOnly(),
                cls, TO_STRING, String.class, NO_ARG);
        processClassContext.removeMethod(toStringMethod);
        addFacetIfPresent(TitleFacetInferredFromToStringMethod
                .create(toStringMethod, facetHolder));
    }

}
