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

/**
 * Returns random ISBN codes, either 10 or 13 character forms.
 *
 * @since 2.0 {@index}
 */
public class Books extends AbstractRandomValueGenerator {

    com.github.javafaker.Code javaFakerCode;

    Books(final FakeDataService fakeDataService) {
        super(fakeDataService);
        javaFakerCode = fakeDataService.javaFaker().code();
    }

    public String isbn10() {
        return javaFakerCode.isbn10();
    }

    public String isbn13() {
        return javaFakerCode.isbn13();
    }
}
