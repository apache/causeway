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

class JsonUtilsTest {

    /*
     * [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:testCompile
     *      on project causeway-commons: Compilation failure:
     * [ERROR] package com.google.gson does not exist
     * [ERROR] cannot find symbol
     * [ERROR]   symbol:   class GsonBuilder
     * [ERROR]   location: class org.approvaltests.JsonApprovals
     */
    org.approvaltests.JsonApprovals dummy1; // references com.google.gson.GsonBuilder
    com.google.gson.GsonBuilder dummy2; // Requires (GSON)[https://mvnrepository.com/artifact/com.google.code.gson/gson]

    private Person person;

    @BeforeEach
    void setup() {
        this.person = _TestDomain.samplePerson();
    }

    @Test
    void toStringUtf8_indentedOutput() {
        var json = JsonUtils.toStringUtf8(person, JsonUtils::indentedOutput);
        Approvals.verify(json);
    }

    @Test
    void parseRecord() {
        var jsonTemplate =
                  " {\r\n"
                + "     \"name\": \"sven\",\r\n"
                + "     \"java8Time\": {\r\n"
                + "         \"localTime\" : \"${localTime}\",\r\n"
                + "         \"localDate\" : \"${localDate}\",\r\n"
                + "         \"localDateTime\" : \"${localDateTime}\",\r\n"
                + "         \"offsetTime\" : \"${offsetTime}\",\r\n"
                + "         \"offsetDateTime\" : \"${offsetDateTime}\",\r\n"
                + "         \"zonedDateTime\" : \"${zonedDateTime}\"\r\n"
                + "     },\r\n"
                + "     \"address\": {\r\n"
                + "         \"zip\":1234,\r\n"
                + "         \"street\":\"backerstreet\"\r\n"
                + "     },\r\n"
                + "     \"additionalAddresses\" : [ {\r\n"
                + "         \"zip\" : 23,\r\n"
                + "         \"street\" : \"brownstreet\"\r\n"
                + "     }, {\r\n"
                + "         \"zip\" : 34,\r\n"
                + "         \"street\" : \"bluestreet\"\r\n"
                + "     } ],\r\n"
                + "  \"phone\" : {\r\n"
                + "    \"home\" : \"+99 1234\",\r\n"
                + "    \"work\" : null\r\n"
                + "  }"
                + " }";

        var json = person.getJava8Time().interpolator().applyTo(jsonTemplate);

        //debug
        //System.err.printf("%s%n", json);

        var person = JsonUtils.tryRead(Person.class, json)
                .valueAsNonNullElseFail();
        assertEquals(this.person, person);
    }

}
