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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.LabeledWebMarkupContainer;
import org.apache.wicket.model.IModel;

import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Refs;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug._Probe.EntryPoint;
import org.apache.isis.core.metamodel.commons.ScalarRepresentation;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.common.model.feature.ParameterUiModel;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionPrompt;
import org.apache.isis.viewer.wicket.model.models.ActionPromptProvider;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.InlinePromptContext;
import org.apache.isis.viewer.wicket.model.models.ScalarModel;
import org.apache.isis.viewer.wicket.model.models.ScalarPropertyModel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelFactory;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditFormPanel;
import org.apache.isis.viewer.wicket.ui.components.property.PropertyEditPanel;
import org.apache.isis.viewer.wicket.ui.components.propertyheader.PropertyEditPromptHeaderPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.blobclob.IsisBlobOrClobPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.scalars.primitive.BooleanPanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.reference.ReferencePanel;
import org.apache.isis.viewer.wicket.ui.components.scalars.valuechoices.ValueChoicesSelect2Panel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.Wkt;
import org.apache.isis.viewer.wicket.ui.util.Wkt.EventTopic;

import lombok.NonNull;
import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;


public abstract class ScalarPanelAbstract
extends PanelAbstract<ManagedObject, ScalarModel>
implements ScalarModelSubscriber {

    private static final long serialVersionUID = 1L;

    protected static final String ID_SCALAR_TYPE_CONTAINER = "scalarTypeContainer";

    protected static final String ID_SCALAR_IF_COMPACT = "scalarIfCompact";
    protected static final String ID_SCALAR_IF_REGULAR = "scalarIfRegular";
    protected static final String ID_SCALAR_NAME = "scalarName";
    protected static final String ID_SCALAR_VALUE = "scalarValue";

    /**
     * as per {@link #inlinePromptLink}
     */
    protected static final String ID_SCALAR_VALUE_INLINE_PROMPT_LINK = "scalarValueInlinePromptLink";
    protected static final String ID_SCALAR_VALUE_INLINE_PROMPT_LABEL = "scalarValueInlinePromptLabel";

    /**
     * as per {@link #scalarIfRegularInlinePromptForm}.
     */
    public static final String ID_SCALAR_IF_REGULAR_INLINE_PROMPT_FORM = "scalarIfRegularInlinePromptForm";

    private static final String ID_EDIT_PROPERTY = "editProperty";
    private static final String ID_FEEDBACK = "feedback";
    private static final String ID_ASSOCIATED_ACTION_LINKS_BELOW = "associatedActionLinksBelow";
    private static final String ID_ASSOCIATED_ACTION_LINKS_RIGHT = "associatedActionLinksRight";

    public enum Repaint {
        ENTIRE_FORM,
        PARAM_ONLY,
        NOTHING
    }

    /**
     *
     * @param paramModel - the action being invoked
     * @param target - in case there's more to be repainted...
     *
     * @return - true if changed as a result of these pending arguments.
     */
    public Repaint updateIfNecessary(
            final @NonNull ParameterUiModel paramModel,
            final @NonNull Optional<AjaxRequestTarget> target) {

        // visibility
        val visibilityConsent = paramModel.getParameterNegotiationModel().getVisibilityConsent(paramModel.getParameterIndex());
        val visibilityBefore = isVisible();
        val visibilityAfter = visibilityConsent.isAllowed();
        setVisible(visibilityAfter);

        // usability
        val usabilityConsent = paramModel.getParameterNegotiationModel().getUsabilityConsent(paramModel.getParameterIndex());
        val usabilityBefore = isEnabled();
        val usabilityAfter = usabilityConsent.isAllowed();
        if(usabilityAfter) {
            onEditable(target);
        } else {
            onNotEditable(usabilityConsent.getReason(), target);
        }

        val paramValue = paramModel.getValue();
        val valueChanged = !Objects.equals(scalarModel.getObject(), paramValue);

        if(valueChanged) {
            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(paramValue)) {
                scalarModel.setObject(null);
            } else {
                scalarModel.setObject(paramValue);
            }
            scalarModel.clearPending();
        }


        // repaint the entire form if visibility has changed
        if (!visibilityBefore || !visibilityAfter) {
            return Repaint.ENTIRE_FORM;
        }

        // repaint the param if usability has changed
        if (!usabilityAfter || !usabilityBefore) {
            return Repaint.PARAM_ONLY;
        }

        // also repaint the param if its pending arg has changed.
        return valueChanged
                ? Repaint.PARAM_ONLY
                : Repaint.NOTHING;
    }

    public static class InlinePromptConfig {
        private final boolean supported;
        private final Component componentToHideIfAny;

        public static InlinePromptConfig supported() {
            return new InlinePromptConfig(true, null);
        }

        public static InlinePromptConfig notSupported() {
            return new InlinePromptConfig(false, null);
        }

        public static InlinePromptConfig supportedAndHide(final Component componentToHideIfAny) {
            return new InlinePromptConfig(true, componentToHideIfAny);
        }

        private InlinePromptConfig(final boolean supported, final Component componentToHideIfAny) {
            this.supported = supported;
            this.componentToHideIfAny = componentToHideIfAny;
        }

        boolean isSupported() {
            return supported;
        }

        Component getComponentToHideIfAny() {
            return componentToHideIfAny;
        }
    }

    // -- CONSTRUCTION

    protected final ScalarModel scalarModel;

    private Component scalarIfCompact;
    private MarkupContainer scalarIfRegular;

    private WebMarkupContainer scalarTypeContainer;

    /**
     * Populated
     * Used by most subclasses ({@link ScalarPanelAbstract}, {@link ReferencePanel}, {@link ValueChoicesSelect2Panel}) but not all ({@link IsisBlobOrClobPanelAbstract}, {@link BooleanPanel})
     */
    private WebMarkupContainer scalarIfRegularInlinePromptForm;

    WebMarkupContainer inlinePromptLink;

    protected ScalarPanelAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);
        this.scalarModel = scalarModel;
    }

    // -- INIT

    @Override
    protected void onInitialize() {
        super.onInitialize();

        buildGuiAndCallHooks();

        setOutputMarkupId(true);

    }

    private void buildGuiAndCallHooks() {

        buildGui();

        final ScalarModel scalarModel = getModel();

        final String disableReasonIfAny = scalarModel.disableReasonIfAny();
        final boolean mustBeEditable = scalarModel.mustBeEditable();
        if (disableReasonIfAny != null) {
            if(mustBeEditable) {
                onInitializeNotEditable();
            } else {
                onInitializeReadonly(disableReasonIfAny);
            }
        } else {
            if (scalarModel.isViewMode()) {
                onInitializeNotEditable();
            } else {
                onInitializeEditable();
            }
        }

    }

    /**
     * determines the CSS that is added to the outermost 'scalarTypeContainer' div.
     */
    public final String getCssClassName() {
        return _Strings.decapitalize(getClass().getSimpleName());
    }

    /**
     * Mandatory hook for implementations to indicate whether it supports the {@link PromptStyle#INLINE inline} or
     * {@link PromptStyle#INLINE_AS_IF_EDIT prompt}s, and if so, how.
     *
     * <p>
     *     For those that do, both {@link #createInlinePromptForm()} and
     *     {@link #createInlinePromptLink()} must return non-null values (and their corresponding markup
     *     must define the corresponding elements).
     * </p>
     *
     * <p>
     *     Implementations that support inline prompts are: ({@link ScalarPanelAbstract}, {@link ReferencePanel} and
     *     {@link ValueChoicesSelect2Panel}; those that don't are {@link IsisBlobOrClobPanelAbstract} and {@link BooleanPanel}.
     * </p>
     *
     */
    protected abstract InlinePromptConfig getInlinePromptConfig();

    /**
     * Builds GUI lazily prior to first render.
     *
     * <p>
     * This design allows the panel to be configured first.
     *
     * @see #onBeforeRender()
     */
    private void buildGui() {

        scalarTypeContainer = Wkt.containerAdd(this, ID_SCALAR_TYPE_CONTAINER);
        Wkt.cssAppend(scalarTypeContainer, getCssClassName());

        this.scalarIfCompact = createComponentForCompact();
        this.scalarIfRegular = createComponentForRegular();
        scalarIfRegular.setOutputMarkupId(true);

        scalarTypeContainer.addOrReplace(scalarIfCompact, scalarIfRegular);

        // find associated actions for this scalar property (only properties will have any.)
        final ScalarModel.AssociatedActions associatedActions =
                scalarModel.getAssociatedActions();
        val inlineActionIfAny =
                associatedActions.getFirstAssociatedWithInlineAsIfEdit();
        val remainingAssociated = associatedActions.getRemainingAssociated();

        // convert those actions into UI layer widgets
        final Can<LinkAndLabel> remainingLinkAndLabels = remainingAssociated.stream()
        .map(LinkAndLabelFactory.forPropertyOrParameter(this.scalarModel))
        .collect(Can.toCan());

        final InlinePromptConfig inlinePromptConfig = getInlinePromptConfig();
        if(inlinePromptConfig.isSupported()) {

            this.scalarIfRegularInlinePromptForm = createInlinePromptForm();
            scalarTypeContainer.addOrReplace(scalarIfRegularInlinePromptForm);
            inlinePromptLink = createInlinePromptLink();
            scalarIfRegular.add(inlinePromptLink);

            // even if this particular scalarModel (property) is not configured for inline edits,
            // it's possible that one of the associated actions is.  Thus we set the prompt context
            scalarModel.setInlinePromptContext(
                    new InlinePromptContext(
                            getComponentForRegular(),
                            scalarIfRegularInlinePromptForm, scalarTypeContainer));

            // start off assuming that neither the property nor any of the associated actions
            // are using inline prompts

            val componentToHideRef = _Refs.<Component>objectRef(inlinePromptLink);

            if (this.scalarModel.getPromptStyle().isInline()
                    && scalarModel.canEnterEditMode()) {
                // we configure the prompt link if _this_ property is configured for inline edits...
                Wkt.behaviorAddOnClick(inlinePromptLink, this::onPropertyInlineEditClick);
                componentToHideRef.setValue(inlinePromptConfig.getComponentToHideIfAny());

            } else {

                // not editable property, but maybe one of the actions is.
                inlineActionIfAny
                .map(LinkAndLabelFactory.forPropertyOrParameter(scalarModel))
                .map(LinkAndLabel::getUiComponent)
                .map(ActionLink.class::cast)
                .filter(ActionLink::isVisible)
                .filter(ActionLink::isEnabled)
                .ifPresent(actionLinkInlineAsIfEdit->{
                    Wkt.behaviorAddOnClick(inlinePromptLink, actionLinkInlineAsIfEdit::onClick);
                    componentToHideRef.setValue(inlinePromptConfig.getComponentToHideIfAny());
                });
            }

            componentToHideRef.getValue()
            .ifPresent(componentToHide->componentToHide.setVisibilityAllowed(false));
        }

        // prevent from tabbing into non-editable widgets.
        if(scalarModel.isProperty()
                && scalarModel.getMode() == ScalarRepresentation.VIEWING
                && (scalarModel.getPromptStyle().isDialog()
                        || !scalarModel.canEnterEditMode())) {
            getScalarValueComponent().add(new AttributeAppender("tabindex", "-1"));
        }

        addPositioningCssTo(scalarIfRegular, remainingLinkAndLabels);
        addActionLinksBelowAndRight(scalarIfRegular, remainingLinkAndLabels);

        addEditPropertyTo(scalarIfRegular);
        addFeedbackOnlyTo(scalarIfRegular, getScalarValueComponent());

        getRendering().buildGui(this);
        addCssFromMetaModel();

        notifyOnChange(this);
        addFormComponentBehaviourToUpdateSubscribers();
    }

    /**
     * The widget starts off in read-only, but should be possible to activate into edit mode.
     */
    protected void onInitializeNotEditable() {
    }

    /**
     * The widget starts off read-only, and CANNOT be activated into edit mode.
     */
    protected void onInitializeReadonly(final String disableReason) {
    }

    /**
     * The widget starts off immediately editable.
     */
    protected void onInitializeEditable() {
    }

    /**
     * The widget is no longer editable, but should be possible to activate into edit mode.
     */
    protected void onNotEditable(final String disableReason, final Optional<AjaxRequestTarget> target) {
    }

    /**
     * The widget should be made editable.
     *
     */
    protected void onEditable(final @NonNull Optional<AjaxRequestTarget> target) {
    }

    private void addCssFromMetaModel() {
        final String cssForMetaModel = getModel().getCssClass();
        Wkt.cssAppend(this, cssForMetaModel);

        ScalarModel model = getModel();
        final CssClassFacet facet = model.getFacet(CssClassFacet.class);
        if(facet != null) {
            val parentAdapter =
                    model.getParentUiModel().getManagedObject();

            final String cssClass = facet.cssClass(parentAdapter);
            Wkt.cssAppend(this, cssClass);
        }
    }


    // //////////////////////////////////////

    /**
     * Each component is now responsible for determining if it should be visible or not.
     *
     * <p>
     * Unlike the constructor and <tt>onInitialize</tt>, which are called only once, the <tt>onConfigure</tt> callback
     * is called multiple times, just prior to <tt>onBeforeRendering</tt>.  It is therefore the correct place for
     * components to set up their visibility/usability.
     * </p>
     *
     */
    @Override
    protected void onConfigure() {

        final ScalarModel scalarModel = getModel();

        final boolean hidden = scalarModel.whetherHidden();
        setVisibilityAllowed(!hidden);

        super.onConfigure();
    }


    // //////////////////////////////////////


    static class ScalarUpdatingBehavior extends AjaxFormComponentUpdatingBehavior {
        private static final long serialVersionUID = 1L;

        private final ScalarPanelAbstract scalarPanel;

        private ScalarUpdatingBehavior(final ScalarPanelAbstract scalarPanel) {
            super("change");
            this.scalarPanel = scalarPanel;
        }

        @Override
        protected void onUpdate(final AjaxRequestTarget target) {

            _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                    + "originating from User either having changed a Property value during inline editing "
                    + "or having changed a Parameter value within an open ActionPrompt.");

            for (ScalarModelSubscriber subscriber : scalarPanel.subscribers) {
                subscriber.onUpdate(target, scalarPanel);
            }
        }

        @Override
        protected void onError(final AjaxRequestTarget target, final RuntimeException e) {
            super.onError(target, e);
            for (ScalarModelSubscriber subscriber : scalarPanel.subscribers) {
                subscriber.onError(target, scalarPanel);
            }
        }
    }

    private final List<ScalarModelSubscriber> subscribers = _Lists.newArrayList();

    public void notifyOnChange(final ScalarModelSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    private void addFormComponentBehaviourToUpdateSubscribers() {
        Component scalarValueComponent = getScalarValueComponent();
        if(scalarValueComponent == null) {
            return;
        }
        for (Behavior b : scalarValueComponent.getBehaviors(ScalarUpdatingBehavior.class)) {
            scalarValueComponent.remove(b);
        }
        scalarValueComponent.add(new ScalarUpdatingBehavior(this));
    }

    // //////////////////////////////////////

    @Override
    public void onUpdate(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
        if(getModel().isParameter()) {
            Wkt.javaScriptAdd(target, EventTopic.FOCUS_FIRST_PARAMETER, getMarkupId());
        }
    }


    @Override
    public void onError(final AjaxRequestTarget target, final ScalarPanelAbstract scalarPanel) {
    }


    // ///////////////////////////////////////////////////////////////////


    public enum Rendering {
        /**
         * Does not show labels, eg for use in tables
         */
        COMPACT {
            @Override
            public String getLabelCaption(final LabeledWebMarkupContainer labeledContainer) {
                return "";
            }

            @Override
            public void buildGui(final ScalarPanelAbstract panel) {
                panel.scalarIfCompact.setVisible(true);
                panel.scalarIfRegular.setVisible(false);
                Components.permanentlyHide(panel.scalarIfRegular, ID_SCALAR_NAME);
            }

        },
        /**
         * Does show labels, eg for use in forms.
         */
        REGULAR {
            @Override
            public String getLabelCaption(final LabeledWebMarkupContainer labeledContainer) {
                return labeledContainer.getLabel().getObject();
            }

            @Override
            public void buildGui(final ScalarPanelAbstract panel) {
                panel.scalarIfRegular.setVisible(true);
                panel.scalarIfCompact.setVisible(false);
            }

        };

        public abstract String getLabelCaption(LabeledWebMarkupContainer labeledContainer);

        public abstract void buildGui(ScalarPanelAbstract panel);

        private static Rendering renderingFor(final EntityModel.RenderingHint renderingHint) {
            return renderingHint.isRegular()? Rendering.REGULAR :Rendering.COMPACT;
        }
    }

    protected Rendering getRendering() {
        return Rendering.renderingFor(scalarModel.getRenderingHint());
    }

    // ///////////////////////////////////////////////////////////////////

    protected final Component getComponentForRegular() {
        return scalarIfRegular;
    }

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#REGULAR regular} format.
     *
     * <p>
     *     Is added to {@link #scalarTypeContainer}.
     * </p>
     */
    protected abstract MarkupContainer createComponentForRegular();

    /**
     * Mandatory hook method to build the component to render the model when in
     * {@link Rendering#COMPACT compact} format.
     *
     * <p>
     *     Is added to {@link #scalarTypeContainer}.
     * </p>
     */
    protected abstract Component createComponentForCompact();

    protected Label createScalarName(final String id, final String labelCaption) {
        final Label scalarName = Wkt.label(id, labelCaption);
        final ScalarModel scalarModel = getModel();
        if(scalarModel.isRequired() && scalarModel.isEnabled()) {
            final String label = scalarName.getDefaultModelObjectAsString();
            if(!_Strings.isNullOrEmpty(label)) {
                Wkt.cssAppend(scalarName, "mandatory");
            }
        }

        scalarName.setEscapeModelStrings(true);
        return scalarName;
    }

    /**
     * Returns a container holding an empty form.  This can be switched out using {@link #switchFormForInlinePrompt(AjaxRequestTarget)}.
     */
    private WebMarkupContainer createInlinePromptForm() {

        // (placeholder initially, create dynamically when needed - otherwise infinite loop because form references regular)

        WebMarkupContainer scalarIfRegularInlinePromptForm =
                new WebMarkupContainer( ID_SCALAR_IF_REGULAR_INLINE_PROMPT_FORM);
        scalarIfRegularInlinePromptForm.setOutputMarkupId(true);
        scalarIfRegularInlinePromptForm.setVisible(false);

        return scalarIfRegularInlinePromptForm;
    }

    private WebMarkupContainer createInlinePromptLink() {
        final IModel<String> inlinePromptModel = obtainInlinePromptModel();
        if(inlinePromptModel == null) {
            throw new IllegalStateException(this.getClass().getName()
                    + ": obtainInlinePromptModel() returning null is not compatible "
                    + "with supportsInlinePrompt() returning true ");
        }

        final WebMarkupContainer inlinePromptLink =
                new WebMarkupContainer(ID_SCALAR_VALUE_INLINE_PROMPT_LINK);
        inlinePromptLink.setOutputMarkupId(true);
        inlinePromptLink.setOutputMarkupPlaceholderTag(true);

        configureInlinePromptLink(inlinePromptLink);

        final Component editInlineLinkLabel =
                createInlinePromptComponent(ID_SCALAR_VALUE_INLINE_PROMPT_LABEL, inlinePromptModel);
        inlinePromptLink.add(editInlineLinkLabel);

        return inlinePromptLink;
    }

    protected void configureInlinePromptLink(final WebMarkupContainer inlinePromptLink) {
        Wkt.cssAppend(inlinePromptLink, obtainInlinePromptLinkCssIfAny());
    }

    protected String obtainInlinePromptLinkCssIfAny() {
        return "form-control form-control-sm";
    }

    protected Component createInlinePromptComponent(
            final String id, final IModel<String> inlinePromptModel) {
        return Wkt.labelNoTab(id, inlinePromptModel);
    }

    // ///////////////////////////////////////////////////////////////////

    /**
     * Components returning true for {@link #getInlinePromptConfig()} are required to override and return a non-null value.
     */
    protected IModel<String> obtainInlinePromptModel() {
        return null;
    }

    private void onPropertyInlineEditClick(final AjaxRequestTarget target) {
        scalarModel.toEditMode();

        switchFormForInlinePrompt(target);

        getComponentForRegular().setVisible(false);
        scalarIfRegularInlinePromptForm.setVisible(true);

        target.add(scalarTypeContainer);

        Wkt.focusOnMarkerAttribute(scalarIfRegularInlinePromptForm, target);

    }

    private void switchFormForInlinePrompt(final AjaxRequestTarget target) {
        scalarIfRegularInlinePromptForm = (PropertyEditFormPanel) getComponentFactoryRegistry()
                .addOrReplaceComponent(
                    scalarTypeContainer,
                    ID_SCALAR_IF_REGULAR_INLINE_PROMPT_FORM,
                    ComponentType.PROPERTY_EDIT_FORM,
                    scalarModel);

        onSwitchFormForInlinePrompt(scalarIfRegularInlinePromptForm, target);
    }

    /**
     * Optional hook.
     */
    protected void onSwitchFormForInlinePrompt(
            final WebMarkupContainer inlinePromptForm,
            final AjaxRequestTarget target) {

    }


    // ///////////////////////////////////////////////////////////////////

    protected void addEditPropertyTo(
            final MarkupContainer scalarIfRegularFormGroup) {

        if(scalarModel.canEnterEditMode() &&
                (scalarModel.getPromptStyle().isDialog() ||
                        !getInlinePromptConfig().isSupported())) {
            val editProperty = Wkt.containerAdd(scalarIfRegularFormGroup, ID_EDIT_PROPERTY);
            Wkt.behaviorAddOnClick(editProperty, this::onPropertyEditClick);
        } else {
            Components.permanentlyHide(scalarIfRegularFormGroup, ID_EDIT_PROPERTY);
        }
    }

    private void onPropertyEditClick(final AjaxRequestTarget target) {
        final ObjectSpecification specification = scalarModel.getScalarTypeSpec();
        final MetaModelService metaModelService = getServiceRegistry()
                .lookupServiceElseFail(MetaModelService.class);
        final BeanSort sort = metaModelService.sortOf(specification.getCorrespondingClass(), MetaModelService.Mode.RELAXED);

        final ActionPrompt prompt = ActionPromptProvider
                .getFrom(ScalarPanelAbstract.this).getActionPrompt(scalarModel.getPromptStyle(), sort);

        PropertyEditPromptHeaderPanel titlePanel = new PropertyEditPromptHeaderPanel(
                prompt.getTitleId(),
                (ScalarPropertyModel)ScalarPanelAbstract.this.scalarModel);

        final PropertyEditPanel propertyEditPanel =
                (PropertyEditPanel) getComponentFactoryRegistry().createComponent(
                        ComponentType.PROPERTY_EDIT_PROMPT, prompt.getContentId(),
                        ScalarPanelAbstract.this.scalarModel);

        propertyEditPanel.setShowHeader(false);

        prompt.setTitle(titlePanel, target);
        prompt.setPanel(propertyEditPanel, target);
        prompt.showPrompt(target);
    }

    /**
     * Mandatory hook, used to determine which component to attach feedback to.
     */
    protected abstract Component getScalarValueComponent();


    private void addFeedbackOnlyTo(final MarkupContainer markupContainer, final Component component) {
        markupContainer.addOrReplace(new NotificationPanel(ID_FEEDBACK, component, new ComponentFeedbackMessageFilter(component)));
    }

    private void addActionLinksBelowAndRight(
            final MarkupContainer labelIfRegular,
            final Can<LinkAndLabel> linkAndLabels) {

        val linksBelow = linkAndLabels
                .filter(LinkAndLabel.isPositionedAt(ActionLayout.Position.BELOW));
        AdditionalLinksPanel.addAdditionalLinks(
                labelIfRegular, ID_ASSOCIATED_ACTION_LINKS_BELOW, linksBelow, AdditionalLinksPanel.Style.INLINE_LIST);

        val linksRight = linkAndLabels
                .filter(LinkAndLabel.isPositionedAt(ActionLayout.Position.RIGHT));
        AdditionalLinksPanel.addAdditionalLinks(
                labelIfRegular, ID_ASSOCIATED_ACTION_LINKS_RIGHT, linksRight, AdditionalLinksPanel.Style.DROPDOWN);
    }

    /**
     * Applies the {@literal @}{@link LabelAtFacet} and also CSS based on
     * whether any of the associated actions have {@literal @}{@link ActionLayout layout} positioned to
     * the {@link ActionLayout.Position#RIGHT right}.
     *
     * @param markupContainer The form group element
     * @param actionLinks
     */
    private void addPositioningCssTo(
            final MarkupContainer markupContainer,
            final Can<LinkAndLabel> actionLinks) {
        Wkt.cssAppend(markupContainer, determinePropParamLayoutCss(getModel()));
        Wkt.cssAppend(markupContainer, determineActionLayoutPositioningCss(actionLinks));
    }

    private static String determinePropParamLayoutCss(final ScalarModel model) {
        final LabelAtFacet facet = model.getFacet(LabelAtFacet.class);
        if (facet != null) {
            switch (facet.label()) {
            case LEFT:
                return "label-left";
            case RIGHT:
                return "label-right";
            case NONE:
                return "label-none";
            case TOP:
                return "label-top";
            case DEFAULT:
            case NOT_SPECIFIED:
            default:
                break;
            }
        }
        return "label-left";
    }

    private static String determineActionLayoutPositioningCss(final Can<LinkAndLabel> entityActionLinks) {
        return entityActionLinks.stream()
                .anyMatch(LinkAndLabel.isPositionedAt(ActionLayout.Position.RIGHT))
                    ? "actions-right"
                    : null;
    }

    // ///////////////////////////////////////////////////////////////////

    /**
     * Repaints this panel of just some of its children
     *
     * @param target The Ajax request handler
     */
    public void repaint(final AjaxRequestTarget target) {
        target.add(this);
    }


}
