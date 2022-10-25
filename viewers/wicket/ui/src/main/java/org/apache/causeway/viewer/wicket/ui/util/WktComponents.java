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

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.lang.Args;

import org.apache.causeway.commons.internal.exceptions._Exceptions.FluentException;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;

import de.agilecoders.wicket.jquery.util.Strings2;

public final class WktComponents {

    /**
     * Permanently hides by replacing with a {@link Label} that has an empty
     * string for its caption.
     */
    public static void permanentlyHide(final MarkupContainer container, final String... ids) {
        for (final String id : ids) {
            permanentlyHideSingle(container, id);
        }
    }

    /**
     * @see #permanentlyHide(MarkupContainer, String...)
     */
    public static void permanentlyHide(final MarkupContainer container, final UiComponentType... componentIds) {
        for (final UiComponentType uiComponentType : componentIds) {
            permanentlyHideSingle(container, uiComponentType.getId());
        }
    }

    /**
     * Not overloaded because - although compiles ok on JDK6u20 (Mac), fails to
     * on JDK6u18 (Ubuntu)
     */
    private static void permanentlyHideSingle(final MarkupContainer container, final String id) {
        final WebMarkupContainer invisible = new WebMarkupContainer(id);
        invisible.setVisible(false);
        container.addOrReplace(invisible);
    }

    /**
     * Sets the visibility of the child component(s) within the supplied
     * container.
     */
    public static void setVisible(final MarkupContainer container, final boolean visibility, final String... ids) {
        for (final String id : ids) {
            setVisible(container, visibility, id);
        }
    }

    /**
     * @see #setVisible(MarkupContainer, boolean, String...)
     */
    public static void setVisible(final MarkupContainer container, final boolean visibility, final UiComponentType... componentTypes) {
        for (final UiComponentType uiComponentType : componentTypes) {
            setVisible(container, visibility, uiComponentType.getId());
        }
    }

    private static void setVisible(final MarkupContainer container, final boolean visibility, final String wicketId) {
        final Component childComponent = container.get(wicketId);
        childComponent.setVisible(visibility);
    }

    public static boolean isRenderedComponent(final Component component) {
        return (component.getOutputMarkupId() && !(component instanceof Page));
    }

    public static boolean hasPage(final Component component) {
        return component.findParent(Page.class)!=null;
    }

    public static void addToAjaxRequest(final AjaxRequestTarget target, final Component component) {

        if (target == null || component == null) {
            return;
        }

        //TODO as of Wicket-8 we (for lack of a better solution) silently ignore
        // java.lang.IllegalArgumentException ...

        try {
            target.add(component);
        } catch (IllegalArgumentException cause) {
            FluentException.of(cause)
            .suppressIfMessageContains("Cannot update component because its page is not the same");
        }

    }

    public static CharSequence getMarkupId(final Component component) {
        return Strings2.getMarkupId(Args.notNull(component, "component"));
    }

}
