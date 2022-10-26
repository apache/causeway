/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.tooling.cli.projdoc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProjectDocModel_sanitizeDescription_Test {

    @Test
    void strips() {

        final String str = ProjectDocModel.sanitizeDescription(
                "JDO Spring integration.\n" +
                "\t\t\n" +
                "\t\tThis is a fork of the Spring ORM JDO sources at github, \n" +
                "        for which support had been dropped back in 2016 [1].\n" +
                "\t\t\n" +
                "\t\tCredits to the original authors.\n" +
                "\t\t\n" +
                "\t\t[1] https://github.com/spring-projects/spring-framework/issues/18702");

        Assertions.assertEquals(str,
                "JDO Spring integration.\n" +
                "\n" +
                "This is a fork of the Spring ORM JDO sources at github,\n" +
                "for which support had been dropped back in 2016 [1].\n" +
                "\n" +
                "Credits to the original authors.\n" +
                "\n" +
                "[1] https://github.com/spring-projects/spring-framework/issues/18702");
    }
}
