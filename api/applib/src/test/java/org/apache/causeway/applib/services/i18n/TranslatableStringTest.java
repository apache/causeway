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
package org.apache.causeway.applib.services.i18n;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslatableStringTest {

    public static class GetText extends TranslatableStringTest {

        @Test
        public void singularForm() throws Exception {
            final TranslatableString ts = TranslatableString.tr("No, you can't do that!");

            assertThat(ts.pattern(), is("No, you can't do that!"));
        }

        @Test
        public void pluralFormOne() throws Exception {
            final TranslatableString ts = TranslatableString.trn(
                    "You can't do that because there is a dependent object",
                    "You can't do that because there are dependent objects", 1);

            assertThat(ts.pattern(), is("You can't do that because there is a dependent object"));
        }

        @Test
        public void pluralFormTwo() throws Exception {
            final TranslatableString ts = TranslatableString.trn(
                    "You can't do that because there is a dependent object",
                    "You can't do that because there are dependent objects", 2);

            assertThat(ts.pattern(), is("You can't do that because there are dependent objects"));
        }

    }

    public static class Identity extends TranslatableStringTest {

        final TranslationService tsIdentity = TranslationService.identity();

        @Test
        public void singularForm() throws Exception {
            final TranslatableString ts = TranslatableString.tr(
                    "My name is {lastName}, {firstName} {lastName}.",
                    "lastName", "Bond", "firstName", "James");

            assertEquals(
                    "My name is Bond, James Bond.",
                    ts.translate(tsIdentity, TranslationContext.named("someContext")));
        }

        @Test
        public void pluralFormOne() throws Exception {
            final TranslatableString ts = TranslatableString.trn(
                    "My name is {lastName}, {firstName} {lastName}.",
                    "My name is {firstName} {lastName}.",
                    1,
                    "lastName", "Bond", "firstName", "James");

            assertThat(ts.translated("Ich heisse {lastName}, {firstName} {lastName}."),
                    is("Ich heisse Bond, James Bond."));
        }
    }

    public static class Translated extends TranslatableStringTest {

        @Test
        public void singularForm() throws Exception {
            final TranslatableString ts = TranslatableString.tr(
                    "My name is {lastName}, {firstName} {lastName}.",
                    "lastName", "Bond", "firstName", "James");

            assertThat(ts.translated("Ich heisse {lastName}, {firstName} {lastName}."),
                    is("Ich heisse Bond, James Bond."));
        }

        @Test
        public void pluralFormOne() throws Exception {
            final TranslatableString ts = TranslatableString.trn(
                    "My name is {lastName}, {firstName} {lastName}.",
                    "My name is {firstName} {lastName}.",
                    1,
                    "lastName", "Bond", "firstName", "James");

            assertThat(ts.translated("Ich heisse {lastName}, {firstName} {lastName}."),
                    is("Ich heisse Bond, James Bond."));
        }
    }

    @ExtendWith(MockitoExtension.class)
    public static class Translate extends TranslatableStringTest {

        @Mock TranslationService mockTranslationService;

        @Test
        public void singularForm() throws Exception {
            // given
            final String simpleText = "text to translate";
            final TranslationContext someContext = TranslationContext.named("someContext");
            final String translation = "the translation";

            final TranslatableString ts = TranslatableString.tr(simpleText);

            // expect
            Mockito.when(mockTranslationService.translate(someContext, simpleText))
            .thenReturn(translation);

            // when
            assertThat(ts.translate(mockTranslationService, someContext), is(translation));
        }

        @Test
        public void pluralFormOne() throws Exception {

            // given
            final String singularText = "singular text to translate";
            final String pluralText = "plural text to translate";
            final TranslationContext someContext = TranslationContext.named("someContext");
            final String translation = "the translation";

            final TranslatableString ts = TranslatableString.trn(singularText, pluralText, 1);

            // expect
            Mockito.when(mockTranslationService.translate(someContext, singularText, pluralText, 1))
            .thenReturn(translation);

            // when
            assertThat(ts.translate(mockTranslationService, someContext), is(translation));
        }

        @Test
        public void pluralFormTwo() throws Exception {

            // given
            final String singularText = "singular text to translate";
            final String pluralText = "plural text to translate";
            final TranslationContext someContext = TranslationContext.named("someContext");
            final String translation = "the translation";
            final int number = 2; // != 1

            final TranslatableString ts = TranslatableString.trn(singularText, pluralText, number);

            // expect
            Mockito.when(mockTranslationService.translate(someContext, singularText, pluralText, number))
            .thenReturn(translation);

            // when
            assertThat(ts.translate(mockTranslationService, someContext), is(translation));
        }
    }

}