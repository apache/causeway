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

import java.util.List;

import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.approvaltests.reporters.linux.ReportWithMeldMergeLinux;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.io._TestDomain.Person;

class YamlUtilsTest {

    private Person person;
    private List<Person> persons;

    @BeforeEach
    void setup() {
        this.person = _TestDomain.samplePerson();
        this.persons = List.of(
        		person,
        		_TestDomain.samplePerson("fred"));
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
    
    @Test
    @UseReporter(DiffReporter.class)
    void toStringUtf8ForList_yamlList() {
        var yaml = YamlUtils.toStringUtf8(persons);
        Approvals.verify(yaml, defaultOptions());
    }

    @Test
    @UseReporter(DiffReporter.class)
    void toStringUtf8ForList_multiDoc() {
    	var yaml = YamlUtils.writeMultiDoc(
    			persons.stream()
        			.map(YamlUtils::toStringUtf8));
    	Approvals.verify(yaml, defaultOptions());
    }

    //TODO de-duplicate (from internaltestsupport)
	static Options defaultOptions() {
		var opts = new Options();
		// on Linux, at time of writing, the default reporter find mechanism throws an exception while evaluating Windows Diff Reporters;
		// this is a workaround, provided you are on Linux and have Meld installed
		return ReportWithMeldMergeLinux.INSTANCE.checkFileExists()
			? opts.withReporter(ReportWithMeldMergeLinux.INSTANCE)
			: opts;
	}
    
}
