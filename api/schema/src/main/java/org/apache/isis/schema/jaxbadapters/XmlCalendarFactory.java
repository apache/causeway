/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.schema.jaxbadapters;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.function.Function;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class XmlCalendarFactory {
    
    // -- JAVA TIME - FROM XML

    public static LocalDate toLocalDate(XMLGregorianCalendar cal) {
        return LocalDate.of(cal.getYear(), cal.getMonth(), cal.getDay());
    }

    public static LocalTime toLocalTime(XMLGregorianCalendar cal) {
        return LocalTime.of(cal.getHour(), cal.getMinute(), cal.getSecond(), 
                cal.getMillisecond()*1000_000);
    }

    public static LocalDateTime toLocalDateTime(XMLGregorianCalendar cal) {
        return LocalDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(), 
                cal.getMillisecond()*1000_000);
    }

    public static OffsetDateTime toOffsetDateTime(XMLGregorianCalendar cal) {
        return OffsetDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(), 
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }

    public static OffsetTime toOffsetTime(XMLGregorianCalendar cal) {
        return OffsetTime.of(
                cal.getHour(), cal.getMinute(), cal.getSecond(), 
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }

    public static ZonedDateTime toZonedDateTime(XMLGregorianCalendar cal) {
        return ZonedDateTime.of(cal.getYear(), cal.getMonth(), cal.getDay(),
                cal.getHour(), cal.getMinute(), cal.getSecond(), 
                cal.getMillisecond()*1000_000,
                ZoneOffset.ofTotalSeconds(cal.getTimezone()*60));
    }
    
    // -- JAVA TIME - TO XML
    
    public static XMLGregorianCalendar create(LocalDate localDate) {
        return localDate!=null 
                ? withTypeFactoryDo(factory->factory.newXMLGregorianCalendarDate(
                        localDate.getYear(),
                        localDate.getMonthValue(),
                        localDate.getDayOfMonth(),
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        ))  
                : null;
    }
    
    public static XMLGregorianCalendar create(LocalTime localTime) {
        return localTime!=null 
                ? withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
                        localTime.getHour(),
                        localTime.getMinute(),
                        localTime.getSecond(),
                        localTime.getNano()/1000_000, // millis
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        )) 
                : null;
    }
    
    public static XMLGregorianCalendar create(LocalDateTime localDateTime) {
        return localDateTime!=null 
                ? withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        localDateTime.getYear(),
                        localDateTime.getMonthValue(),
                        localDateTime.getDayOfMonth(),
                        localDateTime.getHour(),
                        localDateTime.getMinute(),
                        localDateTime.getSecond(),
                        localDateTime.getNano()/1000_000, // millis
                        DatatypeConstants.FIELD_UNDEFINED // timezone offset in minutes
                        )) 
                : null;
    }
    
    public static XMLGregorianCalendar create(OffsetTime offsetTime) {
        return offsetTime!=null 
                ? withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
                        offsetTime.getHour(),
                        offsetTime.getMinute(),
                        offsetTime.getSecond(),
                        offsetTime.getNano()/1000_000, // millis
                        offsetTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        ))  
                : null;
    }
    
    public static XMLGregorianCalendar create(OffsetDateTime offsetDateTime) {
        return offsetDateTime!=null 
                ? withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        offsetDateTime.getYear(),
                        offsetDateTime.getMonthValue(),
                        offsetDateTime.getDayOfMonth(),
                        offsetDateTime.getHour(),
                        offsetDateTime.getMinute(),
                        offsetDateTime.getSecond(),
                        offsetDateTime.getNano()/1000_000, // millis
                        offsetDateTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        )) 
                : null;
    }
    
    public static XMLGregorianCalendar create(ZonedDateTime zonedDateTime) {
        return zonedDateTime!=null 
                ? withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
                        zonedDateTime.getYear(),
                        zonedDateTime.getMonthValue(),
                        zonedDateTime.getDayOfMonth(),
                        zonedDateTime.getHour(),
                        zonedDateTime.getMinute(),
                        zonedDateTime.getSecond(),
                        zonedDateTime.getNano()/1000_000, // millis
                        zonedDateTime.getOffset().getTotalSeconds()/60 // timezone offset in minutes
                        )) 
                : null;
    }
    
    
    // -- JODA TIME
    
	public static XMLGregorianCalendar create(org.joda.time.DateTime dateTime) {
		return dateTime!=null 
				? withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(dateTime.toGregorianCalendar())) 
				: null;
	}

	public static XMLGregorianCalendar create(org.joda.time.LocalDateTime localDateTime) {
		return localDateTime!=null 
				? withTypeFactoryDo(factory->factory.newXMLGregorianCalendar(
						localDateTime.getYear(),
				        localDateTime.getMonthOfYear(),
				        localDateTime.getDayOfMonth(),
				        localDateTime.getHourOfDay(),
				        localDateTime.getMinuteOfHour(),
				        localDateTime.getSecondOfMinute(),
				        localDateTime.getMillisOfSecond(),
				        DatatypeConstants.FIELD_UNDEFINED
						)) 
				: null;
	}

	public static XMLGregorianCalendar create(org.joda.time.LocalDate localDate) {
		return localDate!=null 
				? withTypeFactoryDo(factory->factory.newXMLGregorianCalendarDate(
						localDate.getYear(),
						localDate.getMonthOfYear(),
						localDate.getDayOfMonth(),
						DatatypeConstants.FIELD_UNDEFINED
						)) 
				: null;
	}

	public static XMLGregorianCalendar create(org.joda.time.LocalTime localTime) {
		return localTime!=null 
				? withTypeFactoryDo(factory->factory.newXMLGregorianCalendarTime(
				        localTime.getHourOfDay(),
				        localTime.getMinuteOfHour(),
				        localTime.getSecondOfMinute(),
				        localTime.getMillisOfSecond(),
				        DatatypeConstants.FIELD_UNDEFINED
						)) 
				: null;
	}
	
	// -- HELPER
	
	/*
	 * Gets an instance of DatatypeFactory and passes it to the factory argument. (thread-safe)
	 */
	private static XMLGregorianCalendar withTypeFactoryDo(
			Function<DatatypeFactory, XMLGregorianCalendar> factory) {
		
		final DatatypeFactory dataTypeFactory;
		
		try {
			
			dataTypeFactory = DatatypeFactory.newInstance();
			
		} catch (DatatypeConfigurationException e) {
			
			System.err.println("Within "+XmlCalendarFactory.class.getName()+": "+
					"Exception in call to DatatypeFactory.newInstance()" + e);
			return null;
			
		}
		
		return factory.apply(dataTypeFactory);
		
	}
	
}
