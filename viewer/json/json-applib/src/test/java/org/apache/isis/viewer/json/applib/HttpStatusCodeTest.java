package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.viewer.json.applib.HttpStatusCode;
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
        assertThat(statusFor.getFamily(), is(Family.OTHER));
    }

}
