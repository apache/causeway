package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import org.apache.isis.runtimes.dflt.runtime.persistence.ObjectPersistenceException;

public class DataNucleusException extends ObjectPersistenceException {

    private static final long serialVersionUID = 1L;

    public DataNucleusException() {
        super();
    }

    public DataNucleusException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNucleusException(String message) {
        super(message);
    }

    public DataNucleusException(Throwable cause) {
        super(cause);
    }

}
