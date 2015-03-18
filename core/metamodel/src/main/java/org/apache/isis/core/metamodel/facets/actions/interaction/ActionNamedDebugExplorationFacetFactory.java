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

package org.apache.isis.core.metamodel.facets.actions.interaction;

import java.lang.reflect.Method;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.MethodPrefixBasedFacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.actions.debug.DebugFacet;
import org.apache.isis.core.metamodel.facets.actions.exploration.ExplorationFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForDeprecatedMethodPrefix;

/**
 * Sets up {@link org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet} and also an {@link org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionInvocationFacet}, along with a number of supporting
 * facets that are based on the action's name.
 * 
 * <p>
 * The supporting methods are: {@link ExplorationFacet}
 * and {@link DebugFacet}. In addition a {@link NamedFacet} is inferred from the
 * name (taking into account the above well-known prefixes).
 *
 * @deprecated
 */
@Deprecated
public class ActionNamedDebugExplorationFacetFactory extends MethodPrefixBasedFacetFactoryAbstract implements MetaModelValidatorRefiner, IsisConfigurationAware {

    private static final String EXPLORATION_PREFIX = "Exploration";
    private static final String DEBUG_PREFIX = "Debug";

    private final MetaModelValidatorForDeprecatedMethodPrefix explorationValidator = new MetaModelValidatorForDeprecatedMethodPrefix(EXPLORATION_PREFIX);

    private final MetaModelValidatorForDeprecatedMethodPrefix debugValidator = new MetaModelValidatorForDeprecatedMethodPrefix(DEBUG_PREFIX);

    private static final String[] PREFIXES = { EXPLORATION_PREFIX, DEBUG_PREFIX };

    public ActionNamedDebugExplorationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY, OrphanValidation.VALIDATE, PREFIXES);
    }

    // ///////////////////////////////////////////////////////
    // Actions
    // ///////////////////////////////////////////////////////

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        // DebugFacet, ExplorationFacet
        attachDebugFacetIfActionMethodNamePrefixed(processMethodContext);
        attachExplorationFacetIfActionMethodNamePrefixed(processMethodContext);

        // inferred name
        // (must be called after the attachinvocationFacet methods)
        attachNamedFacetInferredFromMethodName(processMethodContext); 
    }

    private void attachDebugFacetIfActionMethodNamePrefixed(final ProcessMethodContext processMethodContext) {
        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        if (!capitalizedName.startsWith(DEBUG_PREFIX)) {
            return;
        }
        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        final Facet facet = new DebugFacetViaNamingConvention(facetedMethod);
        FacetUtil.addFacet(debugValidator.flagIfPresent(facet, processMethodContext));
    }

    private void attachExplorationFacetIfActionMethodNamePrefixed(final ProcessMethodContext processMethodContext) {

        final Method actionMethod = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(actionMethod.getName());
        if (!capitalizedName.startsWith(EXPLORATION_PREFIX)) {
            return;
        }
        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        final Facet facet = new ExplorationFacetViaNamingConvention(facetedMethod);
        FacetUtil.addFacet(explorationValidator.flagIfPresent(facet, processMethodContext));
    }

    /**
     * Must be called after added the debug, exploration etc facets.
     */
    private void attachNamedFacetInferredFromMethodName(final ProcessMethodContext processMethodContext) {

        final Method method = processMethodContext.getMethod();
        final String capitalizedName = StringExtensions.asCapitalizedName(method.getName());

        // this is nasty...
        String name = capitalizedName;
        name = StringExtensions.removePrefix(name, DEBUG_PREFIX);
        name = StringExtensions.removePrefix(name, EXPLORATION_PREFIX);
        name = StringExtensions.asNaturalName2(name);

        final FacetHolder facetedMethod = processMethodContext.getFacetHolder();
        FacetUtil.addFacet(new NamedFacetInferred(name, facetedMethod));
    }


    @Override
    public void refineMetaModelValidator(final MetaModelValidatorComposite metaModelValidator, final IsisConfiguration configuration) {
        metaModelValidator.add(explorationValidator);
        metaModelValidator.add(debugValidator);
    }

    @Override
    public void setConfiguration(final IsisConfiguration configuration) {
        explorationValidator.setConfiguration(configuration);
        debugValidator.setConfiguration(configuration);
    }

}
