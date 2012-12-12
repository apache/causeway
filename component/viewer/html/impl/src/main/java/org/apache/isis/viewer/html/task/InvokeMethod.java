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

package org.apache.isis.viewer.html.task;

import java.util.List;

import org.apache.isis.core.commons.exceptions.UnknownTypeException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacetUtils;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.MessageBroker;
import org.apache.isis.viewer.html.action.Action;
import org.apache.isis.viewer.html.action.ActionException;
import org.apache.isis.viewer.html.component.Page;
import org.apache.isis.viewer.html.context.Context;
import org.apache.isis.viewer.html.request.ForwardRequest;
import org.apache.isis.viewer.html.request.Request;

public final class InvokeMethod implements Action {

    @Override
    public void execute(final Request request, final Context context, final Page page) {
        final String idString = request.getObjectId();
        if (idString == null) {
            throw new ActionException("Task no longer in progress");
        }
        final ObjectAdapter target = context.getMappedObject(idString);
        final String id = request.getActionId();
        final ObjectAction action = context.getMappedAction(id);
        if (action == null) {
            throw new ActionException("No such action: " + id);
        }

        boolean executeImmediately = false;
        // TODO use new promptForParameters method instead of all this
        final boolean isContributedMethod = action.isContributed();
        if (action.getParameterCount() == 0) {
            executeImmediately = true;
        } else if (action.getParameterCount() == 1 && isContributedMethod && target.getSpecification().isOfType(action.getParameters().get(0).getSpecification())) {
            executeImmediately = true;
        }

        if (executeImmediately) {
            final ObjectAdapter[] parameters = isContributedMethod ? new ObjectAdapter[] { target } : null;
            final ObjectAdapter result = action.execute(target, parameters);
            final MessageBroker broker = IsisContext.getMessageBroker();
            final List<String> messages = broker.getMessages();
            final List<String> warnings = broker.getWarnings();
            context.setMessagesAndWarnings(messages, warnings);
            context.processChanges();
            final String targetId = context.mapObject(target);
            displayMethodResult(request, context, page, result, targetId);
        } else {
            final MethodTask methodTask = new MethodTask(context, target, action);
            context.addTaskCrumb(methodTask);
            request.forward(ForwardRequest.task(methodTask));
        }
    }

    static void displayMethodResult(final Request request, final Context context, final Page page, final ObjectAdapter result, final String targetId) {
        if (result == null) {
            // TODO ask context for page to display - this will be the most
            // recent object prior to the task
            // null object - so just view service
            request.forward(ForwardRequest.viewService(targetId));
        } else {
            if (result.getSpecification().isParentedOrFreeCollection()) {
                final CollectionFacet facet = CollectionFacetUtils.getCollectionFacetFromSpec(result);
                if (facet.size(result) == 1) {
                    forwardObjectResult(request, context, facet.firstElement(result));
                } else {
                    forwardCollectionResult(request, context, result);
                }
            } else if (result.getSpecification().isValueOrIsParented()) {
                // TODO deal with this object properly, it might not be just a
                // simple string
                final List<String> messages = context.getMessages();
                messages.add(0, "Action returned: " + result.titleString());
                request.forward(ForwardRequest.viewObject(targetId));
            } else if (result.getSpecification().isNotCollection()) {
                forwardObjectResult(request, context, result);
            } else {
                throw new UnknownTypeException(result.getSpecification().getFullIdentifier());
            }
        }
    }

    static void forwardCollectionResult(final Request request, final Context context, final ObjectAdapter coll) {
        final String collectionId = context.mapCollection(coll);
        request.forward(ForwardRequest.listCollection(collectionId));
    }

    static void forwardObjectResult(final Request request, final Context context, final ObjectAdapter resultAdapter) {
        final String objectId = context.mapObject(resultAdapter);
        if (resultAdapter.isTransient() && resultAdapter.getSpecification().persistability() == Persistability.USER_PERSISTABLE) {
            request.forward(ForwardRequest.editObject(objectId));
        } else if (resultAdapter.getSpecification().isService()) {
            request.forward(ForwardRequest.viewService(objectId));
        } else {
            request.forward(ForwardRequest.viewObject(objectId));
        }
    }

    @Override
    public String name() {
        return "method";
    }

}
