package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence;

import java.util.Date;

import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.spi.PersistenceCapable;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;

public class Utils {

    @SuppressWarnings("unused")
    private static Object jdoObjectIdFor(InstanceLifecycleEvent event) {
        PersistenceCapable persistenceCapable = Utils.persistenceCapableFor(event);
        Object jdoObjectId = persistenceCapable.jdoGetObjectId();
        return jdoObjectId;
    }

    static PersistenceCapable persistenceCapableFor(InstanceLifecycleEvent event) {
        return (PersistenceCapable)event.getSource();
    }

    static void clearDirtyFor(final ObjectAdapter adapter) {
        adapter.getSpecification().clearDirty(adapter);
    }

    static Version getVersionIfAny(final PersistenceCapable pojo, final AuthenticationSession authenticationSession) {
        Object jdoVersion = pojo.jdoGetVersion();
        if(jdoVersion instanceof Long) {
            return SerialNumberVersion.create((Long) jdoVersion, authenticationSession.getUserName(), new Date()); 
        } 
        return null;
    }

}
