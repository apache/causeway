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
package org.apache.causeway.viewer.wicket.ui.components.scalars.choices;

import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;

import org.apache.causeway.applib.services.placeholder.PlaceholderRenderService.PlaceholderLiteral;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFrame;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelSelectAbstract;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarPanelSelectAbstract.ChoiceTitleHandler;
import org.apache.causeway.viewer.wicket.ui.components.widgets.entitysimplelink.EntityLinkSimplePanel;
import org.apache.causeway.viewer.wicket.ui.components.widgets.select2.providers.ChoiceProviderForReferences;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

import lombok.val;

/**
 * Panel for rendering scalars which of are of reference type (as opposed to
 * value types).
 */
public class ObjectChoicesSelect2Panel
extends ScalarPanelSelectAbstract
implements ChoiceTitleHandler {

    private static final long serialVersionUID = 1L;

    private static final String ID_AUTO_COMPLETE = "autoComplete";
    private static final String ID_ENTITY_TITLE_IF_NULL = "entityTitleIfNull";

    private ChoiceFormComponent entityLink;
    private EntityLinkSimplePanel entityLinkOutputFormat;
    private final boolean isCompactFormat;

    public ObjectChoicesSelect2Panel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        this.isCompactFormat = !scalarModel.getRenderingHint().isRegular();
    }

    @Override
    protected Component createComponentForOutput(final String id) {

        val scalarModel = scalarModel();
        final String name = scalarModel.getFriendlyName();

        this.entityLinkOutputFormat = (EntityLinkSimplePanel) getComponentFactoryRegistry()
                .createComponent(UiComponentType.ENTITY_LINK, scalarModel);

        entityLinkOutputFormat.setOutputMarkupId(true);
        entityLinkOutputFormat.setLabel(Model.of(name));

        return CompactFragment.ENTITY_LINK
                .createFragment(id, this, scalarValueId->entityLinkOutputFormat);
    }

    @Override
    protected FormComponent<ManagedObject> createFormComponent(final String id, final ScalarModel scalarModel) {
        this.entityLink = new ChoiceFormComponent(UiComponentType.ENTITY_LINK.getId(), this);
        entityLink.setRequired(scalarModel.isRequired());

        this.select2 = createSelect2(ID_AUTO_COMPLETE,
                ChoiceProviderForReferences::new);

        entityLink.addOrReplace(select2.asComponent());
        entityLink.setOutputMarkupId(true);

        return entityLink;
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
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
        super.onNotEditable(disableReason, target);
        if(isCompactFormat) return;
        setTitleAttribute(disableReason);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        super.onEditable(target);
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

        val scalarModel = scalarModel();

        lookupScalarValueContainer()
        .ifPresent(container->{
            val componentFactory = getComponentFactoryRegistry()
                    .findComponentFactory(UiComponentType.ENTITY_ICON_AND_TITLE, scalarModel);
            val iconAndTitle = componentFactory
                    .createComponent(UiComponentType.ENTITY_ICON_AND_TITLE.getId(), scalarModel);
            container.addOrReplace(iconAndTitle);

            val isInlinePrompt = scalarModel.isInlinePrompt();
            if(isInlinePrompt) {
                iconAndTitle.setVisible(false);
            }

            val adapter = scalarModel.getObject();
            if(adapter != null
                    || isInlinePrompt) {
                WktComponents.permanentlyHide(container, ID_ENTITY_TITLE_IF_NULL);
            } else {
                Wkt.markupAdd(container, ID_ENTITY_TITLE_IF_NULL,
                        getPlaceholderRenderService().asHtml(PlaceholderLiteral.NULL_REPRESENTATION));
            }
        });

        // syncLinkWithInputIfAutoCompleteOrChoices
        if(isEditableWithEitherAutoCompleteOrChoices()) {

            if(select2 == null) {
                throw new IllegalStateException("select2 should be created already");
            } else {
                //
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
            }

            if(fieldFrame != null) {
                WktComponents.permanentlyHide(fieldFrame, ID_ENTITY_TITLE_IF_NULL);
            }

            // syncUsability
            if(select2 != null) {
                final boolean mutability = entityLink.isEnableAllowed() && !getModel().isViewMode();
                select2.setEnabled(mutability);
            }

            WktComponents.permanentlyHide(entityLink, ID_ENTITY_TITLE_IF_NULL);
        } else {
            // this is horrid; adds a label to the id
            // should instead be a 'temporary hide'
            WktComponents.permanentlyHide(entityLink, ID_AUTO_COMPLETE);
            // setSelect2(null); // this forces recreation next time around
        }

    }

    // -- GET INPUT AS TITLE

    String getTitleForFormComponentInput() {
        val pendingElseCurrentAdapter = scalarModel().getObject();
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

        val scalarModel = scalarModel();
        val pendingValue = scalarModel.proposedValue().getValue();

        if(isEditableWithEitherAutoCompleteOrChoices()) {

            // flush changes to pending model
            val adapter = select2.getConvertedInputValue();
            pendingValue.setValue(adapter);
        }

        entityLink.setConvertedInput(pendingValue.getValue());
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
        entityLink.setEnabled(true);
        Wkt.attributeReplace(entityLink, "title", "");
    }

    @Override
    public void setTitleAttribute(final String titleAttribute) {
        if(_Strings.isNullOrEmpty(titleAttribute)) {
            clearTitleAttribute();
            return;
        }
        entityLink.setEnabled(false);
        Wkt.attributeReplace(entityLink, "title", titleAttribute);
    }

}


