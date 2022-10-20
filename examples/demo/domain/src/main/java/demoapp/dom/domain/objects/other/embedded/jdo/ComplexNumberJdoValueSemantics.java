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
package demoapp.dom.domain.objects.other.embedded.jdo;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.applib.value.semantics.ValueSemanticsProvider;
import org.apache.causeway.schema.common.v2.ValueType;

// tag::class[]
@Component
public class ComplexNumberJdoValueSemantics
        extends ValueSemanticsAbstract<ComplexNumberJdo>{

 // end::class[]

    @Override
    public Class<ComplexNumberJdo> getCorrespondingClass() {
        return ComplexNumberJdo.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.COMPOSITE;
    }

 // tag::getRenderer[]
     @Override
     public Renderer<ComplexNumberJdo> getRenderer() {
 // end::getRenderer[]
         // ...
 // tag::getRenderer[]
         return new Renderer<ComplexNumberJdo>() {
             @Override
             public String titlePresentation(final ValueSemanticsProvider.Context context, final ComplexNumberJdo object) {
                 return object!=null ? object.title() : "NaN";
             }
         };
     }
 // end::getRenderer[]
// tag::getParser[]
    @Override
    public Parser<ComplexNumberJdo> getParser() {
// end::getParser[]
        // ...
// tag::getParser[]
        return new Parser<ComplexNumberJdo>() {
            @Override
            public ComplexNumberJdo parseTextRepresentation(final ValueSemanticsProvider.Context context, final String entry) {
                return ComplexNumberJdo.parse(entry).orElse(null);
            }
            @Override
            public int typicalLength() {
                return 30;
            }
            @Override
            public String parseableTextRepresentation(final ValueSemanticsProvider.Context context, final ComplexNumberJdo existing) {
                return existing!=null ? existing.title() : null;
            }
        };
    }
// end::getParser[]

// tag::getEncoderDecoder[]
    @Override
    public ValueDecomposition decompose(final ComplexNumberJdo value) {
        return CommonDtoUtils.typedTupleBuilder(value)
                .addFundamentalType(ValueType.DOUBLE, "re", ComplexNumberJdo::getRe)
                .addFundamentalType(ValueType.DOUBLE, "im", ComplexNumberJdo::getIm)
                .buildAsDecomposition();
    }

    @Override
    public ComplexNumberJdo compose(final ValueDecomposition decomposition) {
        return decomposition.right()
                .map(CommonDtoUtils::typedTupleAsMap)
                .map(map->ComplexNumberJdo.of(
                        (Double)map.get("re"),
                        (Double)map.get("im")))
                .orElse(null);
    }
// end::getEncoderDecoder[]

// tag::getDefaultsProvider[]
    @Override
    public DefaultsProvider<ComplexNumberJdo> getDefaultsProvider() {
// end::getDefaultsProvider[]
        // ...
// tag::getDefaultsProvider[]
        return ()-> ComplexNumberJdo.of(0, 0);
    }
// end::getDefaultsProvider[]
// tag::class[]
}
// end::class[]
