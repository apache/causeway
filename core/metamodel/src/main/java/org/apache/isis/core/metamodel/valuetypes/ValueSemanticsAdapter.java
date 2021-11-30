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

import org.apache.isis.applib.value.semantics.Converter;
import org.apache.isis.applib.value.semantics.EncoderDecoder;
import org.apache.isis.applib.value.semantics.OrderRelation;
import org.apache.isis.applib.value.semantics.Parser;
import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.schema.common.v2.ValueType;

import lombok.val;

public abstract class ValueSemanticsAdapter<T, D, E>
extends ValueSemanticsAbstract<T>
implements
    OrderRelation<T, E>,
    EncoderDecoder<T>,
    Parser<T>,
    Renderer<T>,
    Converter<T, D>{

    public abstract ValueSemanticsAbstract<D> getDelegate();

    /**
     * By design, adapters always share their <i>SchemaValueType</i> with their delegate.
     * @see ValueSemanticsProvider#getSchemaValueType()
     */
    @Override
    public final ValueType getSchemaValueType() {
        return getDelegate().getSchemaValueType();
    }

    // -- ORDER RELATION

    @Override
    public final E epsilon() {
        return delegateOrderRelation().epsilon();
    }

    @Override
    public final int compare(final T a, final T b, final E epsilon) {
        return delegateOrderRelation()
                .compare(toDelegateValue(a), toDelegateValue(b), epsilon);
    }

    @Override
    public final boolean equals(final T a, final T b, final E epsilon) {
        return delegateOrderRelation()
                .equals(toDelegateValue(a), toDelegateValue(b), epsilon);
    }

    // -- CONVERTER


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

    @Override
    public String getPattern(final Context context) {
        return delegateParser().getPattern(context);
    }

    // -- HELPER

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private OrderRelation<D, E> delegateOrderRelation() {
        return ((OrderRelation)getDelegate());
    }

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
