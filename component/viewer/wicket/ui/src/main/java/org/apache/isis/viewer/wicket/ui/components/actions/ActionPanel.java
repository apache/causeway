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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.isis.PersistenceSessionProvider;
import org.apache.isis.viewer.wicket.model.models.ActionExecutor;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkableModel;
import org.apache.isis.viewer.wicket.model.models.BookmarkedPagesModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ValueModel;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.pages.BookmarkedPagesModelProvider;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
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
public class ActionPanel extends PanelAbstract<ActionModel> implements ActionExecutor {

    private static final long serialVersionUID = 1L;

    
    /**
     * The various component types, one of which will be rendered.
     * 
     * @see #hideAllBut(ComponentType)
     */
    private static final List<ComponentType> COMPONENT_TYPES = Arrays.asList(
            ComponentType.PARAMETERS, 
            ComponentType.ENTITY_LINK, 
            ComponentType.ENTITY, 
            ComponentType.VALUE, 
            ComponentType.EMPTY_COLLECTION, 
            ComponentType.VOID_RETURN, 
            ComponentType.COLLECTION_CONTENTS);

    private static final String ID_ACTION_NAME = "actionName";

    public ActionPanel(final String id, final ActionModel actionModel) {
        super(id, actionModel);
        actionModel.setExecutor(this);
        buildGui(actionModel);
    }

    private void buildGui(final ActionModel actionModel) {
        if (actionModel.getActionMode() == ActionModel.Mode.PARAMETERS) {
            buildGuiForParameters(actionModel);
        } else {
            executeActionAndProcessResults(null);
        }
    }

    private ActionModel getActionModel() {
        return super.getModel();
    }

    private void buildGuiForParameters(ActionModel actionModel) {


        ObjectAdapter targetAdapter = null;
        try {
            targetAdapter = getActionModel().getTargetAdapter();
            
            hideAllBut(ComponentType.PARAMETERS, ComponentType.ENTITY_ICON_AND_TITLE);
            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.PARAMETERS, getActionModel());
            getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.ENTITY_ICON_AND_TITLE, new EntityModel(targetAdapter));

            final String actionName = getActionModel().getActionMemento().getAction().getName();
            addOrReplace(new Label(ID_ACTION_NAME, Model.of(actionName)));
            
        } catch (final ConcurrencyException ex) {

            // second attempt should succeed, because the Oid would have
            // been updated in the attempt
            if (targetAdapter == null) {
                targetAdapter = getModel().getTargetAdapter();
            }
            
            // forward onto the target page with the concurrency exception
            ResultType.OBJECT.addResults(this, targetAdapter, ex);

            getMessageBroker().addWarning(ex.getMessage());

        }
    }

    protected void bookmarkPage(BookmarkableModel<?> model) {
        getBookmarkedPagesModel().bookmarkPage(model);
    }

    private BookmarkedPagesModel getBookmarkedPagesModel() {
        BookmarkedPagesModelProvider application = (BookmarkedPagesModelProvider) getApplication();
        return application.getBookmarkedPagesModel();
    }

    
    /**
     * @param feedbackForm - for feedback messages.
     */
    @Override
    public void executeActionAndProcessResults(MarkupContainer feedbackForm) {

        permanentlyHide(ComponentType.ENTITY_ICON_AND_TITLE);

        ObjectAdapter targetAdapter = null;
        boolean clearArgs = true;
        try {
            try {
                targetAdapter = getModel().getTargetAdapter();

                // no concurrency exception, so continue...
                clearArgs = executeActionOnTargetAndProcessResults(targetAdapter, feedbackForm);

            } catch (ConcurrencyException ex) {

                // second attempt should succeed, because the Oid would have
                // been updated in the attempt
                if (targetAdapter == null) {
                    targetAdapter = getModel().getTargetAdapter();
                }

                
                // forward onto the target page with the concurrency exception
                ResultType.OBJECT.addResults(this, targetAdapter, ex);

                getMessageBroker().addWarning(ex.getMessage());
                return;
            }
        } finally {
            if(clearArgs) {
                getActionModel().clearArguments();
            }
            
        }
    }

    /**
     * @return whether to clear args or not (they aren't if there was a validation exception)
     */
    private boolean executeActionOnTargetAndProcessResults(ObjectAdapter targetAdapter, MarkupContainer feedbackForm) {

        // validate the action parameters (if any)
        final ActionModel actionModel = getActionModel();
        final String invalidReasonIfAny = actionModel.getReasonInvalidIfAny();
        if (invalidReasonIfAny != null) {
            feedbackForm.error(invalidReasonIfAny);
            return false;
        } 
        // the object store could raise an exception (eg uniqueness constraint)
        // so we handle it here.
        try {
            // could be programmatic flushing, so must include in the try... finally
            final ObjectAdapter resultAdapter = executeActionHandlingApplicationExceptions();
      
            // flush any queued changes, so concurrency or violation exceptions (if any)
            // will be thrown here
            getTransactionManager().flushTransaction();
            
            final ResultType resultType = ResultType.determineFor(resultAdapter);
            resultType.addResults(this, resultAdapter);

            if (actionModel.isBookmarkable()) {
                bookmarkPage(actionModel);
            }

            return true;

        } catch (RuntimeException ex) {

            String message = recognizeException(ex, feedbackForm);
            
            if (message != null) {
                if(feedbackForm == null) {
                    // forward on instead to void page
                    // (otherwise, we'll have rendered an action parameters page 
                    // and so we'll be staying on that page)
                    final ResultType resultType = ResultType.determineFor(null);
                    resultType.addResults(this, null);
                }
                
                return false;
            }
            
            // not handled, so propagate
            throw ex;
        }
    }

    private String recognizeException(RuntimeException ex, Component feedbackComponent) {
        
        // REVIEW: this code is similar to stuff in EntityPropertiesForm, perhaps move up to superclass?
        // REVIEW: similar code also in WebRequestCycleForIsis; combine?
        
        // see if the exception is recognized as being a non-serious error
        // (nb: similar code in WebRequestCycleForIsis, as a fallback)
        List<ExceptionRecognizer> exceptionRecognizers = getServicesInjector().lookupServices(ExceptionRecognizer.class);
        String recognizedErrorIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        if(recognizedErrorIfAny != null) {

            // recognized
            if(feedbackComponent != null) {
                feedbackComponent.error(recognizedErrorIfAny);
            }
            getMessageBroker().addWarning(recognizedErrorIfAny);

            getTransactionManager().getTransaction().clearAbortCause();
            
            // there's no need to abort the transaction, it will have already been done
            // (in IsisTransactionManager#executeWithinTransaction(...)).

        }
        return recognizedErrorIfAny;
    }

    /**
     * Executes the action, handling any {@link ApplicationException}s that
     * might be encountered.
     * 
     * <p>
     * If an {@link ApplicationException} is encountered, then the application error will be
     * {@link MessageBroker#setApplicationError(String) set} so that a suitable message can be 
     * rendered higher up the call stack.
     * 
     * <p>
     * Any other types of exception will be ignored (to be picked up higher up in the callstack)
     */
    private ObjectAdapter executeActionHandlingApplicationExceptions() {
        final ActionModel actionModel = getActionModel();
        try {
            ObjectAdapter resultAdapter = actionModel.getObject();
            return resultAdapter;

        } catch (RuntimeException ex) {
            
            // see if is an application-defined exception
            final ApplicationException appEx = getApplicationExceptionIfAny(ex);
            if (appEx != null) {
                getMessageBroker().setApplicationError(appEx.getMessage());

                // there's no need to abort the transaction, it will have already been done
                // (in IsisTransactionManager#executeWithinTransaction(...)). 
                return null;
            } 
            
            // the ExceptionRecognizers stuff is done in the calling method (because may be triggered
            // by a flush to the object store (which in most cases won't have happened in the execute of
            // the action body)

            // not handled, so propagate
            throw ex;
        }
    }


    private ApplicationException getApplicationExceptionIfAny(Exception ex) {
        Iterable<ApplicationException> appEx = Iterables.filter(Throwables.getCausalChain(ex), ApplicationException.class);
        Iterator<ApplicationException> iterator = appEx.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }


    enum ResultType {
        OBJECT {
            @Override
            public void addResults(final ActionPanel actionPanel, final ObjectAdapter resultAdapter) {
                final ObjectAdapter actualAdapter = determineActualAdapter(resultAdapter, actionPanel);

                //actionPanel.set
                addResultsAccordingToSingleResultsMode(actionPanel, actualAdapter, null);
            }

            @Override
            public void addResults(ActionPanel actionPanel, ObjectAdapter targetAdapter, ConcurrencyException ex) {
                addResultsAccordingToSingleResultsMode(actionPanel, targetAdapter, ex);
            }

            private ObjectAdapter determineActualAdapter(final ObjectAdapter resultAdapter, final PersistenceSessionProvider psa) {
                ObjectAdapter actualAdapter;
                if (resultAdapter.getSpecification().isNotCollection()) {
                    actualAdapter = resultAdapter;
                } else {
                    // will only be a single element
                    final List<Object> pojoList = asList(resultAdapter);
                    final Object pojo = pojoList.get(0);
                    actualAdapter = adapterFor(pojo, psa);
                }
                return actualAdapter;
            }

            private void addResultsAccordingToSingleResultsMode(final ActionPanel panel, final ObjectAdapter actualAdapter, ConcurrencyException exIfAny) {
                panel.permanentlyHide(ID_ACTION_NAME);
                final ActionModel actionModel = panel.getActionModel();
                final ActionModel.SingleResultsMode singleResultsMode = actionModel.getSingleResultsMode();

                if (singleResultsMode == ActionModel.SingleResultsMode.REDIRECT) {

                    // force any changes in state etc to happen now prior to the redirect;
                    // this should cause our page mementos (eg EntityModel) to hold the correct state.  I hope.
                    panel.getTransactionManager().flushTransaction();
                    
                    // build page, also propogate any concurrency exception that might have occurred already
                    final EntityPage entityPage = new EntityPage(actualAdapter, exIfAny);
                    
                    // "redirect-after-post"
                    
                    // panel.setRedirect(true); // no longer required, http://mail-archives.apache.org/mod_mbox/wicket-users/201103.mbox/%3CAANLkTin3NmEBaMY9CF8diXA+wTMamQPc2O+tWvG_HCiW@mail.gmail.com%3E
                    panel.setResponsePage(entityPage);
                    
                } else if (singleResultsMode == ActionModel.SingleResultsMode.INLINE) {
                    final ComponentType componentType = ComponentType.ENTITY;
                    panel.hideAllBut(componentType);
                    panel.addOrReplace(componentType, new EntityModel(actualAdapter));
                } else {
                    final ComponentType componentType = ComponentType.ENTITY_LINK;
                    panel.hideAllBut(componentType);
                    panel.addOrReplace(componentType, new EntityModel(actualAdapter));
                }
            }

            private ObjectAdapter adapterFor(final Object pojo, final PersistenceSessionProvider psa) {
                return psa.getPersistenceSession().getAdapterManager().adapterFor(pojo);
            }
        },
        COLLECTION {
            @Override
            public void addResults(final ActionPanel panel, final ObjectAdapter resultAdapter) {
                ObjectAction action = panel.getActionModel().getActionMemento().getAction();
                panel.addOrReplace(new Label(ActionPanel.ID_ACTION_NAME, Model.of(action.getName())));
                
                panel.hideAllBut(ComponentType.COLLECTION_CONTENTS);
                addOrReplaceCollectionResultsPanel(panel, resultAdapter);
            }

            private void addOrReplaceCollectionResultsPanel(final ActionPanel panel, final ObjectAdapter resultAdapter) {
                final EntityCollectionModel collectionModel = EntityCollectionModel.createStandalone(resultAdapter);
                collectionModel.setActionHint(panel.getActionModel());
                final ComponentFactoryRegistry componentFactoryRegistry = panel.getComponentFactoryRegistry();
                componentFactoryRegistry.addOrReplaceComponent(panel, ComponentType.COLLECTION_CONTENTS, collectionModel);

            }
        },
        EMPTY {
            @Override
            public void addResults(final ActionPanel panel, final ObjectAdapter resultAdapter) {
                panel.permanentlyHide(ID_ACTION_NAME);
                panel.hideAllBut(ComponentType.EMPTY_COLLECTION);
                final ActionModel actionModel = panel.getActionModel();
                panel.getComponentFactoryRegistry().addOrReplaceComponent(panel, ComponentType.EMPTY_COLLECTION, actionModel);
            }
        },
        VALUE {
            @Override
            public void addResults(final ActionPanel panel, final ObjectAdapter resultAdapter) {
                panel.permanentlyHide(ID_ACTION_NAME);
                panel.hideAllBut(ComponentType.VALUE);
                panel.getComponentFactoryRegistry().addOrReplaceComponent(panel, ComponentType.VALUE, new ValueModel(resultAdapter));
            }
        },
        VOID {
            @Override
            public void addResults(final ActionPanel panel, final ObjectAdapter resultAdapter) {
                panel.permanentlyHide(ID_ACTION_NAME);
                panel.hideAllBut(ComponentType.VOID_RETURN);
                panel.getComponentFactoryRegistry().addOrReplaceComponent(panel, ComponentType.VOID_RETURN, null);
            }
        };

        public abstract void addResults(ActionPanel panel, ObjectAdapter resultAdapter);

        /**
         * Only overridden for ResultType.OBJECT
         */
        public void addResults(ActionPanel actionPanel, ObjectAdapter targetAdapter, ConcurrencyException ex) {
            throw new UnsupportedOperationException("Cannot render concurrency exception for any result type other than OBJECT");
        }

        static ResultType determineFor(final ObjectAdapter resultAdapter) {
            if(resultAdapter == null) {
                return ResultType.VOID;
            }
            final ObjectSpecification resultSpec = resultAdapter.getSpecification();
            if (resultSpec.isNotCollection()) {
                if (resultSpec.getFacet(ValueFacet.class) != null) {
                    return ResultType.VALUE;
                } else {
                    return ResultType.OBJECT;
                }
            } else {
                final List<Object> pojoList = asList(resultAdapter);
                switch (pojoList.size()) {
                case 0:
                    return ResultType.EMPTY;
                case 1:
                    return ResultType.OBJECT;
                default:
                    return ResultType.COLLECTION;
                }
            }
        }

        @SuppressWarnings("unchecked")
        private static List<Object> asList(final ObjectAdapter resultAdapter) {
            return (List<Object>) resultAdapter.getObject();
        }
    }

    private void hideAllBut(final ComponentType... visibleComponentTypes) {
        final List<ComponentType> visibleComponentTypeList = Arrays.asList(visibleComponentTypes);
        for (final ComponentType componentType : COMPONENT_TYPES) {
            if (!visibleComponentTypeList.contains(componentType)) {
                permanentlyHide(componentType);
            }
        }
    }

    private void hideAll() {
        hideAllBut();
    }


    ///////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////
    
    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    protected ServicesInjector getServicesInjector() {
        return IsisContext.getPersistenceSession().getServicesInjector();
    }

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

}
