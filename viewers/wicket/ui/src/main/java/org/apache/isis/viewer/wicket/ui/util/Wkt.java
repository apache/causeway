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
package org.apache.isis.viewer.wicket.ui.util;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.danekja.java.util.function.serializable.SerializableBooleanSupplier;
import org.danekja.java.util.function.serializable.SerializableConsumer;

import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.debug._Probe.EntryPoint;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.ui.components.widgets.linkandlabel.ActionLink;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Wkt {

    public <T extends Component> T add(final MarkupContainer container, final T component) {
        container.addOrReplace((Component)component);
        return component;
    }

    public <T extends Behavior> T add(final MarkupContainer container, final T component) {
        container.add((Behavior)component);
        return component;
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

    public Behavior behaviorAddOnClick(
            final MarkupContainer markupProvider,
            final SerializableConsumer<AjaxRequestTarget> onClick) {
        return add(markupProvider, behaviorOnClick(onClick));
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

    // -- FRAGMENT

    /**
     * @param container - The component whose markup contains the fragment's markup
     * @param id - The component id
     * @param markupId - The associated id of the associated markup fragment
     */
    public Fragment fragmentAddNoTab(final MarkupContainer container, final String id, final String markupId) {
        return new Fragment(id, markupId, container) {
            private static final long serialVersionUID = 1L;
            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("tabindex", "-1");
            }
        };
    }

    // -- LABEL

    public Label label(final String id, final String label) {
        return new Label(id, label);
    }

    public Label label(final String id, final IModel<String> labelModel) {
        return new Label(id, labelModel);
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

    public Label labelAdd(final MarkupContainer container, final String id, final String label) {
        return add(container, label(id, label));
    }

    public Label labelAdd(final MarkupContainer container, final String id, final IModel<String> labelModel) {
        return add(container, new Label(id, labelModel));
    }

    public Label labelAddNoTab(final MarkupContainer container, final String id, final IModel<String> labelModel) {
        return add(container, labelNoTab(id, labelModel));
    }

    // -- LINK

    public ActionLink linkAdd(final MarkupContainer container, final String id, final LinkAndLabel linkAndLabel) {
        val component = linkAndLabel.getUiComponent();
        container.addOrReplace(component);
        return (ActionLink) component;
    }

    public Link<Void> linkAdd(
            final MarkupContainer container,
            final String linkId,
            final String labelId,
            final String linkName) {
        val link = new Link<Void>(linkId) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onClick() {
            }
        };
        container.addOrReplace(link);
        Wkt.labelAdd(link, labelId, linkName);
        return link;
    }

    // -- LIST VIEW

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
            final IModel<? extends List<T>> listModel,
            final SerializableConsumer<ListItem<T>> itemPopulator) {
        return add(container, listView(id, listModel, itemPopulator));
    }

    // -- TEXT AREA

    public TextArea<String> textAreaNoTab(final String id, final IModel<String> textModel) {
        return new TextArea<String>(id, textModel) {
            private static final long serialVersionUID = 1L;
            @Override protected void onComponentTag(final ComponentTag tag) {
                super.onComponentTag(tag);
                tag.put("tabindex", "-1");
            }
        };
    }

    public TextArea<String> textAreaAddNoTab(
            final MarkupContainer container, final String id, final IModel<String> textModel) {
        return add(container, textAreaNoTab(id, textModel));
    }

    // -- FOCUS UTILITY

    /**
     * If the container has any child with the marker attribute {@code data-isis-focus},
     * then the first one found will receive focus (in the browser).
     * @implNote HTML allows for custom attributes with naming convention {@code data-}.
     */
    public void focusOnMarkerAttribute(
            final MarkupContainer container,
            final AjaxRequestTarget target) {

        container.streamChildren()
        .filter(child->child.getMarkupAttributes().containsKey("data-isis-focus"))
        .findFirst()
        .ifPresent(child->{
            target.focusComponent(child);
        });

    }

}
