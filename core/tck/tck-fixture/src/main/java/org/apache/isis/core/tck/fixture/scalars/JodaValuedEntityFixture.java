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

package org.apache.isis.core.tck.fixture.scalars;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.core.tck.dom.scalars.JodaValuedEntity;
import org.apache.isis.core.tck.dom.scalars.JodaValuedEntityRepository;

public class JodaValuedEntityFixture extends AbstractFixture {

    @Override
    public void install() {
        createEntity();
        createEntity();
        createEntity();
        createEntity();
        createEntity();
    }

    private JodaValuedEntity createEntity() {
        final JodaValuedEntity jve = jodaValuesEntityRepository.newEntity();
        jve.setLocalDateProperty(new LocalDate(2008,3,21));
        jve.setLocalDateTimeProperty(new LocalDateTime(2009, 4, 29, 13, 45, 22));
        jve.setDateTimeProperty(new DateTime(2010, 3, 31, 9, 50, 43, DateTimeZone.UTC));
        return jve;
    }

    private JodaValuedEntityRepository jodaValuesEntityRepository;

    public void setJdkValuesEntityRepository(final JodaValuedEntityRepository jodaValuesEntityRepository) {
        this.jodaValuesEntityRepository = jodaValuesEntityRepository;
    }

}
