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
package demoapp.dom.types.tuple;

import java.util.stream.Collectors;

import org.apache.isis.applib.adapters.DefaultsProvider;
import org.apache.isis.applib.adapters.EncoderDecoder;
import org.apache.isis.applib.adapters.Parser;
import org.apache.isis.applib.adapters.ValueSemanticsProvider;
import org.apache.isis.core.commons.internal.base._NullSafe;
import org.apache.isis.core.commons.internal.base._Strings;

import lombok.val;

public class ComplexNumberValueSemantics implements ValueSemanticsProvider<ComplexNumber>{
    
    @Override
    public Parser<ComplexNumber> getParser() {
        return null;
       
//        return new Parser<ComplexNumber>() {
//
//            @Override
//            public ComplexNumber parseTextEntry(Object contextPojo, String entry) {
//                throw _Exceptions.unsupportedOperation();
//            }
//
//            @Override
//            public int typicalLength() {
//                return 150;
//            }
//
//            @Override
//            public String displayTitleOf(ComplexNumber object) {
//                return object!=null ? object.toString() : "NaN";
//            }
//
//            @Override
//            public String displayTitleOf(ComplexNumber object, String usingMask) {
//                throw _Exceptions.unsupportedOperation();
//            }
//
//            @Override
//            public String parseableTitleOf(ComplexNumber existing) {
//                throw _Exceptions.unsupportedOperation();
//            }
//        }; 
    }

   
    
    @Override
    public EncoderDecoder<ComplexNumber> getEncoderDecoder() {

        return new EncoderDecoder<ComplexNumber>() {

            @Override
            public String toEncodedString(ComplexNumber toEncode) {
                
                if(toEncode==null) {
                    return null;
                }
                
                val re = Double.doubleToLongBits(toEncode.getRe());
                val im = Double.doubleToLongBits(toEncode.getIm());
                
                return String.format("%s:%s", Long.toHexString(re), Long.toHexString(im)); 
            }

            @Override
            public ComplexNumber fromEncodedString(String encodedString) {
                
                if(_NullSafe.isEmpty(encodedString)) {
                    return null;
                }
                
                val chunks = _Strings.splitThenStream(encodedString, ":")
                .limit(2)
                .collect(Collectors.toList());
                
                if(chunks.size()<2) {
                    throw new IllegalArgumentException("cannot parse " + encodedString);
                }
                
                val re = Double.longBitsToDouble(Long.parseLong(chunks.get(0), 16));
                val im = Double.longBitsToDouble(Long.parseLong(chunks.get(1), 16));
                
                return ComplexNumber.of(re, im);
            }
        };
    }

    @Override
    public DefaultsProvider<ComplexNumber> getDefaultsProvider() {
        return ()->ComplexNumber.of(0, 0);
    }
    

}
