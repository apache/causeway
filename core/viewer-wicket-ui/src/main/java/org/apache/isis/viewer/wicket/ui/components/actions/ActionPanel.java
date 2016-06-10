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

import java.util.List;

import com.google.common.base.Throwables;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.Command.Executor;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ExecutingPanel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponse;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseHandlingStrategy;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseType;
import org.apache.isis.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} representing an action invocation, backed by an
 * {@link ActionModel}.
 * 
 * <p>
 * Based on the {@link ActionModel.Mode mode}, will render either parameter
 * dialog or the results.
 * 
 * <p>
 * TODO: on results panel, have a button to resubmit?
 */
public class ActionPanel extends PanelAbstract<ActionModel> implements ExecutingPanel {

    private static final long serialVersionUID = 1L;

    private static final String ID_HEADER = "header";

    static final String ID_ACTION_NAME = "actionName";

    private ActionPrompt actionPrompt;

    /**
     * Gives a chance to hide the header part of this action panel, e.g. when shown in an action prompt
     */
    private boolean showHeader = true;

    public ActionPanel(final String id, final ActionModel actionModel) {
        super(id, actionModel);
        actionModel.setExecutingPanel(this);
        buildGui(getActionModel());
    }

    /**
     * Sets the owning action prompt (modal window), if any.
     */
    public void setActionPrompt(ActionPrompt actionPrompt) {
        this.actionPrompt = actionPrompt;
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        buildGui(getModel());
    }

    private void buildGui(final ActionModel actionModel) {
        if (actionModel.hasParameters()) {
            buildGuiForParameters(getActionModel());
        } else {
            buildGuiForNoParameters(actionModel);
        }
    }

    ActionModel getActionModel() {
        return super.getModel();
    }

    public ActionPanel setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        return this;
    }

    private void buildGuiForParameters(final ActionModel actionModel) {

        WebMarkupContainer header = addHeader();

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = actionModel.getTargetAdapter();

            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.PARAMETERS, getActionModel());
            getComponentFactoryRegistry().addOrReplaceComponent(header, ComponentType.ENTITY_ICON_AND_TITLE, new EntityModel(targetAdapter));

            final String actionName = getActionModel().getActionMemento().getAction(actionModel.getSpecificationLoader()).getName();
            header.add(new Label(ID_ACTION_NAME, Model.of(actionName)));

        } catch (final ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = getModel().getTargetAdapter();
            }
            
            // forward onto the target page with the concurrency exception
            ActionResultResponse resultResponse = ActionResultResponseType.OBJECT.interpretResult(this.getActionModel(), targetAdapter, ex);
            resultResponse.getHandlingStrategy().handleResults(this, resultResponse, getIsisSessionFactory());

            getMessageBroker().addWarning(ex.getMessage());
        }
    }

    private WebMarkupContainer addHeader() {
        WebMarkupContainer header = new WebMarkupContainer(ID_HEADER) {
            @Override
            protected void onConfigure() {
                super.onConfigure();

                setVisible(showHeader);
            }
        };
        addOrReplace(header);
        return header;
    }

    private void buildGuiForNoParameters(final ActionModel actionModel) {

        boolean succeeded = executeAndProcessResults(null, null);
        if(succeeded) {
            // nothing to do
        } else {

            // render the target entity again
            //
            // (One way this can occur is if an event subscriber has a defect and throws an exception; in which case
            // the EventBus' exception handler will automatically veto.  This results in a growl message rather than
            // an error page, but is probably 'good enough').
            final ObjectAdapter targetAdapter = actionModel.getTargetAdapter();

            ActionResultResponse resultResponse = ActionResultResponseType.OBJECT.interpretResult(this.getActionModel(), targetAdapter, null);
            resultResponse.getHandlingStrategy().handleResults(this, resultResponse, getIsisSessionFactory());
        }
    }


    protected void bookmarkPage(BookmarkableModel<?> model) {
        getBookmarkedPagesModel().bookmarkPage(model);
    }

    private BookmarkedPagesModel getBookmarkedPagesModel() {
        BookmarkedPagesModelProvider application = (BookmarkedPagesModelProvider) getSession();
        return application.getBookmarkedPagesModel();
    }

    
    /**
     * @param feedbackForm - for feedback messages.
     * @return 
     */
    @Override
    public boolean executeAndProcessResults(AjaxRequestTarget target, Form<?> feedbackForm) {

        permanentlyHide(ComponentType.ENTITY_ICON_AND_TITLE);

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = getModel().getTargetAdapter();

            // no concurrency exception, so continue...
            return executeActionOnTargetAndProcessResults(target, feedbackForm);

        } catch (ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = getModel().getTargetAdapter();
            }

            // forward onto the target page with the concurrency exception
            ActionResultResponse resultResponse = ActionResultResponseType.OBJECT.interpretResult(this.getActionModel(), targetAdapter, ex);
            resultResponse.getHandlingStrategy().handleResults(this, resultResponse, getIsisSessionFactory());

            getMessageBroker().addWarning(ex.getMessage());
            return false;
        }
    }

    /**
     * @param target 
     * @return whether to clear args or not (they aren't if there was a validation exception)
     */
    private boolean executeActionOnTargetAndProcessResults(
            final AjaxRequestTarget target,
            final Form<?> feedbackForm) {
        
        final ActionModel actionModel = getActionModel();
        
        // validate the action parameters (if any)
        final String invalidReasonIfAny = actionModel.getReasonInvalidIfAny();
        
        if (invalidReasonIfAny != null) {
            raiseWarning(target, feedbackForm, invalidReasonIfAny);
            return false;
        }
        
        final CommandContext commandContext = getServicesInjector().lookupService(CommandContext.class);
        final Command command;
        if (commandContext != null) {
            command = commandContext.getCommand();
            command.setExecutor(Executor.USER);
        } else {
            command = null;
        }
        
        
        // the object store could raise an exception (eg uniqueness constraint)
        // so we handle it here.
        try {
            // could be programmatic flushing, so must include in the try... finally
            final ObjectAdapter resultAdapter = getActionModel().executeHandlingApplicationExceptions();
      
            // flush any queued changes, so concurrency or violation exceptions (if any)
            // will be thrown here
            getTransactionManager().flushTransaction();
            
            ActionResultResponse resultResponse = ActionResultResponseType.determineAndInterpretResult(this.getActionModel(), target, resultAdapter);
            resultResponse.getHandlingStrategy().handleResults(this, resultResponse, getIsisSessionFactory());

            if (actionModel.isBookmarkable()) {
                bookmarkPage(actionModel);
            }
            
            if(actionPrompt != null) {
                actionPrompt.closePrompt(target);
                // cos will be reused next time, so mustn't cache em.
                actionModel.clearArguments();
            }

            return true;

        } catch (RuntimeException ex) {

            String message = recognizeException(ex, target, feedbackForm);
            
            if (message != null) {
                // no need to add to message broker, should already have been added...
                
                if(feedbackForm == null) {
                    // forward on instead to void page
                    // (otherwise, we'll have rendered an action parameters page 
                    // and so we'll be staying on that page)
                    ActionResultResponseHandlingStrategy.REDIRECT_TO_VOID.handleResults(this, null, getIsisSessionFactory());
                }

                return false;
            }
            
            // not handled, so capture and propagate
            if(command != null) {
                command.setException(Throwables.getStackTraceAsString(ex));
            }

            throw ex;
        }
    }


    private String recognizeException(RuntimeException ex, AjaxRequestTarget target, Form<?> feedbackForm) {
        
        // REVIEW: this code is similar to stuff in EntityPropertiesForm, perhaps move up to superclass?
        // REVIEW: similar code also in WebRequestCycleForIsis; combine?
        
        // see if the exception is recognized as being a non-serious error
        // (nb: similar code in WebRequestCycleForIsis, as a fallback)
        List<ExceptionRecognizer> exceptionRecognizers = getServicesInjector().lookupServices(ExceptionRecognizer.class);
        String recognizedErrorIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        if(recognizedErrorIfAny != null) {

            // recognized
            raiseWarning(target, feedbackForm, recognizedErrorIfAny);

            getTransactionManager().getCurrentTransaction().clearAbortCause();
            
            // there's no need to abort the transaction, it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).
        }
        return recognizedErrorIfAny;
    }

    public void raiseWarning(AjaxRequestTarget target, Form<?> feedbackForm, String error) {
        if(target != null && feedbackForm != null) {
            target.add(feedbackForm);
            feedbackForm.error(error);
        } else {
            getMessageBroker().addWarning(error);
        }
    }


    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////
    
    protected IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

}
