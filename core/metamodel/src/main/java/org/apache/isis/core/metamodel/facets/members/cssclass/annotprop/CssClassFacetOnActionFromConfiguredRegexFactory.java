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

package org.apache.isis.core.metamodel.facets.members.cssclass.annotprop;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public class CssClassFacetOnActionFromConfiguredRegexFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public CssClassFacetOnActionFromConfiguredRegexFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        if(facetHolder.containsNonFallbackFacet(CssClassFacet.class)) {
            return;
        }

        final Method method = processMethodContext.getMethod();
        final String name = method.getName();

        // bit of a bodge... we want to ignore any service actions; any contributed actions 
        // will be picked up below
        // in process(final ProcessContributeeMemberContext processMemberContext)
        //
        // if we don't do this, then any contributed properties or collections end up picking 
        // up the CssClass; almost certainly not what is expected/required.
        final Class<?> owningType = facetHolder.getOwningType();
        if(getServiceRegistry().select(owningType).isNotEmpty()) {
            return;
        }
        CssClassFacet cssClassFacet = createFromConfiguredRegexIfPossible(name, facetHolder);

        // no-op if null
        super.addFacet(cssClassFacet);
    }


    @Override
    public void process(final ProcessContributeeMemberContext processMemberContext) {

        final ObjectMember objectMember = processMemberContext.getFacetHolder();
        if(!(objectMember instanceof ObjectAction)) {
            return;
        }
        if(objectMember.containsNonFallbackFacet(CssClassFacet.class)) {
            return;
        }

        final String id = objectMember.getId();
        CssClassFacet cssClassFacet = createFromConfiguredRegexIfPossible(id, objectMember);

        // no-op if null
        super.addFacet(cssClassFacet);
    }

    // -- cssClassFromPattern

    private CssClassFacet createFromConfiguredRegexIfPossible(String name, FacetHolder facetHolder) {
        String value = cssIfAnyFor(name);
        return value != null
                ? new CssClassFacetOnActionFromConfiguredRegex(value, facetHolder)
                        : null;
    }

    private String cssIfAnyFor(String name) {
        final Map<Pattern, String> cssClassByPattern = getCssClassByPattern();

        for (Map.Entry<Pattern, String> entry : cssClassByPattern.entrySet()) {
            final Pattern pattern = entry.getKey();
            final String cssClass = entry.getValue();
            if(pattern.matcher(name).matches()) {
                return cssClass;
            }
        }
        return null;
    }

    private Map<Pattern,String> cssClassByPattern;

    private Map<Pattern, String> getCssClassByPattern() {
        if (cssClassByPattern == null) {
            // build lazily
            this.cssClassByPattern = getConfiguration().getApplib().getAnnotation().getActionLayout().getCssClass().getPatterns();
        }
        return cssClassByPattern;
    }

}

