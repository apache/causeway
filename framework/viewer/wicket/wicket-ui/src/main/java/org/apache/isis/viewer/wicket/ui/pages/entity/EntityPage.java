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

package org.apache.isis.viewer.wicket.ui.pages.entity;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * Web page representing an entity.
 */
@AuthorizeInstantiation("org.apache.isis.viewer.wicket.roles.USER")
public class EntityPage extends PageAbstract {

    private final EntityModel model;

    public EntityPage(final PageParameters pageParameters) {
        super(pageParameters, ComponentType.ENTITY);
        this.model = new EntityModel(pageParameters);
        addChildComponents(model);
    }

    public EntityPage(final ObjectAdapter adapter) {
        this(adapter, null);
    }

    /**
     * Ensure that any {@link ConcurrencyException} that might have occurred already
     * (eg from an action invocation) is show.
     */
    public EntityPage(ObjectAdapter adapter, ConcurrencyException exIfAny) {
        super(new PageParameters(), ComponentType.ENTITY);
        this.model = new EntityModel(adapter);
        model.setException(exIfAny);
        addChildComponents(model);
    }


    /**
     * A rather crude way of intercepting the redirect-and-post strategy.
     * 
     * <p>
     * Performs eager loading of corresponding {@link EntityModel}, with
     * {@link ConcurrencyChecking#NO_CHECK no} concurrency checking.
     */
    @Override
    protected void onBeforeRender() {
        this.model.load(ConcurrencyChecking.NO_CHECK);
        super.onBeforeRender();
    }

}
