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

import org.apache.isis.applib.annotation.DomainObjectLayout;

import lombok.Getter;

/**
 * Emitted for subscribers to provide a cssClass hint (equivalent to the
 * <tt>cssClass()</tt> supporting method or the
 * {@link DomainObjectLayout#cssClass()} element).
 *
 * <p>
 * The class has a number of responsibilities:
 * </p>
 *
 * <ul>
 *     <li>
 *          capture the target object being interacted with
 *     </li>
 *     <li>
 *          capture the CSS class, if any, as specified to one of the subscribers
 *     </li>
 * </ul>
 *
 * <p>
 *  The class itself is instantiated automatically by the framework.
 * </p>
 *
 * @since 1.x {@index}
 */
public abstract class CssClassUiEvent<S> extends AbstractUiEvent<S> {

    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.DomainObjectLayout#cssClassUiEvent()}
     * annotation element.
     *
     * <p>
     * Whether this raises an event or not depends upon the
     * <tt>isis.applib.annotation.domain-object-layout.css-class-ui-event.post-for-default</tt>
     * configuration property.
     * </p>
     */
    public static class Default extends CssClassUiEvent<Object> {}

    /**
     * Convenience class to use indicating that an event should <i>not</i> be
     * posted (irrespective of the configuration property setting for the
     * {@link Default} event.
     */
    public static class Noop extends CssClassUiEvent<Object> {}

    /**
     * Convenience class meaning that an event <i>should</i> be posted
     * (irrespective of the configuration property setting for the
     * {@link Default} event..
     */
    public static class Doop extends CssClassUiEvent<Object> {}


    /**
     * If used then the framework will set state via (non-API) setters.
     *
     */
    public CssClassUiEvent() {
        this(null);
    }

    public CssClassUiEvent(final S source) {
        super(source);
    }

    /**
     * The CSS class as provided by a subscriber using {@link #setCssClass(String)}.
     */
    @Getter
    private String cssClass;

    /**
     * For subscribers to call to provide a CSS class for this object.
     */
    public void setCssClass(final String cssClass) {
        this.cssClass = cssClass;
    }

}
