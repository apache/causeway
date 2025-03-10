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
package org.apache.causeway.viewer.wicket.ui.pages.value;

import java.util.function.BiFunction;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;

import org.apache.causeway.applib.services.publishing.spi.PageRenderSubscriber;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ValueModel;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

/**
 * Web page representing an action invocation.
 */
@AuthorizeInstantiation(UserMemento.AUTHORIZED_USER_ROLE)
public class ValuePage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_ACTION_NAME = "actionName";
    private final ValueModel valueModel;

    public ValuePage(final ValueModel valueModel, final String actionName) {
        super(PageParameterUtils.newPageParameters(), actionName, UiComponentType.VALUE);
        this.valueModel = valueModel;

        Wkt.labelAdd(themeDiv, ID_ACTION_NAME, actionName);

        addChildComponents(themeDiv, valueModel);
        addBookmarkedPages(themeDiv);
    }

    @Override
    public void onRendering(final Can<PageRenderSubscriber> enabledObjectRenderSubscribers) {
        onRenderingOrRendered(enabledObjectRenderSubscribers, (pageRenderSubscriber, value) -> {
            pageRenderSubscriber.onRenderingValue(value);
            return null;
        });
    }

    @Override
    public void onRendered(final Can<PageRenderSubscriber> enabledObjectRenderSubscribers) {
        onRenderingOrRendered(enabledObjectRenderSubscribers, (pageRenderSubscriber, value) -> {
            pageRenderSubscriber.onRenderedValue(value);
            return null;
        });
    }

    private void onRenderingOrRendered(
            final Can<PageRenderSubscriber> pageRenderSubscribers,
            final BiFunction<PageRenderSubscriber, Object, Void> handler) {

        if(pageRenderSubscribers.isEmpty()) {
            return;
        }

        // guard against unspecified
        ManagedObjects.asSpecified(valueModel.getObject())
        .ifPresent(managedObject->{

            var nullableValuePojo = managedObject.getPojo();

            pageRenderSubscribers.forEach(subscriber -> handler.apply(subscriber, nullableValuePojo));
        });
    }
}
