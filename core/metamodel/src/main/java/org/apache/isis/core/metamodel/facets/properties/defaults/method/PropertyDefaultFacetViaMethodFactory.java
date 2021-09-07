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
package org.apache.isis.core.metamodel.facets.properties.defaults.method;

import javax.inject.Inject;

import org.apache.isis.core.config.progmodel.ProgrammingModelConstants.MemberSupportPrefix;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.methods.MemberSupportFacetFactoryAbstract;
import org.apache.isis.core.metamodel.methods.MethodFinder;
import org.apache.isis.core.metamodel.methods.MethodFinderOptions;

import lombok.val;

public class PropertyDefaultFacetViaMethodFactory
extends MemberSupportFacetFactoryAbstract {

    @Inject
    public PropertyDefaultFacetViaMethodFactory(final MetaModelContext mmc) {
     // to also support properties from mixins, need to not only include properties but also actions
        super(mmc, FeatureType.PROPERTIES_AND_ACTIONS, MemberSupportPrefix.DEFAULT);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // optimization step, not strictly required
        if(!super.isPropertyOrMixinMain(processMethodContext)) {
            return;
        }

        val getterOrMixinMain = processMethodContext.getMethod();
        val methodNameCandidates = memberSupportPrefix.getMethodNamePrefixes()
                .flatMap(processMethodContext::memberSupportCandidates);

        val cls = processMethodContext.getCls();
        val returnType = getterOrMixinMain.getReturnType();
        val method = MethodFinder
                .findMethod(
                    MethodFinderOptions
                        .memberSupport(processMethodContext.getIntrospectionPolicy()),
                    cls,
                    methodNameCandidates,
                    returnType,
                    NO_ARG)
                .findFirst()
                .orElse(null);
        if (method == null) {
            return;
        }
        processMethodContext.removeMethod(method);

        final FacetHolder property = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new PropertyDefaultFacetViaMethod(method, property));
    }

}
