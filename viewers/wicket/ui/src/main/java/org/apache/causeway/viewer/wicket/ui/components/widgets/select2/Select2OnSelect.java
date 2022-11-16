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
package org.apache.causeway.viewer.wicket.ui.components.widgets.select2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.INamedParameters.NamedPair;
import org.springframework.lang.Nullable;
import org.wicketstuff.select2.JQuery;

import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.debug._XrayEvent;
import org.apache.causeway.commons.internal.debug.xray.XrayUi;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarModelChangeDispatcher;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Listen on select2:select, select2:unselect and select2:clear events
 * so that we then can send an AJAX request including the event type and
 * the selected or un-selected object(.id),
 * while still doing parameter negotiation; that is, not submitting the form yet.
 * @see "https://select2.org/programmatic-control/events#listening-for-events"
 * @since 2.0
 */
@RequiredArgsConstructor
class Select2OnSelect extends AbstractAjaxBehavior {

    private static final long serialVersionUID = 1L;
    private final ScalarModel scalarModel;
    private final ScalarModelChangeDispatcher select2ChangeDispatcher;

    private static enum Event {
        SELECT, UNSELECT, CLEAR;
        String key() { return name().toLowerCase(); }
        static Optional<Select2OnSelect.Event> valueOf(final NamedPair pair) {
            for(var event : Event.values()) {
                if(event.name().equalsIgnoreCase(pair.getKey())) {
                    return Optional.of(event);
                }
            }
            return Optional.empty();
        }
    };

    @Override
    public void renderHead(final Component component, final IHeaderResponse response) {
        for(var event : Event.values()) {
            //response.render(OnDomReadyHeaderItem.forScript("Wicket.Log.enabled=true;"));
            response.render(OnDomReadyHeaderItem.forScript(JQuery.execute("$('#%s')"
                    + ".on('select2:%s', function (e) {"
                    + "var data = e.params.data;"
                    //debug + "console.log(e);"
                    + "Wicket.Ajax.get({'u': '%s&%s=' + data.id});"
                    + "});",
                    component.getMarkupId(),
                    event.key(),
                    getCallbackUrl(),
                    event.key()
                    )));
        }
    }

    @Override
    public void onRequest() {
        updatePendingModels();
    }

    /**
     * update the param negotiation model
     */
    private void updatePendingModels() {
        PageParameterUtils.streamCurrentRequestParameters()
        .forEach(pair->
            Event.valueOf(pair)
            .ifPresent(event->{
                if(getComponent() instanceof Select2MultiChoiceExt) {
                    val objectMementoFromEvent = ObjectMemento.destringFromUrlBase64(pair.getValue());
                    if(objectMementoFromEvent==null) {
                        return; // add or remove nothing is a no-op
                    }
                    val component = (Select2MultiChoiceExt)getComponent();
                    switch(event) {
                    case SELECT:{
                        val newSelection = copySelection(component.getModelObject());
                        newSelection.add(objectMementoFromEvent);
                        component.setModelObject(newSelection);
                        updateReceiver().setValue(demementify(newSelection));
                        break;
                    }
                    case UNSELECT:{
                        val newSelection = copySelection(component.getModelObject());
                        newSelection.remove(objectMementoFromEvent);
                        component.setModelObject(newSelection);
                        updateReceiver().setValue(demementify(newSelection));
                        break;
                    }
                    case CLEAR:
                        component.setModelObject(null);
                        clearUpdateReceiver();
                        break;
                    }
                }
                else
                if(getComponent() instanceof Select2ChoiceExt) {
                    val component = (Select2ChoiceExt)getComponent();
                    switch(event) {
                    case SELECT:
                        val objectMementoFromEvent = ObjectMemento.destringFromUrlBase64(pair.getValue());
                        if(objectMementoFromEvent==null) {
                            // select nothing is rather a CLEAR operation
                            component.clearInput();
                            clearUpdateReceiver();
                            return;
                        }
                        component.setModelObject(objectMementoFromEvent);
                        updateReceiver().setValue(demementify(objectMementoFromEvent));
                        break;
                    case UNSELECT:
                    case CLEAR:
                        component.clearInput();
                        clearUpdateReceiver();
                        break;
                    }

                } else return;

                if(XrayUi.isXrayEnabled()) {
                    val objectMementoFromEvent = ObjectMemento.destringFromUrlBase64(pair.getValue());
                    if(objectMementoFromEvent!=null) {
                        _XrayEvent.event("Select2 event: %s %s", event, objectMementoFromEvent.getBookmark());
                    } else {
                        _XrayEvent.event("Select2 event: %s %s", event, "(none)");
                    }
                }

                // schedule form update (AJAX)

                WebApplication app = (WebApplication)getComponent().getApplication();
                AjaxRequestTarget target = app.newAjaxRequestTarget(getComponent().getPage());

                // triggers update of dependent args (action prompt)
                select2ChangeDispatcher.notifyUpdate(target);

                RequestCycle requestCycle = RequestCycle.get();
                requestCycle.scheduleRequestHandlerAfterCurrent(target);

            })
        );
    }

    // -- HELPER

    private List<ObjectMemento> copySelection(final @Nullable Collection<ObjectMemento> outdatedSelection) {
        return outdatedSelection!=null
                ? new ArrayList<>(outdatedSelection)
                : new ArrayList<>(1);
    }

    private ManagedObject demementify(final ObjectMemento memento) {
        return scalarModel.getObjectManager().demementify(memento);
    }

    private PackedManagedObject demementify(
            final @NonNull List<ObjectMemento> mementos) {
        return ManagedObject.packed(
                elementSpec(),
                mementos.stream().map(this::demementify).collect(Can.toCan()));
    }

    private @NonNull Bindable<ManagedObject> updateReceiver() {
        val updateReceiver = scalarModel.getSpecialization().fold(
                param->
                    param.getParameterNegotiationModel().getBindableParamValue(param.getParameterIndex()),
                prop->
                    prop.getPendingPropertyModel().getValue());
        return updateReceiver;
    }

    private @NonNull ObjectSpecification elementSpec() {
        val updateReceiver = scalarModel.getSpecialization().fold(
                param->param.getScalarTypeSpec(),
                prop->prop.getScalarTypeSpec());
        return updateReceiver;
    }

    private void clearUpdateReceiver() {
        scalarModel.getSpecialization().accept(
            param->
                param.getParameterNegotiationModel().getParamModels()
                .getElseFail(param.getParameterIndex())
                .clear(),
            prop->
                prop.getPendingPropertyModel().clear());
    }

}