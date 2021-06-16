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

package org.apache.isis.core.metamodel.facets.members.cssclassfa.annotprop;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.isis.applib.layout.component.CssClassFaPosition;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;

import lombok.val;

public class CssClassFaFacetOnMemberFactory
extends FacetFactoryAbstract {

    @Inject
    public CssClassFaFacetOnMemberFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        addFacetIfPresent(
                createFromConfiguredRegexIfPossible(processMethodContext));
    }

//    /*
//     * The pattern matches definitions like:
//     * <ul>
//     * <li>methodNameRegex:cssClassFa - will render the Font Awesome icon on the left of the title</li>
//     *     <li>methodNameRegex:cssClassFa:(left|right) - will render the Font Awesome icon on the specified position of the title</li>
//     * </ul>
//     */
//    private static final Pattern FA_ICON_REGEX_PATTERN = Pattern.compile("([^:]+):(.+)");

    private Optional<CssClassFaFacet> createFromConfiguredRegexIfPossible(final ProcessMethodContext processMethodContext) {
        val method = processMethodContext.getMethod();

        return faIconIfAnyFor(MixinInterceptor.intendedNameOf(method))
        .map(faIcon->{
            CssClassFaPosition position = CssClassFaPosition.LEFT;
            int idxOfSeparator = faIcon.indexOf(':');
            if (idxOfSeparator > -1) {
                faIcon = faIcon.substring(0, idxOfSeparator);
                String rest = faIcon.substring(idxOfSeparator + 1);
                position = CssClassFaPosition.valueOf(rest.toUpperCase());
            }
            return new CssClassFaFacetOnMemberFromConfiguredRegex(
                    faIcon, position, processMethodContext.getFacetHolder());
        });

    }

    private Optional<String> faIconIfAnyFor(final String name) {
        final Map<Pattern, String> faIconByPattern = getFaIconByPattern();

        for (Map.Entry<Pattern, String> entry : faIconByPattern.entrySet()) {
            final Pattern pattern = entry.getKey();
            final String faIcon = entry.getValue();
            if (pattern.matcher(name).matches()) {
                return Optional.ofNullable(faIcon);
            }
        }
        return Optional.empty();
    }

    private Map<Pattern, String> faIconByPattern;
    private Map<Pattern, String> getFaIconByPattern() {
        if (faIconByPattern == null) {
            // build lazily
            this.faIconByPattern = getConfiguration().getApplib().getAnnotation().getActionLayout().getCssClassFa().getPatterns();
        }
        return faIconByPattern;
    }


}
