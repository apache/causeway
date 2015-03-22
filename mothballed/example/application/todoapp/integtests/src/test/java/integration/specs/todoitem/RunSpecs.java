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
package integration.specs.todoitem;

import cucumber.api.junit.Cucumber;

import org.junit.runner.RunWith;


/**
 * Runs scenarios in all <tt>.feature</tt> files (this package and any subpackages). 
 */
@RunWith(Cucumber.class)
@Cucumber.Options(
        format = {
                "html:target/cucumber-html-report"
                ,"json:target/cucumber.json"
        },
        glue={"classpath:integration.glue"},
        strict = true,
        tags = { "~@backlog", "~@ignore" })
public class RunSpecs {
    // intentionally empty 
}
