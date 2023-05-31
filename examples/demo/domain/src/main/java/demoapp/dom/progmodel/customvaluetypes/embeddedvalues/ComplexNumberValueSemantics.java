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
package demoapp.dom.progmodel.customvaluetypes.embeddedvalues;

import javax.inject.Named;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.schema.common.v2.ValueType;

import demoapp.dom.progmodel.customvaluetypes.embeddedvalues.jpa.ComplexNumberJpa;

//tag::class[]
@Named("demo.embeddedvalues.ComplexNumberValueSemantics")
@Component
public class ComplexNumberValueSemantics
        extends ValueSemanticsAbstract<ComplexNumber> {
    // ...
//end::class[]

    @Override
    public Class<ComplexNumber> getCorrespondingClass() {
        return ComplexNumber.class;
    }

    @Override
    public ValueType getSchemaValueType() {
        return ValueType.COMPOSITE;
    }

//tag::compose[]
    @Override
    public ValueDecomposition decompose(final ComplexNumber value) {
        return CommonDtoUtils.typedTupleBuilder(value)
                .addFundamentalType(ValueType.DOUBLE, "re", ComplexNumber::getRe)
                .addFundamentalType(ValueType.DOUBLE, "im", ComplexNumber::getIm)
                .buildAsDecomposition();
    }

    @Override
    public ComplexNumberJpa compose(final ValueDecomposition decomposition) {
        return decomposition.right()
                .map(CommonDtoUtils::typedTupleAsMap)
                .map(map->ComplexNumberJpa.of(
                        (Double)map.get("re"),
                        (Double)map.get("im")))
                .orElse(null);
    }
//end::compose[]

//tag::getRenderer[]
    @Override
    public Renderer<ComplexNumber> getRenderer() {
        return (context, object) -> title(object, "NaN");
    }

    private static String title(final ComplexNumber complexNumber, final String fallbackIfNull) {
        if (complexNumber == null) return fallbackIfNull;
        return complexNumber.getRe() +
                (complexNumber.getIm() >= 0
                        ? (" + " +  complexNumber.getIm())
                        : (" - " + (-complexNumber.getIm())))
                + "i";
    }
//end::getRenderer[]

//tag::class[]
}
//end::class[]
