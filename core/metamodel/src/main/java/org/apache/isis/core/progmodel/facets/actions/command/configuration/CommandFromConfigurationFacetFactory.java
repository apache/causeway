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

package org.apache.isis.core.progmodel.facets.actions.command.configuration;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.Command;
import org.apache.isis.applib.services.HasTransactionId;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.semantics.ActionSemanticsFacet;

/**
 * {@link CommandFacet} can also be installed via a naming convention, see
 * {@link org.apache.isis.core.progmodel.facets.actions.invoke.event.InteractionWithActionFacetFactory}.
 */
public class CommandFromConfigurationFacetFactory extends FacetFactoryAbstract implements IsisConfigurationAware  {

    private static final String  COMMAND_ACTIONS_KEY = "isis.services.command.actions";

    private static enum ActionCategorization {
        ALL,
        IGNORE_SAFE,
        NONE;
        public static ActionCategorization parse(final String value) {
            if ("ignoreQueryOnly".equalsIgnoreCase(value) || "ignoreSafe".equalsIgnoreCase(value)) {
                return IGNORE_SAFE;
            } else if ("all".equals(value)) {
                return ALL;
            } else {
                return NONE;
            }
        }
    }
    
    private IsisConfiguration configuration;

    public CommandFromConfigurationFacetFactory() {
        super(FeatureType.ACTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        final FacetedMethod facetHolder = processMethodContext.getFacetHolder();
        final Class<?> cls = processMethodContext.getCls();
        final Method method = processMethodContext.getMethod();

        final ActionSemanticsFacet actionSemanticsFacet = facetHolder.getFacet(ActionSemanticsFacet.class);
        if(actionSemanticsFacet == null) {
            throw new IllegalStateException("Require ActionSemanticsFacet in order to process");
        }
        if(facetHolder.containsDoOpFacet(CommandFacet.class)) {
            // do not replace
            return;
        }
        if(HasTransactionId.class.isAssignableFrom(cls)) {
            // do not install on any implementation of HasTransactionId
            // (ie commands, audit entries, published events).
            return; 
        }
        final ActionCategorization categorization = ActionCategorization.parse(configuration.getString(COMMAND_ACTIONS_KEY));
        if(categorization == ActionCategorization.NONE) {
            return;
        }
        if(actionSemanticsFacet.value() == Of.SAFE && categorization == ActionCategorization.IGNORE_SAFE) {
            return;
        }
        final Command annotation = Annotations.getAnnotation(method, Command.class);
        FacetUtil.addFacet(create(annotation, facetHolder));
    }

    private CommandFacet create(final Command annotation, final FacetHolder holder) {
        return new CommandFacetFromConfiguration(Command.Persistence.PERSISTED, Command.ExecuteIn.FOREGROUND, holder);
    }

    // //////////////////////////////////////

    @Override
    public void setConfiguration(IsisConfiguration configuration) {
        this.configuration = configuration;
    }

}
