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
package org.apache.causeway.viewer.wicket.ui.util;

import static de.agilecoders.wicket.jquery.JQuery.$;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.FormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.parser.XmlTag.TagType;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.JQueryPluginResourceReference;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.validation.IValidationError;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;
import org.danekja.java.util.function.serializable.SerializableBiConsumer;
import org.danekja.java.util.function.serializable.SerializableBooleanSupplier;
import org.danekja.java.util.function.serializable.SerializableConsumer;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.fa.FontAwesomeLayers;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.debug._Probe.EntryPoint;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.functions._Functions.SerializableFunction;
import org.apache.causeway.commons.internal.functions._Functions.SerializableSupplier;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.commons.model.components.UiString;
import org.apache.causeway.viewer.wicket.model.hints.CausewayActionCompletedEvent;
import org.apache.causeway.viewer.wicket.model.hints.CausewayEnvelopeEvent;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowToggleWkt;
import org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt;
import org.apache.causeway.viewer.wicket.ui.components.scalars.markup.MarkupComponent;
import org.apache.causeway.viewer.wicket.ui.components.text.TextAreaWithConverter;
import org.apache.causeway.viewer.wicket.ui.components.text.TextFieldWithConverter;
import org.apache.causeway.viewer.wicket.ui.components.widgets.fileinput.FileUploadFieldWithNestingFix;
import org.apache.causeway.viewer.wicket.ui.components.widgets.links.AjaxLinkNoPropagate;
import org.apache.causeway.viewer.wicket.ui.panels.PanelUtil;
import org.apache.causeway.viewer.wicket.ui.util.BootstrapConstants.ButtonSemantics;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameAppender;
import de.agilecoders.wicket.core.util.Attributes;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationBehavior;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.confirmation.ConfirmationConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxX;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxXConfig;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.checkboxx.CheckBoxXConfig.Sizes;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.form.fileinput.FileInputConfig;
import de.agilecoders.wicket.jquery.Key;

/**
 * Wicket common idioms, in alphabetical order.
 */
@UtilityClass
public class Wkt {

    public <T extends Component> T add(final @NonNull MarkupContainer container, final @Nullable T component) {
        if(component==null) return null;
        container.addOrReplace((Component)component);
        return component;
    }

    public <T extends Behavior> T add(final @NonNull Component component, final @Nullable T behavior) {
        if(behavior==null) return null;
        component.add((Behavior)behavior);
        return behavior;
    }

    /**
     * If condition is true, adds a new {@link Component} to the {@link MarkupContainer},
     * using given componentFactory. Otherwise permanently hide the corresponding tag
     * that is mapped by given id.
     * <p>
     * Regardless, if given componentFactory is null or returns null, the tag is hidden as well.
     */
    public <T extends Component> Optional<T> addIfElseHide(
            final boolean condition,
            final @NonNull MarkupContainer container,
            final @NonNull String id,
            final @Nullable Function<String, T> componentFactory) {
        final Optional<T> componentOpt = condition
                ? Optional.ofNullable(componentFactory)
                    .map(factory->factory.apply(id))
                : Optional.empty();
        componentOpt.ifPresentOrElse(
                component->add(container, component),
                ()->WktComponents.permanentlyHide(container, id));
        return componentOpt;
    }

    // -- AJAX ENABLER

    /**
     * Requirement for AJAX updates to work.
     */
    public <T extends Component> T ajaxEnable(final T component) {
        component.setOutputMarkupId(true);
        return component;
    }

    // -- ATTRIBUTES

    /**
     * If any of {@code component} or {@code attributeName} is null or empty, does nothing.
     * On empty {@code attributeValue} removes the attribute.
     */
    public <T extends Component> T attributeReplace(
            final @Nullable T component,
            final @Nullable String attributeName,
            final @Nullable String attributeValue) {
        if(component==null
                || _Strings.isEmpty(attributeName)) {
            return component;
        }
        if(_Strings.isEmpty(attributeValue)) {
            component.add(AttributeModifier.remove(attributeName));
            return component;
        }
        component.add(AttributeModifier.replace(attributeName, attributeValue));
        return component;
    }

    /**
     * If any of {@code component} or {@code attributeName} is null or empty, does nothing.
     * On missing {@code attributeValue} removes the attribute.
     */
    public <T extends Component> T attributeReplace(
            final @Nullable T component,
            final @Nullable String attributeName,
            final @Nullable Integer attributeValue) {
        return attributeReplace(component, attributeName, attributeValue!=null
                ? ""+attributeValue
                : null);
    }

    // -- BEHAVIOR

    public Behavior behaviorOnClick(final SerializableConsumer<AjaxRequestTarget> onClick) {
        return new AjaxEventBehavior("click") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onEvent(final AjaxRequestTarget target) {
                _Probe.entryPoint(EntryPoint.USER_INTERACTION, "Wicket Ajax Request, "
                        + "originating from User clicking on an "
                        + "editable Property (to start inline editing)"
                        + "or an Action (to enter param negotiaton or directly execute the Action).");

                onClick.accept(target);
            }
        };
    }

    public Behavior behaviorFireOnEscapeKey(final SerializableConsumer<AjaxRequestTarget> onRespond) {
        return new AbstractDefaultAjaxBehavior() {
            private static final long serialVersionUID = 1L;
            private static final String PRE_JS =
                    "" + "$(document).ready( function() { \n"
                            + "  $(document).bind('keyup', function(evt) { \n"
                            + "    if (evt.keyCode == 27) { \n";
            private static final String POST_JS =
                    "" + "      evt.preventDefault(); \n   "
                            + "    } \n"
                            + "  }); \n"
                            + "});";
            @Override public void renderHead(final Component component, final IHeaderResponse response) {
                super.renderHead(component, response);
                final String javascript = PRE_JS + getCallbackScript() + POST_JS;
                response.render(
                        new JavaScriptContentHeaderItem(javascript, null));
            }
            @Override protected void respond(final AjaxRequestTarget target) {
                onRespond.accept(target);
            }
        };
    }

    private static class ReplaceDisabledTagWithReadonlyTagBehavior extends Behavior {
        private static final long serialVersionUID = 1L;
        @Override public void onComponentTag(final Component component, final ComponentTag tag) {
            super.onComponentTag(component, tag);
            if(component.isEnabled()) {
                return;
            }
            tag.remove("disabled");
            tag.put("readonly","readonly");
        }
    }

    public Behavior behaviorReplaceDisabledTagWithReadonlyTag() {
        return new ReplaceDisabledTagWithReadonlyTagBehavior();
    }

    public Behavior behaviorConfirm(final ConfirmationConfig config) {
        return new ConfirmationBehavior(config);
    }

    public Behavior behaviorAddOnClick(
            final Component component,
            final SerializableConsumer<AjaxRequestTarget> onClick) {
        return add(component, behaviorOnClick(onClick));
    }

    public Behavior behaviorAddFireOnEscapeKey(
            final Component component,
            final SerializableConsumer<AjaxRequestTarget> onRespond) {
        return add(component, behaviorFireOnEscapeKey(onRespond));
    }

    public Behavior behaviorAddConfirm(
            final Component component,
            final ConfirmationConfig config) {
        return add(component, behaviorConfirm(config));
    }

    public void behaviorAddReplaceDisabledTagWithReadonlyTag(final @Nullable Component component) {
        if(component==null) {
            return;
        }
        if (component.getBehaviors(ReplaceDisabledTagWithReadonlyTagBehavior.class).isEmpty()) {
            component.add(new ReplaceDisabledTagWithReadonlyTagBehavior());
        }
    }

    // -- BOKMARKABLE PAGE LINK

    public BookmarkablePageLink<Void> bookmarkablePageLinkWithVisibility(
            final String id,
            final Class<? extends Page> pageClass,
            final PageParameters pageParameters,
            final SerializableBooleanSupplier dynamicVisibility) {

        return new BookmarkablePageLink<Void>(
                id, pageClass, pageParameters) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return dynamicVisibility.getAsBoolean();
            }

            //XXX CAUSEWAY[3022] adds support for CTRL down behavior, that is, opens URL in new tab if CTRL pressed
            @Override protected CharSequence getOnClickScript(final CharSequence url) {
                return "var win = this.ownerDocument.defaultView || this.ownerDocument.parentWindow; "
                        + "if (win == window) {"
                        + "  if(event.ctrlKey) {"
                        + "    window.open('" + url + "', '_blank').focus();"
                        + "  } else {"
                        + "    window.location.href='" + url + "';"
                        + "  }"
                        + "}"
                        + "return false";
            }

        };

    }

    // -- BUTTON

    public AjaxButton button(
            final String id,
            final IModel<String> labelModel,
            final SerializableBiConsumer<AjaxButton, AjaxRequestTarget> onClick) {
        return new AjaxButton(id, labelModel) {
            private static final long serialVersionUID = 1L;
            @Override public void onSubmit(final AjaxRequestTarget target) {
                onClick.accept(this, target);
            }
        };
    }

    public AjaxButton buttonOk(
            final String id,
            final IModel<String> labelModel,
            final Wicket settings,
            final SerializableBiConsumer<AjaxButton, AjaxRequestTarget> onClick) {

        // be aware: settings is not Serializable
        var isPreventDoubleClickForFormSubmit = settings.isPreventDoubleClickForFormSubmit();

        return settings.isUseIndicatorForFormSubmit()
        ? new IndicatingAjaxButton(id, labelModel) {
            private static final long serialVersionUID = 1L;
            @Override public void onSubmit(final AjaxRequestTarget target) {
                onClick.accept(this, target);
            }
            @Override protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
                if (isPreventDoubleClickForFormSubmit) {
                    PanelUtil.disableBeforeReenableOnComplete(attributes, this);
                }
            }
            @Override protected void onError(final AjaxRequestTarget target) {
                target.add(getForm());
            }
        }
        : new AjaxButton(id, labelModel) {
            private static final long serialVersionUID = 1L;
            @Override public void onSubmit(final AjaxRequestTarget target) {
                onClick.accept(this, target);
            }
            @Override protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
                if (isPreventDoubleClickForFormSubmit) {
                    PanelUtil.disableBeforeReenableOnComplete(attributes, this);
                }
            }
            @Override protected void onError(final AjaxRequestTarget target) {
                target.add(getForm());
            }
        };
    }

    public AjaxButton buttonAdd(
            final MarkupContainer markupContainer,
            final String id,
            final IModel<String> labelModel,
            final SerializableBiConsumer<AjaxButton, AjaxRequestTarget> onClick) {
        return add(markupContainer, button(id, labelModel, onClick));
    }

    public AjaxButton buttonAddOk(
            final MarkupContainer markupContainer,
            final String id,
            final IModel<String> labelModel,
            final Wicket settings,
            final SerializableBiConsumer<AjaxButton, AjaxRequestTarget> onClick) {
        return add(markupContainer, buttonOk(id, labelModel, settings, onClick));
    }

    // -- CHECKBOX

    /**
     * In correspondence with ScalarPanelFormFieldAbstract.html
     * <wicket:fragment wicket:id="fragment-prompt-checkboxYes">
     */
    final String fragment_prompt_checkboxYes = "<label class=\"fs-4\" style=\"color: green;\">"
            + "<i class=\"fa-regular fa-check-square\"></i></label>";
    /**
     * In correspondence with ScalarPanelFormFieldAbstract.html
     * <wicket:fragment wicket:id="fragment-prompt-checkboxNo">
     */
    final String fragment_prompt_checkboxNo = "<label class=\"fs-4\">"
            + "<i class=\"fa-regular fa-square\"></i></label>";
    /**
     * In correspondence with ScalarPanelFormFieldAbstract.html
     * <wicket:fragment wicket:id="fragment-prompt-checkboxIntermediate">
     */
    final String fragment_prompt_checkboxIntermediate = "<label class=\"fs-4\" style=\"color: silver;\">"
            + "<i class=\"fa-regular fa-square-minus\"></i></label>";

    public static CheckBoxX checkboxX(
            final String id,
            final IModel<Boolean> checkedModel,
            final boolean required,
            final Sizes size) {

         final CheckBoxXConfig config = new CheckBoxXConfig() {
            private static final long serialVersionUID = 1L;
            {
                // so can tab to the checkbox
                // not part of the API, so have to use this object initializer
                put(new Key<String>("tabindex"), "0");
            }
        }
        .withSize(size)
        .withEnclosedLabel(false)
        .withIconChecked(fragment_prompt_checkboxYes)
        .withIconNull(fragment_prompt_checkboxIntermediate)
        .withIconUnchecked(fragment_prompt_checkboxNo)
        .withThreeState(!required);

        final CheckBoxX checkBox = new CheckBoxX(id, checkedModel) {
            private static final long serialVersionUID = 1L;
            @Override public CheckBoxXConfig getConfig() {
                return config;
            }
            //override to don't express FontAwesome twice, we already do that for all pages
            @Override public void renderHead(final IHeaderResponse response) {
                response.render(CssHeaderItem.forReference(new CssResourceReference(CheckBoxX.class, "css/checkbox-x.css")));
                response.render(JavaScriptHeaderItem.forReference(new JQueryPluginResourceReference(CheckBoxX.class, "js/checkbox-x.js")));
                response.render(OnDomReadyHeaderItem.forScript($(this).chain("checkboxX", getConfig()).get()));
            }
            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                //
                // this is a horrid hack to allow the space bar to work as a way of toggling the checkbox.
                // this hack works for 1.5.4 of the JS plugin (https://github.com/kartik-v/bootstrap-checkbox-x)
                //
                // the problem is that the "change" event is not fired for a keystroke; instead the callback in the
                // JS code (https://github.com/kartik-v/bootstrap-checkbox-x/blob/v1.5.4/js/checkbox-x.js#L70)
                // calls self.change().  This in turn calls validateCheckbox().  In that method it is possible to
                // cause the "change" event to fire, but only if the input element is NOT type="checkbox".
                // (https://github.com/kartik-v/bootstrap-checkbox-x/blob/v1.5.4/js/checkbox-x.js#L132)
                //
                // It's not possible to simply change the associated markup to input type='xx' because it falls foul
                // of a check in super.onComponentTag(tag).  So instead we let that through then hack the tag
                // afterwards:
                //
                tag.put("type", "xx");
            }
        };
        return ajaxEnable(checkBox);
    }

    public static AjaxCheckBox checkbox(
            final String id,
            final IModel<Boolean> checkedModel,
            final SerializableConsumer<AjaxRequestTarget> onUpdate) {
        return new AjaxCheckBox(id, checkedModel) {
            private static final long serialVersionUID = 1L;
            @Override protected void onUpdate(final AjaxRequestTarget target) {
                onUpdate.accept(target);
            }
            /**
             * [CAUSEWAY-3005] Any action dialog submission on the same page will
             * result in a new {@link DataTableInteractive}, where any previously rendered check-boxes
             * run out of sync with their DataRowToggle model.
             * Hence we intercept such events and reset check-boxes to un-checked.
             */
            @Override public void onEvent(final IEvent<?> event) {
                if(event==null) return; // just in case
                _Casts.castTo(CausewayEnvelopeEvent.class, event.getPayload())
                .ifPresent(envelopeEvent->{
                    if(envelopeEvent.getLetter() instanceof CausewayActionCompletedEvent) {
                        var model = this.getModel();
                        if(hasMemoizedDataRow(model)
                                && Boolean.TRUE.equals(model.getObject())) {
                            this.setModelObject(false);
                            envelopeEvent.getTarget().add(this);
                       }
                    }
                });
                super.onEvent(event);
            }
            /**
             * Whether it is safe (free of side-effects) to load/access given model's object.
             * <p>
             * As of [CAUSEWAY-3658], don't call
             * {@link org.apache.causeway.viewer.wicket.model.models.interaction.coll.DataRowWkt#getObject()},
             * when the model's object is not transiently already loaded, because otherwise it would
             * enforce a page-reload as side-effect.
             */
            private boolean hasMemoizedDataRow(final IModel<Boolean> model) {
                if(model instanceof DataRowToggleWkt) {
                    var chainedModel = ((DataRowToggleWkt)model).getChainedModel();
                    if(chainedModel instanceof DataRowWkt) {
                        final DataRowWkt dataRowWkt = (DataRowWkt)chainedModel;
                        return dataRowWkt.hasMemoizedDataRow();
                    }
                }
                return false;
            }
        };
    }

    public static AjaxCheckBox checkboxAdd(
            final MarkupContainer container,
            final String id,
            final IModel<Boolean> checkedModel,
            final SerializableConsumer<AjaxRequestTarget> onUpdate) {
        return add(container, checkbox(id, checkedModel, onUpdate));
    }

    // -- CONTAINER

    public WebMarkupContainer container(final String id) {
        final WebMarkupContainer component = new WebMarkupContainer(id);
        component.setOutputMarkupId(true);
        return component;
    }

    public WebMarkupContainer containerWithVisibility(
            final String id,
            final SerializableBooleanSupplier isVisible) {
        final WebMarkupContainer component = new WebMarkupContainer(id) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isVisible() {
                return isVisible.getAsBoolean();
            }
        };
        component.setOutputMarkupId(true);
        return component;
    }

    public WebMarkupContainer containerAdd(final MarkupContainer container, final String id) {
        return add(container, container(id));
    }

    // -- CSS

    /**
     * If {@code cssClass} is empty, does nothing.
     */
    public ComponentTag cssAppend(final ComponentTag tag, final @Nullable String cssClass) {
        if(_Strings.isNotEmpty(cssClass)) {
            tag.append("class", cssClass, " ");
        }
        return tag;
    }

    /**
     * If {@code cssClass} is empty, does nothing.
     */
    public <T extends Component> T cssAppend(final T component, final @Nullable String cssClass) {
        if(_Strings.isNotEmpty(cssClass)) {
            if(ButtonSemantics.appliesTo(cssClass)) {
                component.add(ButtonSemantics.remover());
            }
            component.add(new CssClassNameAppender(cssClass));
        }
        return component;
    }

    public <T extends Component> T cssAppend(final T component, final @Nullable IModel<String> cssClassModel) {
        if(cssClassModel!=null) {
            cssAppend(component, cssClassModel.getObject());
        }
        return component;
    }

    public <T extends Component> T cssAppend(final T component, final Identifier identifier) {
        return cssAppend(component, cssNormalize(identifier));
    }

    public <T extends Component> T cssReplace(final T component, final @Nullable String cssClass) {
        component.add(AttributeModifier.replace("class", _Strings.nullToEmpty(cssClass)));
        return component;
    }

    public static String cssNormalize(final Identifier identifier) {
        var sb = new StringBuilder();
        sb.append("causeway-");
        sb.append(identifier.getLogicalType().getLogicalTypeName());
        if(_Strings.isNullOrEmpty(identifier.getMemberLogicalName())) {
            sb.append("-");
            sb.append(identifier.getMemberLogicalName());
        }
        return cssNormalize(sb.toString());
    }

    public static String cssNormalize(final String cssClass) {
        var trimmed = _Strings.blankToNullOrTrim(cssClass);
        return _Strings.isNullOrEmpty(trimmed)
                ? null
                : cssClass.replaceAll("\\.", "-").replaceAll("[^A-Za-z0-9- ]", "").replaceAll("\\s+", "-");
    }

    // -- DOWNLOAD (RESOURCE LINK)

    public ResourceLinkVolatile downloadLinkNoCache(final String id, final IResource resourceModel) {
        return new ResourceLinkVolatile(id, resourceModel);
    }

    // -- DROPDOWN CHOICE

    public <T extends Serializable> DropDownChoice<T> dropDownChoice(final String id,
            final IModel<T> model,
            final List<? extends T> choices) {
        return new DropDownChoice<T>(id, model, choices);
    }

    public <T extends Serializable> DropDownChoice<T> dropDownChoiceWithAjaxUpdate(final String id,
            final IModel<T> model,
            final List<? extends T> choices) {
        var component = dropDownChoice(id, model, choices);
        component.setOutputMarkupId(true);
        component.add(new FormComponentUpdatingBehavior());
        return component;
    }

    public <T extends Serializable> DropDownChoice<T> dropDownChoiceWithAjaxUpdateAdd(
            final MarkupContainer container,
            final String id,
            final IModel<T> model,
            final List<? extends T> choices) {
        return add(container, dropDownChoiceWithAjaxUpdate(id, model, choices));
    }

    // -- FILE DOWNLOAD

    /**
     * Schedules a file download within the context of the current {@link RequestCycle}.
     * @param model - provides a (temporary) file located at the host, which is deleted after consumption
     */
    public void fileDownloadClickHandler(final IModel<File> model, final CommonMimeType mime, final String fileName) {
        final File file = model.getObject();
        if (file == null) {
            throw _Exceptions.illegalState("Failed to retrieve a File object from model %s", model.getClass().getName());
        }
        RequestCycle.get().scheduleRequestHandlerAfterCurrent(
                Wkt.fileResourceStreamRequestHandler(file, mime)
                .setFileName(fileName)
                .setContentDisposition(ContentDisposition.ATTACHMENT));
    }

    private ResourceStreamRequestHandler fileResourceStreamRequestHandler(final File file, final CommonMimeType mime) {
        return new ResourceStreamRequestHandler(Wkt.fileResourceStream(file, mime)){
            @Override public void respond(final IRequestCycle requestCycle) {
                super.respond(requestCycle);
                Files.remove(file);
            }
        };
    }

    private FileResourceStream fileResourceStream(final File file, final CommonMimeType mime) {
        return new FileResourceStream(
                new org.apache.wicket.util.file.File(file)) {
            private static final long serialVersionUID = 1L;
            @Override public String getContentType() {
                return mime.getBaseType();
            }
        };
    }

    // -- FILE UPLOAD

    public FileUploadField fileUploadField(
            final String id,
            final String initialCaption,
            final IModel<List<FileUpload>> model) {
        var fileUploadField = new FileUploadFieldWithNestingFix(
                id,
                model,
                new FileInputConfig()
                    .maxFileCount(1)
                    .mainClass("input-group-sm")
                    .initialCaption(initialCaption)
                    .captionClass("form-control-sm")
                    .showUpload(false));
        return fileUploadField;
    }

    // -- FONT AWESOME

    public String faIcon(final String faClasses) {
        return String.format("<i class=\"%s\"></i>", faClasses);
    }

    public MarkupComponent faIconLayers(
            final String id,
            final FontAwesomeLayers fontAwesomeLayers) {
        return markup(id, fontAwesomeLayers.toHtml());
    }

    public MarkupComponent faIconLayersAdd(final MarkupContainer container, final String id,
            final FontAwesomeLayers faLayers) {
        return add(container, faIconLayers(id, faLayers));
    }

    // -- FRAGMENT

    /**
     * @param id - The component id
     * @param fragmentId - The id of the associated markup fragment
     * @param markupProvider - The component whose markup contains the fragment's markup
     */
    public Fragment fragment(final String id, final String fragmentId, final MarkupContainer markupProvider) {
        return new Fragment(id, fragmentId, markupProvider);
    }

    public Fragment fragmentDebug(final String id, final String fragmentId, final MarkupContainer markupProvider) {
        return new Fragment(id, fragmentId, markupProvider) {
            private static final long serialVersionUID = 1L;
            @Override public MarkupContainer add(final Component... children) {
                for(var child:children) {
                    System.err.printf("add %s -> %s %n", this.getId(), child.getId());
                }
                return super.add(children); }
            @Override public MarkupContainer addOrReplace(final Component... children) {
                for(var child:children) {
                    System.err.printf("addOrReplace %s -> %s %n", this.getId(), child.getId());
                }
                return super.addOrReplace(children); }
        };
    }

    /**
     * @param id - The component id
     * @param fragmentId - The id of the associated markup fragment
     * @param markupProvider - The component whose markup contains the fragment's markup
     */
    public Fragment fragmentNoTab(final String id, final String fragmentId, final MarkupContainer markupProvider) {
        return new Fragment(id, fragmentId, markupProvider) {
            private static final long serialVersionUID = 1L;
            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("tabindex", "-1");}};
    }

    /**
     * @param container - The component to add the fragment to
     * @param id - The component id
     * @param fragmentId - The id of the associated markup fragment
     * @param markupProvider - The component whose markup contains the fragment's markup
     */
    public Fragment fragmentAdd(final MarkupContainer container,
            final String id, final String fragmentId, final MarkupContainer markupProvider) {
        return add(container, fragment(id, fragmentId, markupProvider));
    }

    /**
     * @param container - The component to add the fragment to
     * @param id - The component id
     * @param fragmentId - The id of the associated markup fragment
     * @param markupProvider - The component whose markup contains the fragment's markup
     */
    public Fragment fragmentAddNoTab(final MarkupContainer container,
            final String id, final String fragmentId, final MarkupContainer markupProvider) {
        return add(container, fragmentNoTab(id, fragmentId, markupProvider));
    }

    // -- FORM

    public static Form<Object> form(final String id) {
        return new Form<Object>(id);
    }

    public static Form<Object> formAdd(final MarkupContainer container, final String id) {
        return add(container, form(id));
    }

    // -- IMAGE

    public Image imageCachable(final String id, final ResourceReference imageResource) {
        return new Image(id, imageResource) {
            private static final long serialVersionUID = 1L;
            @Override protected boolean shouldAddAntiCacheParameter() { return false; }
        };
    }

    public Image imageAddCachable(final MarkupContainer container, final String id, final ResourceReference imageResource) {
        return add(container, imageCachable(id, imageResource));
    }

    // -- LABEL

    public Label label(final String id, final String label) {
        return new Label(id, label);
    }

    public Label label(final String id, final IModel<String> labelModel) {
        return new Label(id, labelModel);
    }

    /**
     *  Whether to escape the underlying String for rendering
     *  is dynamically based on the provided {@link UiString}.
     */
    public Label labelWithDynamicEscaping(final String id, final SerializableSupplier<UiString> labelModel) {
        var label = new Label(id, ()->labelModel.get().getString()) {
            private static final long serialVersionUID = 1L;
            // we are using this method as a hook to update the ESCAPE flag before rendering
            @Override public <C> IConverter<C> getConverter(final Class<C> type) {
                setEscapeModelStrings(!labelModel.get().isMarkup());
                return super.getConverter(type);
            }
        };
        return label;
    }

    public Label labelNoTab(final String id, final IModel<String> labelModel) {
        return new Label(id, labelModel) {
            private static final long serialVersionUID = 1L;
            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("tabindex", "-1");
            }
        };
    }

    public <T> Label labelWithConverter(
            final String id, final IModel<T> model, final Class<T> type, final IConverter<T> converter) {
        return new Label(id, model) {
            private static final long serialVersionUID = 1L;
            @SuppressWarnings("unchecked")
            @Override public <C> IConverter<C> getConverter(final Class<C> cType) {
                return cType == type
                        ? (IConverter<C>) converter
                        : super.getConverter(cType);}
        };
    }

    public Label labelAdd(final MarkupContainer container, final String id, final String label) {
        return add(container, label(id, label));
    }

    public Label labelAdd(final MarkupContainer container, final String id, final IModel<String> labelModel) {
        return add(container, new Label(id, labelModel));
    }

    public Label labelAddNoTab(final MarkupContainer container, final String id, final IModel<String> labelModel) {
        return add(container, labelNoTab(id, labelModel));
    }

    public <T> Label labelAddWithConverter(
            final MarkupContainer container,
            final String id, final IModel<T> model, final Class<T> type, final IConverter<T> converter) {
        return add(container, labelWithConverter(id, model, type, converter));
    }

    // -- LINK

    public AjaxLinkNoPropagate link(final String id, final SerializableConsumer<AjaxRequestTarget> onClick) {
        return new AjaxLinkNoPropagate(id, onClick);
    }

    public AjaxLinkNoPropagate linkAdd(
            final MarkupContainer container,
            final String id,
            final SerializableConsumer<AjaxRequestTarget> onClick) {
        return add(container, link(id, onClick));
    }

    /** renders plain HTML; useful in combination with font-awesome icons */
    public AjaxLinkNoPropagate linkWithBody(final String id, final String bodyHtml,
            final SerializableConsumer<AjaxRequestTarget> onClick) {
        return new AjaxLinkNoPropagate(id, onClick) {
            private static final long serialVersionUID = 1L;
            @Override public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag){
                replaceComponentTagBody(markupStream, openTag, bodyHtml); }
            @Override protected void onComponentTag(final ComponentTag tag)   {
                super.onComponentTag(tag);
                tag.setType(TagType.OPEN); }
        };
    }

    /** renders plain HTML; useful in combination with font-awesome icons */
    public AjaxLinkNoPropagate linkAddWithBody(
            final MarkupContainer container,
            final String id, final String bodyHtml,
            final SerializableConsumer<AjaxRequestTarget> onClick) {
        return add(container, linkWithBody(id, bodyHtml, onClick));
    }

    /** renders plain HTML; useful in combination with font-awesome icons */
    public AjaxLinkNoPropagate linkAddWithBody(final RepeatingView rv, final String bodyHtml,
            final SerializableConsumer<AjaxRequestTarget> onClick) {
        return linkAddWithBody(rv, rv.newChildId(), bodyHtml, onClick);
    }

    // -- LIST VIEW

    public <T> ListView<T> listView(
            final String id,
            final List<T> list,
            final SerializableConsumer<ListItem<T>> itemPopulator) {
        return new ListView<T>(id, list) {
            private static final long serialVersionUID = 1L;
            @Override protected void populateItem(final ListItem<T> item) {
                itemPopulator.accept(item);
            }
        };
    }

    public <T> ListView<T> listView(
            final String id,
            final IModel<? extends List<T>> listModel,
            final SerializableConsumer<ListItem<T>> itemPopulator) {
        return new ListView<T>(id, listModel) {
            private static final long serialVersionUID = 1L;
            @Override protected void populateItem(final ListItem<T> item) {
                itemPopulator.accept(item);
            }
        };
    }

    public <T> ListView<T> listViewAdd(
            final MarkupContainer container,
            final String id,
            final List<T> list,
            final SerializableConsumer<ListItem<T>> itemPopulator) {
        return add(container, listView(id, list, itemPopulator));
    }

    public <T> ListView<T> listViewAdd(
            final MarkupContainer container,
            final String id,
            final IModel<? extends List<T>> listModel,
                    final SerializableConsumer<ListItem<T>> itemPopulator) {
        return add(container, listView(id, listModel, itemPopulator));
    }

    // -- MARKUP

    public MarkupComponent markup(final String id, final IModel<String> htmlModel) {
        return new MarkupComponent(id, htmlModel);
    }

    public MarkupComponent markup(final String id, final String html) {
        return markup(id, Model.of(html));
    }

    public MarkupComponent markupAdd(final MarkupContainer container, final String id, final IModel<String> htmlModel) {
        return add(container, markup(id, htmlModel));
    }

    public MarkupComponent markupAdd(final MarkupContainer container, final String id, final String html) {
        return add(container, markup(id, html));
    }

    // -- REPEATING VIEW

    public RepeatingView repeatingView(final String id) {
        return new RepeatingView(id);
    }

    public RepeatingView repeatingViewAdd(final MarkupContainer container, final String id) {
        return add(container, repeatingView(id));
    }

    public <T> RepeatingView repeatingViewAdd(
            final MarkupContainer container,
            final String id,
            final Stream<T> elementStream,
            final BiConsumer<WebMarkupContainer, T> itemPopulator) {
        var repeatingView = add(container, repeatingView(id));
        elementStream
        .forEach(t->{
            var innerContainer = Wkt.container(repeatingView.newChildId());
            repeatingView.add(innerContainer);
            itemPopulator.accept(innerContainer, t);
        });
        return repeatingView;
    }

    // -- TABLES

    public <T> Item<T> oddEvenItem(
            final String id, final int index, final IModel<T> model,
            final SerializableFunction<T, String> cssClassProvider) {

        return new OddEvenItem<T>(id, index, model) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                Wkt.cssAppend(tag, cssClassProvider.apply(model.getObject()));
            }
        };
    }

    // -- TEXT AREA

    public TextArea<String> textArea(final String id, final IModel<String> textModel) {
        return new TextArea<String>(id, textModel);
    }

    public TextArea<String> textAreaNoTab(final String id, final IModel<String> textModel) {
        return new TextArea<String>(id, textModel) {
            private static final long serialVersionUID = 1L;
            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("tabindex", "-1");
            }
        };
    }

//    public TextArea<String> textAreaAddNoTab(
//            final MarkupContainer container, final String id, final IModel<String> textModel) {
//        return add(container, textAreaNoTab(id, textModel));
//    }

    /**
     * @param converter - if {@code Optional.empty()} returns {@link TextArea} using Wicket's default converters.
     */
    public <T> TextArea<T> textAreaWithConverter(
            final @NonNull String id,
            final @NonNull IModel<T> model,
            final @NonNull Class<T> type,
            final @NonNull Optional<IConverter<T>> converter) {
        return new TextAreaWithConverter<T>(id, model, type, converter);
    }

    // -- TEXT FIELD

    /**
     * @param converter - if {@code Optional.empty()} returns {@link TextField} using Wicket's default converters.
     */
    public <T> TextField<T> textFieldWithConverter(
            final @NonNull String id,
            final @NonNull IModel<T> model,
            final @NonNull Class<T> type,
            final @NonNull Optional<IConverter<T>> converter) {
        return new TextFieldWithConverter<T>(id, model, type, converter);
    }

    public <T> TextField<T> passwordFieldWithConverter(
            final @NonNull String id,
            final @NonNull IModel<T> model,
            final @NonNull Class<T> type,
            final @NonNull Optional<IConverter<T>> converter) {
        return new TextFieldWithConverter<T>(id, model, type, converter) {
            private static final long serialVersionUID = 2L;
            @Override protected void onComponentTag(final ComponentTag tag) {
                Attributes.set(tag, "type", "password");
                super.onComponentTag(tag);
            }
            @Override protected String[] getInputTypes() {
                return new String[] {"password"};
            }
        };

    }

    // -- FOCUS UTILITY

    /**
     * If the container has any child with the marker attribute {@code data-causeway-focus},
     * then the first one found will receive focus (in the browser).
     * @implNote HTML allows for custom attributes with naming convention {@code data-}.
     */
    public void focusOnMarkerAttribute(
            final MarkupContainer container,
            final AjaxRequestTarget target) {

        container.streamChildren()
        .filter(child->child.getMarkupAttributes().containsKey("data-causeway-focus"))
        .findFirst()
        .ifPresent(child->{
            target.focusComponent(child);
        });

    }

    // -- JAVA SCRIPT UTILITY

    public enum EventTopic {
        FOCUS_FIRST_PROPERTY,
        FOCUS_FIRST_PARAMETER,
        OPEN_SELECT2,
        //CLOSE_SELECT2,
    }

    public void javaScriptAdd(final AjaxRequestTarget target, final EventTopic topic, final String containerId) {
        target.appendJavaScript(javaScriptFor(topic, containerId));
    }

    public void javaScriptAdd(final IHeaderResponse response, final EventTopic topic, final String containerId) {
        response.render(OnDomReadyHeaderItem.forScript(javaScriptFor(topic, containerId)));
    }

    private String javaScriptFor(final EventTopic topic, final String containerId) {
        return _Strings.isNotEmpty(containerId)
                ? String.format("Wicket.Event.publish(Causeway.Topic.%s, '%s')", topic.name(), containerId)
                : String.format("Wicket.Event.publish(Causeway.Topic.%s)", topic.name());
    }

    public OnDomReadyHeaderItem javaScriptAsOnDomReadyHeaderItem(final String javaScript) {
        return OnDomReadyHeaderItem.forScript(javaScript);
    }

    // -- TABBING UTILITY

    public Component noTabbing(final @Nullable Component component) {
        if(component != null) {
            component.add(new AttributeAppender("tabindex", "-1"));
        }
        return component;
    }

    // -- ERROR MESSAGE UTILITY

    /**
     * Reports a validation error against given form component.
     * Uses plain error message from ConversionException, circumventing resource bundles.
     * @return whether handled
     */
    public boolean errorMessageIgnoringResourceBundles(
            final @Nullable FormComponent<?> formComponent,
            final @Nullable IValidationError error) {
        if(formComponent==null
                || error==null) {
            return true;
        }
        if(error instanceof ValidationError) {
            var message = ((ValidationError)error).getMessage();
            // use plain error message from ConversionException, circumventing resource bundles.
            if(_Strings.isNotEmpty(message)) {
                formComponent.error(message);
                return true; // handled
            }
        }
        return false; // not-handled
    }

    // -- FORM COMPONENT ATTRIBUTE UTILITY

    public void setFormComponentAttributes(
            final FormComponent<?> formComponent,
            final Supplier<OptionalInt> multilineNumberOfLines,
            final Supplier<OptionalInt> maxLength,
            final Supplier<OptionalInt> typicalLength) {

        if(formComponent instanceof TextArea) {
            multilineNumberOfLines.get()
            .ifPresent(numberOfLines->
                Wkt.attributeReplace(formComponent, "rows", numberOfLines));
        }

        maxLength.get()
        .ifPresent(maxLen->{
            // for TextArea in conjunction with javascript in CausewayWicketViewerJsResourceReference
            // see http://stackoverflow.com/questions/4459610/set-maxlength-in-html-textarea

            Wkt.attributeReplace(formComponent, "maxlength", maxLen);
            if(formComponent.getType().equals(String.class)) {
                formComponent.add(StringValidator.maximumLength(maxLen));
            }
        });

        typicalLength.get()
        .ifPresent(typicalLen->
            Wkt.attributeReplace(formComponent, "size", typicalLen));

    }

    // -- DISABLE WORKAROUND

    /**
     * MOVED over from Wicket 8 - potentially no longer required
     * <p>
     * HACK issue #79: wicket changes tag name if component wasn't enabled
     *
     * @param component the component to fix
     * @param tag       the component tag
     * @deprecated since Wicket 7.0: doesn't mangle the link/button's markup anymore
     */
    @Deprecated
    public void fixDisabledState(final Component component, final ComponentTag tag) {
        if (!component.isEnabledInHierarchy()) {
            if (component instanceof AbstractLink) {
                tag.setName("a");
            } else if (component instanceof Button) {
                tag.setName("button");
            } else {
                if (tag.getAttribute("value") != null) {
                    tag.setName("input");
                } else {
                    tag.setName("button");
                }
            }
            tag.put("disabled", "disabled");
        }
    }

}
