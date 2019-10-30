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

package org.apache.isis.metamodel.facets.members.order.annotprop;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.members.order.MemberOrderFacet;

public class MemberOrderFacetFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public MemberOrderFacetFactory() {
        super(FeatureType.MEMBERS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        MemberOrderFacet memberOrderFacet = createFromAnnotationIfPossible(processMethodContext);

        // no-op if facet is null
        FacetUtil.addFacet(memberOrderFacet);
    }

    @Override
    public void process(final ProcessContributeeMemberContext processMemberContext) {

    }

    private MemberOrderFacet createFromAnnotationIfPossible(final ProcessMethodContext processMethodContext) {
        
        final MemberOrder annotation = processMethodContext.synthesizeOnMethod(MemberOrder.class)
                .orElse(null);
                
//        _Assert.assertEquals("expected same", annotation,
//                Annotations.getAnnotation(processMethodContext.getMethod(), MemberOrder.class));
        
        if (annotation != null) {
            return new MemberOrderFacetAnnotation(
                    annotation.name(),
                    annotation.sequence(),
                    getTranslationService(),
                    processMethodContext.getFacetHolder());
        }
        else {
            return null;
        }
    }

}
