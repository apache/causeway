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
package org.apache.causeway.viewer.wicket.ui.errors;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.request.resource.JavaScriptResourceReference;

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.services.message.MessageBroker;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.util.WktContext;

import lombok.val;

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
public class JGrowlBehaviour
extends AbstractDefaultAjaxBehavior
implements HasCommonContext {

    private static final long serialVersionUID = 1L;

    public JGrowlBehaviour(final MetaModelContext commonContext) {
        this.mmc = commonContext;
    }

    @Override
    protected void respond(final AjaxRequestTarget target) {

        val configuration = getMetaModelContext().getConfiguration();
        getMessageBroker().ifPresent(messageBroker->{
            String feedbackMsg = JGrowlUtil.asJGrowlCalls(messageBroker, configuration);
            if(!_Strings.isNullOrEmpty(feedbackMsg)) {
                target.appendJavaScript(feedbackMsg);
            }
        });
    }

    @Override
    public void renderHead(final Component component, final IHeaderResponse response) {
        super.renderHead(component, response);

        renderFeedbackMessages(response);
    }

    public void renderFeedbackMessages(final IHeaderResponse response) {
        response.render(
                JavaScriptHeaderItem
                .forReference(new JavaScriptResourceReference(JGrowlBehaviour.class, "js/causeway-bootstrap-growl.js")));

        val configuration = getMetaModelContext().getConfiguration();
        getMessageBroker().ifPresent(messageBroker->{

            String feedbackMsg = JGrowlUtil.asJGrowlCalls(messageBroker, configuration);
            if(_Strings.isNotEmpty(feedbackMsg)) {
                response.render(OnDomReadyHeaderItem.forScript(feedbackMsg));
            }

        });

    }

    private transient MetaModelContext mmc;
    @Override
    public MetaModelContext getMetaModelContext() {
        return mmc = WktContext.computeIfAbsent(mmc);
    }

}
