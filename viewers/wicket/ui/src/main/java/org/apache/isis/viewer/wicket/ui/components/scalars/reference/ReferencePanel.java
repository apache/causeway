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
package org.apache.isis.viewer.wicket.ui.components.scalars.reference;

import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.wicketstuff.select2.ChoiceProvider;
import org.wicketstuff.select2.Settings;

import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract.PlaceholderLiteral;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.util.Facets;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.CompactFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FieldFrame;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.InputFragment;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelSelectAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.entitysimplelink.EntityLinkSimplePanel;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.Select2;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForReferenceChoices;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForReferenceObjectAutoComplete;
import org.apache.isis.viewer.wicket.ui.components.widgets.select2.providers.ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.Wkt.EventTopic;
import org.apache.isis.viewer.wicket.ui.util.WktComponents;

import lombok.val;

/**
 * Panel for rendering scalars which of are of reference type (as opposed to
 * value types).
 */
public class ReferencePanel extends ScalarPanelSelectAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_AUTO_COMPLETE = "autoComplete";
    private static final String ID_ENTITY_TITLE_IF_NULL = "entityTitleIfNull";

    private EntityLinkSelect2Panel entityLink;
    private EntityLinkSimplePanel entityLinkOutputFormat;
    private final boolean isCompactFormat;

    public ReferencePanel(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        this.isCompactFormat = !scalarModel.getRenderingHint().isRegular();
    }

    @Override
    protected String obtainOutputFormat() {
        return select2.obtainOutputFormatModel().getObject();
    }

    @Override
    protected Component createComponentForOutput(final String id) {

        val scalarModel = scalarModel();
        final String name = scalarModel.getFriendlyName();

        this.entityLinkOutputFormat = (EntityLinkSimplePanel) getComponentFactoryRegistry()
                .createComponent(ComponentType.ENTITY_LINK, scalarModel);

        entityLinkOutputFormat.setOutputMarkupId(true);
        entityLinkOutputFormat.setLabel(Model.of(name));

        return CompactFragment.ENTITY_LINK
                .createFragment(id, this, scalarValueId->entityLinkOutputFormat);
    }

    @Override
    protected Optional<InputFragment> getInputFragmentType() {
        return Optional.of(InputFragment.SELECT2);
    }

    @Override
    protected FormComponent<ManagedObject> createFormComponent(final String id, final ScalarModel scalarModel) {

        this.entityLink = new EntityLinkSelect2Panel(ComponentType.ENTITY_LINK.getId(), this);
        entityLink.setRequired(scalarModel().isRequired());

        this.select2 = createSelect2(ID_AUTO_COMPLETE);
        addSelect2Semantics(select2);

        entityLink.addOrReplace(select2.asComponent());
        entityLink.setOutputMarkupId(true);

        return this.entityLink;
    }


    private void addSelect2Semantics(final Select2 select2) {
        val scalarModel = scalarModel();

        final Settings settings = select2.getSettings();

        // one of these three case should be true
        // (as per the isEditableWithEitherAutoCompleteOrChoices() guard above)
        if(scalarModel.hasChoices()) {

            settings.setPlaceholder(scalarModel.getFriendlyName());

        } else if(scalarModel.hasAutoComplete()) {

            final int minLength = scalarModel.getAutoCompleteMinLength();
            settings.setMinimumInputLength(minLength);
            settings.setPlaceholder(scalarModel.getFriendlyName());

        } else if(hasObjectAutoComplete()) {
            Facets.autoCompleteMinLength(scalarModel.getScalarTypeSpec())
            .ifPresent(settings::setMinimumInputLength);
        }
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
        entityLink.setEnabled(false);
        Wkt.attributeReplace(entityLink, "title", disableReason);
    }

    @Override
    protected void onEditable(final Optional<AjaxRequestTarget> target) {
        super.onEditable(target);
        if(isCompactFormat) return;
        entityLink.setEnabled(true);
        Wkt.attributeReplace(entityLink, "title", "");
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
                    .findComponentFactory(ComponentType.ENTITY_ICON_AND_TITLE, scalarModel);
            val iconAndTitle = componentFactory
                    .createComponent(ComponentType.ENTITY_ICON_AND_TITLE.getId(), scalarModel);
            container.addOrReplace(iconAndTitle);

            val isInlinePrompt = scalarModel.isInlinePrompt();
            if(isInlinePrompt) {
                iconAndTitle.setVisible(false);

                // bit of a hack... allows us to suppress the title using CSS
                //Wkt.cssAppend(iconAndTitle, "inlinePrompt");
            }

            val adapter = scalarModel.getObject();
            if(adapter != null
                    || isInlinePrompt) {
                WktComponents.permanentlyHide(container, ID_ENTITY_TITLE_IF_NULL);
            } else {
                Wkt.markupAdd(container, ID_ENTITY_TITLE_IF_NULL,
                        PlaceholderLiteral.NULL_REPRESENTATION.asHtml(this::translate));
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

    @Override
    protected ChoiceProvider<ObjectMemento> buildChoiceProvider() {

        val scalarModel = scalarModel();

        if (scalarModel.hasChoices()) {
            return new ObjectAdapterMementoProviderForReferenceChoices(scalarModel);
        }
        if(scalarModel.hasAutoComplete()) {
            return new ObjectAdapterMementoProviderForReferenceParamOrPropertyAutoComplete(scalarModel);
        }
        return new ObjectAdapterMementoProviderForReferenceObjectAutoComplete(scalarModel);
    }

    // -- GET INPUT AS TITLE

    String getTitleForFormComponentInput() {
        val pendingElseCurrentAdapter = scalarModel().getObject();
        return pendingElseCurrentAdapter != null
                ? pendingElseCurrentAdapter.titleString()
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

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
        super.onUpdate(target, scalarPanel);
        Wkt.javaScriptAdd(target, EventTopic.CLOSE_SELECT2, getMarkupId());
    }

    // -- HELPERS

    private boolean isEditableWithEitherAutoCompleteOrChoices() {
        if(getModel().getRenderingHint().isInTable()) {
            return false;
        }
        // doesn't apply if not editable, either
        if(getModel().isViewMode()) {
            return false;
        }
        return getModel().hasChoices() || getModel().hasAutoComplete() || hasObjectAutoComplete();
    }

    private boolean hasObjectAutoComplete() {
        return Facets.autoCompleteIsPresent(scalarModel().getScalarTypeSpec());
    }

}


