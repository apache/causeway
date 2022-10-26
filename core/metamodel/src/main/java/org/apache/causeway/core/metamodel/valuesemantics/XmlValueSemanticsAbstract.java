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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.util.Objects;

import org.apache.causeway.applib.value.semantics.OrderRelation;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.schema.common.v2.ValueType;

import lombok.NonNull;
import lombok.val;

public abstract class XmlValueSemanticsAbstract<T>
extends ValueSemanticsAbstract<T>
implements
    OrderRelation<T, Void>,
    Renderer<T> {

    @Override
    public final ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- ORDER RELATION

    @Override
    public final Void epsilon() {
        return null; // not used
    }

    @Override
    public final int compare(final T a, final T b, final Void epsilon) {
        if(a==null
                || b==null) {
            return Objects.equals(a, b)
                    ? 0
                    : a==null
                        ? -1
                        : 1;
        }
        val _a = toXml(a);
        val _b = toXml(b);
        return _a.compareTo(_b);
    }

    @Override
    public final boolean equals(final T a, final T b, final Void epsilon) {
        if(a==null
                || b==null) {
            return Objects.equals(a, b);
        }
        val _a = toXml(a);
        val _b = toXml(b);
        return _a.equals(_b);
    }

    // -- COMPOSER

    @Override
    public ValueDecomposition decompose(final T value) {
        return decomposeAsString(value, this::toXml, ()->null);
    }

    @Override
    public T compose(final ValueDecomposition decomposition) {
        return composeFromString(decomposition, this::fromXml, ()->null);
    }

    // -- RENDERER

    @Override
    public String titlePresentation(final Context context, final T value) {
        return renderTitle(value, v->String.format("XML[length=%d]", toXml(v).length()));
    }

    @Override
    public String htmlPresentation(final Context context, final T value) {
        return renderHtml(value, v->renderXml(context, toXml(v)));
    }

    protected String renderXml(final @NonNull Context context, final @NonNull String xml) {
        return xml;
    }

    // -- TO AND FROM XML

    protected abstract String toXml(final T dto);
    protected abstract T fromXml(final String xml);

}
