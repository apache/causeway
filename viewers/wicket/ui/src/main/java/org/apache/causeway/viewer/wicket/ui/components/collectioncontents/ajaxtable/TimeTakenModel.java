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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.Locale;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.commons.internal.base._Timing;
import org.apache.causeway.commons.internal.base._Timing.StopWatch;
import org.apache.causeway.core.interaction.session.CausewayInteraction;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

/**
 * Produces render/response timing info when in prototyping mode.
 * <p>
 * Currently used to add a 'took seconds' label to bottom of data tables.
 *
 * @since 2.0
 */
class TimeTakenModel
implements IModel<String> {
    private static final long serialVersionUID = 1L;

    static IModel<String> createForPrototypingElseBlank(final MetaModelContext mmc) {
        return mmc.getSystemEnvironment().isPrototyping()
            ? new TimeTakenModel(mmc)
            : Model.of("");
    }

    private final StopWatch stopWatch;

    protected TimeTakenModel(final MetaModelContext mmc) {
        this.stopWatch = mmc.getInteractionService().currentInteraction()
            .map(CausewayInteraction.class::cast)
            .map(interaction->_Timing.atSystemNanos(interaction.getStartedAtSystemNanos()))
            .orElseGet(_Timing::now);
    }

    @Override
    public String getObject() {
        return String.format(Locale.US, "... took %.2f seconds", stopWatch.getSeconds());
    }

}
