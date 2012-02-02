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
package org.apache.isis.viewer.bdd.concordion.internal.fixtures;

import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.CellBindingDefault;
import org.apache.isis.viewer.bdd.common.IsisViewerConstants;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.fixtures.SetUpObjectsPeer;

public class SetUpObjectsForConcordion extends AbstractFixture<SetUpObjectsPeer> {

    public SetUpObjectsForConcordion(final AliasRegistry aliasesRegistry, final String className, final SetUpObjectsPeer.Mode mode) {
        this(aliasesRegistry, className, mode, CellBindingDefault.builder(IsisViewerConstants.ALIAS_RESULT_NAME, IsisViewerConstants.ALIAS_RESULT_HEAD_SET).autoCreate().build());
    }

    private SetUpObjectsForConcordion(final AliasRegistry storyFixture, final String className, final SetUpObjectsPeer.Mode mode, final CellBinding aliasBinding) {
        super(new SetUpObjectsPeer(storyFixture, className, mode, aliasBinding));
    }

    public String executeHeader(final String alias, final String... propertyNames) {

        // create bindings (there's only one)
        getPeer().getAliasBinding().setHeadColumn(0);

        // define properties and the alias column
        int colNum = 0;
        getPeer().definePropertyOrAlias(alias, colNum++);

        for (final String propertyName : propertyNames) {
            getPeer().definePropertyOrAlias(propertyName, colNum++);
        }

        return ""; // ok
    }

    public String executeRow(final String alias, final String... propertyValues) {

        // set property values and the alis
        getPeer().addPropertyValueOrAlias(alias);
        for (final String propertyValue : propertyValues) {
            getPeer().addPropertyValueOrAlias(propertyValue);
        }

        // create the object
        try {
            getPeer().createObject();
            return "ok";
        } catch (final ScenarioBoundValueException ex) {
            return ex.toString();
        }

    }

}
