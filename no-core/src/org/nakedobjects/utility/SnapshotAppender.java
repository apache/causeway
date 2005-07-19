package org.nakedobjects.utility;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.TriggeringEventEvaluator;


class DefaultEvaluator implements TriggeringEventEvaluator {
    public boolean isTriggeringEvent(LoggingEvent event) {
        return event.getLevel().isGreaterOrEqual(Level.ERROR);
    }
}

public abstract class SnapshotAppender extends AppenderSkeleton {
    private int bufferSize = 512;
    protected CyclicBuffer buffer = new CyclicBuffer(bufferSize);
    private boolean locationInfo = false;
    protected TriggeringEventEvaluator triggerEvaluator;

    /**
     * The default constructor will instantiate the appender with a {@link TriggeringEventEvaluator}
     * that will trigger on events with level ERROR or higher.
     */
    public SnapshotAppender() {
        this(new DefaultEvaluator());
    }

    public SnapshotAppender(TriggeringEventEvaluator evaluator) {
        this.triggerEvaluator = evaluator;
    }

    public void append(LoggingEvent event) {
        if (shouldAppend()) {
            event.getThreadName();
            event.getNDC();
            if (locationInfo) {
                event.getLocationInformation();
            }
            buffer.add(event);
            if (triggerEvaluator.isTriggeringEvent(event)) {
                writeSnapshot(buffer);
            }
        }
    }


    /**
      * Send the contents of the cyclic buffer as an web server posting.
      */
     private void writeSnapshot(CyclicBuffer buffer) {
         StringBuffer details = new StringBuffer();
         String header = layout.getHeader();
         if (header != null) {
             details.append(header);
         }
         int len = buffer.length();
         String message = "";
         for (int i = 0; i < len; i++) {
             LoggingEvent event = buffer.get();
             message = event.getLoggerName() + ": " + event.getMessage();
             details.append(layout.format(event));
             if (layout.ignoresThrowable()) {
                 String[] s = event.getThrowableStrRep();
                 if (s != null) {
                     for (int j = 0; j < s.length; j++) {
                         details.append(s[j]);
                         details.append('\n');
                     }
                 }
             }
         }
         String footer = layout.getFooter();
         if (footer != null) {
             details.append(footer);
         }
         
         writeSnapshot(message, details.toString());
     }
     
    protected abstract void writeSnapshot(String message, String details);
    
    synchronized public void close() {
        this.closed = true;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public String getEvaluatorClass() {
        return triggerEvaluator == null ? null : triggerEvaluator.getClass().getName();
    }

    public boolean getLocationInfo() {
        return locationInfo;
    }

    /**
     * returns true to show that this appender requires a {@linkorg.apache.log4j.Layout layout}.
     */
    public boolean requiresLayout() {
        return true;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        buffer.resize(bufferSize);
    }

    public void setEvaluatorClass(String value) {
        triggerEvaluator = (TriggeringEventEvaluator) OptionConverter.instantiateByClassName(value,
                TriggeringEventEvaluator.class, triggerEvaluator);
    }

    public void setLocationInfo(boolean locationInfo) {
        this.locationInfo = locationInfo;
    }

    protected boolean shouldAppend() {
        if (triggerEvaluator == null) {
            errorHandler.error("No TriggeringEventEvaluator is set for appender [" + name + "].");
            return false;
        }

        if (layout == null) {
            errorHandler.error("No layout set for appender named [" + name + "].");
            return false;
        }

        return true;
    }
}
