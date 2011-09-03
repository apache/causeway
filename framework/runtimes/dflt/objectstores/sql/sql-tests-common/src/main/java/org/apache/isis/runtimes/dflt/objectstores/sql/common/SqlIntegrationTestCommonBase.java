package org.apache.isis.runtimes.dflt.objectstores.sql.common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.apache.isis.runtimes.dflt.objectstores.sql.singleton.SqlIntegrationTestSingleton;

public abstract class SqlIntegrationTestCommonBase extends TestCase {

    protected SqlIntegrationTestSingleton getSingletonInstance() {
        return SqlIntegrationTestSingleton.getInstance();
    }

    public Properties getProperties() {
        try {
            final Properties properties = new Properties();
            properties.load(new FileInputStream("src/test/config/" + getPropertiesFilename()));
            return properties;
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public abstract String getPropertiesFilename();

    /**
     * This method can be used to do any DB specific actions the first time the test framework is setup. e.g. In the XML
     * test, it must delete all XML files in the data store directory.
     */
    public void initialiseTests() {
    }

    // Set-up the test environment
    @Override
    public void setUp() throws FileNotFoundException, IOException, ClassNotFoundException, InstantiationException,
        IllegalAccessException, SQLException {
        Logger.getRootLogger().setLevel(Level.INFO);

        // Initialise the framework
        if (getSingletonInstance().getState() == 0) {
            final Properties properties = getProperties();
            if (properties == null) {
                getSingletonInstance().initNOF("src/test/config", getPropertiesFilename());
            } else {
                getSingletonInstance().initNOF(properties);
            }

            final String sqlSetupString = getSqlSetupString();
            if (sqlSetupString != null) {
                getSingletonInstance().sqlExecute(sqlSetupString);
            }
        }
    }

    // Tear down the test environment
    @Override
    public void tearDown() {
        if (getSingletonInstance().getState() == 0) {
            final String sqlTeardownString = getSqlTeardownString();
            if (sqlTeardownString != null) {
                try {
                    getSingletonInstance().sqlExecute(sqlTeardownString);
                } catch (final SQLException e) {
                    e.printStackTrace();
                }
            }
            getSingletonInstance().shutDown();
        }
    }

    /**
     * TODO Confirm that the system tables are created as expected
     */

    public String getSqlSetupString() {
        return null;
    }

    public String getSqlTeardownString() {
        return null;
    }

}
