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
package org.apache.causeway.applib.jaxb;

import java.util.function.Function;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import lombok.experimental.UtilityClass;

/**
 * @since 2.0 {@index}
 */
@UtilityClass
public class DataTypeFactory {

    /*
     * Gets an instance of DatatypeFactory and passes it to the factory argument. (thread-safe)
     */
    public static XMLGregorianCalendar withTypeFactoryDo(
            Function<DatatypeFactory, XMLGregorianCalendar> factory) {

        final DatatypeFactory dataTypeFactory;

        try {

            dataTypeFactory = DatatypeFactory.newInstance();

        } catch (DatatypeConfigurationException e) {

            System.err.println("Within "+ JavaTimeXMLGregorianCalendarMarshalling.class.getName()+": "+
                    "Exception in call to DatatypeFactory.newInstance()" + e);
            return null;

        }

        return factory.apply(dataTypeFactory);
    }

}
