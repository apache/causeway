package org.apache.isis.core.metamodel.specloader.specimpl;

import org.junit.Test;

import org.apache.isis.core.commons.lang.StringExtensions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ObjectActionMixedInTest {


    public static class SuffixAfterUnderscore extends ObjectActionMixedInTest {

        @Test
        public void exactly_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.suffix("_"));
            assertThat(s, is("_"));
        }

        @Test
        public void ends_with_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.suffix("abc_"));
            assertThat(s, is("Abc_"));
        }

        @Test
        public void has_no_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.suffix("defghij"));
            assertThat(s, is("Defghij"));
        }

        @Test
        public void contains_one_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.suffix("abc_def"));
            assertThat(s, is("Def"));
        }

        @Test
        public void contains_more_than_one_underscore() throws Exception {
            final String s = StringExtensions.asCapitalizedName(ObjectMemberAbstract.suffix("abc_def_ghi"));
            assertThat(s, is("Ghi"));
        }
    }

}