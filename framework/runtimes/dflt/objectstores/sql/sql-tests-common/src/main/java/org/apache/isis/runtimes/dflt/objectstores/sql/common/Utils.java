package org.apache.isis.runtimes.dflt.objectstores.sql.common;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.runtimes.dflt.objectstores.dflt.InMemoryPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.objectstores.sql.Sql;
import org.apache.isis.runtimes.dflt.objectstores.sql.SqlObjectStore;
import org.apache.isis.runtimes.dflt.objectstores.sql.SqlPersistorInstaller;
import org.apache.isis.runtimes.dflt.objectstores.xml.XmlPersistenceMechanismInstaller;
import org.apache.isis.runtimes.dflt.runtime.installerregistry.installerapi.PersistenceMechanismInstallerAbstract;

public class Utils {

    static PersistenceMechanismInstallerAbstract createPersistorInstaller(final IsisConfiguration configuration) {
        
        final String jdbcDriver = configuration.getString(SqlObjectStore.BASE_NAME + ".jdbc.driver");
        if (jdbcDriver != null) {
            return new SqlPersistorInstaller();
        } 
        
        final String persistor = configuration.getString("isis.persistor");
        if (persistor.equals(InMemoryPersistenceMechanismInstaller.NAME)) {
            return new InMemoryPersistenceMechanismInstaller();
        }
        if (persistor.equals(XmlPersistenceMechanismInstaller.NAME)) {
            return new XmlPersistenceMechanismInstaller();
        }
        if (persistor.equals(SqlPersistorInstaller.NAME)) {
            return new SqlPersistorInstaller();
        }
        return new InMemoryPersistenceMechanismInstaller();
    }

    static String tableIdentifierFor(final String tableName) {
        if (tableName.substring(0, 4).toUpperCase().equals("ISIS")) {
            return Sql.tableIdentifier(tableName);
        } else {
            return Sql.tableIdentifier("isis_" + tableName);
        }
    }



}
