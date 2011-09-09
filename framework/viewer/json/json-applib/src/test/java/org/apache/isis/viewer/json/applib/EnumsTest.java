package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.viewer.json.applib.util.Enums;
import org.junit.Test;

public class EnumsTest {

    private static enum MyEnum {
        CONTENT_TYPE,
        LAST_MODIFIED,
        WARNING,
        X_REPRESENTATION_TYPE,
        OBJECT_ACTION
    }
    
    @Test
    public void converts() {
        assertConverts(MyEnum.CONTENT_TYPE, "Content-Type", "contentType");
        assertConverts(MyEnum.LAST_MODIFIED, "Last-Modified", "lastModified");
        assertConverts(MyEnum.WARNING, "Warning", "warning");
        assertConverts(MyEnum.X_REPRESENTATION_TYPE, "X-Representation-Type", "xRepresentationType");
    }

    protected void assertConverts(Enum<?> someEnum, String httpHeader, String camelCase) {
        assertThat(Enums.enumToHttpHeader(someEnum), is(httpHeader));
        assertThat(Enums.enumToCamelCase(someEnum), is(camelCase));
    }

}
