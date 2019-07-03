/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.specsupport.specs;

import org.junit.runner.RunWith;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

/**
 * Base class for all Cucumber specs run at unit-scope; runs the spec as a JUnit test.
 *
 * @deprecated - with no replacement
 */
@Deprecated
@RunWith(Cucumber.class)
@CucumberOptions(
        format = {
                "html:target/cucumber-html-report"
                // addHook causes an exception to be thrown if this reporter is registered...
                // ,"json-pretty:target/cucumber-json-report.json"
        },
        strict = true,
        tags = { "~@backlog" })
public abstract class CukeSpecsAbstract {


}
