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
package org.apache.causeway.core.metamodel.facets.properties.autocomplete.method;

import javax.inject.Inject;

import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.members.support.MemberSupportFacetFactoryAbstract;
import org.apache.causeway.core.metamodel.methods.MethodFinder;

import lombok.val;

public class PropertyAutoCompleteFacetMethodFactory
extends MemberSupportFacetFactoryAbstract {

    @Inject
    public PropertyAutoCompleteFacetMethodFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.PROPERTIES_ONLY, MemberSupportPrefix.AUTO_COMPLETE);
    }

    @Override
    protected void search(
            final ProcessMethodContext processMethodContext,
            final MethodFinder methodFinder) {

        val getterOrMixinMain = processMethodContext.getMethod();

        methodFinder
        .streamMethodsMatchingSignature(STRING_ARG)
        .peek(processMethodContext::removeMethod)
        .forEach(autoCompleteMethod->{
            addFacet(
                    new PropertyAutoCompleteFacetMethod(
                            autoCompleteMethod, getterOrMixinMain.getReturnType(), processMethodContext.getFacetHolder()));
        });

    }

}
