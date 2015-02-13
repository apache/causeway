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
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.Lists;

public final class TranslatableString {

    //region > tr, trn (factory methods)

    /**
     * A translatable string supporting both singular and plural forms.
     *
     * @param pattern - pattern for all (singular and plural) form, with <code>${xxx}</code> placeholders
     * @param paramArgs - parameter/argument pairs (string and object, string and object, ...)
     */
    public static TranslatableString tr(
            final String pattern,
            final Object... paramArgs) {
        return new TranslatableString(Type.TR, pattern, null, 1, asMap(paramArgs));
    }

    /**
     * A translatable string supporting both singular and plural forms.
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
    //endregion


    private final Type type;
    private final String singularPattern;
    private final String pluralPattern;
    private final int number;
    private final Map<String, Object> argumentsByParameterName;

    private enum Type {
        /**
         * No plurals
         */
        TR {
            @Override
            public String toString(final TranslatableString trString) {
                return "tr: " + trString.singularPattern;
            }
        },
        /**
         * With plurals
         */
        TRN {
            @Override
            public String toString(final TranslatableString trString) {
                return "trn: " + trString.pluralPattern;
            }
        };

        public abstract String toString(final TranslatableString trString);
    }

    private TranslatableString(
            final Type type,
            final String singularPattern,
            final String pluralPattern,
            final int number,
            final Map<String, Object> argumentsByParameterName) {

        this.type = type;
        this.singularPattern = singularPattern;
        this.pluralPattern = pluralPattern;
        this.number = number;
        this.argumentsByParameterName = argumentsByParameterName;
    }

    /**
     * The pattern (or the singular pattern if {@link #isPluralForm()} is <code>true</code>).
     */
    String getSingularPattern() {
        return singularPattern;
    }

    /**
     * The plural pattern (but will be <code>null</code> if {@link #isPluralForm()} is <code>false</code>).
     */
    String getPluralPattern() {
        return pluralPattern;
    }

    boolean isPluralForm() {
        return type == Type.TRN;
    }

    /**
     * The arguments; excluded from {@link #equals(Object)} comparison.
     */
    Map<String, Object> getArgumentsByParameterName() {
        return argumentsByParameterName;
    }

    //region > equals, hashCode

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final TranslatableString that = (TranslatableString) o;

        if (pluralPattern != null ? !pluralPattern.equals(that.pluralPattern) : that.pluralPattern != null)
            return false;
        if (singularPattern != null ? !singularPattern.equals(that.singularPattern) : that.singularPattern != null)
            return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (singularPattern != null ? singularPattern.hashCode() : 0);
        result = 31 * result + (pluralPattern != null ? pluralPattern.hashCode() : 0);
        return result;
    }

    //endregion


    //region > translate

    public String translate(final TranslationService translationService, final String context, final Locale locale) {
        final String translatedText = translationService.translate(context, getText(), locale);
        return translated(translatedText);
    }

    /**
     * The text to be translated; depends on whether {@link #isPluralForm()} and whether to be translated.
     *
     * <p>
     *     May or may not hold placeholders.
     * </p>
     */
    String getText() {
        return !isPluralForm() || number == 1 ? getSingularPattern() : getPluralPattern();
    }

    String translated(final String translatedText) {
        return format(translatedText, argumentsByParameterName);
    }

    static String format(String format, Map<String, Object> values)
    {
        StringBuilder formatter = new StringBuilder(format);
        List<Object> valueList = Lists.newArrayList();

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

    //endregion


    @Override
    public String toString() {
        return type.toString(this);
    }
}
