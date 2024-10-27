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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScalarFragmentFactory {

    @RequiredArgsConstructor
    public static enum FrameFragment {
        COMPACT("scalarIfCompact"),
        REGULAR("scalarIfRegular"),
        INLINE_PROMPT_FORM("scalarIfRegularInlinePromptForm"),
        ;
        @Getter private final String containerId;
        public <T extends Component> T createComponent(final Function<String, T> factory) {
            return factory.apply(containerId);
        }
    }

    @RequiredArgsConstructor
    public static enum RegularFrame {

        FIELD("container-fieldFrame"),

        FEEDBACK("feedback"),
        ASSOCIATED_ACTION_LINKS_BELOW("associatedActionLinksBelow"),
        ASSOCIATED_ACTION_LINKS_RIGHT("associatedActionLinksRight"),
        ;
        @Getter private final String containerId;
        public <T extends Component> T createComponent(final Function<String, T> factory) {
            return factory.apply(containerId);
        }
        /* not used
        public void permanentlyHideIn(final MarkupContainer container) {
            var toHide = container.get(containerId);
            if (toHide != null) {
                toHide.setVisibilityAllowed(false);
                toHide.setVisible(false);
            } else {
                WktComponents.permanentlyHide(container, containerId);
            }
        }*/
    }

    @RequiredArgsConstructor
    public static enum FieldFrame {
        SCALAR_VALUE_INLINE_PROMPT_LINK("scalarValueInlinePromptLink"),
        SCALAR_VALUE_CONTAINER("container-scalarValue"),
        ;
        @Getter private final String containerId;
        public <T extends Component> T createComponent(final Function<String, T> factory) {
            return factory.apply(containerId);
        }
        /* not used
        public Component addComponentIfMissing(final MarkupContainer container,
                final Function<String, ? extends Component> factory) {
            var alreadyExisting = container.get(containerId);
            return alreadyExisting!=null
                    ? alreadyExisting
                    : Wkt.add(container, createComponent(factory));
        }*/
        public Optional<Component> lookupIn(final MarkupContainer container) {
            return Optional.ofNullable(container.get(containerId));
        }
    }

    @RequiredArgsConstructor
    public enum FieldFragment {
        /**
         * field linking to its editing prompt
         */
        LINK_TO_PROMT("fragment-fieldFrame-withLink"){
            @Override
            public RepeatingView createButtonContainer(
                    final MarkupContainer container) {
                return Wkt.repeatingViewAdd(container, "scalarValueInlinePromptLink-buttons");
            }
        },
        /**
         * readonly field
         */
        NO_LINK_VIEWING("fragment-fieldFrame-withoutLink-viewing") {
            @Override
            public RepeatingView createButtonContainer(
                    final MarkupContainer container) {
                // typically would hold buttons 'reason-disabled' and 'copy-to-clipboard'
                return Wkt.repeatingViewAdd(container, "scalarValueView-buttons");
            }
        },
        /**
         * directly editable field
         */
        NO_LINK_EDITING("fragment-fieldFrame-withoutLink-editing") {
            @Override
            public RepeatingView createButtonContainer(
                    final MarkupContainer container) {
                // typically would hold buttons 'edit', 'clear-field' and 'copy-to-clipboard'
                return Wkt.repeatingViewAdd(container, "scalarValueInput-buttons");
            }
        };

        public boolean isLinkToPrompt() { return this == LINK_TO_PROMT; }
        public boolean isNoLinkViewing() { return this == NO_LINK_VIEWING; }
        public boolean isNoLinkEditing() { return this == NO_LINK_EDITING; }

        public static Optional<FieldFragment> matching(final @Nullable MarkupContainer container) {
            if(container instanceof Fragment) {
                final String fragmentId = ((Fragment)container).getAssociatedMarkupId();
                for(var fieldFragement : FieldFragment.values()) {
                    if(fieldFragement.getFragmentId().equals(fragmentId)) {
                        return Optional.of(fieldFragement);
                    }
                }
            }
            return Optional.empty();
        }

        @Getter private final String fragmentId;
        @Getter private final String containerId = "container-fieldFrame";

        /* not used
        private boolean isMatching(final @Nullable MarkupContainer container) {
            if(container instanceof Fragment) {
                return fragmentId.equals(((Fragment)container).getAssociatedMarkupId());
            }
            return false;
        }*/
        public abstract RepeatingView createButtonContainer(final MarkupContainer container);
    }

    // -- OUTPUT/COMPACT FRAGMENTS

    /** Can be used for both CompactFrame and RegularFrame. */
    @RequiredArgsConstructor
    public static enum CompactFragment {
        LABEL("fragment-compact-label"),
        LINK("fragment-compact-link"),
        BADGE("fragment-compact-badge"),
        ENTITY_LINK("fragment-compact-entityLink"),
        CHECKBOX_YES("fragment-compact-checkboxYes"),
        CHECKBOX_NO("fragment-compact-checkboxNo"),
        CHECKBOX_INTERMEDIATE("fragment-compact-checkboxIntermediate"),
        ;
        public static String ID_LINK_LABEL = "linkLabel";
        private final String fragmentId;
        /**
         * @param id - Where to 'put' the fragment
         * @param markupProvider - The component whose markup contains the fragment's markup
         * @param componentFactory - creates the scalarValue component to be added to the fragment
         */
        public <T extends Component> Fragment createFragment(
                final String id, final MarkupContainer markupProvider, final Function<String, T> componentFactory) {
            var fragment = Wkt.fragment(id, fragmentId, markupProvider);
            fragment.add(componentFactory.apply(ScalarPanelAbstract.ID_SCALAR_VALUE));
            return fragment;
        }
        /**
         * @param id - Where to 'put' the fragment
         * @param markupProvider - The component whose markup contains the fragment's markup
         */
        public static Fragment createCheckboxFragment(
                final String id, final MarkupContainer markupProvider, final Boolean value) {
            final CompactFragment chkFragment = value==null
                    ? CHECKBOX_INTERMEDIATE
                    : value
                        ? CHECKBOX_YES
                        : CHECKBOX_NO;
            return Wkt.fragment(id, chkFragment.fragmentId, markupProvider);
        }
    }

    // INPUT FRAGMENTS

    /** Corresponds to EDITING a field in the UI. */
    @RequiredArgsConstructor
    public static enum InputFragment {
        TEXT("fragment-input-text"),
        TEXTAREA("fragment-input-textarea"),
        TEMPORAL("fragment-input-temporal"),
        TEMPORAL_WITH_OFFSET("fragment-input-temporal-with-offset"),
        TEMPORAL_WITH_ZONE("fragment-input-temporal-with-zone"),
        CHECKBOX("fragment-input-checkbox"),
        FILE("fragment-input-file"),
        SELECT_VALUE("fragment-input-select_value"),
        SELECT_OBJECT("fragment-input-select_object"),
        ;
        private final String fragmentId;
        /**
         * @param markupProvider - The component whose markup contains the fragment's markup
         */
        public Fragment createFragment(final MarkupContainer markupProvider, final FormComponent<?> inputComponent) {
            var fragment = Wkt.fragment(
                    FieldFrame.SCALAR_VALUE_CONTAINER.getContainerId(), fragmentId, markupProvider);
            fragment.add(inputComponent);
            return fragment;
        }
    }

    // PROMPT FRAGMENTS

    /** Corresponds to VIEWING a field in the UI, that can enter EDITING mode when eg. clicked into. */
    @RequiredArgsConstructor
    public static enum PromptFragment {
        LABEL("fragment-prompt-label", promptLabelModel->
            Wkt.label(ScalarPanelAbstract.ID_SCALAR_VALUE, promptLabelModel)),
        TEXTAREA("fragment-prompt-textarea", promptLabelModel->
            Wkt.textArea(ScalarPanelAbstract.ID_SCALAR_VALUE, promptLabelModel)),
        CHECKBOX_YES("fragment-prompt-checkboxYes", null),
        CHECKBOX_NO("fragment-prompt-checkboxNo", null),
        CHECKBOX_INTERMEDIATE("fragment-prompt-checkboxIntermediate", null),
        ;
        private final String fragmentId;
        private final Function<IModel<String>, Component> componentFactory;
        /**
         * @param id - Where to 'put' the fragment
         * @param markupProvider - The component whose markup contains the fragment's markup
         * @param componentFactory - creates the scalarValue component to be added to the fragment
         */
        public <T extends Component> Fragment createFragment(
                final String id, final MarkupContainer markupProvider, final Function<String, T> componentFactory) {
            var fragment = Wkt.fragment(id, fragmentId, markupProvider);
            fragment.add(componentFactory.apply(ScalarPanelAbstract.ID_SCALAR_VALUE));
            return fragment;
        }

        /**
         * @param markupProvider - The component whose markup contains the fragment's markup
         */
        public Fragment createFragment(final MarkupContainer markupProvider,
                final IModel<String> promptLabelModel,
                final @Nullable Consumer<FormComponent<String>> onComponentCreated) {
            var fragment = Wkt.fragment(
                    FieldFrame.SCALAR_VALUE_CONTAINER.getContainerId(), fragmentId, markupProvider);
            var component = componentFactory.apply(promptLabelModel);
            fragment.add(component);
            if(onComponentCreated!=null
                    && component instanceof FormComponent) {
                onComponentCreated.accept(_Casts.uncheckedCast(component));
            }
            return fragment;
        }
        /**
         * @param id - Where to 'put' the fragment
         * @param markupProvider - The component whose markup contains the fragment's markup
         */
        public static Fragment createCheckboxFragment(
                final String id, final MarkupContainer markupProvider, final Boolean value) {
            final PromptFragment chkFragment = value==null
                    ? CHECKBOX_INTERMEDIATE
                    : value
                        ? CHECKBOX_YES
                        : CHECKBOX_NO;
            return Wkt.fragment(id, chkFragment.fragmentId, markupProvider);
        }
    }

}
