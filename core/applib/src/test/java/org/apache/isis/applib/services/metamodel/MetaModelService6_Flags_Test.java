package org.apache.isis.applib.services.metamodel;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

public class MetaModelService6_Flags_Test {

    @Test
    public void ignore_noop() throws Exception {

        // when
        MetaModelService6.Flags flags = new MetaModelService6.Flags();
        // then
        assertThat(flags.isIgnoreNoop(), is(equalTo(false)));

        // and when
        MetaModelService6.Flags flags2 = flags.ignoreNoop();

        // then
        assertNotSame(flags, flags2);
        assertThat(flags.isIgnoreNoop(), is(equalTo(false)));
        assertThat(flags2.isIgnoreNoop(), is(equalTo(true)));
    }
}