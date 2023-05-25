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


//tag::class[]
@org.apache.causeway.applib.annotation.Value                // <.>
@lombok.Value                                               // <.>
@lombok.AllArgsConstructor(staticName = "of")
public class ComplexNumber {

    double re;                                              // <.>
    double im;                                              // <3>

    public ComplexNumber add(ComplexNumber other) {         // <.>
        return ComplexNumber.of(re + other.re, im + other.im);
    }
    public ComplexNumber subtract(ComplexNumber other) {    // <4>
        return ComplexNumber.of(re - other.re, im - other.im);
    }
}
//end::class[]
