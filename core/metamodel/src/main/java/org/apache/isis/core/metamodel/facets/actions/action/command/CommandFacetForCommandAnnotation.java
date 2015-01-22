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

package org.apache.isis.core.metamodel.facets.actions.action.command;

import org.apache.isis.applib.annotation.Command;
import org.apache.isis.applib.annotation.Command.ExecuteIn;
import org.apache.isis.applib.annotation.Command.Persistence;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacet;
import org.apache.isis.core.metamodel.facets.actions.command.CommandFacetAbstract;

/**
 * @deprecated
 */
@Deprecated
public class CommandFacetForCommandAnnotation extends CommandFacetAbstract {

    /**
     * @deprecated
     */
    @Deprecated
    public static CommandFacet create(final Command annotation, final FacetHolder holder) {
        return annotation == null
                ? null
                : new CommandFacetForCommandAnnotation(annotation.persistence(), annotation.executeIn(), Enablement.isDisabled(annotation.disabled()), holder);
    }

    private CommandFacetForCommandAnnotation(
            final Persistence persistence,
            final ExecuteIn executeIn,
            final Enablement enablement,
            final FacetHolder holder) {
        super(persistence, executeIn, enablement, holder);
    }


}
