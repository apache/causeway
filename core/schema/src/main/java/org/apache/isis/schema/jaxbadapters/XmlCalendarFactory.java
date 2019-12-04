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

import java.util.function.Function;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

class XmlCalendarFactory {
	
	public static XMLGregorianCalendar create(DateTime dateTime) {
		return dateTime!=null 
				? withTypeFactoryDo(dtf->dtf.newXMLGregorianCalendar(dateTime.toGregorianCalendar())) 
				: null;
	}

	public static XMLGregorianCalendar create(LocalDateTime localDateTime) {
		return localDateTime!=null 
				? withTypeFactoryDo(dtf->dtf.newXMLGregorianCalendar(
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

	public static XMLGregorianCalendar create(LocalDate localDate) {
		return localDate!=null 
				? withTypeFactoryDo(dtf->dtf.newXMLGregorianCalendarDate(
						localDate.getYear(),
						localDate.getMonthOfYear(),
						localDate.getDayOfMonth(),
						DatatypeConstants.FIELD_UNDEFINED
						)) 
				: null;
	}

	public static XMLGregorianCalendar create(LocalTime localTime) {
		return localTime!=null 
				? withTypeFactoryDo(dtf->dtf.newXMLGregorianCalendarTime(
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
	 * Gets an instance of DatatypeFactory and passes it to the factory argument for single use.
	 * 
	 * [ahuber] we don't want to store the DatatypeFactory.newInstance() into a static field for 
	 * reuse, because then we would need to cleanup after IsisContext.destroy() as well.
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
