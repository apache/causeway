package org.nakedobjects.utility;

import org.apache.log4j.spi.TriggeringEventEvaluator;



public class WebSnapshotAppender extends SnapshotAppender {
    private String proxyAddress;
    private int proxyPort = -1;

    /**
     * The default constructor will instantiate the appender with a {@link TriggeringEventEvaluator}
     * that will trigger on events with level ERROR or higher.
     */
    public WebSnapshotAppender() {
        this(new DefaultEvaluator());
    }

    public WebSnapshotAppender(TriggeringEventEvaluator evaluator) {
        super(evaluator);
    }

    public String getProxyAddress() {
        return proxyAddress;
    }

    public int getProxyPort() {
        return proxyPort;
    }
    
   protected void writeSnapshot(String message, String details) {
        RemoteLogger.submitLog(message, details, proxyAddress, proxyPort);
    }

    public void setProxyAddress(String proxyAddess) {
        this.proxyAddress = proxyAddess;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
