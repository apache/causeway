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
package org.apache.isis.core.metamodel.valuetypes;

import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.Renderer;
import org.apache.isis.applib.adapters.ValueSemanticsAbstract;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;

import lombok.val;

public abstract class ValueSemanticsAdapter<T, D>
extends ValueSemanticsAbstract<T>
implements
    EncoderDecoder<T>,
    Parser<T>,
    Renderer<T> {

    public abstract ValueSemanticsAbstract<D> getDelegate();

    public abstract T fromDelegateValue(D value);
    public abstract D toDelegateValue(T value);

    // -- ENCODER DECODER

    @Override
    public String toEncodedString(final T object) {
        val delegateValue = toDelegateValue(object);
        return delegateEncoderDecoder().toEncodedString(delegateValue);
    }

    @Override
    public T fromEncodedString(final String data) {
        val delegateValue = delegateEncoderDecoder().fromEncodedString(data);
        return fromDelegateValue(delegateValue);
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final T value) {
        val delegateValue = value!=null
                ? toDelegateValue(value)
                : null;
        return delegateRenderer().simpleTextPresentation(context, delegateValue);
    }

    // -- PARSER

    @Override
    public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final T value) {
        val delegateValue = value!=null
                ? toDelegateValue(value)
                : null;
        return delegateParser().parseableTextRepresentation(context, delegateValue);
    }

    @Override
    public T parseTextRepresentation(final ValueSemanticsProvider.Context context, final String text) {
        val delegateValue = delegateParser().parseTextRepresentation(context, text);
        return delegateValue!=null
                ? fromDelegateValue(delegateValue)
                : null;
    }

    @Override
    public int typicalLength() {
        return delegateParser().typicalLength();
    }

    @Override
    public int maxLength() {
        return delegateParser().maxLength();
    }

    // -- HELPER

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Parser<D> delegateParser() {
        return ((Parser)getDelegate());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Renderer<D> delegateRenderer() {
        return ((Renderer)getDelegate());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private EncoderDecoder<D> delegateEncoderDecoder() {
        return ((EncoderDecoder)getDelegate());
    }

}
