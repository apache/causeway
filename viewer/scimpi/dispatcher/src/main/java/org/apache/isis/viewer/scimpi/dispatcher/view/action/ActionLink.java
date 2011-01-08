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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.scimpi.dispatcher.AbstractElementProcessor;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;
import org.apache.isis.viewer.scimpi.dispatcher.view.HelpLink;


public class ActionLink extends AbstractElementProcessor {

    public void process(Request request) {
        String objectId = request.getOptionalProperty(OBJECT);
        String method = request.getOptionalProperty(METHOD);
        String forwardResultTo = request.getOptionalProperty(VIEW);
        String forwardVoidTo = request.getOptionalProperty(VOID);
        String resultOverride = request.getOptionalProperty(RESULT_OVERRIDE);
        String resultOverrideSegment = resultOverride == null ? "" : "&amp;" + RESULT_OVERRIDE + "=" + resultOverride;
        String resultName = request.getOptionalProperty(RESULT_NAME);
        String resultNameSegment = resultName == null ? "" : "&amp;" + RESULT_NAME + "=" + resultName;
        String scope = request.getOptionalProperty(SCOPE);
        String scopeSegment = scope == null ? "" : "&amp;" + SCOPE + "=" + scope;
        String confirm = request.getOptionalProperty(CONFIRM);
        // TODO need a mechanism for globally dealing with encoding; then use the new encode method
        String confirmSegment = confirm == null ? "" : "&amp;" + CONFIRM + "=" + URLEncoder.encode(confirm);

        RequestContext context = request.getContext();
        ObjectAdapter object = MethodsUtils.findObject(context, objectId);
        String version = context.mapVersion(object);
        ObjectAction action = MethodsUtils.findAction(object, method);

        ActionContent parameterBlock = new ActionContent(action);
        request.setBlockContent(parameterBlock);
        request.pushNewBuffer();
        request.processUtilCloseTag();
        String text = request.popBuffer();

        if (MethodsUtils.isVisibleAndUsable(object, action)) {
            writeLink(request, objectId, version, method, forwardResultTo, forwardVoidTo, resultNameSegment, scopeSegment, confirmSegment,
                    context, action, parameterBlock, text);
        }
        request.popBlockContent();
    }

    public static void writeLink(
            Request request,
            String objectId,
            String version,
            String method,
            String forwardResultTo,
            String forwardVoidTo,
            String resultNameSegment,
            String scopeSegment,
            String confirmSegment,
            RequestContext context,
            ObjectAction action,
            ActionContent parameterBlock,
            String text) {
        text = text == null || text.trim().equals("") ? action.getName() : text;
   
        String parameterSegment = "";
        String[] parameters = parameterBlock.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            parameterSegment += "&param" + i + "=" + parameters[i];
        }
        
        String interactionParamters = context.encodedInteractionParameters();
        String forwardResultSegment = forwardResultTo == null ? "" :  "&amp;" + VIEW + "=" + context.fullFilePath(forwardResultTo);
        String voidView = context.fullFilePath(forwardVoidTo == null ? context.getResourceFile() : forwardVoidTo);
        String forwardVoidSegment = "&amp;" + VOID + "=" + voidView;
        request.appendHtml("<a href=\"action.app?" + OBJECT + "=" + objectId + "&amp;" + VERSION + "=" + version + "&amp;" + METHOD + "=" + method
                + forwardResultSegment + forwardVoidSegment + resultNameSegment + parameterSegment + scopeSegment + confirmSegment + interactionParamters + "\">");
        request.appendHtml(text);
        request.appendHtml("</a>");
        HelpLink.append(request, action.getHelp(), action.getDescription());
    }

    public String getName() {
        return "action-link";
    }

}

