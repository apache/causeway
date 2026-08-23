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
package demoapp.dom.progmodel.customvaluetypes.compositevalues;

import jakarta.inject.Named;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.util.schema.CommonDtoUtils;
import org.apache.causeway.applib.value.semantics.DefaultsProvider;
import org.apache.causeway.applib.value.semantics.Renderer;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsAbstract;
import org.apache.causeway.schema.common.v2.ValueType;

//tag::class[]
@Named("demo.ComplexNumberValueSemantics")
@Component
@Import({
        ComplexNumber_default.class                         // <.>
})
public class ComplexNumberValueSemantics
        extends ValueSemanticsAbstract<ComplexNumber> {     // <.>
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
    public ComplexNumber compose(final ValueDecomposition decomposition) {
        return decomposition.compositeAsOptional()
                .map(CommonDtoUtils::typedTupleAsMap)
                .map(map-> ComplexNumber.of(
                        (Double)map.get("re"),
                        (Double)map.get("im")))
                .orElse(null);
    }
//end::compose[]

//tag::getDefaultsProvider[]
    @Override
    public DefaultsProvider<ComplexNumber> getDefaultsProvider() {
        return ()-> ComplexNumber.of(0, 0);
    }
//end::getDefaultsProvider[]

//tag::getRenderer[]
    @Override
    public Renderer<ComplexNumber> getRenderer() {
        return new Renderer<>() {
            @Override
            public String titlePresentation(Context context, ComplexNumber object) {
                if (object == null) return "NaN";
                return object.getRe() +
                        (object.getIm() >= 0
                                ? (" + " +  object.getIm())
                                : (" - " + (-object.getIm())))
                        + "i";
            }
        };
    }
//end::getRenderer[]

//tag::class[]
}
//end::class[]
