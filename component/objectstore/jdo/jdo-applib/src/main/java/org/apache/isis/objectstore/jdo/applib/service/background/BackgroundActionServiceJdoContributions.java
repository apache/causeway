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
import org.apache.isis.applib.services.reifiableaction.ReifiableAction;
import org.apache.isis.objectstore.jdo.applib.service.reifiableaction.ReifiableActionJdo;


public class BackgroundActionServiceJdoContributions extends AbstractFactoryAndRepository {

    @ActionSemantics(Of.SAFE)
    @NotInServiceMenu
    @NotContributed(As.ACTION)
    @Render(Type.EAGERLY)
    public List<ReifiableActionJdo> backgroundActions(final ReifiableActionJdo parent) {
        return backgroundActionRepository.findByParent(parent);
    }

    @ActionSemantics(Of.SAFE)
    @NotInServiceMenu
    @NotContributed(As.ACTION)
    @Render(Type.EAGERLY)
    public List<ReifiableActionJdo> siblingActions(final ReifiableActionJdo siblingAction) {
        final ReifiableAction parent = siblingAction.getParent();
        if(parent == null || !(parent instanceof ReifiableActionJdo)) {
            return Collections.emptyList();
        }
        final ReifiableActionJdo parentJdo = (ReifiableActionJdo) parent;
        final List<ReifiableActionJdo> siblingActions = backgroundActionRepository.findByParent(parentJdo);
        siblingActions.remove(siblingAction);
        return siblingActions;
    }
    
    // //////////////////////////////////////

    @javax.inject.Inject
    private BackgroundActionServiceJdoRepository backgroundActionRepository;
    
}
