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
package org.apache.causeway.viewer.wicket.ui.components.widgets.links;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes.EventPropagation;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.danekja.java.util.function.serializable.SerializableConsumer;

import org.apache.causeway.viewer.wicket.ui.util.Wkt;

import lombok.Getter;
import lombok.Setter;

/**
 * Stops the propagation of the JavaScript event to the parent of its target.
 */
public class AjaxLinkNoPropagate extends AjaxLink<Void> {

    private static final long serialVersionUID = 1L;

    private final SerializableConsumer<AjaxRequestTarget> onClick;

    @Getter @Setter
    private EventPropagation eventPropagation;


    public AjaxLinkNoPropagate(final String id, final SerializableConsumer<AjaxRequestTarget> onClick) {
        this(id, EventPropagation.STOP, onClick);
    }

    public AjaxLinkNoPropagate(
            final String id,
            final EventPropagation eventPropagation,
            final SerializableConsumer<AjaxRequestTarget> onClick) {
        super(id);
        this.onClick = onClick;
        this.eventPropagation = eventPropagation;
    }


    @Override
    public final void onClick(final AjaxRequestTarget target) {
        onClick.accept(target);
    }

    @Override
    protected final AjaxEventBehavior newAjaxEventBehavior(final String event) {
        return new AjaxEventBehavior(event) {
            private static final long serialVersionUID = 1L;

            @Override protected void onEvent(final AjaxRequestTarget target) {
                onClick(target);
            }

            @Override  protected void updateAjaxAttributes(final AjaxRequestAttributes attributes) {
                attributes.setPreventDefault(true);
                attributes.setEventPropagation(eventPropagation);
                super.updateAjaxAttributes(attributes);
            }

            @Override public boolean getStatelessHint(final Component component) {
                return false;
            }

            @Override protected void onComponentTag(final ComponentTag tag)   {
                super.onComponentTag(tag);
                Wkt.fixDisabledState(AjaxLinkNoPropagate.this, tag);
            }

        };
    }
}
