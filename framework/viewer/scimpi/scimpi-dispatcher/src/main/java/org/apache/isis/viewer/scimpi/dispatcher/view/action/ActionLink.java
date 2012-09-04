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

package org.apache.isis.viewer.scimpi.dispatcher.view.action;

import java.net.URLEncoder;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;
import org.apache.isis.viewer.scimpi.dispatcher.view.HelpLink;

public class ActionLink extends AbstractElementProcessor {

    // REVIEW: confirm this rendering context
    private final Where where = Where.OBJECT_FORM;

    @Override
    public void process(final Request request) {
        String objectId = request.getOptionalProperty(OBJECT);
        final String method = request.getOptionalProperty(METHOD);
        final String forwardResultTo = request.getOptionalProperty(VIEW);
        final String forwardVoidTo = request.getOptionalProperty(VOID);
        final String resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        
        @SuppressWarnings("unused")
        final String resultOverrideSegment = resultOverride == null ? "" : "&amp;" + RESULT_OVERRIDE + "=" + resultOverride;
        
        final String resultName = request.getOptionalProperty(RESULT_NAME);
        final String resultNameSegment = resultName == null ? "" : "&amp;" + RESULT_NAME + "=" + resultName;
        final String scope = request.getOptionalProperty(SCOPE);
        final String scopeSegment = scope == null ? "" : "&amp;" + SCOPE + "=" + scope;
        final String confirm = request.getOptionalProperty(CONFIRM);
        final String completionMessage = request.getOptionalProperty(MESSAGE);

        // TODO need a mechanism for globally dealing with encoding; then use
        // the new encode method
        final String confirmSegment = confirm == null ? "" : "&amp;" + "_" + CONFIRM + "=" + URLEncoder.encode(confirm);
        final String messageSegment = completionMessage == null ? "" : "&amp;" + "_" + MESSAGE + "=" + URLEncoder.encode(completionMessage);

        final RequestContext context = request.getContext();
        final ObjectAdapter object = MethodsUtils.findObject(context, objectId);
        final String version = context.mapVersion(object);
        final ObjectAction action = MethodsUtils.findAction(object, method);
        objectId = request.getContext().mapObject(object, Scope.REQUEST);

        final ActionContent parameterBlock = new ActionContent(action);
        
        request.setBlockContent(parameterBlock);
        request.pushNewBuffer();
        request.processUtilCloseTag();
        final String text = request.popBuffer();

        if (MethodsUtils.isVisibleAndUsable(object, action, where)) {
            writeLink(request, objectId, version, method, forwardResultTo, forwardVoidTo, resultNameSegment, scopeSegment, confirmSegment, messageSegment, context, action, parameterBlock, text);
        }
        request.popBlockContent();
    }

    public static void writeLink(final Request request, final String objectId, final String version, final String method, final String forwardResultTo, final String forwardVoidTo, final String resultNameSegment, final String scopeSegment, final String confirmSegment, final String messageSegment,
            final RequestContext context, final ObjectAction action, final ActionContent parameterBlock, String text) {
        text = text == null || text.trim().equals("") ? action.getName() : text;

        String parameterSegment = "";
        final String[] parameters = parameterBlock.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            parameterSegment += "&param" + (i + 1) + "=" + parameters[i];
        }

        final String interactionParamters = context.encodedInteractionParameters();
        final String forwardResultSegment = forwardResultTo == null ? "" : "&amp;" + "_" + VIEW + "=" + context.fullFilePath(forwardResultTo);
        final String voidView = context.fullFilePath(forwardVoidTo == null ? context.getResourceFile() : forwardVoidTo);
        final String forwardVoidSegment = "&amp;" + "_" + VOID + "=" + voidView;
        request.appendHtml("<a href=\"action.app?" + "_" + OBJECT + "=" + objectId + "&amp;" + "_" + VERSION + "=" + version + "&amp;" + "_" + METHOD + "=" + method + forwardResultSegment + forwardVoidSegment + resultNameSegment + parameterSegment + scopeSegment + confirmSegment + messageSegment
                + interactionParamters + "\">");
        request.appendHtml(text);
        request.appendHtml("</a>");
        HelpLink.append(request, action.getDescription(), action.getHelp());
    }

    @Override
    public String getName() {
        return "action-link";
    }

}
