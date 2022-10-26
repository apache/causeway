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
package org.apache.causeway.core.metamodel.valuetypes;

import org.apache.causeway.applib.value.semantics.Converter;
import org.apache.causeway.applib.value.semantics.OrderRelation;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.val;

/**
 * @param <T> value-type
 * @param <D> delegate value-type
 * @param <E> order-relation type for measure of distance
 */
public abstract class ValueSemanticsAdapter<T, D, E>
extends ValueSemanticsAbstract<T>
implements
    OrderRelation<T, E>,
    Parser<T>,
    Renderer<T>,
    Converter<T, D> {

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

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final T value) {
        val delegateValue = toDelegateValue(value);
        return getDelegate().decompose(delegateValue);
    }

    @Override
    public T compose(final ValueDecomposition decomposition) {
        val delegateValue = getDelegate().compose(decomposition);
        return fromDelegateValue(delegateValue);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final ValueSemanticsProvider.Context context, final T value) {
        val delegateValue = value!=null
                ? toDelegateValue(value)
                : null;
        return delegateRenderer().titlePresentation(context, delegateValue);
    }

    @Override
    public String htmlPresentation(final ValueSemanticsProvider.Context context, final T value) {
        val delegateValue = value!=null
                ? toDelegateValue(value)
                : null;
        return delegateRenderer().htmlPresentation(context, delegateValue);
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

}
