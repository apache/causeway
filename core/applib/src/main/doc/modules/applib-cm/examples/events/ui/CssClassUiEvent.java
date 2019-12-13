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

/**
 * Emitted for subscribers to obtain a cssClass hint (equivalent to the <tt>cssClass()</tt> supporting method or the {@link DomainObjectLayout#cssClass()} attribute).
 */
public abstract class CssClassUiEvent<S> extends AbstractUiEvent<S> {

    // -- constructors
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

    // -- Default class
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.DomainObjectLayout#cssClassUiEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the <tt>isis.reflector.facet.domainObjectLayoutAnnotation.cssClassUiEvent.postForDefault</tt>
     * configuration property.
     */
    public static class Default extends CssClassUiEvent<Object> {}


    // -- Noop class

    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    public static class Noop extends CssClassUiEvent<Object> {}


    // -- Doop class

    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
     */
    public static class Doop extends CssClassUiEvent<Object> {}


    // -- cssClass
    private String cssClass;

    /**
     * The CSS class as provided by a subscriber using {@link #setCssClass(String)}.
     */
    public String getCssClass() {
        return cssClass;
    }

    /**
     * For subscribers to call to provide a CSS class for this object.
     */
    public void setCssClass(final String cssClass) {
        this.cssClass = cssClass;
    }


}