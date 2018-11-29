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
package org.apache.isis.config.builder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import java.util.Arrays;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.config.builder.ModulePackageHelper;
import org.apache.isis.core.runtime.systemusinginstallers.fixture.budget.SomeServiceToInclude;
import org.apache.isis.core.runtime.systemusinginstallers.fixture.budgetassignment.SomeServiceNotToInclude;

public class IsisComponentProvider_within_Test {

    @Test
    public void within() throws Exception {
        final String budgetPackageWithDot =
                SomeServiceToInclude.class.getPackage().getName() + ".";
        final String budgetAssignmentPackageWithDot =
                SomeServiceNotToInclude.class.getPackage().getName()  + ".";

        final Set<Class<?>> within = ModulePackageHelper.withinPackageAndNotAnonymous(
                Arrays.asList(budgetPackageWithDot),
                _Sets.of(SomeServiceToInclude.class, SomeServiceNotToInclude.class));

        Assert.assertThat(within.size(), is(equalTo(1)));
    }
}