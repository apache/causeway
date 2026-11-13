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
package org.apache.causeway.applib.value.semantics;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract.FormatUsageFor;

/**
 * A base for all numerical value types.
 *
 * @since 4.0
 */
public interface NumericValueSemantics<T>
extends
    ValueSemanticsProvider<T>,
    Renderer<T>,
    Parser<T> {

    /**
     * No grouping separators are used for display nor editing. However, white-spaces on input are ignored.
     */
    public final static String NO_GROUPING = "no-grouping";
    /**
     * Locale specific grouping separators are used for display,
     * while any non-white-space grouping separators are not allowed for input.
     * White-spaces on input are ignored.
     */
    public final static String LOCALE_GROUPING_DISPLAY = "locale-grouping-display";
    /**
     * Uses locale specific grouping separators for display and allows grouping separators for numerical input (when editing).
     */
    public final static String LOCALE_GROUPING_ALL = "locale-grouping-all";

    /**
     * Specifies the grouping separation behavior for parsing and rendering.
     *
     * @apiNote Subclasses may provide their own to customize grouping behavior.
     */
    interface GroupingSeparatorProvider {
        @Nullable String separator(@Nullable Context context, FormatUsageFor usedFor);

        static GroupingSeparatorProvider NO_GROUPING = (context, usedFor) -> null;
        static GroupingSeparatorProvider SPACED_GROUPING = (context, usedFor) -> switch(usedFor) {
            case RENDERING_AS_TEXT -> " "; // UTF8 U+2009
            case RENDERING_AS_HTML -> "&#8239;"; // small space
            case PARSING -> " "; // UTF8 U+2009;
        };
        static GroupingSeparatorProvider LOCALE_GROUPING_DISPLAY = (context, usedFor) -> switch(usedFor) {
            case RENDERING_AS_TEXT, RENDERING_AS_HTML -> "" + localeGroupingSeparator(context);
            case PARSING -> " "; // UTF8 U+2009;
        };
        static GroupingSeparatorProvider LOCALE_GROUPING_ALL = (context, usedFor) -> "" + localeGroupingSeparator(context);
    }

    /**
     * Specifies the grouping separation behavior for parsing and rendering.
     *
     * @apiNote Subclasses may override to customize grouping behavior.
     */
    default GroupingSeparatorProvider grouping() {
        return GroupingSeparatorProvider.SPACED_GROUPING;
    }

    // -- UTIL

    /**
     * Returns locale based grouping separator. If no context is given, system defaults apply.
     */
    public static char localeGroupingSeparator(@Nullable final Context context) {
        var userLocale = ValueSemanticsProvider.getUserLocale(context);
        return new DecimalFormatSymbols(userLocale.numberFormatLocale()).getGroupingSeparator();
    }

    /**
     * Returns locale based {@link DecimalFormat}. If no context is given, system defaults apply.
     */
    public static DecimalFormat localeDecimalFormat(@Nullable final Context context) {
        var userLocale = ValueSemanticsProvider.getUserLocale(context);
        return (DecimalFormat)NumberFormat.getNumberInstance(userLocale.numberFormatLocale());
    }

}
