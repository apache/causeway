package org.apache.isis.core.commons.config;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class IsisConfigurationDefault_safe_Test {

    @Test
    public void not_a_password() throws Exception {
        assertThat(IsisConfigurationDefault.safe("foo", "bar"), is(equalTo("bar")));
    }

    @Test
    public void a_password() throws Exception {
        assertThat(IsisConfigurationDefault.safe("xyz.password.abc", "bar"), is(equalTo("*******")));
    }

    @Test
    public void a_PassWord() throws Exception {
        assertThat(IsisConfigurationDefault.safe("xyz.PassWord.abc", "bar"), is(equalTo("*******")));
    }

    @Test
    public void is_null() throws Exception {
        assertThat(IsisConfigurationDefault.safe(null, "bar"), is(equalTo("bar")));
    }

}