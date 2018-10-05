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
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
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
        if(facetHolder.containsDoOpFacet(CssClassFacet.class)) {
            return;
        }

        final Method method = processMethodContext.getMethod();
        final String name = method.getName();

        // bit of a bodge... we want to ignore any service actions; any contributed actions will be picked up below
        // in process(final ProcessContributeeMemberContext processMemberContext)
        //
        // if we don't do this, then any contributed properties or collections end up picking up the CssClass; almost
        // certainly not what is expected/required.
        final Class<?> owningType = facetHolder.getOwningType();
        if(servicesInjector.isService(owningType)) {
            return;
        }
        CssClassFacet cssClassFacet = createFromConfiguredRegexIfPossible(name, facetHolder);

        // no-op if null
        FacetUtil.addFacet(cssClassFacet);
    }


    @Override
    public void process(final ProcessContributeeMemberContext processMemberContext) {

        final ObjectMember objectMember = processMemberContext.getFacetHolder();
        if(!(objectMember instanceof ObjectAction)) {
            return;
        }
        if(objectMember.containsDoOpFacet(CssClassFacet.class)) {
            return;
        }

        final String id = objectMember.getId();
        CssClassFacet cssClassFacet = createFromConfiguredRegexIfPossible(id, objectMember);

        // no-op if null
        FacetUtil.addFacet(cssClassFacet);
    }

    // -- cssClassFromPattern

    private final static Pattern CSS_CLASS_REGEX_PATTERN = Pattern.compile("([^:]+):(.+)");

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
            final String cssClassPatterns = getConfiguration().getString("isis.reflector.facet.cssClass.patterns");
            this.cssClassByPattern = buildCssClassByPattern(cssClassPatterns);
        }
        return cssClassByPattern;
    }

    private static Map<Pattern, String> buildCssClassByPattern(String cssClassPatterns) {
        final Map<Pattern,String> cssClassByPattern = _Maps.newLinkedHashMap();
        if(cssClassPatterns != null) {
            final StringTokenizer regexToCssClasses = new StringTokenizer(cssClassPatterns, ConfigurationConstants.LIST_SEPARATOR);
            final Map<String,String> cssClassByRegex = _Maps.newLinkedHashMap();
            while (regexToCssClasses.hasMoreTokens()) {
                String regexToCssClass = regexToCssClasses.nextToken().trim();
                if (_Strings.isNullOrEmpty(regexToCssClass)) {
                    continue;
                }
                final Matcher matcher = CSS_CLASS_REGEX_PATTERN.matcher(regexToCssClass);
                if(matcher.matches()) {
                    cssClassByRegex.put(matcher.group(1), matcher.group(2));
                }
            }
            for (Map.Entry<String, String> entry : cssClassByRegex.entrySet()) {
                final String regex = entry.getKey();
                final String cssClass = entry.getValue();
                cssClassByPattern.put(Pattern.compile(regex), cssClass);
            }
        }
        return cssClassByPattern;
    }




}

