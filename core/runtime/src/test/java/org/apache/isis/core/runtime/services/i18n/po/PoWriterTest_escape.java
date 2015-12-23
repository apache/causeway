package org.apache.isis.core.runtime.services.i18n.po;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PoWriterTest_escape {

    @Test
    public void no_quotes() throws Exception {
        String escape = PoWriter.escape("abc");
        assertThat(escape, is(equalTo("abc")));
    }

    @Test
    public void with_quotes() throws Exception {
        String escape = PoWriter.escape(str('a', '"', 'b', '"', 'c'));
        assertThat(escape, is(equalTo(str('a', '\\', '"', 'b', '\\', '"', 'c'))));
    }

    private static String str(char... params) {
        return new String(params);
    }
}