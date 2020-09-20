package org.apache.isis.extensions.commandreplay.secondary;


public class StatusException extends Exception {
    public final SecondaryStatus secondaryStatus;

    public StatusException(SecondaryStatus secondaryStatus) {
        this(secondaryStatus, null);
    }
    public StatusException(SecondaryStatus secondaryStatus, final Exception ex) {
        super(ex);
        this.secondaryStatus = secondaryStatus;
    }
}
