package org.apache.isis.viewer.json.applib.util;

import java.util.Map;

import com.google.common.collect.Maps;

public class HttpStatusCode {
    
    public enum Range {
        CONTINUE(100,199),
        SUCCESS(200,299),
        REDIRECT(300,399),
        CLIENT_ERROR(400,499),
        SERVER_ERROR(500,599),
        OUT_OF_RANGE_LOW(Integer.MIN_VALUE, 99),
        OUT_OF_RANGE_HIGH(600,Integer.MAX_VALUE);

        public static Range lookup(int statusCode) {
            Range[] values = values();
            for (Range range : values) {
                if(range.includes(statusCode)) {
                    return range;
                }
            }
            // shouldn't happen
            throw new IllegalStateException("Unable to locate Range for statusCode");
        }

        private final int from;
        private final int to;

        private Range(int from, int to) {
            this.from = from;
            this.to = to;
        }

        private boolean includes(int statusCode) {
            return from <= statusCode && statusCode <= to;
        }
    }

////Field descriptor #62 I
//public static final int SC_CONTINUE = 100;
//
////Field descriptor #62 I
//public static final int SC_SWITCHING_PROTOCOLS = 101;
//
////Field descriptor #62 I
//public static final int SC_PROCESSING = 102;
//

    private final static Map<Integer, HttpStatusCode> statusCodes = Maps.newHashMap();

    public final static HttpStatusCode OK = new HttpStatusCode(200);
    public final static HttpStatusCode CREATED = new HttpStatusCode(201);

//
////Field descriptor #62 I
//public static final int SC_ACCEPTED = 202;
//
////Field descriptor #62 I
//public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
//
////Field descriptor #62 I
//public static final int SC_NO_CONTENT = 204;
//
////Field descriptor #62 I
//public static final int SC_RESET_CONTENT = 205;
//
////Field descriptor #62 I
//public static final int SC_PARTIAL_CONTENT = 206;
//
////Field descriptor #62 I
//public static final int SC_MULTI_STATUS = 207;
//
    
////Field descriptor #62 I
//public static final int SC_MULTIPLE_CHOICES = 300;
//
////Field descriptor #62 I
//public static final int SC_MOVED_PERMANENTLY = 301;
//
////Field descriptor #62 I
//public static final int SC_MOVED_TEMPORARILY = 302;
//
////Field descriptor #62 I
//public static final int SC_SEE_OTHER = 303;
//
////Field descriptor #62 I
//public static final int SC_NOT_MODIFIED = 304;
//
////Field descriptor #62 I
//public static final int SC_USE_PROXY = 305;
//
////Field descriptor #62 I
//public static final int SC_TEMPORARY_REDIRECT = 307;
//
    
    public final static HttpStatusCode BAD_REQUEST = new HttpStatusCode(400);
    public final static HttpStatusCode UNAUTHORIZED = new HttpStatusCode(401);

//
////Field descriptor #62 I
//public static final int SC_PAYMENT_REQUIRED = 402;
//
////Field descriptor #62 I
//public static final int SC_FORBIDDEN = 403;
//
    
    public final static HttpStatusCode NOT_FOUND = new HttpStatusCode(401);
    public final static HttpStatusCode NOT_METHOD_NOT_ALLOWED = new HttpStatusCode(405);

//
////Field descriptor #62 I
//public static final int SC_NOT_ACCEPTABLE = 406;
//
////Field descriptor #62 I
//public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
//
////Field descriptor #62 I
//public static final int SC_REQUEST_TIMEOUT = 408;
//
////Field descriptor #62 I
//public static final int SC_CONFLICT = 409;
    
    public final static HttpStatusCode NOT_CONFLICT = new HttpStatusCode(409);

//
////Field descriptor #62 I
//public static final int SC_GONE = 410;
//
////Field descriptor #62 I
//public static final int SC_LENGTH_REQUIRED = 411;
//
    
    public final static HttpStatusCode PRECONDITION_FAILED = new HttpStatusCode(412);

//
////Field descriptor #62 I
//public static final int SC_REQUEST_TOO_LONG = 413;
//
////Field descriptor #62 I
//public static final int SC_REQUEST_URI_TOO_LONG = 414;
//
////Field descriptor #62 I
//public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
    
    public final static HttpStatusCode UNSUPPORTED_MEDIA_TYPE = new HttpStatusCode(415);

//
////Field descriptor #62 I
//public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
//
////Field descriptor #62 I
//public static final int SC_EXPECTATION_FAILED = 417;
//
////Field descriptor #62 I
//public static final int SC_INSUFFICIENT_SPACE_ON_RESOURCE = 419;
//
////Field descriptor #62 I
//public static final int SC_METHOD_FAILURE = 420;
//
////Field descriptor #62 I
//public static final int SC_UNPROCESSABLE_ENTITY = 422;
//
////Field descriptor #62 I
//public static final int SC_LOCKED = 423;
//
////Field descriptor #62 I
//public static final int SC_FAILED_DEPENDENCY = 424;
//
    
    public final static HttpStatusCode INTERNAL_SERVER_ERROR = new HttpStatusCode(500);

//
////Field descriptor #62 I
//public static final int SC_NOT_IMPLEMENTED = 501;
//
////Field descriptor #62 I
//public static final int SC_BAD_GATEWAY = 502;
//
////Field descriptor #62 I
//public static final int SC_SERVICE_UNAVAILABLE = 503;
//
////Field descriptor #62 I
//public static final int SC_GATEWAY_TIMEOUT = 504;
//
////Field descriptor #62 I
//public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;
//
////Field descriptor #62 I
//public static final int SC_INSUFFICIENT_STORAGE = 507;



    public final static HttpStatusCode statusFor(int statusCode) {
        HttpStatusCode httpStatusCode = statusCodes.get(statusCode);
        if(httpStatusCode != null) {
            return httpStatusCode;
        }
        return syncStatusFor(statusCode);
    }

    private final static synchronized HttpStatusCode syncStatusFor(int statusCode) {
        HttpStatusCode httpStatusCode = statusCodes.get(statusCode);
        if(httpStatusCode == null) {
            httpStatusCode = new HttpStatusCode(statusCode);
            statusCodes.put(statusCode, httpStatusCode);
        }
        return httpStatusCode;
    }
    

    private final int statusCode;
    private final Range range;

    private HttpStatusCode(int statusCode) {
        this.statusCode = statusCode;
        range = Range.lookup(statusCode);
        statusCodes.put(statusCode, this);
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public Range getRange() {
        return range;
    }

    
    @Override
    public String toString() {
        return "HttpStatusCode " + statusCode + ", " + range;
    }
    
    
}

