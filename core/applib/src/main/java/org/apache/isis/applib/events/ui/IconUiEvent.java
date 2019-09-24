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


/**
 * Emitted for subscribers to obtain a cssClass hint (equivalent to the <tt>iconName()</tt> supporting method).
 */
public abstract class IconUiEvent<S> extends AbstractUiEvent<S> {

    // -- constructors
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



    // -- Default class
    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.DomainObjectLayout#iconUiEvent()} annotation attribute.  Whether this
     * raises an event or not depends upon the "isis.reflector.facet.domainObjectLayoutAnnotation.iconUiEvent.postForDefault"
     * configuration property.
     */
    public static class Default extends IconUiEvent<Object> { }


    // -- Noop class

    /**
     * Convenience class to use indicating that an event should <i>not</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event.
     */
    public static class Noop extends IconUiEvent<Object> { }


    // -- Doop class

    /**
     * Convenience class meaning that an event <i>should</i> be posted (irrespective of the configuration
     * property setting for the {@link Default} event..
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