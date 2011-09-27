package org.apache.isis.viewer.json.applib;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class RepresentationTypeTest {

    @Test
    public void converts() {
        assertThat(RepresentationType.CAPABILITIES.getName(), is("capabilities"));
        assertThat(RepresentationType.HOME_PAGE.getName(), is("homePage"));
        assertThat(RepresentationType.TYPE_ACTION_PARAMETER.getName(), is("typeActionParameter"));
    }


}
