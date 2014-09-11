package org.apache.isis.viewer.wicket.ui.errors;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class JGrowlUtilTest {

    @Test
    public void testEscape() throws Exception {

        assertThat(JGrowlUtil.escape(
                "double quotes \" and single quotes ' and <p>markup</p>"), equalTo(
                "double quotes ' and single quotes ' and &lt;p&gt;markup&lt;/p&gt;"));
    }

}