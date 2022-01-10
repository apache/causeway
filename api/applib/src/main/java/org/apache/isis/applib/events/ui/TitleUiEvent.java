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

import org.apache.isis.applib.services.i18n.TranslatableString;

import lombok.Getter;

/**
 * Emitted for subscribers to obtain a title hint (equivalent to the
 * <tt>title()</tt> supporting method).
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
 *          capture the title, if any, as specified to one of the subscribers
 *     </li>
 * </ul>
 *
 * <p>
 *  The class itself is instantiated automatically by the framework.
 * </p>
 *
 * @since 1.x {@index}
 */
public abstract class TitleUiEvent<S> extends AbstractUiEvent<S> {

    /**
     * This class is the default for the
     * {@link org.apache.isis.applib.annotation.DomainObjectLayout#titleUiEvent()}
     * annotation element.
     *
     * <p>
     * Whether this raises an event or not depends upon the
     * <tt>isis.applib.annotation.domain-object-layout.title-ui-event.post-for-default</tt>
     * configuration property.
     * </p>
     */
    public static class Default extends TitleUiEvent<Object> {}

    /**
     * Convenience class to use indicating that an event should <i>not</i> be
     * posted (irrespective of the configuration property seting for the
     * {@link Default} event.
     */
    public static class Noop extends TitleUiEvent<Object> {}

    /**
     * Convenience class meaning that an event <i>should</i> be posted
     * (irrespective of the configuration property setting for the
     * {@link Default} event.
     */
    public static class Doop extends TitleUiEvent<Object> {}


    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Because the {@link EventObject} superclass prohibits a null source, a dummy value is temporarily used.
     * </p>
     */
    public TitleUiEvent() {
        this(null);
    }

    public TitleUiEvent(final S source) {
        super(source);
    }


    /**
     * The title as provided by a subscriber using {@link #setTitle(String)}.
     *
     * <p>
     *     Note that a {@link #getTranslatableTitle()} will be used in preference, if available.
     * </p>
     */
    @Getter
    private String title;

    /**
     * For subscribers to call to provide a (non-translated) title for this object.
     */
    public void setTitle(final String title) {
        this.title = title;
    }


    /**
     * The translatable (i18n) title as provided by a subscriber using {@link #setTranslatableTitle(TranslatableString)}.
     *
     * <p>
     *     If a translatable title has been provided then this will be used in preference to any
     *     {@link #getTitle() non-translatable title}.
     * </p>
     */
    @Getter
    private TranslatableString translatableTitle;

    /**
     * For subscribers to call to provide a translatable (i18n) title for this object.
     */
    public void setTranslatableTitle(final TranslatableString translatableTitle) {
        this.translatableTitle = translatableTitle;
    }


}
