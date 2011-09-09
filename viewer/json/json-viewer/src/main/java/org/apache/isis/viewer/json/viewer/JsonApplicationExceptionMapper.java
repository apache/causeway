package org.apache.isis.viewer.json.viewer;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.isis.viewer.json.applib.HttpStatusCode;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.util.JsonMapper;

import com.google.common.collect.Lists;

public class JsonApplicationExceptionMapper implements ExceptionMapper<JsonApplicationException> {

    @Override
    public Response toResponse(JsonApplicationException ex) {
        ResponseBuilder builder = 
                Response.status(ex.getHttpStatusCode().getJaxrsStatusType())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(jsonFor(ex));
        withWarningIfAny(ex, builder);
        return builder.build();
    }

    private static void withWarningIfAny(JsonApplicationException ex, ResponseBuilder builder) {
        String message = ex.getMessage();
        if(message != null) {
            builder.header(RestfulResponse.Header.WARNING.getName(), message);
        }
    }

    private static class ExceptionPojo {

        public static ExceptionPojo create(Exception ex) {
            return new ExceptionPojo(ex);
        }

        private static String format(StackTraceElement stackTraceElement) {
            return stackTraceElement.toString();
        }

        private final String message;
        private final List<String> stackTrace = Lists.newArrayList();
        private ExceptionPojo causedBy;

        public ExceptionPojo(Throwable ex) {
            this.message = ex.getMessage();
            StackTraceElement[] stackTraceElements = ex.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTraceElements) {
                this.stackTrace.add(format(stackTraceElement));
            }
            Throwable cause = ex.getCause();
            if(cause != null && cause != ex) {
                this.causedBy = new ExceptionPojo(cause);
            }
        }
        
        public String getMessage() {
            return message;
        }
        
        public List<String> getStackTrace() {
            return stackTrace;
        }
        
        public ExceptionPojo getCausedBy() {
            return causedBy;
        }

    }
    
    static String jsonFor(Exception ex) {
        try {
            return JsonMapper.instance().write(ExceptionPojo.create(ex));
        } catch (Exception e) {
            // fallback
            return "{ \"exception\": \"" + ExceptionUtils.getFullStackTrace(ex) + "\" }";
        }
    }



    
}
