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
package org.apache.isis.applib.adapters;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.adapters.Parser.Context;
import org.apache.isis.applib.services.iactnlayer.InteractionContext;

/**
 * @since 1.x {@index}
 */
public abstract class AbstractValueSemanticsProvider<T>
implements ValueSemanticsProvider<T> {

    @SuppressWarnings("unchecked")
    @Override
    public EncoderDecoder<T> getEncoderDecoder() {
        return (EncoderDecoder<T>) (this instanceof EncoderDecoder ? this : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Parser<T> getParser() {
        return (Parser<T>) (this instanceof Parser ? this : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DefaultsProvider<T> getDefaultsProvider() {
        return (DefaultsProvider<T>) (this instanceof DefaultsProvider ? this : null);
    }

    /**
     * @param context - nullable in support of JUnit testing
     * @return {@link Locale} from given context or else system's default
     */
    protected Locale getLocale(final @Nullable Context context) {
        return Optional.ofNullable(context)
        .map(Context::getInteractionContext)
        .map(InteractionContext::getLocale)
        .orElseGet(Locale::getDefault);
    }

    /**
     * @param context - nullable in support of JUnit testing
     * @return {@link NumberFormat} the default from from given context's locale
     * or else system's default locale
     */
    protected DecimalFormat getNumberFormat(final @Nullable Context context) {
        return (DecimalFormat)NumberFormat.getNumberInstance(getLocale(context));
    }

    @Deprecated
    protected final URL doParse(final Context context, final String entry) {
        // TODO Auto-generated method stub
        return null;
    }

    @Deprecated
    protected final String titleString(final Object object) {
        // TODO Auto-generated method stub
        return null;
    }

}
