package org.apache.isis.viewer.json.applib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;

public abstract class Parser<T> {
    
    public abstract T valueOf(String str);
    public abstract String asString(T t);

    public final static Parser<String> forStrings() {
        return new Parser<String>() {
            @Override
            public String valueOf(String str) {
                return str;
            }
            @Override
            public String asString(String t) {
                return t;
            }
        };
    }

    public static Parser<Date> forDates() {

        return new Parser<Date>() {
            private final SimpleDateFormat RFC1123_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z");

            @Override
            public Date valueOf(String str) {
                try {
                    return RFC1123_DATE_FORMAT.parse(str);
                } catch (ParseException e) {
                    return null;
                }
            }
            @Override
            public String asString(Date t) {
                return RFC1123_DATE_FORMAT.format(t);
            }
        };
    }

    public static Parser<CacheControl> forCacheControl() {
        return new Parser<CacheControl>(){
            @Override
            public CacheControl valueOf(String str) {
                return CacheControl.valueOf(str);
            }
            @Override
            public String asString(CacheControl t) {
                return null;
            }};
    }

    public static Parser<MediaType> forMediaType() {
        return new Parser<MediaType>() {
            @Override
            public MediaType valueOf(String str) {
                return MediaType.valueOf(str);
            }

            @Override
            public String asString(MediaType t) {
                return t.toString();
            }
            
        };
    }

    public static Parser<Boolean> forBoolean() {
        return new Parser<Boolean>() {
            @Override
            public Boolean valueOf(String str) {
                return str.equals("yes")?Boolean.TRUE:Boolean.FALSE;
            }

            @Override
            public String asString(Boolean t) {
                return t?"yes":"no";
            }
            
        };
    }

    public static Parser<RepresentationType> forRepresentationType() {
        return new Parser<RepresentationType>() {
            @Override
            public RepresentationType valueOf(String str) {
                return RepresentationType.lookup(str);
            }
            @Override
            public String asString(RepresentationType t) {
                return t.getName();
            }
        };
    }

}
