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
package org.apache.causeway.testdomain.cucumber;

import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.testdomain.conf.Configuration_headless;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(
        classes = {
                Configuration_headless.class,
        })
public class StepDefs {
    private String today;
    private String actualAnswer;

//    @Given("today is Sunday")
//    public void today_is_sunday() {
//        this.today = "Sunday";
//    }

    @Given("^today is (.+)$")
    public void today_is(final String today) {
        this.today = today;
    }

    @When("I ask whether it's Friday yet")
    public void i_ask_whether_it_s_Friday_yet() {
        actualAnswer = "Friday".equals(today) ? "TGIF" : "Nope";
    }

    @Then("I should be told {string}")
    public void i_should_be_told(final String expectedAnswer) {
        assertEquals(expectedAnswer, actualAnswer);
    }
}
