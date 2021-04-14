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
package org.apache.isis.testing.fakedata.applib.services;

import java.util.List;

/**
 * @since 2.0 {@index}
 */
public class Lorem extends AbstractRandomValueGenerator {

    com.github.javafaker.Lorem javaFakerLorem;

    Lorem(final FakeDataService fakeDataService) {
        super(fakeDataService);
        javaFakerLorem = fakeDataService.javaFaker().lorem();
    }

    public List<String> words(int num) {
        return javaFakerLorem.words(num);
    }

    public List<String> words() {
        return javaFakerLorem.words();
    }

    public String sentence(int wordCount) {
        return javaFakerLorem.sentence(wordCount);
    }

    public String sentence() {
        return javaFakerLorem.sentence();
    }

    public String paragraph(int sentenceCount) {
        return javaFakerLorem.paragraph(sentenceCount);
    }

    public String paragraph() {
        return javaFakerLorem.paragraph();
    }

    public List<String> paragraphs(int paragraphCount) {
        return javaFakerLorem.paragraphs(paragraphCount);
    }
}
