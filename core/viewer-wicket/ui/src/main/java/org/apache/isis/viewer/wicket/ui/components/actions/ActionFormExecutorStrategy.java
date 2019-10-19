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
package org.apache.isis.viewer.wicket.ui.components.actions;

import org.apache.wicket.ajax.AjaxRequestTarget;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.panels.FormExecutorStrategy;

public class ActionFormExecutorStrategy implements FormExecutorStrategy<ActionModel> {

    private final ActionModel model;

    public ActionFormExecutorStrategy(final ActionModel actionModel) {
        model = actionModel;
    }

    @Override
    public ActionModel getModel() {
        return model;
    }

    @Override
    public ObjectAdapter obtainTargetAdapter() {
        return model.getTargetAdapter();
    }

    @Override
    public String getReasonInvalidIfAny() {
        return model.getReasonInvalidIfAny();
    }

    @Override
    public void onExecuteAndProcessResults(final AjaxRequestTarget target) {

        if (model.isBookmarkable()) {
            /*
            BookmarkedPagesModelProvider application = (BookmarkedPagesModelProvider) Session.get();
            BookmarkedPagesModel bookmarkedPagesModel = application.getBookmarkedPagesModel();
            bookmarkedPagesModel.bookmarkPage(model);
             */
        }

        if (actionPrompt != null) {
            actionPrompt.closePrompt(target);
            // cos will be reused next time, so mustn't cache em.
            model.clearArguments();
        }
    }

    @Override
    public ObjectAdapter obtainResultAdapter() {
        return model.execute();
    }

    @Override
    public void redirectTo(
            final ObjectAdapter resultAdapter,
            final AjaxRequestTarget targetIfany) {
        
        ActionResultResponse resultResponse = ActionResultResponseType
                .determineAndInterpretResult(model, targetIfany, resultAdapter);
        
        resultResponse.getHandlingStrategy().handleResults(model.getCommonContext(), resultResponse);
    }


    ///////////////////////////////////////////////////////

    private ActionPrompt actionPrompt;
    void setActionPrompt(final ActionPrompt actionPrompt) {
        this.actionPrompt = actionPrompt;
    }


}
