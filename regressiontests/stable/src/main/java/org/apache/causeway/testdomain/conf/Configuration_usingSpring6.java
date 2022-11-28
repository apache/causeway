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
package org.apache.causeway.testdomain.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.causeway.testing.fixtures.applib.fixturescripts.ExecutionParametersServiceAutoConfiguration;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScriptsSpecificationProviderAutoConfiguration;

import lombok.extern.log4j.Log4j2;

/**
 * Introduced for the purpose of troubleshooting Spring 5.x to 6.x migration issues.
 * @deprecated marked deprecated, to be removed once no longer needed
 */
@Deprecated(forRemoval = true)
@Configuration
@Import({
    FixtureScriptsSpecificationProviderAutoConfiguration.class, // because something? disables autoconfiguration
    ExecutionParametersServiceAutoConfiguration.class           // because something? disables autoconfiguration
})
@Log4j2
public class Configuration_usingSpring6 {


}
