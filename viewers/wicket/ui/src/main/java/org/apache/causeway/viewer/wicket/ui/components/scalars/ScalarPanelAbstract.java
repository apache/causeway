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
package org.apache.causeway.viewer.wicket.ui.components.scalars;

import java.io.Serializable;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.debug._Probe.EntryPoint;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facets.objectvalue.labelat.LabelAtFacet;
import org.apache.causeway.core.metamodel.interactions.managed.InteractionVeto;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.commons.model.decorators.FormLabelDecorator.FormLabelDecorationModel;
import org.apache.causeway.viewer.commons.model.scalar.UiParameter;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.ScalarModel;
import org.apache.causeway.viewer.wicket.ui.components.actionlinks.entityactions.ActionLinksPanel;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.FrameFragment;
import org.apache.causeway.viewer.wicket.ui.components.scalars.ScalarFragmentFactory.RegularFrame;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.Wkt.EventTopic;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktDecorators;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

public abstract class ScalarPanelAbstract
extends PanelAbstract<ManagedObject, ScalarModel>
implements ScalarModelChangeListener {

    private static final long serialVersionUID = 1L;

    protected static final String ID_SCALAR_TYPE_CONTAINER = "scalarTypeContainer";

    /** used for label-position LEFT and TOP */
    protected static final String ID_SCALAR_NAME_BEFORE_VALUE = "scalarNameBeforeValue";
    /** used for label-position RIGHT */
    protected static final String ID_SCALAR_NAME_AFTER_VALUE = "scalarNameAfterValue";
    protected static final String ID_SCALAR_VALUE = "scalarValue";
    protected static final String ID_XRAY_DETAILS = "xrayDetails";

    public enum FormatModifier {
        MARKUP,
        MULTILINE,
        TEXT_ONLY,
    }

    public enum RenderScenario {
        /**
         * As used for table cell content.
         */
        COMPACT,
        /**
         * Is viewing and cannot edit.
         * But there might be associated actions with dialog feature inline-as-if-edit.
         */
        READONLY,
        /**
         * Is viewing and can edit.
         */
        CAN_EDIT,
        CAN_EDIT_INLINE,
        CAN_EDIT_INLINE_VIA_ACTION,
        /**
         * Is editing (either prompt form or other dialog).
         */
        EDITING,
        EDITING_WITH_LINK_TO_NESTED,
        ;

        public boolean isCompact() { return this==COMPACT;}
        public boolean isReadonly() { return this==READONLY;}
        public boolean isCanEdit() { return this==CAN_EDIT;}
        public boolean isEditing() { return this==EDITING;}
        public boolean isEditingAny() {
            return this==EDITING
                    || this==CAN_EDIT_INLINE_VIA_ACTION;}
        public boolean isViewingAndCanEditAny() {
            return this==CAN_EDIT
                || this==CAN_EDIT_INLINE
                || this==CAN_EDIT_INLINE_VIA_ACTION; }

        static RenderScenario inferFrom(final ScalarPanelAbstract scalarPanel) {
            var scalarModel = scalarPanel.scalarModel();
            if(scalarModel.getRenderingHint().isInTable()) {
                return COMPACT;
            }
            if(scalarModel.isParameter()) {
                return _Util.canParameterEnterNestedEdit(scalarModel)
                        ? EDITING_WITH_LINK_TO_NESTED // nested/embedded dialog
                        : EDITING; // for params always EDITING even if editing is vetoed
            }
            // at this point we are processing a property (not a parameter)
            if(scalarModel.isEditingMode()) {
                return EDITING;
            }
            if(_Util.canPropertyEnterInlineEditDirectly(scalarModel)) {
                return CAN_EDIT_INLINE;
            }
            if(_Util.lookupPropertyActionForInlineEdit(scalarModel).isPresent()) {
                return CAN_EDIT_INLINE_VIA_ACTION;
            }
            return scalarModel.disabledReason().isPresent()
                    ? READONLY
                    : CAN_EDIT;
        }

    }

    /**
     * During AJAX requests, first the {@link ScalarModel} gets updated,
     * then later, changed components get a chance to participate in the partial page update
     * based on whether their models have changed.
     * <p>
     * This enum helps evaluate whether components using this model need repainting.
     */
    public enum Repaint {
        OPTIONAL,
        REQUIRED,
        /** if a previously hidden component becomes visible, its parent must be added to the AJAX request target */
        REQUIRED_ON_PARENT;
        public static Repaint required(final boolean needsRepainting) {
            return needsRepainting ? Repaint.REQUIRED : Repaint.OPTIONAL;
        }
        public boolean isOptional() { return this == OPTIONAL; }
        public boolean isRequired() { return this == REQUIRED; }
        public boolean isRequiredOnParent() { return this == REQUIRED_ON_PARENT; }
        public Repaint max(final @NonNull Repaint other) {
            return this.ordinal()>=other.ordinal()
                    ? this
                    : other;
        }
    }

    // -- CONSTRUCTION

    /**
     * Identical to super.getModel()
     */
    public final ScalarModel scalarModel() {
        return super.getModel();
    }

    @Getter
    private final ImmutableEnumSet<FormatModifier> formatModifiers;
    protected void setupFormatModifiers(final EnumSet<FormatModifier> modifiers) {}

    // -- COMPACT FRAME

    @Getter(AccessLevel.PROTECTED)
    private Component compactFrame;

    /**
     * Builds the component to render the model when in COMPACT form.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected abstract Component createCompactFrame();

    // -- REGULAR FRAME

    @Getter(AccessLevel.PROTECTED)
    private MarkupContainer regularFrame;

    /**
     * Builds the component to render the model when in REGULAR format.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected abstract MarkupContainer createRegularFrame();

    // -- INLINE EDIT FORM FRAME

    /**
     * Used by most subclasses
     * ({@link ScalarPanelAbstract},
     * {@link org.apache.causeway.viewer.wicket.ui.components.scalars.choices.ObjectChoicesSelect2Panel},
     * {@link org.apache.causeway.viewer.wicket.ui.components.scalars.choices.ValueChoicesSelect2Panel})
     * but not all
     * ({@link org.apache.causeway.viewer.wicket.ui.components.scalars.blobclob.CausewayBlobOrClobPanelAbstract},
     * {@link org.apache.causeway.viewer.wicket.ui.components.scalars.bool.BooleanPanel})
     */
    @Getter(AccessLevel.PROTECTED)
    private WebMarkupContainer formFrame;

    // -- FRAME CONTAINER

    private WebMarkupContainer scalarFrameContainer;
    protected final WebMarkupContainer getScalarFrameContainer() { return scalarFrameContainer; }

    // -- RENDER SCENARIO

    @Getter(AccessLevel.PROTECTED)
    private final RenderScenario renderScenario;

    // -- CONSTRUCTION

    protected ScalarPanelAbstract(final String id, final ScalarModel scalarModel) {
        super(id, scalarModel);

        var formatModifiers = EnumSet.noneOf(FormatModifier.class);
        setupFormatModifiers(formatModifiers);

        this.formatModifiers = ImmutableEnumSet.from(formatModifiers);
        this.renderScenario = RenderScenario.inferFrom(this);
    }

    // -- INIT

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
        callHooks();
        setOutputMarkupId(true);
    }

    /**
     * determines the CSS that is added to the outermost 'scalarTypeContainer' div.
     */
    public final String getCssClassName() {
        return _Strings.decapitalize(getClass().getSimpleName());
    }

    /**
     * Builds GUI lazily prior to first render.
     *
     * <p>
     * This design allows the panel to be configured first.
     *
     * @see #onBeforeRender()
     */
    private void buildGui() {

        var scalarModel = scalarModel();

        scalarFrameContainer = Wkt.containerAdd(this, ID_SCALAR_TYPE_CONTAINER);
        Wkt.cssAppend(scalarFrameContainer, getCssClassName());

        if(scalarModel.getRenderingHint().isInTable()) {
            regularFrame = createShallowRegularFrame();
            compactFrame = createCompactFrame();
            regularFrame.setVisible(false);
            compactFrame.setVisible(true);

            scalarFrameContainer.addOrReplace(compactFrame, regularFrame,
                    formFrame = createFormFrame());
        } else {
            regularFrame = Wkt.ajaxEnable(createRegularFrame());
            compactFrame = createShallowCompactFrame();
            regularFrame.setVisible(true);
            compactFrame.setVisible(false);

            scalarFrameContainer.addOrReplace(compactFrame, regularFrame,
                    formFrame = createFormFrame());

            var associatedLinksAndLabels = _Util.associatedActionModels(scalarModel);
            addPositioningCssTo(regularFrame, associatedLinksAndLabels);
            addActionLinksBelowAndRight(regularFrame, associatedLinksAndLabels);

            addFeedbackOnlyTo(regularFrame, getValidationFeedbackReceiver());

            setupInlinePrompt();
        }

        // prevent from tabbing into non-editable widgets.
        if(_Util.isPropertyWithEnterEditNotAvailable(scalarModel)) {
            Wkt.noTabbing(getValidationFeedbackReceiver());
        }

        addCssFromMetaModel();

        addChangeListener(this);
        installScalarModelChangeBehavior();
    }

    protected abstract void setupInlinePrompt();

    /**
     * Builds the hidden REGULAR component when in COMPACT format.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected MarkupContainer createShallowRegularFrame() {
        var shallowRegularFrame = FrameFragment.REGULAR
                .createComponent(Wkt::container);
        WktComponents.permanentlyHide(shallowRegularFrame,
                ID_SCALAR_NAME_BEFORE_VALUE, ID_SCALAR_VALUE, ID_SCALAR_NAME_AFTER_VALUE,
                RegularFrame.FIELD.getContainerId(),
                RegularFrame.FEEDBACK.getContainerId(),
                RegularFrame.ASSOCIATED_ACTION_LINKS_BELOW.getContainerId(),
                RegularFrame.ASSOCIATED_ACTION_LINKS_RIGHT.getContainerId());
        return shallowRegularFrame;
    }

    /**
     * Builds the hidden COMPACT component when in REGULAR format.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected Component createShallowCompactFrame() {
        return FrameFragment.COMPACT
                .createComponent(Wkt::container); // empty component;
    }

    /**
     * Builds the component to render the model when in INLINE EDITING FORM format.
     * <p>Is added to {@link #getScalarFrameContainer()}.
     */
    protected WebMarkupContainer createFormFrame() {
        var isNotInTable = scalarModel().getRenderingHint().isNotInTable();
        return (WebMarkupContainer)FrameFragment.INLINE_PROMPT_FORM
                .createComponent(WebMarkupContainer::new)
                .setVisible(false)
                .setOutputMarkupId(isNotInTable);
    }

    // -- FRAME SWITCHING

    protected final void switchRegularFrameToFormFrame() {
        getComponentFactoryRegistry()
                .addOrReplaceComponent(
                    getScalarFrameContainer(),
                    FrameFragment.INLINE_PROMPT_FORM.getContainerId(),
                    UiComponentType.PROPERTY_EDIT_FORM,
                    scalarModel());

        getRegularFrame().setVisible(false);
        getFormFrame().setVisible(true);
    }

    // -- HOOKS

    private void callHooks() {

        var scalarModel = scalarModel();

        if (scalarModel.disabledReason().isPresent()) {

            /*
             * Whether this model should be surfaced in the UI using a widget rendered such that it is either already in
             * edit mode (eg for a parameter), or can be switched into edit mode, eg for an editable property or an
             * associated action of a property with 'inline_as_if_edit'
             *
             * <tt>true</tt> if the widget for this model must be editable.
             */
            final boolean isOrCanBeSwitchedToEditable =
                    scalarModel.isEditingMode()
                        || scalarModel.isParameter()
                        || scalarModel.hasAssociatedActionWithInlineAsIfEdit();

            if(isOrCanBeSwitchedToEditable) {
                onInitializeNotEditable();
            } else {

                final String disabledReason = scalarModel.disabledReason()
                        .flatMap(InteractionVeto::getReasonAsString)
                        .orElseThrow(()->_Exceptions
                                .unrecoverable("framework bug: ScalarModel indicates it has a disabled-reason, yet its empty"));

                onInitializeReadonly(disabledReason);
            }
        } else {
            if (scalarModel.isViewingMode()) {
                onInitializeNotEditable();
            } else {
                onInitializeEditable();
            }
        }
    }

    /**
     * On rendering a new page, the widget starts off read-only, but should be possible to activate into edit mode.
     */
    protected void onInitializeNotEditable() {}

    /**
     * On rendering a new page, the widget starts off read-only, and CANNOT be activated into edit mode.
     */
    protected void onInitializeReadonly(final String disableReason) {}

    /**
     * On rendering a new page, the widget starts off immediately editable.
     */
    protected void onInitializeEditable() {}

    /**
     * Called when a partial page update (AJAX) decides for a model to transition from editable to not-editable.
     */
    protected abstract void onMakeNotEditable(final String disableReason);

    /**
     * Called when a partial page update (AJAX) decides for a model to transition from not-editable to editable.
     */
    protected abstract void onMakeEditable();

    private void addCssFromMetaModel() {
        var scalarModel = scalarModel();

        Wkt.cssAppend(this, scalarModel.getCssClass());

        Facets.cssClass(scalarModel.getMetaModel(), scalarModel.getParentUiModel().getManagedObject())
        .ifPresent(cssClass->
            Wkt.cssAppend(this, cssClass));
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
        final boolean hidden = scalarModel().whetherHidden();
        setVisibilityAllowed(!hidden);
        super.onConfigure();
    }

    // //////////////////////////////////////

    protected void installScalarModelChangeBehavior() {
        addOrReplaceBehavoir(ScalarModelDefaultChangeBehavior.class, ()->new ScalarModelDefaultChangeBehavior(this));
    }

    @RequiredArgsConstructor
    static class ScalarModelChangeDispatcherImpl
    implements ScalarModelChangeDispatcher, Serializable {
        private static final long serialVersionUID = 1L;
        private final List<ScalarModelChangeListener> changeListeners = _Lists.newArrayList();

        @Getter(onMethod_={@Override})
        private final ScalarPanelAbstract scalarPanel;

        @Override
        public void notifyUpdate(final AjaxRequestTarget target) {
            _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                    + "originating from User either having changed a Property value during inline editing "
                    + "or having changed a Parameter value within an open ActionPrompt.");
            _Xray.onParamOrPropertyEdited(scalarPanel);
            ScalarModelChangeDispatcher.super.notifyUpdate(target);
        }

        @Override
        public @NonNull Iterable<ScalarModelChangeListener> getChangeListeners() {
            return Collections.unmodifiableCollection(changeListeners);
        }

        void addChangeListener(final ScalarModelChangeListener listener) {
            changeListeners.add(listener);
        }
    }

    @Getter
    private final ScalarModelChangeDispatcher scalarModelChangeDispatcher =
            new ScalarModelChangeDispatcherImpl(this);

    public void addChangeListener(final ScalarModelChangeListener listener) {
        ((ScalarModelChangeDispatcherImpl)getScalarModelChangeDispatcher()).addChangeListener(listener);
    }

    protected final <T extends Behavior> void addOrReplaceBehavoir(
            final @NonNull Class<T> behaviorClass, final @NonNull Supplier<T> factory) {
        var validationFeedbackReceiver = getValidationFeedbackReceiver();
        if(validationFeedbackReceiver == null) { return; }
        for (var behavior : validationFeedbackReceiver.getBehaviors(behaviorClass)) {
            validationFeedbackReceiver.remove(behavior);
        }
        validationFeedbackReceiver.add(factory.get());
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

    /**
     * When label-position LEFT or TOP populates Wicket template ID_SCALAR_NAME_BEFORE_VALUE,
     * else when label-position RIGHT then populates Wicket template ID_SCALAR_NAME_AFTER_VALUE.
     * <p>
     * When label-position NONE, then no label should be rendered.
     */
    protected final void scalarNameLabelAddTo(final MarkupContainer container, final IModel<String> labelCaption) {

        var scalarModel = scalarModel();

        var helper = ScalarNameHelper.from(scalarModel);

        helper.hideHiddenLabels(container);

        helper.visibleLabelId.ifPresent(visibleLabelId->{

            final Label scalarNameLabel = Wkt.labelAdd(container, visibleLabelId, labelCaption);
            if(_Strings.isNullOrEmpty(labelCaption.getObject())) {
                return;
            }

            WktDecorators.formLabel()
                .decorate(scalarNameLabel, FormLabelDecorationModel
                        .mandatory(scalarModel.isShowMandatoryIndicator()));

            scalarModel.getDescribedAs()
                .ifPresent(describedAs->WktTooltips.addTooltip(scalarNameLabel, describedAs));
        });
    }

    @Value
    private static class ScalarNameHelper {
        static ScalarNameHelper from(final ScalarModel scalarModel) {
            final LabelPosition labelPostion = Facets.labelAt(scalarModel.getMetaModel());
            return labelPostion == LabelPosition.NONE
                    ? new ScalarNameHelper(Optional.empty(), new String[]{ID_SCALAR_NAME_BEFORE_VALUE, ID_SCALAR_NAME_AFTER_VALUE})
                    : labelPostion == LabelPosition.RIGHT
                            ? new ScalarNameHelper(Optional.of(ID_SCALAR_NAME_AFTER_VALUE), new String[]{ID_SCALAR_NAME_BEFORE_VALUE})
                            : new ScalarNameHelper(Optional.of(ID_SCALAR_NAME_BEFORE_VALUE), new String[]{ID_SCALAR_NAME_AFTER_VALUE});
        }
        final Optional<String> visibleLabelId;
        final String[] hiddenLabelIds;

        void hideHiddenLabels(final MarkupContainer container) {
            for(final String hiddenLabelId : hiddenLabelIds) {
                WktComponents.permanentlyHide(container, hiddenLabelId);
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////

    /**
     * Component to attach feedback to.
     */
    @Nullable
    protected abstract Component getValidationFeedbackReceiver();

    private void addFeedbackOnlyTo(final MarkupContainer markupContainer, final Component component) {
        if(component==null) {
            return;
        }
        markupContainer.addOrReplace(
                RegularFrame.FEEDBACK.createComponent(id->
                    new NotificationPanel(id, component, new ComponentFeedbackMessageFilter(component))));
    }

    private void addActionLinksBelowAndRight(
            final MarkupContainer labelIfRegular,
            final Can<ActionModel> actionModels) {

        var linksBelow = actionModels
                .filter(ActionModel.isPositionedAt(ActionLayout.Position.BELOW));
        ActionLinksPanel.addActionLinks(
                labelIfRegular, RegularFrame.ASSOCIATED_ACTION_LINKS_BELOW.getContainerId(),
                linksBelow, ActionLinksPanel.Style.INLINE_LIST);

        var linksRight = actionModels
                .filter(ActionModel.isPositionedAt(ActionLayout.Position.RIGHT));
        ActionLinksPanel.addActionLinks(
                labelIfRegular, RegularFrame.ASSOCIATED_ACTION_LINKS_RIGHT.getContainerId(),
                linksRight, ActionLinksPanel.Style.DROPDOWN);
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
            final Can<ActionModel> actionLinks) {
        Wkt.cssAppend(markupContainer, determinePropParamLayoutCss(getModel()));
        Wkt.cssAppend(markupContainer, determineActionLayoutPositioningCss(actionLinks));
    }

    private static String determinePropParamLayoutCss(final ScalarModel scalarModel) {
        return Facets.labelAtCss(scalarModel.getMetaModel());
    }

    private static String determineActionLayoutPositioningCss(final Can<ActionModel> entityActionLinks) {
        return entityActionLinks.stream()
                .anyMatch(ActionModel.isPositionedAt(ActionLayout.Position.RIGHT))
                    ? "actions-right"
                    : null;
    }

    // --

    /**
     * @param paramModel - the action being invoked
     *
     * @return - {@link Repaint} as a result of these pending arguments<ul>
     * <li>{@link Repaint#OPTIONAL} if nothing changed</li>
     * <li>{@link Repaint#REQUIRED} if param value changed</li>
     * </ul>
     */
   public Repaint updateIfNecessary(
           final @NonNull UiParameter paramModel) {

       var visibilityBefore = isVisibilityAllowed();
       var usabilityBefore = isCurrentlyRenderedAsUsable();

       var paramNegotiationModel = paramModel.getParameterNegotiationModel();
       final int paramIndex = paramModel.getParameterIndex();
       paramNegotiationModel.invalidateVisibilityAndUsability(paramIndex);

       /*
        * VISIBILITY, cases to consider:
        * (1) start showing     -> Repaint.REQUIRED_ON_PARENT
        * (2) keep showing      -> Repaint.OPTIONAL
        * (3) stop showing      -> Repaint.REQUIRED
        * (4) keep hiding       -> Repaint.OPTIONAL
        */
       var visibilityConsent = paramNegotiationModel.getVisibilityConsent(paramIndex);
       var visibilityAfter = visibilityConsent.isAllowed();
       setVisibilityAllowed(visibilityAfter);

       /*
        * USABILITY, cases to consider:
        * (5) start being usable    -> Repaint.REQUIRED (but only if visible)
        * (6) keep being usable     -> Repaint.OPTIONAL
        * (7) stop being usable     -> Repaint.REQUIRED (but only if visible)
        * (8) keep being readonly   -> Repaint.OPTIONAL
        */
       var usabilityConsent = paramNegotiationModel.getUsabilityConsent(paramIndex);
       var usabilityAfter = usabilityConsent.isAllowed();
       if(usabilityAfter) {
           onMakeEditable();
       } else {
           onMakeNotEditable(usabilityConsent.getReasonAsString().orElse(null));
       }

       if (visibilityBefore != visibilityAfter) {
           // repaint the param panel if visibility has changed
           return visibilityAfter
                   ? Repaint.REQUIRED_ON_PARENT
                   : Repaint.REQUIRED;
       }

       if (usabilityBefore != usabilityAfter
               && visibilityAfter) {
           // repaint the param panel if usability has changed, but only if visible
           return Repaint.REQUIRED;
       }

       return Repaint.OPTIONAL;
   }

   // -- HELPER

   /**
    * Whether the underlying UI component is currently rendered as usable.
    */
   private boolean isCurrentlyRenderedAsUsable() {
       if(!isEnabled()) return false;
       return _Casts.castTo(ScalarPanelFormFieldAbstract.class, this)
           .map(ScalarPanelFormFieldAbstract::getFormComponent)
           .map(FormComponent::isEnabled)
           .orElse(true);
   }

}
