package org.nakedobjects.utility;

import org.apache.log4j.spi.TriggeringEventEvaluator;



public class WebSnapshotAppender extends SnapshotAppender {
    private String proxyAddess;
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

    public String getProxyAddess() {
        return proxyAddess;
    }

    public int getProxyPort() {
        return proxyPort;
    }
    
   protected void writeSnapshot(String message, String details) {
        RemoteLogger.submitLog(message, details, proxyAddess, proxyPort);
    }

    public void setProxyAddess(String proxyAddess) {
        this.proxyAddess = proxyAddess;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }
}
