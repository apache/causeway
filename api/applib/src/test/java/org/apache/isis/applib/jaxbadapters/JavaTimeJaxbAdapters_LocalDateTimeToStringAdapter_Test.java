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
package org.apache.isis.applib.jaxbadapters;

import java.time.LocalDateTime;

import org.apache.isis.applib.jaxb.JavaTimeJaxbAdapters;

public class JavaTimeJaxbAdapters_LocalDateTimeToStringAdapter_Test extends JaxbXmlAdaptersContractTest<LocalDateTime> {

    public JavaTimeJaxbAdapters_LocalDateTimeToStringAdapter_Test() {
        super(new JavaTimeJaxbAdapters.LocalDateTimeToStringAdapter()
                , LocalDateTime.of(2015, 5, 23, 9, 54, 1));
    }

}
