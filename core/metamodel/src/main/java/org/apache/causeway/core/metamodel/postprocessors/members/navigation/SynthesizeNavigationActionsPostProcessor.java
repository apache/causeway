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
package org.apache.causeway.core.metamodel.postprocessors.members.navigation;

import javax.inject.Inject;

import org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;

/**
 * Synthesizes the synthetic navigation ("selector") actions for parented collections (and scalar
 * references) once per type, during the post-processing phase.
 *
 * <p>
 * Active only when command-log recording support is enabled <i>and</i> the
 * {@link CommandLog.NavigationActionSynthesis#POST_PROCESS POST_PROCESS} synthesis strategy is selected
 * (see {@code causeway.extensions.command-log.navigation-action-synthesis}).  Under the default inline
 * strategy this post-processor is disabled and synthesis happens during introspection instead.
 *
 * <p>
 * Performing synthesis here (rather than from the lazy {@code streamDeclaredActions} path) keeps it out of
 * ordinary action access, so it no longer forces re-entrant introspection of collection element types and
 * cannot recurse without bound on a cyclic collection graph.
 */
public class SynthesizeNavigationActionsPostProcessor
extends MetaModelPostProcessorAbstract {

    @Inject
    public SynthesizeNavigationActionsPostProcessor(final MetaModelContext mmc) {
        super(mmc);
    }

    @Override
    public boolean isEnabled() {
        final CommandLog commandLog = getMetaModelContext().getConfiguration()
                .getExtensions().getCommandLog();
        return commandLog.getRecordingSupport().isEnabled()
                && commandLog.getNavigationActionSynthesis().isPostProcess();
    }

    @Override
    public void postProcessObject(final ObjectSpecification objSpec) {
        if (objSpec instanceof ObjectSpecificationAbstract) {
            ((ObjectSpecificationAbstract) objSpec).synthesizeNavigationActions();
        }
    }

}
