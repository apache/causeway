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
package org.apache.isis.viewer.wicket.graceful;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.commons.internal.base._Files;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.val;

class WicketArtifactReplacer {

    @EnabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
    @Test
    void processWicketArtifacts() {
        assertTrue(true);

        val m2Repo = resolveM2Repo()
                .orElseThrow(()->_Exceptions.unrecoverable("cannot find m2 repo, "
                        + "where to put the patched artifacts to"));
        val resources = resourcesDir()
                .orElseThrow(()->_Exceptions.unrecoverable("cannot locate resources, "
                        + "from where to read the patched artifacts"));

        _Files.copy(
                new File(resources, "wicket-core-9.11.0.jar.mangled"),
                new File(m2Repo, "org/apache/wicket/wicket-core/9.11.0/wicket-core-9.11.0.jar"));
        _Files.copy(
                new File(resources, "wicket-util-9.11.0.jar.mangled"),
                new File(m2Repo, "org/apache/wicket/wicket-util/9.11.0/wicket-util-9.11.0.jar"));

    }

    // -- HELPER

    private Optional<File> resolveM2Repo() {
        return _Strings.nonEmpty(System.getProperty("user.home"))
               .flatMap(userHome->_Files.existingDirectory(new File(String.format("/%s/.m2/repository", userHome))));
    }

    private Optional<File> resourcesDir() {
        return _Files.existingDirectory(new File("src/test/resources/wicket-artifacts-no-module-info"));
    }

}
