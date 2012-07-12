package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence;

public interface SuspendableListener {

    boolean isSuspended();

    void setSuspended(boolean suspend);

}
