/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.background;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.NotContributed;
import org.apache.isis.applib.annotation.NotContributed.As;
import org.apache.isis.applib.annotation.NotInServiceMenu;
import org.apache.isis.applib.annotation.Render;
import org.apache.isis.applib.annotation.Render.Type;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.objectstore.jdo.applib.service.command.CommandJdo;


/**
 * This service contributes a <tt>childCommands</tt> collection and a <tt>sublingCommands</tt> collection to
 * any {@link CommandJdo} entity.
 *
 * <p>
 * Because this service influences the UI, it must be explicitly registered as a service
 * (eg using <tt>isis.properties</tt>).
 */
public class BackgroundCommandServiceJdoContributions extends AbstractFactoryAndRepository {

    @ActionSemantics(Of.SAFE)
    @NotInServiceMenu
    @NotContributed(As.ACTION)
    @Render(Type.EAGERLY)
    public List<CommandJdo> childCommands(final CommandJdo parent) {
        return backgroundCommandRepository.findByParent(parent);
    }

    @ActionSemantics(Of.SAFE)
    @NotInServiceMenu
    @NotContributed(As.ACTION)
    @Render(Type.EAGERLY)
    public List<CommandJdo> siblingCommands(final CommandJdo siblingCommand) {
        final Command parent = siblingCommand.getParent();
        if(parent == null || !(parent instanceof CommandJdo)) {
            return Collections.emptyList();
        }
        final CommandJdo parentJdo = (CommandJdo) parent;
        final List<CommandJdo> siblingCommands = backgroundCommandRepository.findByParent(parentJdo);
        siblingCommands.remove(siblingCommand);
        return siblingCommands;
    }


    // //////////////////////////////////////

    @javax.inject.Inject
    private BackgroundCommandServiceJdoRepository backgroundCommandRepository;
    
}
