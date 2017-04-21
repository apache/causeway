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

package org.apache.isis.viewer.wicket.ui.components.scalars;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Strings;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.AbstractTextComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;

import org.apache.isis.applib.annotation.PropertyEditStyle;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.SingleIntValueFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.maxlen.MaxLengthFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.EntityActionUtil;
import org.apache.isis.viewer.wicket.ui.components.widgets.bootstrap.FormGroup;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.extensions.markup.html.bootstrap.xeditable.XEditableBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.xeditable.XEditableOptions;
import de.agilecoders.wicket.jquery.Key;
import de.agilecoders.wicket.jquery.util.Json;

/**
 * Adapter for {@link ScalarPanelAbstract scalar panel}s that are implemented
 * using a simple {@link TextField}.
 */
public abstract class ScalarPanelTextFieldAbstract<T extends Serializable> extends ScalarPanelAbstract {

    private static final long serialVersionUID = 1L;

    private static final String ID_SCALAR_TYPE_CONTAINER = "scalarTypeContainer";

    protected final Class<T> cls;

    protected static class ReplaceDisabledTagWithReadonlyTagBehaviour extends Behavior {
        @Override public void onComponentTag(final Component component, final ComponentTag tag) {
            super.onComponentTag(component, tag);
            if(component.isEnabled()) {
                return;
            }
            tag.remove("disabled");
            tag.put("readonly","readonly");
        }
    }

    protected WebMarkupContainer scalarTypeContainer;
    private AbstractTextComponent<T> textField;
    private WebMarkupContainer editInlineLink;

    public ScalarPanelTextFieldAbstract(final String id, final ScalarModel scalarModel, final Class<T> cls) {
        super(id, scalarModel);
        this.cls = cls;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        scalarTypeContainer = new WebMarkupContainer(ID_SCALAR_TYPE_CONTAINER);
        scalarTypeContainer.add(new CssClassAppender(getScalarPanelType()));
        addOrReplace(scalarTypeContainer);

    }

    private Component getEditComponent() {
        return textField.isVisibilityAllowed()
                ? textField
                : editInlineLink;
    }


    AbstractTextComponent<T> getTextField() {
        return textField;
    }

    protected AbstractTextComponent<T> createTextFieldForRegular(final String id) {
        return createTextField(id);
    }

    protected TextFieldValueModel<T> newTextFieldValueModel() {
        return new TextFieldValueModel<>(this);
    }

    protected TextField<T> createTextField(final String id) {
        return new TextField<>(id, newTextFieldValueModel(), cls);
    }

    @Override
    protected MarkupContainer addComponentForRegular() {

        // even though only one of textField and editInlineLink will ever be visible,
        // am instantiating both to avoid NPEs
        // elsewhere can use Component#isVisibilityAllowed or ScalarModel.getEditStyle() to check whichis visible.

        textField = createTextFieldForRegular(ID_SCALAR_VALUE);
        textField.setOutputMarkupId(true);
        editInlineLink = new WebMarkupContainer(ID_SCALAR_VALUE_EDIT_INLINE);
        editInlineLink.setOutputMarkupId(true);

        final IModel<T> textFieldModel = textField.getModel();

        final Label editInlineLinkLabel = new Label(ID_SCALAR_VALUE_EDIT_INLINE_LABEL, textFieldModel);
        editInlineLink.add(editInlineLinkLabel);

        if(scalarModel.getEditStyle() == PropertyEditStyle.INLINE) {
            textField.setVisibilityAllowed(false);

            final XEditableOptions options = new XEditableOptions() {
                {
                    put(new Key<String>("toggle"), "click");
                    if(scalarModel.isRequired()) {
                        put(new Key<Json.RawValue>("validate"), new Json.RawValue(
                                "function (value) { if (value == '') { return 'Required field'; } }"));
                    }
//                    put(new Key<Json.RawValue>("error"), new Json.RawValue(
//                            "function(response, newValue) {\n"
//                                    + "    if(response.status === 500) {\n"
//                                    + "        return 'Service unavailable. Please try later.';\n"
//                                    + "    } else {\n"
//                                    + "        return response.responseText;\n"
//                                    + "    }\n"
//                                    + "}"));

                }
            }.withMode("inline");

            options.withDefaultValue(asString(textFieldModel));

            XEditableBehavior xEditable = new XEditableBehavior(options) {
                @Override
                protected void onSave(final AjaxRequestTarget target, final String value) {

                    scalarModel.setObjectAsString(value);

                    ObjectAdapter adapter = scalarModel.getParentObjectAdapterMemento()
                            .getObjectAdapter(AdapterManager.ConcurrencyChecking.NO_CHECK, getPersistenceSession(),
                                    getSpecificationLoader());

                    scalarModel.applyValue(adapter);

                    target.add(editInlineLink);

                    options.withDefaultValue(asString(textFieldModel));
                }

                protected AjaxEventBehavior newSaveListener() {
                    return new AjaxEventBehavior("save") {
                        @Override
                        protected void onEvent(AjaxRequestTarget target) {

                            final ObjectAdapter currentValue = scalarModel.getObject();
                            StringValue newValue = RequestCycle.get().getRequest().getRequestParameters().getParameterValue("newValue");

                            try {
                                onSave(target, newValue.toString());
                            } catch (Exception ex) {
                                scalarModel.setObject(currentValue);

                                final String value = asString(textFieldModel);
                                options.withDefaultValue(value);

                                //
                                // hmmm... thought had this working, but turns out that can't rely on the .control-group to exist
                                //
                                // also: even if get going, still need to reset the value of the inline edit, and also
                                // to handle updates to the title (eg Wicket event bus, I think)
                                //
                                final String message = ex.getMessage();
                                target.appendJavaScript(
                                        "( function() { "
                                                + "var component = $(\"#" + editInlineLink.getMarkupId() + "\"); "
                                                + "var parent = $(component).parent(); "

                                                + "var controlGroup = $(parent).find(\".control-group\"); "
                                                + "var errorBlock = $(parent).find(\".editable-error-block\"); "

                                                + "$(component).editable(\"show\"); "
                                                + "$(controlGroup).addClass(\"has-error\"); "
                                                + "$(errorBlock).css(\"display\", \"block\"); "
                                                + "$(errorBlock).text(\"" + message + "\"); "

                                        + "} )();");
                            }

                        }

                        @Override
                        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                            super.updateAjaxAttributes(attributes);
                            attributes.getDynamicExtraParameters().add("return [{'name':'newValue', 'value': attrs.event.extraData.newValue}]");

                            AjaxCallListener myAjaxCallListener = new AjaxCallListener() {

                                @Override
                                public CharSequence getBeforeHandler(Component component) {
                                    return ""; // "var myEl = $(\"#" + component.getMarkupId() + "\"); console.log(myEl);";
                                }

                                @Override public CharSequence getFailureHandler(final Component component) {
                                    return "alert(\"failure!!!\")";
                                }
                            };
                            attributes.getAjaxCallListeners().add(myAjaxCallListener);

                        }
                    };
                }

            };


            editInlineLink.add(xEditable);
        } else {
            editInlineLink.setVisibilityAllowed(false);
        }

        addStandardSemantics();

        final MarkupContainer labelIfRegular = createFormComponentLabel();
        scalarTypeContainer.add(labelIfRegular);

        final Label scalarName = new Label(ID_SCALAR_NAME, getRendering().getLabelCaption(textField));
        NamedFacet namedFacet = getModel().getFacet(NamedFacet.class);
        if (namedFacet != null) {
            scalarName.setEscapeModelStrings(namedFacet.escaped());
        }

        // find the links...

        final List<LinkAndLabel> entityActions = EntityActionUtil.getEntityActionLinksForAssociation(this.scalarModel, getDeploymentCategory());

        addPositioningCssTo(labelIfRegular, entityActions);

        if(getModel().isRequired()) {
            final String label = scalarName.getDefaultModelObjectAsString();
            if(!Strings.isNullOrEmpty(label)) {
                scalarName.add(new CssClassAppender("mandatory"));
            }
        }

        labelIfRegular.add(scalarName);

        final String describedAs = getModel().getDescribedAs();
        if(describedAs != null) {
            labelIfRegular.add(new AttributeModifier("title", Model.of(describedAs)));
        }

        addFeedbackOnlyTo(labelIfRegular, getEditComponent());

        addEditPropertyTo(labelIfRegular);

        // ... add entity links to panel (below and to right)
        addEntityActionLinksBelowAndRight(labelIfRegular, entityActions);

        return labelIfRegular;
    }

    private String asString(final IModel<T> textFieldModel) {
        return textFieldModel != null && textFieldModel.getObject() != null ? textFieldModel.getObject().toString() : null;
    }

    protected abstract IModel<String> getScalarPanelType();

    private void addReplaceDisabledTagWithReadonlyTagBehaviourIfRequired(final Component component) {
        if(!getSettings().isReplaceDisabledTagWithReadonlyTag()) {
            return;
        }
        if (component == null) {
            return;
        }
        if (!component.getBehaviors(ReplaceDisabledTagWithReadonlyTagBehaviour.class).isEmpty()) {
            return;
        }
        component.add(new ReplaceDisabledTagWithReadonlyTagBehaviour());
    }

    private MarkupContainer createFormComponentLabel() {
        Fragment textFieldFragment = createTextFieldFragment("scalarValueContainer");
        final String name = getModel().getName();
        textField.setLabel(Model.of(name));
        
        final FormGroup scalarNameAndValue = new FormGroup(ID_SCALAR_IF_REGULAR, this.textField);

        textFieldFragment.add(this.textField);
        scalarNameAndValue.add(textFieldFragment);

        textFieldFragment.add(this.editInlineLink);

        return scalarNameAndValue;
    }

    protected Fragment createTextFieldFragment(String id) {
        return new Fragment(id, "text", ScalarPanelTextFieldAbstract.this);
    }

    protected void addStandardSemantics() {
        textField.setRequired(getModel().isRequired());
        setTextFieldSizeAndMaxLengthIfSpecified();

        addValidatorForIsisValidation();
    }


    private void addValidatorForIsisValidation() {
        final ScalarModel scalarModel = getModel();

        textField.add(new IValidator<T>() {
            private static final long serialVersionUID = 1L;

            @Override
            public void validate(final IValidatable<T> validatable) {
                final T proposedValue = validatable.getValue();
                final ObjectAdapter proposedAdapter = getPersistenceSession().adapterFor(proposedValue);
                final String reasonIfAny = scalarModel.validate(proposedAdapter);
                if (reasonIfAny != null) {
                    final ValidationError error = new ValidationError();
                    error.setMessage(reasonIfAny);
                    validatable.error(error);
                }
            }
        });
    }

    private void setTextFieldSizeAndMaxLengthIfSpecified() {

        final Integer maxLength = getValueOf(getModel(), MaxLengthFacet.class);
        Integer typicalLength = getValueOf(getModel(), TypicalLengthFacet.class);

        // doesn't make sense for typical length to be > maxLength
        if(typicalLength != null && maxLength != null && typicalLength > maxLength) {
            typicalLength = maxLength;
        }

        if (typicalLength != null) {
            textField.add(new AttributeModifier("size", Model.of("" + typicalLength)));
        }

        if(maxLength != null) {
            textField.add(new AttributeModifier("maxlength", Model.of("" + maxLength)));
        }
    }


    private static Integer getValueOf(ScalarModel model, Class<? extends SingleIntValueFacet> facetType) {
        final SingleIntValueFacet facet = model.getFacet(facetType);
        return facet != null ? facet.value() : null;
    }
    
    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#COMPACT compact} format.
     * 
     * <p>
     * This default implementation uses a {@link Label}, however it may be overridden if required.
     */
    @Override
    protected Component addComponentForCompact() {
        Fragment compactFragment = getCompactFragment(CompactType.SPAN);
        final Label labelIfCompact = new Label(ID_SCALAR_IF_COMPACT, getModel().getObjectAsString());
        compactFragment.add(labelIfCompact);
        scalarTypeContainer.addOrReplace(compactFragment);
        return labelIfCompact;
    }


    @Override
    protected void onBeforeRenderWhenViewMode() {
        super.onBeforeRenderWhenViewMode();

        textField.setEnabled(false);
        addReplaceDisabledTagWithReadonlyTagBehaviourIfRequired(textField);

        setTitleAttribute("");
    }

    @Override
    protected void onBeforeRenderWhenDisabled(final String disableReason) {
        super.onBeforeRenderWhenDisabled(disableReason);
        textField.setEnabled(false);
        editInlineLink.setEnabled(false);
        addReplaceDisabledTagWithReadonlyTagBehaviourIfRequired(textField);
        setTitleAttribute(disableReason);
    }

    @Override
    protected void onBeforeRenderWhenEnabled() {
        super.onBeforeRenderWhenEnabled();
        textField.setEnabled(true);
        editInlineLink.setEnabled(true);
        setTitleAttribute("");
    }

    private void setTitleAttribute(final String titleAttribute) {
        AttributeModifier title = new AttributeModifier("title", Model.of(titleAttribute));
        textField.add(title);
        editInlineLink.add(title);
    }

    @Override
    protected void addFormComponentBehavior(Behavior behavior) {

        // some behaviours can only be attached to one component
        // so we check as to which will actually be visible.
        getEditComponent().add(behavior);
    }


    //region > dependencies

    @com.google.inject.Inject
    private WicketViewerSettings settings;
    protected WicketViewerSettings getSettings() {
        return settings;
    }

    //endregion

}

