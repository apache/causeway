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
package org.apache.isis.core.runtime.services.i18n.po;

import java.util.List;
import java.util.Locale;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.applib.services.i18n.LocaleProvider;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PoReaderTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    TranslationServicePo mockTranslationServicePo;

    @Mock
    LocaleProvider mockLocaleProvider;

    PoReader poReader;

    @Before
    public void setUp() throws Exception {
        context.checking(new Expectations() {{
            allowing(mockTranslationServicePo).getLocaleProvider();
            will(returnValue(mockLocaleProvider));

            allowing(mockLocaleProvider).getLocale();
            will(returnValue(Locale.UK));
        }});
    }

    public static class Translate extends PoReaderTest {

        @Test
        public void singleContext() throws Exception {

            // given
            final String context =
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#object()";
            final String msgId = "Work of art";
            final String msgStr = "Objet d'art";

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    final List<String> lines = Lists.newArrayList();
                    lines.add(String.format("#: %s", context));
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
            final String context1 =
                    "fixture.simple.SimpleObjectsFixturesService#runFixtureScript(org.apache.isis.applib.fixturescripts.FixtureScript,java.lang.String)";
            final String context2 =
                    "org.apache.isis.applib.fixturescripts.FixtureScripts#runFixtureScript(org.apache.isis.applib.fixturescripts.FixtureScript,java.lang.String)";
            final String msgId = "Parameters";
            final String msgStr = "Paramètres";

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    final List<String> lines = Lists.newArrayList();
                    lines.add(String.format("#: %s", context1));
                    lines.add(String.format("#: %s", context2));
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
            final String context1 =
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#object()";
            final String msgid1 = "Work of art";
            final String msgstr1 = "Objet d'art";

            final String context2 =
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#lookup()";
            final String msgid2 = "Lookup";
            final String msgstr2 = "Look up";

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    final List<String> lines = Lists.newArrayList();
                    lines.add(String.format("#: %s", context1));
                    lines.add(String.format("msgid \"%s\"", msgid1));
                    lines.add(String.format("msgstr \"%s\"", msgstr1));

                    lines.add(String.format(""));
                    lines.add(String.format("# "));

                    lines.add(String.format("#: %s", context2));
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
            final String context =
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#object()";
            final String msgid = "Work of art";
            final String msgid_plural = "Works of art";
            final String msgstr$0 = "Œuvre d'art";
            final String msgstr$1 = "Les œuvres d'art";

            poReader = new PoReader(mockTranslationServicePo) {
                @Override
                protected List<String> readPo(final Locale locale) {
                    final List<String> lines = Lists.newArrayList();
                    lines.add(String.format("#: %s", context));
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
                    return Lists.newArrayList();
                }
            };

            // when
            final String translated = poReader.translate("someContext", "Something to translate");

            // then
            assertThat(translated, is(equalTo("Something to translate")));
       }
    }

}