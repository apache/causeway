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
package org.apache.isis.viewer.wicket.ui.errors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import org.apache.isis.applib.RecoverableException;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.runtime.iactn.MessageBroker;
import org.apache.isis.viewer.wicket.model.common.CommonContextUtils;

/**
 * Attach to any Ajax button that might trigger a notification (ie calls
 * {@link MessageBroker#addMessage(String)}, {@link MessageBroker#addWarning(String)},
 * {@link MessageBroker#setApplicationError(String)} or throws an {@link RecoverableException}).
 *
 * <p>
 * Attach using the standard Wicket code:
 * <pre>
 * Button editButton = new AjaxButton(ID_EDIT_BUTTON, Model.of("Edit")) { ... }
 * editButton.add(new JGrowlBehaviour());
 * </pre>
 */
public class JGrowlBehaviour extends AbstractDefaultAjaxBehavior {

    private static final long serialVersionUID = 1L;
    private transient IsisAppCommonContext commonContext;
    
    public JGrowlBehaviour(IsisAppCommonContext commonContext) {
        this.commonContext = commonContext;
    }

    @Override
    protected void respond(AjaxRequestTarget target) {
        
        getCommonContext().getMessageBroker().ifPresent(messageBroker->{
            String feedbackMsg = JGrowlUtil.asJGrowlCalls(messageBroker);
            if(!_Strings.isNullOrEmpty(feedbackMsg)) {
                target.appendJavaScript(feedbackMsg);
            }
        });
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);

        renderFeedbackMessages(response);
    }

    public void renderFeedbackMessages(IHeaderResponse response) {
        response.render(
                JavaScriptHeaderItem
                .forReference(new JavaScriptResourceReference(JGrowlBehaviour.class, "js/bootstrap-growl.js")));

        getCommonContext().getMessageBroker().ifPresent(messageBroker->{
        
            String feedbackMsg = JGrowlUtil.asJGrowlCalls(messageBroker);
            if(_Strings.isNotEmpty(feedbackMsg)) {
                response.render(OnDomReadyHeaderItem.forScript(feedbackMsg));
            }
            
        });
        
    }

    protected IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

}
