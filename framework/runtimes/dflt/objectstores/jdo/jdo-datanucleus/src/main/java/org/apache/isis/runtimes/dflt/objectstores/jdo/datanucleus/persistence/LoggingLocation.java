package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence;

public enum LoggingLocation {
    ENTRY(">>"), EXIT("<<");
    final String prefix;
    private LoggingLocation(String prefix) {
        this.prefix = prefix;
    }
}