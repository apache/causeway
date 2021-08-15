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
package org.apache.isis.testing.archtestsupport.applib.modules;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import org.apache.isis.testing.archtestsupport.applib.modulerules.Subpackage;
import org.apache.isis.testing.archtestsupport.applib.modules.base.BaseModule;
import org.apache.isis.testing.archtestsupport.applib.modules.customer.CustomerModule;
import org.apache.isis.testing.archtestsupport.applib.modules.order.OrderModule;

import static org.apache.isis.testing.archtestsupport.applib.modulerules.ArchitectureModuleRules.analyzeClasses_packagesOf;
import static org.apache.isis.testing.archtestsupport.applib.modulerules.ArchitectureModuleRules.code_dependencies_follow_module_Imports;
import static org.apache.isis.testing.archtestsupport.applib.modulerules.ArchitectureModuleRules.code_dependencies_follow_module_Imports_and_subpackage_rules;

import lombok.RequiredArgsConstructor;

@AnalyzeClasses(
    packagesOf = {
            BaseModule.class,
            CustomerModule.class,
            OrderModule.class
    },
    importOptions = {ImportOption.OnlyIncludeTests.class})
public class ModulesArchTests {

  @ArchTest
  public static ArchRule code_dependencies_follow_module_Imports =
      code_dependencies_follow_module_Imports(
              analyzeClasses_packagesOf(ModulesArchTests.class));

  @ArchTest
  public static ArchRule code_dependencies_follow_module_Imports_and_subpackage_rules =
      code_dependencies_follow_module_Imports_and_subpackage_rules(
              analyzeClasses_packagesOf(ModulesArchTests.class),
              Arrays.asList(SimplifiedSubpackageEnum.values()));


    @RequiredArgsConstructor
    public enum SimplifiedSubpackageEnum implements Subpackage {

        dom(
                singletonList("*"), // wildcard means that all subpackages in this module can access 'dom'
                emptyList()         // no direct access from other modules
        ),
        api(
                singletonList("*"), // wildcard means that all subpackages in this module can access 'api'
                singletonList("*")  // wildcard means that all subpackages in other modules can access 'api'
        ),
        spi(
                singletonList("dom"),              // callers of a module's own SPI
                singletonList("spiimpl")    // other modules should only implement the SPI
        ),
        spiimpl(
                emptyList(), // no direct access
                emptyList() // no direct access
        ),
        ;

        final List<String> local;
        final List<String> referencing;

        public String getName() {
            return name();
        }

        @Override
        public List<String> mayBeAccessedBySubpackagesInSameModule() {
            return local;
        }

        @Override
        public List<String> mayBeAccessedBySubpackagesInReferencingModules() {
            return referencing;
        }

        private static String[] asArray(List<String> list) {
            return list != null ?
                    list.toArray(new String[] {}) : null;
        }
    }

}
