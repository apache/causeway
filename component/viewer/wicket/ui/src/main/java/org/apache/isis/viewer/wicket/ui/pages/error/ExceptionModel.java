package org.apache.isis.viewer.wicket.ui.pages.error;

import java.util.List;

import org.apache.isis.viewer.wicket.model.models.ModelAbstract;
import org.apache.isis.viewer.wicket.ui.errors.StackTraceDetail;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class ExceptionModel extends ModelAbstract<Exception> {

    private static final long serialVersionUID = 1L;

    private static final String MAIN_MESSAGE_IF_NOT_RECOGNIZED = "Sorry, an unexpected error occurred.";
    
    private Exception exception;
    private boolean recognized;

    private final String mainMessage;

    private final String exceptionMessage;
    

    public static ExceptionModel recognized(String message, Exception ex) {
        return new ExceptionModel(message, null, ex);
    }

    public static ExceptionModel notRecognized(Exception ex) {
        return new ExceptionModel(MAIN_MESSAGE_IF_NOT_RECOGNIZED, ex.getMessage(), ex);
    }

    private ExceptionModel(String mainMessage, String exceptionMessage, Exception ex) {
        this.mainMessage = mainMessage;
        this.exceptionMessage = exceptionMessage;
        this.recognized = (exceptionMessage == null);
        this.exception = ex;
    }

    @Override
    protected Exception load() {
        return exception;
    }

    @Override
    public void setObject(Exception ex) {
        if(ex == null) {
            return;
        }
        this.exception = ex;
    }

    public boolean isRecognized() {
        return recognized;
    }

    public String getMainMessage() {
        return mainMessage;
    }
    
    public String getExceptionMessage() {
        return exceptionMessage;
    }
    
    public List<StackTraceDetail> getStackTrace() {
        return asStackTrace(exception);
    }

    
    private static List<StackTraceDetail> asStackTrace(Throwable ex) {
        List<StackTraceDetail> stackTrace = Lists.newArrayList();
        List<Throwable> causalChain = Throwables.getCausalChain(ex);
        for(Throwable cause: causalChain) {
            stackTrace.add(StackTraceDetail.exceptionClassName(cause));
            stackTrace.add(StackTraceDetail.exceptionMessage(cause));
            addStackTraceElements(cause, stackTrace);
            cause = cause.getCause();
        }
        return stackTrace;
    }

    private static void addStackTraceElements(Throwable ex, List<StackTraceDetail> stackTrace) {
        for (StackTraceElement el : ex.getStackTrace()) {
            stackTrace.add(StackTraceDetail.element(el));
        }
    }





}
