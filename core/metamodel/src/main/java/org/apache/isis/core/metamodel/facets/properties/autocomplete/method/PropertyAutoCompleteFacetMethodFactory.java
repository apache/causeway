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

package org.apache.isis.core.metamodel.facets.properties.autocomplete.method;

import javax.inject.Inject;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.core.metamodel.methods.MethodLiteralConstants;
import org.apache.isis.core.metamodel.methods.MethodPrefixBasedFacetFactoryAbstract;

import lombok.val;

public class PropertyAutoCompleteFacetMethodFactory
extends MethodPrefixBasedFacetFactoryAbstract {

    private static final String PREFIX = MethodLiteralConstants.AUTO_COMPLETE_PREFIX;

    @Inject
    public PropertyAutoCompleteFacetMethodFactory(final MetaModelContext mmc) {
        // to also support properties from mixins, need to not only include properties but also actions
        super(mmc, FeatureType.PROPERTIES_AND_ACTIONS, OrphanValidation.VALIDATE, Can.ofSingleton(PREFIX));
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        attachPropertyAutoCompleteFacetIfChoicesMethodIsFound(processMethodContext);
    }

    private void attachPropertyAutoCompleteFacetIfChoicesMethodIsFound(
            final ProcessMethodContext processMethodContext) {

        // optimization step, not strictly required
        if(!super.isPropertyOrMixinMain(processMethodContext)) {
            return;
        }

        val getterOrMixinMain = processMethodContext.getMethod();
        val namingConvention = processMethodContext.isMixinMain()
                ? getNamingConventionForActionSupport(processMethodContext, PREFIX)
                : getNamingConventionForPropertyAndCollectionSupport(processMethodContext, PREFIX); // handles getters

        val cls = processMethodContext.getCls();
        val returnType = getterOrMixinMain.getReturnType();
        val autoCompleteMethod = MethodFinder
                .findMethod(
                        cls,
                        namingConvention,
                        NO_RETURN,
                        STRING_ARG)
                .findFirst()
                .orElse(null);
        if (autoCompleteMethod == null) {
            return;
        }
        processMethodContext.removeMethod(autoCompleteMethod);

        final FacetHolder property = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new PropertyAutoCompleteFacetMethod(autoCompleteMethod, returnType, property));
    }


}
