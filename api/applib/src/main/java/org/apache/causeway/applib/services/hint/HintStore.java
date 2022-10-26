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
package org.apache.causeway.applib.services.hint;

import java.util.Set;

import org.apache.causeway.applib.services.bookmark.Bookmark;

/**
 * Defines a mechanism for viewers to store arbitrary UI hints on a per-object
 * basis.
 *
 * <p>
 * This store <i>is</i> used by the Wicket viewer.   For example, the viewer
 * remembers which tabs are selected, and for collections which view is
 * selected (eg table or hidden), which page of a table to render, or whether
 * "show all" (rows) is toggled.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface HintStore {

    /**
     * Obtain a hint (eg which tab to open) for a particular object.
     *
     * <p>
     * Object identity is represented by {@link Bookmark} so that alternative
     * implementations can easily serialize this state to a string.
     * </p>
     *
     * @apiNote Object identity is represented by {@link Bookmark} so that
     *          alternative implementations can easily serialize this state to a string.
     *
     * @param bookmark - representing the domain object
     * @param hintKey - the key of the hint
     *
     * @return - the value of the hint, or null.
     */
    String get(final Bookmark bookmark, String hintKey);

    /**
     * Set the state of a hint for the domain object
     *
     * @apiNote Object identity is represented by {@link Bookmark} so that
     *          alternative implementations can easily serialize this state to a string.
     *
     * @param bookmark - representing the domain object
     * @param hintKey - the key of the hint
     * @param value - the value of the hint
     */
    void set(final Bookmark bookmark, String hintKey, String value);

    /**
     * Removes hint for the domain object.
     *
     * @apiNote Object identity is represented by {@link Bookmark} so that
     *          alternative implementations can easily serialize this state to a string.
     *
     * @param bookmark - representing the domain object
     * @param hintKey - the key of the hint
     */
    void remove(final Bookmark bookmark, String hintKey);

    /**
     * Remotes all hints for the domain object.
     *
     * @apiNote Object identity is represented by {@link Bookmark} so that
     *          alternative implementations can easily serialize this state to a string.
     *
     * @param bookmark - representing the domain object
     */
    void removeAll(Bookmark bookmark);

    /**
     * The keys of all available hints for the domain object.
     *
     * @apiNote Object identity is represented by {@link Bookmark} so that
     *          alternative implementations can easily serialize this state to a string.
     *
     * @param bookmark - representing the domain object
     */
    Set<String> findHintKeys(Bookmark bookmark);

}
