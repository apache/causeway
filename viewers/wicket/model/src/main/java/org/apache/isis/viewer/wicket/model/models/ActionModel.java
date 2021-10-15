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
package org.apache.isis.viewer.wicket.model.models;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestHandler;

import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.OpenUrlStrategy;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.config.viewer.web.WebAppContextPath;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteractionHead;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.common.model.action.ActionFormUiModel;

import lombok.NonNull;
import lombok.val;

public interface ActionModel
extends ActionFormUiModel, FormExecutorContext, BookmarkableModel, IModel<ManagedObject> {

    /** Resets arguments to their fixed point default values
     * @see ActionInteractionHead#defaults(org.apache.isis.core.metamodel.interactions.managed.ManagedAction)
     */
    void clearArguments();

    void reassessPendingParamUiModels(int skipCount);
    ManagedObject executeActionAndReturnResult();
    Can<ManagedObject> snapshotArgs();
    IRequestHandler downloadHandler(Object value);

    // -- UTILITY

    public static IRequestHandler redirectHandler(
            final Object value,
            final @NonNull OpenUrlStrategy openUrlStrategy,
            final @NonNull WebAppContextPath webAppContextPath) {

        if(value instanceof java.net.URL) {
            val url = (java.net.URL) value;
            return new RedirectRequestHandlerWithOpenUrlStrategy(url.toString());
        }
        if(value instanceof LocalResourcePath) {
            val localResourcePath = (LocalResourcePath) value;
            return new RedirectRequestHandlerWithOpenUrlStrategy(
                    localResourcePath.getEffectivePath(webAppContextPath::prependContextPath),
                    localResourcePath.getOpenUrlStrategy());
        }
        return null;
    }

}