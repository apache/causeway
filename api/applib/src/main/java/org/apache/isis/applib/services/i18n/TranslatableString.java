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
package org.apache.isis.applib.services.i18n;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.commons.internal.collections._Lists;

/**
 * 
 * @since 1.x {@index}
 */
@Value
public final class TranslatableString {

    // -- tr, trn (factory methods); constructor

    /**
     * A translatable string with a single pattern for both singular and plural forms.
     *
     * @param pattern - pattern for all (singular and plural) form, with <code>${xxx}</code> placeholders
     * @param paramArgs - parameter/argument pairs (string and object, string and object, ...)
     */
    public static TranslatableString tr(
            final String pattern,
            final Object... paramArgs) {

        if(pattern == null) {
            return null;
        }
        return new TranslatableString(Type.TR, pattern, null, 1, asMap(paramArgs));

        // ...
    }

    /**
     * A translatable string with different patterns for singular and plural forms, selected automatically by the number
     *
     * @param singularPattern - pattern for the singular form, with <code>${xxx}</code> placeholders
     * @param pluralPattern - pattern for the singular form, with <code>${xxx}</code> placeholders
     * @param number - whether to use singular or plural form when rendering
     * @param paramArgs - parameter/argument pairs (string and object, string and object, ...)
     */
    public static TranslatableString trn(
            final String singularPattern,
            final String pluralPattern,
            final int number,
            final Object... paramArgs) {

        return new TranslatableString(Type.TRN, singularPattern, pluralPattern, number, asMap(paramArgs));

        // ...
    }

    /**
     * Converts a list of objects [a, 1, b, 2] into a map {a -> 1; b -> 2}
     */
    private static Map<String, Object> asMap(final Object[] paramArgs) {
        final HashMap<String, Object> map = new HashMap<String, Object>();
        boolean param = true;
        String paramStr = null;
        for (final Object paramArg : paramArgs) {
            if (param) {
                if (paramArg instanceof String) {
                    paramStr = (String) paramArg;
                } else {
                    throw new IllegalArgumentException("Parameter must be a string");
                }
            } else {
                final Object arg = paramArg;
                map.put(paramStr, arg);
                paramStr = null;
            }
            param = !param;
        }
        if (paramStr != null) {
            throw new IllegalArgumentException("Must have equal number of parameters and arguments");
        }
        return map;
    }

    private TranslatableString(
            final Type type,
            final String singularText,
            final String pluralText,
            final int number,
            final Map<String, Object> argumentsByParameterName) {

        this.type = type;
        this.singularText = singularText;
        this.pluralText = pluralText;
        this.number = number;
        this.argumentsByParameterName = argumentsByParameterName;
    }



    // -- singularText, pluralText, pluralForm

    private final String singularText;
    private final String pluralText;

    /**
     * The text as provided in (either of the {@link #tr(String, Object...) factory} {@link #trn(String, String, int, Object...) method}s,
     * with placeholders rather than substituted arguments; if {@link #isPluralForm()} is <code>true</code> then used only
     * for the singular form.
     */
    String getSingularText() {
        return singularText;
    }

    /**
     * The plural text as provided in the {@link #trn(String, String, int, Object...) factory method}, with placeholders
     * rather than substituted arguments; but will be <code>null</code> if {@link #isPluralForm()} is <code>false</code>.
     */
    String getPluralText() {
        return pluralText;
    }

    private enum Type {
        /**
         * No plurals
         */
        TR {
            @Override
            public String toString(final TranslatableString trString) {
                return "tr: " + trString.singularText;
            }
        },
        /**
         * With plurals
         */
        TRN {
            @Override
            public String toString(final TranslatableString trString) {
                return "trn: " + trString.pluralText;
            }
        };

        public abstract String toString(final TranslatableString trString);
    }


    private final Type type;
    private final int number;

    boolean isPluralForm() {
        return type == Type.TRN;
    }


    // -- argumentsByParameterName
    private final Map<String, Object> argumentsByParameterName;

    /**
     * The arguments; excluded from {@link #equals(Object)} comparison.
     */
    Map<String, Object> getArgumentsByParameterName() {
        return argumentsByParameterName;
    }


    // -- translate

    /**
     * Translates this string using the provided {@link org.apache.isis.applib.services.i18n.TranslationService}, selecting
     * either the single or plural form as per {@link #getPattern()}.
     * @param translationService
     * @param context
     * @return
     */
    public String translate(final TranslationService translationService, final String context) {

        final String translatedText =
                !isPluralForm()
                ? translationService.translate(context, getSingularText())
                        : translationService.translate(context, getSingularText(), getPluralText(), number);
                return translated(translatedText);
    }

    /**
     * The text to be translated; depends on whether {@link #isPluralForm()} and whether to be translated.
     *
     * <p>
     *     Any placeholders will <i>not</i> have been replaced.
     * </p>
     *
     * <p>
     *     NB: this method is exposed only so that implementations of
     *     {@link org.apache.isis.applib.exceptions.TranslatableException} can return a non-null
     *     {@link Exception#getMessage() message} when only a translatable message has been provided.
     * </p>
     */
    public String getPattern() {
        return !isPluralForm() || number == 1 ? getSingularText() : getPluralText();
    }

    String translated(final String translatedText) {
        return format(translatedText, argumentsByParameterName);
    }

    static String format(String format, Map<String, Object> values)
    {
        StringBuilder formatter = new StringBuilder(format);
        List<Object> valueList = _Lists.newArrayList();

        Matcher matcher = Pattern.compile("\\{(\\w+)}").matcher(format);

        while (matcher.find())
        {
            String key = matcher.group(1);

            String formatKey = String.format("{%s}", key);
            int index = formatter.indexOf(formatKey);

            if (index != -1)
            {
                formatter.replace(index, index + formatKey.length(), "%s");
                valueList.add(values.get(key));
            }
        }

        return String.format(formatter.toString(), valueList.toArray());
    }



    // -- equals, hashCode, toString

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TranslatableString that = (TranslatableString) o;

        if (pluralText != null ? !pluralText.equals(that.pluralText) : that.pluralText != null)
            return false;
        if (singularText != null ? !singularText.equals(that.singularText) : that.singularText != null)
            return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (singularText != null ? singularText.hashCode() : 0);
        result = 31 * result + (pluralText != null ? pluralText.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return type.toString(this);
    }

}
