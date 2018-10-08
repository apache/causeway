package org.apache.isis.applib.services.metamodel;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;

public class MetaModelService6_Config_Test {

    @Test
    public void ignore_noop() throws Exception {

        // when
        MetaModelService6.Config config = new MetaModelService6.Config();
        // then
        assertThat(config.isIgnoreNoop(), is(equalTo(false)));

        // and when
        MetaModelService6.Config flags2 = config.withIgnoreNoop();

        // then
        assertNotSame(config, flags2);
        assertThat(config.isIgnoreNoop(), is(equalTo(false)));
        assertThat(flags2.isIgnoreNoop(), is(equalTo(true)));
    }
}