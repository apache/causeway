package org.apache.isis.config;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

public class IsisConfiguration_getMaxTitleLengthInParentedTables_Test {

    private IsisConfiguration configuration;

    @Before
    public void setUp() throws Exception {
        configuration = new IsisConfiguration();
    }

    @Test
    public void when_not_set() throws Exception {
        // when
        int val = configuration.getViewer().getWicket().getMaxTitleLengthInParentedTables();

        // then
        Assertions.assertThat(val).isEqualTo(12);
    }

    @Test
    public void when_not_set_explicitly_but_fallback_has_been() throws Exception {
        // given
        configuration.getViewer().getWicket().setMaxTitleLengthInTables(20);

        // when
        int val = configuration.getViewer().getWicket().getMaxTitleLengthInParentedTables();

        // then
        Assertions.assertThat(val).isEqualTo(20);
    }

    @Test
    public void when_set_explicitly() throws Exception {
        // given
        configuration.getViewer().getWicket().setMaxTitleLengthInParentedTables(25);

        // when
        int val = configuration.getViewer().getWicket().getMaxTitleLengthInParentedTables();

        // then
        Assertions.assertThat(val).isEqualTo(25);
    }

    @Test
    public void when_set_explicitly_ignores_fallback_has_been() throws Exception {
        // given
        configuration.getViewer().getWicket().setMaxTitleLengthInTables(20);
        configuration.getViewer().getWicket().setMaxTitleLengthInParentedTables(25);

        // when
        int val = configuration.getViewer().getWicket().getMaxTitleLengthInParentedTables();

        // then
        Assertions.assertThat(val).isEqualTo(25);
    }
}