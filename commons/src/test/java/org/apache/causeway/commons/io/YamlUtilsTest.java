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
package org.apache.causeway.commons.io;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.io._TestDomain.Person;

class YamlUtilsTest {

    private Person person;

    @BeforeEach
    void setup() {
        this.person = _TestDomain.samplePerson();
    }

    @Test
    void toStringUtf8() {
        var yaml = YamlUtils.toStringUtf8(person);
        Approvals.verify(yaml);
    }

    @Test
    void parseRecord() {
        var yamlTemplate = """
                name: sven
                address: {street: backerstreet, zip: 1234}
                additionalAddresses:
                - zip: 23
                  street: "brownstreet"
                - zip: 34
                  street: "bluestreet"
                java8Time:
                  localTime: ${localTime}
                  localDate: ${localDate}
                  localDateTime: ${localDateTime}
                  offsetTime: ${offsetTime}
                  offsetDateTime: ${offsetDateTime}
                  zonedDateTime: ${zonedDateTime}
                phone:
                  home: "+99 1234"
                  work: null
                """;

        var yaml = person.java8Time().interpolator().applyTo(yamlTemplate);

        // debug
        //System.err.printf("%s%n", yaml);

        var person = YamlUtils.tryRead(Person.class, yaml)
                .valueAsNonNullElseFail();
        assertEquals(this.person, person);
    }

}
