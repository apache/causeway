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
package org.apache.isis.valuetypes.asciidoc.metamodel.semantics;

import org.apache.isis.applib.value.semantics.Renderer;
import org.apache.isis.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.isis.applib.value.semantics.ValueSemanticsProvider;
import org.apache.isis.schema.common.v2.ValueType;
import org.apache.isis.valuetypes.asciidoc.applib.value.AsciiDoc;

import lombok.NonNull;
import lombok.val;

/**
 *  Provides a {@link Renderer} that generates syntax highlighted XML.
 *  @implNote using ascii-doctor under the hoods
 */
abstract class XmlValueSemanticsAbstract<T>
extends ValueSemanticsAbstract<T>
implements
    Renderer<T> {

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.STRING;
    }

    // -- RENDERER

    @Override
    public String simpleTextPresentation(final ValueSemanticsProvider.Context context, final T value) {
        return render(value, xmlContainer->presentationValue(context, value).asHtml());
    }

    private AsciiDoc presentationValue(final Context context, final T value) {
        return asAdoc(asXml(context, value));
    }

    protected abstract String asXml(Context context, @NonNull T value);

    private AsciiDoc asAdoc(final String xml) {
        val adoc = "[source,xml]\n----\n" + xml + "\n----";
        return AsciiDoc.valueOf(adoc);
    }

}
