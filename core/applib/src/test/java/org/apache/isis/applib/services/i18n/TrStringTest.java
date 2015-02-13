package org.apache.isis.applib.services.i18n;

import java.util.Locale;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TrStringTest {

    public static class GetText extends TrStringTest {

        @Test
        public void singularForm() throws Exception {
            final TranslatableString ts = TranslatableString.tr("No, you can't do that!");

            assertThat(ts.getText(), is("No, you can't do that!"));
        }

        @Test
        public void pluralFormOne() throws Exception {
            final TranslatableString ts = TranslatableString.trn("You can't do that because there is a dependent object", "You can't do that because there are dependent objects", 1);

            assertThat(ts.getText(), is("You can't do that because there is a dependent object"));
        }

        @Test
        public void pluralFormTwo() throws Exception {
            final TranslatableString ts = TranslatableString.trn("You can't do that because there is a dependent object", "You can't do that because there are dependent objects", 2);

            assertThat(ts.getText(), is("You can't do that because there are dependent objects"));
        }

    }

    public static class Translated extends TrStringTest {

        @Test
        public void singularForm() throws Exception {
            final TranslatableString ts = TranslatableString.tr("My name is {lastName}, {firstName} {lastName}.", "lastName", "Bond", "firstName", "James");

            assertThat(ts.translated("Iche heisse {lastName}, {firstName} {lastName}."), is("Iche heisse Bond, James Bond."));
        }

        @Test
        public void pluralFormOne() throws Exception {
            final TranslatableString ts = TranslatableString.trn(
                    "My name is {lastName}, {firstName} {lastName}.",
                    "My name is {firstName} {lastName}.",
                    1,
                    "lastName", "Bond", "firstName", "James");

            assertThat(ts.translated("Iche heisse {lastName}, {firstName} {lastName}."), is("Iche heisse Bond, James Bond."));
        }
    }

    public static class Translate extends TrStringTest {

        @Rule
        public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

        private TranslationService echoTranslationService;

        private String originalTextToTranslate;

        @Before
        public void setUp() throws Exception {
            echoTranslationService = new TranslationService() {
                @Override
                public String translate(final String context, final String originalText, final Locale targetLocale) {
                    originalTextToTranslate = originalText;
                    return originalText;
                }
            };
        }

        @Test
        public void singularForm() throws Exception {
            final TranslatableString ts = TranslatableString.tr("My name is {lastName}, {firstName} {lastName}.", "lastName", "Bond", "firstName", "James");

            assertThat(ts.translate(echoTranslationService, null, null), is("My name is Bond, James Bond."));
            assertThat(originalTextToTranslate, is("My name is {lastName}, {firstName} {lastName}."));
        }

        @Test
        public void pluralFormOne() throws Exception {
            final TranslatableString ts = TranslatableString.trn(
                    "My name is {lastName}, {firstName} {lastName}.",
                    "My name is {firstName} {lastName}.",
                    1,
                    "lastName", "Bond", "firstName", "James");

            assertThat(ts.translate(echoTranslationService, null, null), is("My name is Bond, James Bond."));
            assertThat(originalTextToTranslate, is("My name is {lastName}, {firstName} {lastName}."));
        }

        @Test
        public void pluralFormTwo() throws Exception {
            final TranslatableString ts = TranslatableString.trn(
                    "My name is {lastName}, {firstName} {lastName}.",
                    "My name is {firstName} {lastName}.",
                    2,
                    "lastName", "Bond", "firstName", "James");

            assertThat(ts.translate(echoTranslationService, null, null), is("My name is James Bond."));
            assertThat(originalTextToTranslate, is("My name is {firstName} {lastName}."));
        }

    }




}