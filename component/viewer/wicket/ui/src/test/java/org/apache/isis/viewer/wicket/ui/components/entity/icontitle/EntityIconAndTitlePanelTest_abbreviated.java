package org.apache.isis.viewer.wicket.ui.components.entity.icontitle;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EntityIconAndTitlePanelTest_abbreviated {

    @Test
    public void notTruncated() throws Exception {
        assertThat(EntityIconAndTitlePanel.abbreviated("abcdef", 6), is("abcdef"));
    }

    @Test
    public void truncated() throws Exception {
        assertThat(EntityIconAndTitlePanel.abbreviated("abcdefg", 6), is("abc..."));
    }

    @Test
    public void notTruncatedAtEllipsesLimit() throws Exception {
        assertThat(EntityIconAndTitlePanel.abbreviated("abc", 3), is("abc"));
        assertThat(EntityIconAndTitlePanel.abbreviated("ab", 2), is("ab"));
        assertThat(EntityIconAndTitlePanel.abbreviated("a", 1), is("a"));
    }

    @Test
    public void truncatedAtEllipsesLimit() throws Exception {
        assertThat(EntityIconAndTitlePanel.abbreviated("abcd", 3), is(""));
        assertThat(EntityIconAndTitlePanel.abbreviated("abc", 2), is(""));
        assertThat(EntityIconAndTitlePanel.abbreviated("ab", 1), is(""));
    }

}
