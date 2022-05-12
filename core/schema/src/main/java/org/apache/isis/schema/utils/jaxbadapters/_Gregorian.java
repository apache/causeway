package org.apache.isis.schema.utils.jaxbadapters;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.LocalDate;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _Gregorian {

	// this assumes DTF is thread-safe, which it most probably is..
    // http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6466177
    DatatypeFactory datatypeFactory = null;

    public DatatypeFactory getDatatypeFactory() {
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

	public XMLGregorianCalendar of(
			int year, int monthOfYear, int dayOfMonth, int hourOfDay, int minuteOfHour,
			int secondOfMinute, int millisOfSecond) {
		return getDatatypeFactory().newXMLGregorianCalendar(
				year, monthOfYear, dayOfMonth, 
				hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond, 
				0);
	}

	public XMLGregorianCalendar of(int year, int monthOfYear, int dayOfMonth) {
		return getDatatypeFactory().newXMLGregorianCalendar(
				year, monthOfYear, dayOfMonth, 
				0, 0, 0, 0, 
				0);
	}

	public XMLGregorianCalendar of(int hourOfDay, int minuteOfHour, int secondOfMinute, int millisOfSecond) {
		
		val today = LocalDate.now();
		
		return getDatatypeFactory().newXMLGregorianCalendar(
				today.getYear(), today.getMonthOfYear(), today.getDayOfMonth(), 
				hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond, 
				0);
	}
}
