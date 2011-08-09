package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class JsonRepresentationTest_newArray {

    @Test
    public void newArray() throws Exception {
        JsonRepresentation jsonRepresentation = JsonRepresentation.newArray();
        assertThat(jsonRepresentation.arraySize(), is(0));
    }

    @Test
    public void newArrayInitialSize() throws Exception {
        JsonRepresentation jsonRepresentation = JsonRepresentation.newArray(2);
        assertThat(jsonRepresentation.arraySize(), is(2));
        assertThat(jsonRepresentation.elementAt(0).isNull(), is(true));
        assertThat(jsonRepresentation.elementAt(1).isNull(), is(true));
    }

}
