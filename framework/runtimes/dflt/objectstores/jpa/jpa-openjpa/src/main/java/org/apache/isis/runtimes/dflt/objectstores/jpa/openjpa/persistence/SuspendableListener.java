package org.apache.isis.runtimes.dflt.objectstores.jpa.openjpa.persistence;

public interface SuspendableListener {

    boolean isSuspended();

    void setSuspended(boolean suspend);

}
