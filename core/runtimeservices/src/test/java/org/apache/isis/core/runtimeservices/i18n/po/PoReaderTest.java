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
package org.apache.isis.core.runtimeservices.i18n.po;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.i18n.LanguageProvider;
import org.apache.isis.applib.services.i18n.TranslationContext;
import org.apache.isis.applib.services.i18n.TranslationsResolver;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PoReaderTest {

    @Rule public JUnitRuleMockery2 context = JUnitRuleMockery2
            .createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock TranslationServicePo mockTranslationServicePo;
    @Mock TranslationsResolver mockTranslationsResolver;
    @Mock LanguageProvider mockLanguageProvider;

    PoReader poReader;

    @Before
    public void setUp() throws Exception {

        context.checking(new Expectations() {{
            allowing(mockTranslationServicePo).getLanguageProvider();
            will(returnValue(mockLanguageProvider));

            allowing(mockTranslationServicePo).getTranslationsResolver();
            will(returnValue(Can.ofSingleton(mockTranslationsResolver)));

            allowing(mockLanguageProvider).getPreferredLanguage();
            will(returnValue(Optional.of(Locale.UK)));
        }});

    }

    @Test
    public void properMockeryOfNonPublicMethods() {
        //[ahuber] with update of byte-buddy 1.8.0 -> 1.9.2, Apache Isis runs on JDK 11+,
        // we explicitly test proper mockery of non-public methods here ...
        Assert.assertNotNull(mockTranslationServicePo.getLanguageProvider());
        Assert.assertNotNull(mockTranslationServicePo.getTranslationsResolver());
        Assert.assertNotNull(mockLanguageProvider.getPreferredLanguage());
    }

    public static class Translate extends PoReaderTest {

        @Test
        public void singleContext() throws Exception {

            // given
            final TranslationContext context = TranslationContext.ofName(
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#object()");
            final String msgId = "Work of art";
            final String msgStr = "Objet d'art";

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    final List<String> lines = _Lists.newArrayList();
                    lines.add(String.format("#: %s", context.getName()));
                    lines.add(String.format("msgid \"%s\"", msgId));
                    lines.add(String.format("msgstr \"%s\"", msgStr));
                    return lines;
                }
            };

            // when
            final String translated = poReader.translate(context, msgId);

            // then
            assertThat(translated, is(equalTo(msgStr)));
        }

        @Test
        public void multipleContext() throws Exception {

            // given
            final TranslationContext context1 = TranslationContext.ofName(
                    "fixture.simple.SimpleObjectsFixturesService#runFixtureScript(org.apache.isis.applib.fixturescripts.FixtureScript,java.lang.String)");
            final TranslationContext context2 = TranslationContext.ofName(
                    "org.apache.isis.applib.fixturescripts.FixtureScripts#runFixtureScript(org.apache.isis.applib.fixturescripts.FixtureScript,java.lang.String)");
            final String msgId = "Parameters";
            final String msgStr = "Paramètres";

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    final List<String> lines = _Lists.newArrayList();
                    lines.add(String.format("#: %s", context1.getName()));
                    lines.add(String.format("#: %s", context2.getName()));
                    lines.add(String.format("msgid \"%s\"", msgId));
                    lines.add(String.format("msgstr \"%s\"", msgStr));
                    return lines;
                }
            };
            // when
            final String translated = poReader.translate(context1, msgId);

            // then
            assertThat(translated, is(equalTo(msgStr)));

            // when
            final String translated2 = poReader.translate(context2, msgId);

            // then
            assertThat(translated2, is(equalTo(msgStr)));
        }

        @Test
        public void multipleBlocks() throws Exception {

            // given
            final TranslationContext context1 = TranslationContext.ofName(
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#object()");
            final String msgid1 = "Work of art";
            final String msgstr1 = "Objet d'art";

            final TranslationContext context2 = TranslationContext.ofName(
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#lookup()");
            final String msgid2 = "Lookup";
            final String msgstr2 = "Look up";

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    final List<String> lines = _Lists.newArrayList();
                    lines.add(String.format("#: %s", context1.getName()));
                    lines.add(String.format("msgid \"%s\"", msgid1));
                    lines.add(String.format("msgstr \"%s\"", msgstr1));

                    lines.add(String.format(""));
                    lines.add(String.format("# "));

                    lines.add(String.format("#: %s", context2.getName()));
                    lines.add(String.format("msgid \"%s\"", msgid2));
                    lines.add(String.format("msgstr \"%s\"", msgstr2));

                    lines.add(String.format(""));
                    return lines;
                }
            };

            // when
            final String translated1 = poReader.translate(context1, msgid1);

            // then
            assertThat(translated1, is(equalTo(msgstr1)));

            // when
            final String translated2 = poReader.translate(context2, msgid2);

            // then
            assertThat(translated2, is(equalTo(msgstr2)));
        }

        @Test
        public void withPlurals() throws Exception {

            // given
            final TranslationContext context = TranslationContext.ofName(
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#object()");
            final String msgid = "Work of art";
            final String msgid_plural = "Works of art";
            final String msgstr$0 = "Œuvre d'art";
            final String msgstr$1 = "Les œuvres d'art";

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    final List<String> lines = _Lists.newArrayList();
                    lines.add(String.format("#: %s", context.getName()));
                    lines.add(String.format("msgid \"%s\"", msgid));
                    lines.add(String.format("msgid_plural \"%s\"", msgid_plural));
                    lines.add(String.format("msgstr[0] \"%s\"", msgstr$0));
                    lines.add(String.format("msgstr[1] \"%s\"", msgstr$1));
                    return lines;
                }
            };

            // when
            final String translated1 = poReader.translate(context, msgid);

            // then
            assertThat(translated1, is(equalTo(msgstr$0)));

            // when
            final String translated2 = poReader.translate(context, msgid_plural);

            // then
            assertThat(translated2, is(equalTo(msgstr$1)));
        }



        @Test
        public void noTranslation() throws Exception {

            // given

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    return _Lists.newArrayList();
                }
            };

            TranslationContext context = TranslationContext.ofName("someContext");

            // when
            final String translated = poReader.translate(context, "Something to translate");

            // then
            assertThat(translated, is(equalTo("Something to translate")));
        }
    }

}