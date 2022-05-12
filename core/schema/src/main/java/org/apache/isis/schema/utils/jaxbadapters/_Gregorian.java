package org.apache.isis.schema.utils.jaxbadapters;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

//@UtilityClass
class _Gregorian {

	// this assumes DTF is thread-safe, which it most probably is..
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6466177
    static DatatypeFactory datatypeFactory = null;

    public static DatatypeFactory getDatatypeFactory() {
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

	public static XMLGregorianCalendar of(int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour,
			int secondOfMinute, int millisOfSecond) {
		// TODO Auto-generated method stub
		return null;
	}

	public static XMLGregorianCalendar of(int year, int monthOfYear, int dayOfMonth) {
		// TODO Auto-generated method stub
		return null;
	}

	public static XMLGregorianCalendar of(int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) {
		// TODO Auto-generated method stub
		return null;
	}
}
