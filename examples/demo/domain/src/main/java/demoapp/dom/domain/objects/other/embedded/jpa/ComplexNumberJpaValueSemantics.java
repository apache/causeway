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
package demoapp.dom.domain.objects.other.embedded.jpa;

import java.util.stream.Collectors;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;

import lombok.val;

// tag::class[]
public class ComplexNumberJpaValueSemantics
        implements ValueSemanticsProvider<ComplexNumberJpa>{

// end::class[]
// tag::getParser[]
    @Override
    public Parser<ComplexNumberJpa> getParser() {
// end::getParser[]
        // ...
// tag::getParser[]
        return new Parser<ComplexNumberJpa>() {
            @Override
            public ComplexNumberJpa parseTextEntry(Object contextPojo, String entry) {
                return ComplexNumberJpa.parse(entry).orElse(null);
            }
            @Override
            public int typicalLength() {
                return 30;
            }
            @Override
            public String displayTitleOf(ComplexNumberJpa object) {
                return object!=null ? object.title() : "NaN";
            }
            @Override
            public String displayTitleOf(ComplexNumberJpa object, String usingMask) {
                return displayTitleOf(object);
            }
            @Override
            public String parseableTitleOf(ComplexNumberJpa existing) {
                return displayTitleOf(existing);
            }
        };
    }
// end::getParser[]

// tag::getEncoderDecoder[]
    @Override
    public EncoderDecoder<ComplexNumberJpa> getEncoderDecoder() {
// end::getEncoderDecoder[]
        // ...
// tag::getEncoderDecoder[]
        return new EncoderDecoder<ComplexNumberJpa>() {
            @Override
            public String toEncodedString(ComplexNumberJpa cn) {
                if(cn==null) {
                    return null;
                }
                val re = Double.doubleToLongBits(cn.getRe());
                val im = Double.doubleToLongBits(cn.getIm());
                return String.format("%s:%s",
                        Long.toHexString(re), Long.toHexString(im));
            }
            @Override
            public ComplexNumberJpa fromEncodedString(String str) {
                if(_NullSafe.isEmpty(str)) {
                    return null;
                }
                val chunks = _Strings.splitThenStream(str, ":")
                    .limit(2)
                    .collect(Collectors.toList());
                if(chunks.size()<2) {
                    throw new IllegalArgumentException("Invalid format " + str);
                }
                val re = Double.longBitsToDouble(Long.parseLong(chunks.get(0), 16));
                val im = Double.longBitsToDouble(Long.parseLong(chunks.get(1), 16));
                return ComplexNumberJpa.of(re, im);
            }
        };
    }
// end::getEncoderDecoder[]

// tag::getDefaultsProvider[]
    @Override
    public DefaultsProvider<ComplexNumberJpa> getDefaultsProvider() {
// end::getDefaultsProvider[]
        // ...
// tag::getDefaultsProvider[]
        return ()-> ComplexNumberJpa.of(0, 0);
    }
// end::getDefaultsProvider[]
// tag::class[]
}
// end::class[]
