package org.apache.isis.applib.services.metamodel;

import org.junit.Test;

import static org.hamcrest.Matchers.emptyCollectionOf;
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
        MetaModelService6.Config config2 = config.withIgnoreNoop();

        // then
        assertNotSame(config, config2);
        assertThat(config.isIgnoreNoop(), is(equalTo(false)));
        assertThat(config2.isIgnoreNoop(), is(equalTo(true)));
    }

    @Test
    public void packages_prefixes() throws Exception {

        // when
        MetaModelService6.Config config = new MetaModelService6.Config();
        // then
        assertThat(config.getPackagePrefixes(), is(emptyCollectionOf(String.class)));

        // and when
        MetaModelService6.Config config2 = config.withPackagePrefix("org.foo");

        // then
        assertNotSame(config, config2);
        assertThat(config2.getPackagePrefixes().size(), is(equalTo(1)));
        assertThat(config2.getPackagePrefixes().get(0), is(equalTo("org.foo")));

        // and when
        MetaModelService6.Config config3 = config2.withPackagePrefix("org.bar");

        // then
        assertNotSame(config, config3);
        assertNotSame(config2, config3);
        assertThat(config3.getPackagePrefixes().size(), is(equalTo(2)));
        assertThat(config3.getPackagePrefixes().get(0), is(equalTo("org.foo")));
        assertThat(config3.getPackagePrefixes().get(1), is(equalTo("org.bar")));
    }
}