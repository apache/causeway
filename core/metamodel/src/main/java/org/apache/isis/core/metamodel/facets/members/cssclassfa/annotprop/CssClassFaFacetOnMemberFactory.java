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

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.CssClassFa;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaPosition;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFacet;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedAnnotation;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class CssClassFaFacetOnMemberFactory extends FacetFactoryAbstract implements ContributeeMemberFacetFactory, MetaModelValidatorRefiner, IsisConfigurationAware {
    
    private final MetaModelValidatorForDeprecatedAnnotation validator = new MetaModelValidatorForDeprecatedAnnotation(CssClassFa.class);


    public CssClassFaFacetOnMemberFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        CssClassFaFacet cssClassFaFacet = createFromMetadataPropertiesIfPossible(processMethodContext);
        if (cssClassFaFacet == null) {
            cssClassFaFacet = createFromConfiguredRegexIfPossible(processMethodContext);
        }

        // no-op if null
        FacetUtil.addFacet(cssClassFaFacet);
    }

    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {
        CssClassFaFacet cssClassFaFacet = createFromMetadataPropertiesIfPossible(processMemberContext);
        // no-op if null
        FacetUtil.addFacet(cssClassFaFacet);
    }

    private static CssClassFaFacet createFromMetadataPropertiesIfPossible(
            final ProcessContextWithMetadataProperties<? extends FacetHolder> pcwmp) {

        final FacetHolder holder = pcwmp.getFacetHolder();

        final Properties properties = pcwmp.metadataProperties("cssClassFa");
        return properties != null ? new CssClassFaFacetOnMemberFromProperties(properties, holder) : null;
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

        String value = faIconIfAnyFor(method);
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

    private String faIconIfAnyFor(Method method) {
        final String name = method.getName();
        return faIconIfAnyFor(name);
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
            final String cssClassFaPatterns = configuration.getString("isis.reflector.facet.cssClassFa.patterns");
            this.faIconByPattern = buildFaIconByPattern(cssClassFaPatterns);
        }
        return faIconByPattern;
    }

    private static Map<Pattern, String> buildFaIconByPattern(String cssClassFaPatterns) {
        final Map<Pattern, String> faIconByPattern = Maps.newLinkedHashMap();
        if (cssClassFaPatterns != null) {
            final StringTokenizer regexToFaIcons = new StringTokenizer(cssClassFaPatterns, ConfigurationConstants.LIST_SEPARATOR);
            final Map<String, String> faIconByRegex = Maps.newLinkedHashMap();
            while (regexToFaIcons.hasMoreTokens()) {
                String regexToFaIcon = regexToFaIcons.nextToken().trim();
                if (Strings.isNullOrEmpty(regexToFaIcon)) {
                    continue;
                }
                final Matcher matcher = FA_ICON_REGEX_PATTERN.matcher(regexToFaIcon);
                if (matcher.matches()) {
                    faIconByRegex.put(matcher.group(1), matcher.group(2));
                }
            }
            for (Map.Entry<String, String> entry : faIconByRegex.entrySet()) {
                final String regex = entry.getKey();
                final String faIcon = entry.getValue();
                faIconByPattern.put(Pattern.compile(regex), faIcon);
            }
        }
        return faIconByPattern;
    }

    // endregion

    // //////////////////////////////////////

    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(validator);
    }

    // region > injected
    private IsisConfiguration configuration;

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        this.configuration = configuration;
        validator.setConfiguration(configuration);
    }
    // endregion
}
