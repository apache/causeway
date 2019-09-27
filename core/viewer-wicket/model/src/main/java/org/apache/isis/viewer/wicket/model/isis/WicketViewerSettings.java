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

package org.apache.isis.viewer.wicket.model.isis;

import java.io.Serializable;

import org.apache.isis.applib.annotation.PromptStyle;

public interface WicketViewerSettings extends Serializable {

    /**
     * The maximum length that a title of an object will be shown when rendered in a standalone table;
     * will be truncated beyond this (with ellipses to indicate the truncation).
     */
    int getMaxTitleLengthInStandaloneTables();

    /**
     * The maximum length that a title of an object will be shown when rendered in a parented table;
     * will be truncated beyond this (with ellipses to indicate the truncation).
     */
    int getMaxTitleLengthInParentedTables();

    /**
     * The pattern used for rendering and parsing dates.
     *
     * <p>
     * Each Date scalar panel will use {@ #getDatePattern()} or {@linkplain #getDateTimePattern()} depending on its
     * date type.  In the case of panels with a date picker, the pattern will be dynamically adjusted so that it can be
     * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
     * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
     * than those of regular Java code).
     */
    String getDatePattern();

    /**
     * The pattern used for rendering and parsing date/times.
     *
     * <p>
     * Each Date scalar panel will use {@ #getDatePattern()} or {@linkplain #getDateTimePattern()} depending on its
     * date type.  In the case of panels with a date time picker, the pattern will be dynamically adjusted so that it can be
     * used by the <a href="https://github.com/Eonasdan/bootstrap-datetimepicker">Bootstrap Datetime Picker</a>
     * component (which uses <a href="http://momentjs.com/docs/#/parsing/string-format/">Moment.js formats</a>, rather
     * than those of regular Java code).
     */
    String getDateTimePattern();

    /**
     * The pattern used for rendering and parsing timestamps.
     */
    String getTimestampPattern();

    /**
     * in Firefox and more recent versions of Chrome 54+, cannot copy out of disabled fields; instead we use the
     * readonly attribute (https://www.w3.org/TR/2014/REC-html5-20141028/forms.html#the-readonly-attribute)
     * This behaviour is enabled by default but can be disabled using this flag
     */
    boolean isReplaceDisabledTagWithReadonlyTag();

    /**
     * Whether to disable a form submit button after it has been clicked, to prevent users causing an error if they
     * do a double click.
     *
     * This behaviour is enabled by default, but can be disabled using this flag.
     */
    boolean isPreventDoubleClickForFormSubmit();

    /**
     * Whether to disable a no-arg action button after it has been clicked, to prevent users causing an error if they
     * do a double click.
     *
     * This behaviour is enabled by default, but can be disabled using this flag.
     */
    boolean isPreventDoubleClickForNoArgAction();

    /**
     * Whether to show an indicator for a form submit button that it has been clicked.
     *
     * This behaviour is enabled by default, but can be disabled using this flag.
     */
    boolean isUseIndicatorForFormSubmit();

    /**
     * Whether to show an indicator for a no-arg action button that it has been clicked.
     *
     * This behaviour is enabled by default, but can be disabled using this flag.
     */
    boolean isUseIndicatorForNoArgAction();

    /**
     * Whether to use a modal dialog for property edits and for actions associated with properties.
     * This can be overridden on a case-by-case basis using <code>@PropertyLayout#promptStyle</code> and
     * <code>@ActionLayout#promptStyle</code>.
     *
     * This behaviour is disabled by default; the viewer will use an inline prompt in these cases, making for a smoother
     * user experience. If enabled then this reinstates the pre-1.15.0 behaviour of using a dialog prompt in all cases.
     */
    PromptStyle getPromptStyle();

    /**
     * Whether to redirect to a new page, even if the object being shown (after an action invocation or a property edit)
     * is the same as the previous page.
     *
     * This behaviour is disabled by default; the viewer will update the existing page if it can, making for a
     * smoother user experience. If enabled then this reinstates the pre-1.15.0 behaviour of redirecting in all cases.
     */
    boolean isRedirectEvenIfSameObject();

}
