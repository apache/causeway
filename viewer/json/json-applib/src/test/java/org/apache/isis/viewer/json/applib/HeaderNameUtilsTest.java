package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class HeaderNameUtilsTest {


    @Test
    public void converts() {
        assertConverts("CONTENT_TYPE", "Content-Type");
        assertConverts("LAST_MODIFIED", "Last-Modified");
        assertConverts("WARNING", "Warning");
        assertConverts("X_REPRESENTATION_TYPE", "X-Representation-Type");
    }

    protected void assertConverts(String enumName, String expected) {
        assertThat(HeaderNameUtils.convert(enumName), is(expected));
    }

}
