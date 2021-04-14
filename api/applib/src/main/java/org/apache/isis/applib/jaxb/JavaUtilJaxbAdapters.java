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
package org.apache.isis.applib.jaxb;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import lombok.experimental.UtilityClass;

/**
 * Provides JAXB XmlAdapters for Java util temporal types.
 *
 * <p>
 * Example:<pre>
 * &#64;XmlElement &#64;XmlJavaTypeAdapter(JavaUtilJaxbAdapters.DateAdapter.class)
 * &#64;Getter &#64;Setter private java.utilDate javaLocalDate;
 * </pre>
 * 
 *  
 * @since 2.0 {@index}
 */
@UtilityClass
public final class JavaUtilJaxbAdapters {

    public static final class DateToStringAdapter extends XmlAdapter<String, Date> {

        @Override
        public Date unmarshal(String v) throws Exception {
            return v!=null ? new Date(Long.parseLong(v)) : null;
        }

        @Override
        public String marshal(Date v) throws Exception {
            return v!=null ? Long.toString(v.getTime()) : null;
        }
    }
}
