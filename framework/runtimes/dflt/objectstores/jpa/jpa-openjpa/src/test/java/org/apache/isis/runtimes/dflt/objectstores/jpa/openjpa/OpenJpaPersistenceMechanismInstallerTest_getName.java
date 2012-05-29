package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenJpaPersistenceMechanismInstallerTest_getName {

    private OpenJpaPersistenceMechanismInstaller installer;

    @Before
    public void setUp() throws Exception {
        installer = new OpenJpaPersistenceMechanismInstaller();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void isSet() {
        assertThat(installer.getName(), is("openjpa"));
    }

}
