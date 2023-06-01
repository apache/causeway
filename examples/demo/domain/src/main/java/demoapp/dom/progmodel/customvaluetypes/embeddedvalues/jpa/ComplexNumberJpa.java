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

import org.springframework.context.annotation.Profile;

import org.apache.causeway.applib.annotation.Value;

import demoapp.dom.progmodel.customvaluetypes.embeddedvalues.ComplexNumber;
import lombok.AccessLevel;

@Profile("demo-jpa")
//tag::class[]
@javax.persistence.Embeddable                                       // <.>
@Value                                                              // <.>
@lombok.Getter                                                      // <.>
@lombok.Setter(AccessLevel.PRIVATE)                                 // <.>
@lombok.AllArgsConstructor(staticName = "of")
@lombok.NoArgsConstructor                                           // <4>
public class ComplexNumberJpa
        implements ComplexNumber {

    @javax.persistence.Column(nullable = false)
    private double re;                                              // <.>

    @javax.persistence.Column(nullable = false)
    private double im;                                              // <5>
}
//end::class[]
