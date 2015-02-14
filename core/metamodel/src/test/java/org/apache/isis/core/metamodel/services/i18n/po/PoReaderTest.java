package org.apache.isis.core.metamodel.services.i18n.po;

import java.util.List;
import java.util.Locale;
import com.google.common.collect.Lists;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PoReaderTest {

    PoReader poReader;

    public static class ReadFile extends PoReaderTest {

        @Ignore // TODO
        @Test
        public void singleContext() throws Exception {

            // given
            final String context =
                    "org.apache.isis.applib.services.bookmark.BookmarkHolderAssociationContributions#object()";
            final String msgId = "Work of art";
            final String msgStr = "Objet d'art";

            poReader = new PoReader(null) {
                @Override
                List<String> readFile(final Locale locale) {
                    final List<String> lines = Lists.newArrayList();
                    lines.add(String.format("#: %s", context));
                    lines.add(String.format("msgid: \"%s\"", msgId));
                    lines.add(String.format("msgstr: \"%s\"", msgStr));
                    return lines;
                }
            };

            // when
            final String translated = poReader.translate(context, msgId, Locale.FRENCH);

            // then
            assertThat(translated, is(equalTo(msgStr)));
        }

        @Ignore // TODO
        @Test
        public void multipleContext() throws Exception {

            // given
            final String context1 =
                    "fixture.simple.SimpleObjectsFixturesService#runFixtureScript(org.apache.isis.applib.fixturescripts.FixtureScript,java.lang.String)";
            final String context2 =
                    "org.apache.isis.applib.fixturescripts.FixtureScripts#runFixtureScript(org.apache.isis.applib.fixturescripts.FixtureScript,java.lang.String)";
            final String msgId = "Parameters";
            final String msgStr = "Paramètres";

            poReader = new PoReader(null) {
                @Override
                List<String> readFile(final Locale locale) {
                    final List<String> lines = Lists.newArrayList();
                    lines.add(String.format("#: %s", context1));
                    lines.add(String.format("#: %s", context2));
                    lines.add(String.format("msgid: \"%s\"", msgId));
                    lines.add(String.format("msgstr: \"%s\"", msgStr));
                    return lines;
                }
            };
            // when
            final String translated = poReader.translate(context1, msgId, Locale.FRENCH);

            // then
            assertThat(translated, is(equalTo(msgStr)));

            // when
            final String translated2 = poReader.translate(context2, msgId, Locale.FRENCH);

            // then
            assertThat(translated2, is(equalTo(msgStr)));
        }

        @Ignore // TODO
        @Test
        public void noTranslation() throws Exception {

            // given

            poReader = new PoReader(null) {
                @Override
                List<String> readFile(final Locale locale) {
                    return Lists.newArrayList();
                }
            };

            // when
            final String translated = poReader.translate("someContext", "Something to translate", Locale.FRENCH);

            // then
            assertThat(translated, is(equalTo("Something to translate")));
       }

    }

}