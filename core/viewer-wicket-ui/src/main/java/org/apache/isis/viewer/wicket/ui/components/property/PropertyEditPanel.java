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

package org.apache.isis.viewer.wicket.ui.components.property;

import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.base.Throwables;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ExecutingPanel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.actionresponse.ActionResultResponseHandlingStrategy;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

public class PropertyEditPanel extends PanelAbstract<ScalarModel>
        implements ExecutingPanel {

    private static final long serialVersionUID = 1L;

    private static final String ID_HEADER = "header";

    private static final String ID_PROPERTY_NAME = "propertyName";

    /**
     * Gives a chance to hide the header part of this action panel, e.g. when shown in an action prompt
     */
    private boolean showHeader = true;

    public PropertyEditPanel(final String id, final ScalarModel scalarModel) {
        super(id, new ScalarModel(scalarModel.getParentObjectAdapterMemento(), scalarModel.getPropertyMemento()));
        getScalarModel().setExecutingPanel(this);
        buildGui(getScalarModel());
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        buildGui(getModel());
    }

    private void buildGui(final ScalarModel scalarModel) {
        buildGuiForParameters(scalarModel);
    }

    ScalarModel getScalarModel() {
        return super.getModel();
    }

    public PropertyEditPanel setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
        return this;
    }

    private void buildGuiForParameters(final ScalarModel scalarModel) {

        WebMarkupContainer header = addHeader();

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = scalarModel.getParentObjectAdapterMemento()
                    .getObjectAdapter(AdapterManager.ConcurrencyChecking.CHECK, scalarModel.getPersistenceSession(),
                            scalarModel.getSpecificationLoader());

            scalarModel.toEditMode();

            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.PROPERTY_EDIT_FORM, getScalarModel());
            getComponentFactoryRegistry().addOrReplaceComponent(header, ComponentType.ENTITY_ICON_AND_TITLE, new EntityModel(targetAdapter));

            final OneToOneAssociation property = getScalarModel().getPropertyMemento().getProperty(scalarModel.getSpecificationLoader());
            final String propertyName = property.getName();
            final Label label = new Label(ID_PROPERTY_NAME, Model.of(propertyName));

            NamedFacet namedFacet = property.getFacet(NamedFacet.class);
            if (namedFacet != null) {
                label.setEscapeModelStrings(namedFacet.escaped());
            }

            header.add(label);

        } catch (final ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = scalarModel.getParentObjectAdapterMemento()
                        .getObjectAdapter(AdapterManager.ConcurrencyChecking.CHECK, getPersistenceSession(),
                                getSpecificationLoader());
            }

            // page redirect/handling
            final EntityPage entityPage = new EntityPage(targetAdapter, null);
            setResponsePage(entityPage);

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

    /**
     * @param feedbackForm - for feedback messages.
     * @return
     */
    @Override
    public boolean executeAndProcessResults(AjaxRequestTarget target, Form<?> feedbackForm) {

        permanentlyHide(ComponentType.ENTITY_ICON_AND_TITLE);

        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = getModel().getParentObjectAdapterMemento().getObjectAdapter(
                    AdapterManager.ConcurrencyChecking.CHECK, getModel().getPersistenceSession(), getSpecificationLoader());

            // no concurrency exception, so continue...
            return editTargetAndProcessResults(target, feedbackForm);

        } catch (ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = getModel().getParentObjectAdapterMemento().getObjectAdapter(
                        AdapterManager.ConcurrencyChecking.CHECK, getModel().getPersistenceSession(),
                        getSpecificationLoader());
            }

            // page redirect/handling
            final EntityPage entityPage = new EntityPage(targetAdapter, null);
            setResponsePage(entityPage);

            getMessageBroker().addWarning(ex.getMessage());
            return false;
        }
    }



    private boolean editTargetAndProcessResults(
            final AjaxRequestTarget target,
            final Form<?> feedbackForm) {

        final ScalarModel scalarModel = getScalarModel();

        // validate the action parameters (if any)
        final String invalidReasonIfAny = scalarModel.getReasonInvalidIfAny();

        if (invalidReasonIfAny != null) {
            raiseWarning(target, feedbackForm, invalidReasonIfAny);
            return false;
        }

        final CommandContext commandContext = getServicesInjector().lookupService(CommandContext.class);
        final Command command;
        if (commandContext != null) {
            command = commandContext.getCommand();
            command.setExecutor(Command.Executor.USER);
        } else {
            command = null;
        }


        // the object store could raise an exception (eg uniqueness constraint)
        // so we handle it here.
        try {
            // could be programmatic flushing, so must include in the try... finally

            ObjectAdapter adapter = getScalarModel().getParentObjectAdapterMemento()
                    .getObjectAdapter(AdapterManager.ConcurrencyChecking.CHECK, getPersistenceSession(),
                            getSpecificationLoader());

            final ObjectAdapter objectAdapter = getScalarModel().applyValue(adapter);
            // (borrowed some code previously in EntityModel)
            if(objectAdapter != adapter) {
                getModel().getParentObjectAdapterMemento().setAdapter(adapter);
            }

            // flush any queued changes, so concurrency or violation exceptions (if any)
            // will be thrown here
            getTransactionManager().flushTransaction();


            // disabling concurrency checking after the layout XML (grid) feature
            // was throwing an exception when rebuild grid after invoking edit prompt.
            // not certain why that would be the case, but (following similar code for action prompt)
            // think it should be safe to simply disable while recreating the page to re-render back to user.
            final EntityPage entityPage =
                    AdapterManager.ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(
                    new Callable<EntityPage>() {
                        @Override public EntityPage call() throws Exception {
                            return new EntityPage(objectAdapter, null);
                        }
                    }
            );

            setResponsePage(entityPage);

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
