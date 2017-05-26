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

package org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel;

import org.apache.wicket.Application;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.util.time.Duration;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettingsAccessor;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelUtil;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;

public abstract class ActionLink extends AjaxLink<ObjectAdapter> implements IAjaxIndicatorAware {

    private static final long serialVersionUID = 1L;

    private final AjaxIndicatorAppender indicatorAppenderIfAny;

    final AjaxDeferredBehaviour ajaxDeferredBehaviourIfAny;

    public ActionLink(String id, ActionModel model) {
        this(id, model, null);
    }
    ActionLink(String id, ActionModel model, ObjectAction action) {
        super(id, model);

        final boolean useIndicatorForNoArgAction = getSettings().isUseIndicatorForNoArgAction();
        this.indicatorAppenderIfAny =
                useIndicatorForNoArgAction
                        ? new AjaxIndicatorAppender()
                        : null;

        if(this.indicatorAppenderIfAny != null) {
            this.add(this.indicatorAppenderIfAny);
        }

        // trivial optimization; also store the objectAction if it is available (saves looking it up)
        objectAction = action;

        // this returns non-null if the action is no-arg and returns a URL or a Blob or a Clob.
        // Otherwise can use default handling
        // TODO: the method looks at the actual compile-time return type;
        // TODO: cannot see a way to check at runtime what is returned.
        // TODO: see https://issues.apache.org/jira/browse/ISIS-1264 for further detail.
        ajaxDeferredBehaviourIfAny = determineDeferredBehaviour();
        if(ajaxDeferredBehaviourIfAny != null) {
            this.add(ajaxDeferredBehaviourIfAny);
        }
    }

    @Override
    public void onClick(AjaxRequestTarget target) {

        if (ajaxDeferredBehaviourIfAny != null) {
            ajaxDeferredBehaviourIfAny.initiate(target);
            return;
        }

        doOnClick(target);
    }

    protected abstract void doOnClick(AjaxRequestTarget target);

    ActionModel getActionModel() {
        return (ActionModel) getModel();
    }

    private transient ObjectAction objectAction;

    public ObjectAction getObjectAction() {
        return objectAction != null
                ? objectAction
                : (objectAction = getActionModel().getActionMemento().getAction(getSpecificationLoader()));
    }


    @Override
    protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
        super.updateAjaxAttributes(attributes);
        if(getSettings().isPreventDoubleClickForNoArgAction()) {
            PanelUtil.disableBeforeReenableOnComplete(attributes, this);
        }

        // allow the event to bubble so the menu is hidden after click on an item
        attributes.setEventPropagation(AjaxRequestAttributes.EventPropagation.BUBBLE);
    }

    public String getReasonDisabledIfAny() {
        return getActionModel().getReasonDisabledIfAny();
    }

    @Override
    public boolean isVisible() {
        return getActionModel().isVisible();
    }

    @Override
    public boolean isEnabled() {
        try {
            final String reasonDisabledIfAny = getReasonDisabledIfAny();
            return reasonDisabledIfAny == null;
        } catch (ConcurrencyException ex) {
            //
            // this has to be here because it's the first method called by an action link listener
            // on a potentially stale model.
            //
            // there is similar code for editing properties (ScalarPanelAbstract2)
            //
            IsisContext.getSessionFactory().getCurrentSession().getAuthenticationSession().getMessageBroker().addMessage(ex.getMessage());
            throw new RestartResponseException(new EntityPage(getActionModel().getTargetAdapter()));
        }
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        Buttons.fixDisabledState(this, tag);
    }

    public String getAjaxIndicatorMarkupId() {
        return this.indicatorAppenderIfAny != null
                ? this.indicatorAppenderIfAny.getMarkupId()
                : null;
    }


    protected WicketViewerSettings getSettings() {
        return ((WicketViewerSettingsAccessor) Application.get()).getSettings();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return IsisContext.getSessionFactory().getSpecificationLoader();
    }

    AjaxDeferredBehaviour determineDeferredBehaviour() {

        final ObjectAction action = getObjectAction();
        final ActionModel actionModel = this.getActionModel();

        // TODO: should unify with ActionResultResponseType (as used in ActionParametersPanel)
        if (isNoArgReturnTypeRedirect(action)) {
            /**
             * adapted from:
             *
             * @see https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow
             */
            return new AjaxDeferredBehaviour(AjaxDeferredBehaviour.OpenUrlStrategy.NEW_WINDOW) {

                private static final long serialVersionUID = 1L;

                @Override
                protected IRequestHandler getRequestHandler() {
                    ObjectAdapter resultAdapter = actionModel.execute();
                    final Object value = resultAdapter.getObject();
                    return ActionModel.redirectHandler(value);
                }
            };
        }
        if (isNoArgReturnTypeDownload(action)) {

            /**
             * adapted from:
             *
             * @see https://cwiki.apache.org/confluence/display/WICKET/AJAX+update+and+file+download+in+one+blow
             */
            return new AjaxDeferredBehaviour(AjaxDeferredBehaviour.OpenUrlStrategy.SAME_WINDOW) {

                private static final long serialVersionUID = 1L;

                @Override
                protected IRequestHandler getRequestHandler() {
                    final ObjectAdapter resultAdapter = actionModel.execute();
                    final Object value = resultAdapter.getObject();
                    
                    final IRequestHandler handler = ActionModel.downloadHandler(value);
                    
                    //XXX ISIS-1619, prevent clients from caching the response content
                    return isNonIdempotent(actionModel) 
                    		? enforceNoCacheOnClientSide(handler)
                    		: handler                    		
                    		;
                }
            };
        }
        return null;
    }

    // TODO: should unify with ActionResultResponseType (as used in ActionParametersPanel)
    private static boolean isNoArgReturnTypeRedirect(final ObjectAction action) {
        return action.getParameterCount() == 0 &&
                action.getReturnType() != null &&
                action.getReturnType().getCorrespondingClass() == java.net.URL.class;
    }

    // TODO: should unify with ActionResultResponseType (as used in ActionParametersPanel)
    private static boolean isNoArgReturnTypeDownload(final ObjectAction action) {
        return action.getParameterCount() == 0 && action.getReturnType() != null &&
                (action.getReturnType().getCorrespondingClass() == org.apache.isis.applib.value.Blob.class ||
                        action.getReturnType().getCorrespondingClass() == org.apache.isis.applib.value.Clob.class);
    }
    
    private static boolean isNonIdempotent(ActionModel actionModel) {
		final ObjectAction objectAction = actionModel.getActionMemento()
        		.getAction(actionModel.getSpecificationLoader());
		return !SemanticsOf.from(objectAction.getSemantics()).isIdempotentInNature();
	}
    
    // -- CLIENT SIDE CACHING ASPECTS ...
	
    private static IRequestHandler enforceNoCacheOnClientSide(IRequestHandler downloadHandler){
    	if(downloadHandler==null)
    		return downloadHandler;
		
		if(downloadHandler instanceof ResourceStreamRequestHandler) 
			((ResourceStreamRequestHandler) downloadHandler)
				.setCacheDuration(Duration.seconds(0));
		
		return downloadHandler;
	}
    
    // --

}
