package org.apache.isis.runtimes.dflt.objectstores.datanucleus.persistence;

public interface SuspendableListener {

    boolean isSuspended();

    void setSuspended(boolean suspend);

}
