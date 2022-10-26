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
package org.apache.causeway.testing.fakedata.applib.services;

import com.github.javafaker.service.FakeValuesService;

/**
 * Returns strings representing either credit card numbers or types (visa, amex and so on).
 *
 * @since 2.0 {@index}
 */
public class CreditCards extends AbstractRandomValueGenerator {

    final com.github.javafaker.Business javaFakerBusiness;

    CreditCards(final FakeDataService fakeDataService, final FakeValuesService fakeValuesService) {
        super(fakeDataService);
        javaFakerBusiness = fakeDataService.javaFaker().business();
    }

    public String number() {
        return fake.fakeValuesService.fetchString("business.credit_card_numbers");
    }

    public String type() {
        return fake.fakeValuesService.fetchString("business.credit_card_types");
    }

}
