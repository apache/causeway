package org.apache.isis.subdomains.base.applib.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.isis.subdomains.base.applib.valuetypes.LocalDateInterval;

public class TitleBuilderTest {

    public class TestObject {
        public String title(){
            return "Parent [PAR]";
        }
    }

    @Test
    public void testToString() throws Exception {
        assertThat(TitleBuilder.start().withName("Name").withReference("REF").toString())
                .isEqualTo("Name [REF]");
        assertThat(TitleBuilder.start().withParent(new TestObject()).withName("Name").withReference("REF").toString())
                .isEqualTo("Parent [PAR] > Name [REF]");
        assertThat(TitleBuilder.start().withParent(new TestObject()).withName("REF").withReference("REF").toString())
                .isEqualTo("Parent [PAR] > REF");
        assertThat(TitleBuilder.start().withParent(new TestObject()).withName("Name1").withName("Name2").withReference("REF").toString())
                .isEqualTo("Parent [PAR] > Name1 Name2 [REF]");
        assertThat(TitleBuilder.start().withParent(new TestObject()).withName(LocalDateInterval.parseString("2014-01-01/2015-01-01")).toString())
                .isEqualTo("Parent [PAR] > 2014-01-01/2015-01-01");

    }
}