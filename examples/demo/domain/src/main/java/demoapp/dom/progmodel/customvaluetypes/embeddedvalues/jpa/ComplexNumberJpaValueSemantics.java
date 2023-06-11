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
package demoapp.dom.progmodel.customvaluetypes.embeddedvalues.jpa;

import javax.inject.Named;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.semantics.Parser;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.schema.common.v2.ValueType;

import demoapp.dom.progmodel.customvaluetypes.embeddedvalues.ComplexNumber;

@Profile("demo-jpa")
//tag::class[]
@Named("demo.ComplexNumberJpaValueSemantics")
@Component
public class ComplexNumberJpaValueSemantics
        extends ValueSemanticsAbstract<ComplexNumberJpa> {
    // ...
//end::class[]

    @Override
    public Class<ComplexNumberJpa> getCorrespondingClass() {
        return ComplexNumberJpa.class;
    }

//tag::compose[]
    @Override
    public ValueType getSchemaValueType() {                                    // <.>
        /* This type can be easily converted to string and back.
         * So for convenience, we use its string representation (instead of its decomposed representation).
         * The decompose/compose methods below must honor the chosen type here. */
        return ValueType.STRING;
    }

    @Override
    public ValueDecomposition decompose(final ComplexNumberJpa value) {        // <.>
        return decomposeAsString(value, ComplexNumberJpa::asString, ()->null);
    }

    @Override
    public ComplexNumberJpa compose(final ValueDecomposition decomposition) {  // <.>
        return composeFromString(decomposition, string->ComplexNumber.parse(string, ComplexNumberJpa::of), ()->null);
    }
//end::compose[]

//tag::getRenderer[]
    @Override
    public Renderer<ComplexNumberJpa> getRenderer() {
        return (context, object) -> title(object, "NaN");
    }

    private static String title(final ComplexNumber complexNumber, final String fallbackIfNull) {
        return complexNumber == null
                ? fallbackIfNull
                : complexNumber.asString();
    }
//end::getRenderer[]

//tag::getParser[]
    @Override
    public Parser<ComplexNumberJpa> getParser() {
        return new Parser<ComplexNumberJpa>() {
            @Override
            public String parseableTextRepresentation(final Context context, final ComplexNumberJpa value) {
                return title(value, "NaN");
            }

            @Override
            public ComplexNumberJpa parseTextRepresentation(final Context context, final String complexNumberString) {
                if(!org.springframework.util.StringUtils.hasLength(complexNumberString)
                        || complexNumberString.contains("NaN")) {
                    return null;
                }
                return ComplexNumber.parse(complexNumberString, ComplexNumberJpa::of);
            }

            @Override
            public int typicalLength() {
                return 20;
            }
        };
    }
//end::getParser[]

//tag::class[]
}
//end::class[]
