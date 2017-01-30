package org.apache.isis.core.commons.configbuilder;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.core.commons.config.IsisConfigurationDefault;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PrimerForEnvironmentVariableISIS_OPTS_Test {

    IsisConfigurationBuilder builder;

    PrimerForEnvironmentVariableISIS_OPTS primer;

    @Before
    public void setUp() throws Exception {
        builder = new IsisConfigurationBuilder();
    }

    @Test
    public void when_value_contains_equal_signs() throws Exception {

        // given
        primer = new PrimerForEnvironmentVariableISIS_OPTS() {
            @Override
            String getEnv(final String optEnv) {
                return "ISIS_OPTS".equals(optEnv)
                        ? "isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL=jdbc:sqlserver://HALCHILLBLAST;instance=.;databaseName=estatio"
                        : null;
            }
        };

        // when
        primer.prime(builder);

        // then
        final IsisConfigurationDefault configuration = builder.getConfiguration();
        assertThat(configuration.asMap().size(), is(1));
        assertThat(configuration.getString("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL"), is("jdbc:sqlserver://HALCHILLBLAST;instance=.;databaseName=estatio"));
    }

    @Test
    public void when_value_does_not_contain_equal_signs() throws Exception {

        // given
        primer = new PrimerForEnvironmentVariableISIS_OPTS() {
            @Override
            String getEnv(final String optEnv) {
                return "ISIS_OPTS".equals(optEnv)
                        ? "isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionDriverName=com.microsoft.sqlserver.jdbc.SQLServerDriver"
                        : null;
            }
        };

        // when
        primer.prime(builder);

        // then
        final IsisConfigurationDefault configuration = builder.getConfiguration();

        assertThat(configuration.asMap().size(), is(1));
        assertThat(configuration.getString("isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionDriverName"), is("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
    }

    @Test
    public void when_string_does_not_contain_equal_signs() throws Exception {

        // given
        primer = new PrimerForEnvironmentVariableISIS_OPTS() {
            @Override
            String getEnv(final String optEnv) {
                return "ISIS_OPTS".equals(optEnv)
                        ? "isis.persistor.datanucleus.impl.javax.jdo.option.ConnectionURL~foo"
                        : null;
            }
        };

        // when
        primer.prime(builder);

        // then
        final IsisConfigurationDefault configuration = builder.getConfiguration();
        assertThat(configuration.asMap().size(), is(0));
    }


}