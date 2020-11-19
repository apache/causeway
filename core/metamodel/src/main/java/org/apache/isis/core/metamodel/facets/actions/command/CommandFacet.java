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

package org.apache.isis.core.metamodel.facets.actions.command;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.commanddto.processor.CommandDtoProcessor;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;

import lombok.NonNull;
import lombok.val;

/**
 * Indicates that details of the action should be available as a
 * {@link Command} object, if possible.
 *
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to annotating the
 * action method using <tt>@Command</tt>.
 */
public interface CommandFacet extends Facet {

    public CommandDtoProcessor getProcessor();

    public static boolean isDispatchingEnabled(final @NonNull FacetHolder facetHolder) {
        
        val commandFacet = facetHolder.getFacet(CommandFacet.class);
        if(commandFacet!=null) {
            return true;
        }
        return false;
    }
}
