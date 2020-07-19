package org.apache.isis.applib.jaxb;

import java.sql.Timestamp;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class JavaSqlXMLGregorianCalendarMarshalling {

    public static Timestamp toTimestamp(final XMLGregorianCalendar calendar) {
        return calendar != null
                ? new Timestamp(calendar.toGregorianCalendar().getTime().getTime())
                : null;
    }

    public static XMLGregorianCalendar toXMLGregorianCalendar(final Timestamp timestamp) {
        if(timestamp == null) {
            return null;
        }
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(timestamp);
        return getDatatypeFactory().newXMLGregorianCalendar(c);
    }

    // this assumes DTF is thread-safe, which it most probably is..
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6466177
    static DatatypeFactory datatypeFactory = null;

    private static DatatypeFactory getDatatypeFactory() {
        if(datatypeFactory == null) {
            try {
                datatypeFactory = DatatypeFactory.newInstance();
                return datatypeFactory;
            } catch (DatatypeConfigurationException e) {
                throw new RuntimeException(e);
            }
        }
        return datatypeFactory;
    }
}
