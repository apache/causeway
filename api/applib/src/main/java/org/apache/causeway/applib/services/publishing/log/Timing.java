package org.apache.causeway.applib.services.publishing.log;

class Timing {
    private final long startTime;

    Timing() {
        this.startTime = System.currentTimeMillis();
    }

    long took() {
        return System.currentTimeMillis() - startTime;
    }
}
