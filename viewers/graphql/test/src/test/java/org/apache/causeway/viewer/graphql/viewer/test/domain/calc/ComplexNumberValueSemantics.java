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
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR  CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.viewer.graphql.viewer.test.domain.calc;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.value.semantics.ValueSemanticsBasedOnIdStringifierEntityAgnostic;

@Component
public class ComplexNumberValueSemantics
        extends ValueSemanticsBasedOnIdStringifierEntityAgnostic<ComplexNumber> {

    public ComplexNumberValueSemantics() {
        super(ComplexNumber.class);
    }

    @Override
    public String enstring(final ComplexNumber value) {
        return value.canonical();
    }

    @Override
    public ComplexNumber destring(final String stringified) {
        return ComplexNumber.parse(stringified);
    }
}
