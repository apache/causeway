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

package org.apache.isis.metamodel.facets.members.cssclassfa.annotprop;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.ConfigurationConstants;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.metamodel.facets.members.cssclassfa.CssClassFaPosition;

public class CssClassFaFacetOnMemberFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory {

    public CssClassFaFacetOnMemberFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        CssClassFaFacet cssClassFaFacet = createFromConfiguredRegexIfPossible(processMethodContext);

        // no-op if null
        FacetUtil.addFacet(cssClassFaFacet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
    }


    // region > faIconFromPattern

    /**
     * The pattern matches definitions like:
     * <ul>
     * <li>methodNameRegex:cssClassFa - will render the Font Awesome icon on the left of the title</li>
     *     <li>methodNameRegex:cssClassFa:(left|right) - will render the Font Awesome icon on the specified position of the title</li>
     * </ul>
     */
    private final static Pattern FA_ICON_REGEX_PATTERN = Pattern.compile("([^:]+):(.+)");

    private CssClassFaFacet createFromConfiguredRegexIfPossible(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();

        String value = faIconIfAnyFor(MixinInterceptor.intendedNameOf(method));
        CssClassFaPosition position = CssClassFaPosition.LEFT;
        if (value != null) {
            int idxOfSeparator = value.indexOf(':');
            if (idxOfSeparator > -1) {
                value = value.substring(0, idxOfSeparator);
                String rest = value.substring(idxOfSeparator + 1);
                position = CssClassFaPosition.valueOf(rest.toUpperCase());
            }
            return new CssClassFaFacetOnMemberFromConfiguredRegex(value, position, processMethodContext.getFacetHolder());
        } else {
            return null;
        }
    }

    private String faIconIfAnyFor(String name) {
        final Map<Pattern, String> faIconByPattern = getFaIconByPattern();

        for (Map.Entry<Pattern, String> entry : faIconByPattern.entrySet()) {
            final Pattern pattern = entry.getKey();
            final String faIcon = entry.getValue();
            if (pattern.matcher(name).matches()) {
                return faIcon;
            }
        }
        return null;
    }

    private Map<Pattern, String> faIconByPattern;
    private Map<Pattern, String> getFaIconByPattern() {
        if (faIconByPattern == null) {
            // build lazily
            this.faIconByPattern = getConfiguration().getReflector().getFacet().getCssClassFa().getPatterns();
        }
        return faIconByPattern;
    }

    // endregion


}
