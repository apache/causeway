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
package org.apache.isis.applib.events.ui;

import java.util.EventObject;

/**
 * Emitted for subscribers to provide a cssClass hint (equivalent to the
 * <tt>iconName()</tt> supporting method).
 *
 * <p>
 * The class has a number of responsibilities:
 * </p>
 *
 * <ul>
 *     <li>
 *       capture the target object being interacted with
 *     </li>
 *     <li>
 *      capture the icon (name), if any, as specified to one of the subscribers
 *     </li>
 * </ul>
 *
 * <p>
 * The class itself is instantiated automatically by the framework.
 * </p>
 *
 *
 * *
 *
 *
 * If the domain object defines its own xref:refguide:applib-methods:reserved.adoc#iconName[iconName()] supporting method, or if it has the
 * xref:refguide:applib:index/annotation/DomainObjectLayout.adoc#cssClassFa[@DomainObjectLayout#cssClassFa()] attribute, then these will take precedence.
 * </p>
 *
 * @since 1.x {@index}
 */
public abstract class IconUiEvent<S> extends AbstractUiEvent<S> {

    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Because the {@link EventObject} superclass prohibits a null source, a dummy value is temporarily used.
     * </p>
     */
    public IconUiEvent() {
        this(null);
    }

    public IconUiEvent(final S source) {
        super(source);
    }



    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.DomainObjectLayout#iconUiEvent()}
     * annotation element.
     *
     * <p>
     * Whether this raises an event or not depends upon the
     * <tt>isis.applib.annotation.domain-object-layout.icon-ui-event.post-for-default</tt>
     * configuration property.
     * </p>
     */
    public static class Default extends IconUiEvent<Object> { }


    /**
     * Convenience class to use indicating that an event should <i>not</i> be
     * posted (irrespective of the configuration property setting for the
     * {@link Default} event.
     */
    public static class Noop extends IconUiEvent<Object> { }


    // -- Doop class

    /**
     * Convenience class meaning that an event <i>should</i> be posted
     * (irrespective of the configuration property setting for the
     * {@link Default} event..
     */
    public static class Doop extends IconUiEvent<Object> { }



    // -- iconName
    private String iconName;

    /**
     * The icon name as provided by a subscriber using {@link #setIconName(String)}.
     */
    public String getIconName() {
        return iconName;
    }

    /**
     * For subscribers to call to provide an icon name for this object.
     */
    public void setIconName(final String iconName) {
        this.iconName = iconName;
    }


}
