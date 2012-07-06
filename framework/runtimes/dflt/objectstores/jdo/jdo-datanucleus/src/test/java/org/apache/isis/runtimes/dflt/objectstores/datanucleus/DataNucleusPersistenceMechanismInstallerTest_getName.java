package org.apache.isis.runtimes.dflt.objectstores.datanucleus;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataNucleusPersistenceMechanismInstallerTest_getName {

    private DataNucleusPersistenceMechanismInstaller installer;

    @Before
    public void setUp() throws Exception {
        installer = new DataNucleusPersistenceMechanismInstaller();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void isSet() {
        assertThat(installer.getName(), is("datanucleus"));
    }

}
