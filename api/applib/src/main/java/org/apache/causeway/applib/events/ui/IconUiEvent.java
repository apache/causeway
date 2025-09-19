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
package org.apache.causeway.applib.events.ui;

import java.util.EventObject;

import org.apache.causeway.applib.annotation.ObjectSupport;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Emitted for subscribers to provide a cssClass hint (equivalent to the
 * <tt>icon(..)</tt> supporting method).
 *
 * <p>If the domain object defines its own xref:refguide:applib-methods:ui-hints.adoc[iconName()] supporting method,
 * or if it has the xref:refguide:applib:index/annotation/DomainObjectLayout.adoc#cssClassFa[@DomainObjectLayout#cssClassFa()]
 * attribute, then these will take precedence.
 *
 * <p>The class has a number of responsibilities:
 * <ul>
 * <li>capture the target object being interacted with</li>
 * <li>capture the icon (name), if any, as specified to one of the subscribers</li>
 * </ul>
 *
 * <p> The class itself is instantiated automatically by the framework.
 *
 * @since 1.x revised for 4.0 {@index}
 */
public abstract class IconUiEvent<S> extends AbstractUiEvent<S> {

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>Because the {@link EventObject} superclass prohibits a null source, a dummy value is temporarily used.
     */
    protected IconUiEvent() {
        super(null);
    }

    @Getter @Setter @Accessors(fluent = true)
    private ObjectSupport.IconSize iconSize;

    /**
     * Icon resource for corresponding domain object,
     * which subscribers may set, based on {@link #iconSize()}.
     */
    @Getter @Setter
    private ObjectSupport.IconResource icon;

    /**
     * This class is the default for the
     * {@link org.apache.causeway.applib.annotation.DomainObjectLayout#iconUiEvent()}
     * annotation element.
     *
     * <p>Whether this raises an event or not depends upon the
     * <tt>causeway.applib.annotation.domain-object-layout.icon-ui-event.post-for-default</tt>
     * configuration property.
     */
    public static class Default extends IconUiEvent<Object> {
    }

    /**
     * Convenience class to use indicating that an event should <i>not</i> be
     * posted (irrespective of the configuration property setting for the
     * {@link Default} event.
     */
    public static class Noop extends IconUiEvent<Object> {
    }

    /**
     * Convenience class meaning that an event <i>should</i> be posted
     * (irrespective of the configuration property setting for the
     * {@link Default} event..
     */
    public static class Doop extends IconUiEvent<Object> {
    }

}
