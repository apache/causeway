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
package org.apache.causeway.viewer.wicket.ui.components.attributes.choices;

import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.UiAttributeWkt;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.CompactFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.attributes.AttributeFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.attributes.choices.AttributePanelWithSelect.ChoiceTitleHandler;
import org.apache.causeway.viewer.wicket.ui.components.widgets.objectsimplelink.ObjectLinkSimplePanel;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

/**
 * Panel for rendering references to domain objects (as opposed to value types).
 */
class ObjectChoicesAttributePanel
extends AttributePanelWithSelect
implements ChoiceTitleHandler {

    private static final long serialVersionUID = 1L;

    private static final String ID_AUTO_COMPLETE = "autoComplete";
    private static final String ID_OBJECT_TITLE_IF_NULL = "objectTitleIfNull";

    private ChoiceFormComponent objectLink;
    private ObjectLinkSimplePanel objectLinkOutputFormat;
    private final boolean isCompactFormat;

    public ObjectChoicesAttributePanel(final String id, final UiAttributeWkt attributeModel) {
        super(id, attributeModel);
        this.isCompactFormat = attributeModel.getRenderingHint().isInTable();
    }

    @Override
    protected Component createComponentForOutput(final String id) {

        var attributeModel = attributeModel();
        final String name = attributeModel.getFriendlyName();

        this.objectLinkOutputFormat = (ObjectLinkSimplePanel) getComponentFactoryRegistry()
                .createComponent(UiComponentType.OBJECT_LINK, attributeModel);

        objectLinkOutputFormat.setOutputMarkupId(true);
        objectLinkOutputFormat.setLabel(Model.of(name));

        return CompactFragment.OBJECT_LINK
                .createFragment(id, this, scalarValueId->objectLinkOutputFormat);
    }

    @Override
    protected FormComponent<ManagedObject> createFormComponent(final String id, final UiAttributeWkt attributeModel) {

        this.objectLink = new ChoiceFormComponent(UiComponentType.OBJECT_LINK.getId(), this);
        objectLink.setRequired(attributeModel.isRequired());

        createSelect2(ID_AUTO_COMPLETE);

        objectLink.addOrReplace(select2.component());
        objectLink.setOutputMarkupId(true);

        return objectLink;
    }

    @Override
    protected final Optional<InputFragment> getInputFragmentType() {
        return Optional.of(InputFragment.SELECT_OBJECT);
    }

    // -- ON BEFORE RENDER

    @Override
    protected void onInitializeEditable() {
        super.onInitializeEditable();
        syncWithInput();
    }

    @Override
    protected void onInitializeNotEditable() {
        super.onInitializeNotEditable();
        syncWithInput();
    }

    @Override
    protected void onInitializeReadonly(final String disableReason) {
        super.onInitializeReadonly(disableReason);
        syncWithInput();
    }

    @Override
    protected void onMakeNotEditable(final String disableReason) {
        super.onMakeNotEditable(disableReason);
        if(isCompactFormat) return;
        setTitleAttribute(disableReason);
    }

    @Override
    protected void onMakeEditable() {
        super.onMakeEditable();
        if(isCompactFormat) return;
        clearTitleAttribute();
    }

    private Optional<MarkupContainer> lookupScalarValueContainer() {
        return Optional.ofNullable(getFieldFrame())
        .flatMap(FieldFrame.SCALAR_VALUE_CONTAINER::lookupIn)
        .map(MarkupContainer.class::cast);
    }

    private void syncWithInput() {
        if(isCompactFormat) return;

        var attributeModel = attributeModel();

        lookupScalarValueContainer()
        .ifPresent(container->{
            var componentFactory = getComponentFactoryRegistry()
                    .findComponentFactory(UiComponentType.OBJECT_ICON_AND_TITLE, attributeModel);
            var iconAndTitle = componentFactory
                    .createComponent(UiComponentType.OBJECT_ICON_AND_TITLE.getId(), attributeModel);
            container.addOrReplace(iconAndTitle);

            var isInlinePrompt = attributeModel.isInlinePrompt();
            if(isInlinePrompt) {
                iconAndTitle.setVisible(false);
            }

            var adapter = attributeModel.getObject();
            if(adapter != null
                    || isInlinePrompt) {
                WktComponents.permanentlyHide(container, ID_OBJECT_TITLE_IF_NULL);
            } else {
                Wkt.markupAdd(container, ID_OBJECT_TITLE_IF_NULL,
                        getPlaceholderRenderService().asHtml(PlaceholderLiteral.NULL_REPRESENTATION));
            }
        });

        if(!isEditable()) {
            WktComponents.permanentlyHide(objectLink, ID_AUTO_COMPLETE);
            return;
        }

        if(fieldFrame != null) {
            WktComponents.permanentlyHide(fieldFrame, ID_OBJECT_TITLE_IF_NULL);
        }
        WktComponents.permanentlyHide(objectLink, ID_OBJECT_TITLE_IF_NULL);

        if(select2 == null) {
            throw new IllegalStateException("select2 should be created already");
        }

        // set mutability
        select2.setMutable(objectLink.isEnableAllowed()
                && !getModel().isViewingMode());

        /* XXX not sure if required any more
        if(hasAnyChoices()) {

            // the select2Choice already exists, so the widget has been rendered before.  If it is
            // being re-rendered now, it may be because some other property/parameter was invalid.
            // when the form was submitted, the selected object (its oid as a string) would have
            // been saved as rawInput.  If the property/parameter had been valid, then this rawInput
            // would be correctly converted and processed by the select2Choice's choiceProvider.  However,
            // an invalid property/parameter means that the webpage is re-rendered in another request,
            // and the rawInput can no longer be interpreted.  The net result is that the field appears
            // with no input.
            //
            // The fix is therefore (I think) simply to clear any rawInput, so that the select2Choice
            // renders its state from its model.
            //
            // see: FormComponent#getInputAsArray()
            // see: Select2Choice#renderInitializationScript()
            //
            select2.clearInput();

        }*/

        if(!hasAnyChoices()) {
            //TODO for editable, but no choices (eg. param as domain-object reference, with default but without choices)
            // if the param is optional (not mandatory) and has a default, we should provide 2 choices
            // - 1: empty choice (none)
            // - 2: the param's default value
        }

    }

    // -- GET INPUT AS TITLE

    String getTitleForFormComponentInput() {
        var pendingElseCurrentAdapter = attributeModel().getObject();
        return pendingElseCurrentAdapter != null
                ? pendingElseCurrentAdapter.getTitle()
                : "(no object)";
    }

    // -- CONVERT INPUT

    /**
    * Converts and validates the conversion of the raw input string into the object specified by
    * {@link FormComponent#getType()} and records any thrown {@link ConversionException}s.
    * Converted value is available through {@link FormComponent#getConvertedInput()}.
    * <p>
    * Usually the user should do custom conversions by specifying an {@link IConverter} by
    * registering it with the application by overriding {@link Application#getConverterLocator()},
    * or at the component level by overriding {@link #getConverter(Class)} .
    */
    void convertInput() {

        var attributeModel = attributeModel();
        var pendingValue = attributeModel.proposedValue().getValue();

        if(isEditable()) {

            // flush changes to pending model
            var managedObject = select2.convertedInputValue();
            pendingValue.setValue(managedObject);
        }

        objectLink.setConvertedInput(pendingValue.getValue());
    }

    // --

//    @Override
//    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
//        super.onUpdate(target, scalarPanel);
//        Wkt.javaScriptAdd(target, EventTopic.CLOSE_SELECT2, getMarkupId());
//    }

    // -- CHOICE TITLE HANDLER

    @Override
    public void clearTitleAttribute() {
        objectLink.setEnabled(true);
        Wkt.attributeReplace(objectLink, "title", "");
    }

    @Override
    public void setTitleAttribute(final String titleAttribute) {
        if(_Strings.isNullOrEmpty(titleAttribute)) {
            clearTitleAttribute();
            return;
        }
        objectLink.setEnabled(false);
        Wkt.attributeReplace(objectLink, "title", titleAttribute);
    }

}
