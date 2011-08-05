package org.apache.isis.viewer.json.applib.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.viewer.json.applib.util.HttpStatusCode.Range;
import org.junit.Test;

public class HttpStatusCodeTest {

    @Test
    public void knownStatusCode() {
        assertThat(HttpStatusCode.statusFor(200), is(HttpStatusCode.OK));
    }

    @Test
    public void unknownStatusCode() {
        HttpStatusCode statusFor = HttpStatusCode.statusFor(600);
        assertThat(statusFor.getStatusCode(), is(600));
        assertThat(statusFor.getRange(), is(Range.OUT_OF_RANGE_HIGH));
    }

}
