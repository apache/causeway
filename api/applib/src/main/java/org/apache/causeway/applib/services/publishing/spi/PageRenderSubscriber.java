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
package org.apache.causeway.applib.services.publishing.spi;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.having.HasEnabling;

/**
 * Part of the <i>Publishing SPI</i>. A component to receive notifaction that a domain object
 * or a standalone list of domain objects has been rendered.
 *
 * @since 2.0 {@index}
 */
public interface PageRenderSubscriber extends HasEnabling {

    /**
     * Enumerates the different types of pages that can be rendered.
     */
    public enum PageType {
        /**
         * Either a view model or an entity.
         */
        DOMAIN_OBJECT,
        /**
         * A collection of view models or entities.
         */
        COLLECTION,
        /**
         * A single value
         */
        VALUE,
        /**
         * Anything else; might include void, or sign in pages.
         */
        OTHER;
    }

    /**
     * Called just before the rendering process starts.
     *
     * <p>
     *     Determines which of the <code>onRenderedXxx</code> callbacks (if any) will next be called:
     *     <ul>
     *         <li>if {@link PageType#DOMAIN_OBJECT}, then will call {@link #onRenderingDomainObject(Bookmark)} next,
     *             and afterwards {@link #onRenderedDomainObject(Bookmark)} next</li>
     *         <li>if {@link PageType#COLLECTION}, then will call {@link #onRenderedCollection(Supplier)} next</li>
     *         <li>if {@link PageType#VALUE}, then will call {@link #onRenderedValue(Object)} next</li>
     *     </ul>
     *     However, if {@link PageType#OTHER}, then <b>NO</b> <code>onRenderedXxx</code> method will be called.
     * </p>
     *
     * <p>
     *     Also, if any of the pages fail to render (eg due to an authorization exception), then - again -
     *     <b>NO</b> <code>onRenderedXxx</code> method will be called
     * </p>
     *
     * <p>
     *     Implementations could use this to start a stopwatch, for example, or to reset caches.
     * </p>
     *
     * @param pageType - determines which of the subsequent <code>onRenderedXxx</code> callbacks (if any) will next be called.
     */
    default void onRendering(final PageType pageType) {}

    /**
     * Indicates that the domain object represented by the {@link Bookmark} is about to be rendered.
     *
     * @param bookmark - representation of the domain object that are to be rendered.
     */
    default void onRenderingDomainObject(final Bookmark bookmark) {}

    /**
     * Indicates that a standalone list of domain objects (each represented by a {@link Bookmark} is about to be rendered.
     *
     * @param bookmarkSupplier - a supplier representations of the collection of domain objects that are to be rendered.  The level of indirection is for performance (in case no implementation is interested)
     */
    default void onRenderingCollection(final Supplier<List<Bookmark>> bookmarkSupplier) {}

    /**
     * Indicates that a value is about to be rendered.
     *
     * @param value - the actual value that is to be rendered.
     */
    default void onRenderingValue(final @Nullable Object value) {}

    /**
     * Indicates that the domain object represented by the {@link Bookmark} has been rendered.
     *
     * @param bookmark - representation of the domain object that has been rendered.
     */
    default void onRenderedDomainObject(final Bookmark bookmark) {}

    /**
     * Indicates that a standalone list of domain objects (each represented by a {@link Bookmark} has been rendered.
     *
     * @param bookmarkSupplier - a supplier representations of the collection of domain objects that have been rendered.  The level of indirection is for performance (in case no implementation is interested)
     */
    default void onRenderedCollection(final Supplier<List<Bookmark>> bookmarkSupplier) {}

    /**
     * Indicates that a value has been rendered.
     *
     * @param value - the actual value that has been rendered.
     */
    default void onRenderedValue(final @Nullable Object value) {}

}
