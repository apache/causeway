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

package org.apache.isis.core.progmodel.facets.object.membergroups;

import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.MemberGroups;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.object.membergroups.MemberGroupLayoutFacet;
import org.apache.isis.core.progmodel.facets.object.membergroups.annotation.MemberGroupLayoutFacetAnnotation;
import org.apache.isis.core.progmodel.facets.object.membergroups.annotation.MemberGroupLayoutFacetFallback;
import org.apache.isis.core.progmodel.facets.object.membergroups.annotation.MemberGroupsFacetAnnotation;

public class MemberGroupLayoutFacetFactory extends FacetFactoryAbstract {

    public MemberGroupLayoutFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        FacetUtil.addFacet(create(processClassContext));
    }

    private MemberGroupLayoutFacet create(final ProcessClassContext processClassContext) {
        final FacetHolder holder = processClassContext.getFacetHolder();
        
        final MemberGroupLayout mglAnnot = Annotations.getAnnotation(processClassContext.getCls(), MemberGroupLayout.class);
        if (mglAnnot != null) {
            return new MemberGroupLayoutFacetAnnotation(mglAnnot, holder);
        }
        final MemberGroups mgAnnot = Annotations.getAnnotation(processClassContext.getCls(), MemberGroups.class);
        if (mgAnnot != null) {
            return new MemberGroupsFacetAnnotation(mgAnnot, processClassContext.getFacetHolder());
        }
        return new MemberGroupLayoutFacetFallback(holder); 
    }


}
