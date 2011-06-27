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
package org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.property;

import java.util.Date;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.value.date.DateValueFacet;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;
import org.apache.isis.viewer.bdd.common.fixtures.perform.PerformContext;
import org.apache.isis.viewer.bdd.common.fixtures.perform.checkthat.ThatSubcommandAbstract;
import org.apache.isis.viewer.bdd.common.parsers.DateParser;

public class Contains extends ThatSubcommandAbstract {

    public Contains() {
        super("contains", "is", "does contain");
    }

    @Override
    public ObjectAdapter that(final PerformContext performContext) throws ScenarioBoundValueException {

        final OneToOneAssociation otoa = (OneToOneAssociation) performContext.getObjectMember();

        // if we have an expected result
        final CellBinding arg0Binding = performContext.getPeer().getArg0Binding();
        final ScenarioCell arg0Cell = arg0Binding.getCurrentCell();
        final String expected = arg0Cell.getText();

        // get
        final ObjectAdapter resultAdapter = otoa.get(performContext.getOnAdapter());

        // see if matches null
        if (resultAdapter == null) {
            if (StringUtils.isNullOrEmpty(expected)) {
                return resultAdapter;
            }
            throw ScenarioBoundValueException.current(arg0Binding, "(is null)");
        }

        final String resultTitle = resultAdapter.titleString();

        if (!StringUtils.isNullOrEmpty(expected)) {

            // see if expected matches an alias
            final ObjectAdapter expectedAdapter = performContext.getPeer().getAliasRegistry().getAliased(expected);
            if (expectedAdapter != null) {
                // known
                if (resultAdapter == expectedAdapter) {
                    return resultAdapter;
                }
                throw ScenarioBoundValueException.current(arg0Binding, resultTitle);
            }

            // otherwise, see if date and if so compare as such
            final DateValueFacet dateValueFacet = resultAdapter.getSpecification().getFacet(DateValueFacet.class);
            if (dateValueFacet != null) {
                final Date resultDate = dateValueFacet.dateValue(resultAdapter);

                final DateParser dateParser = performContext.getDateParser();
                final Date expectedDate = dateParser.parse(expected);
                if (expectedDate != null) {
                    if (expectedDate.compareTo(resultDate) == 0) {
                        return resultAdapter; // ok
                    }
                }
                final String format = dateParser.format(resultDate);
                throw ScenarioBoundValueException.current(arg0Binding, format);
            }

            // otherwise, compare title
            if (!StringUtils.nullSafeEquals(resultTitle, expected)) {
                throw ScenarioBoundValueException.current(arg0Binding, resultTitle);
            }
        } else {
            // try to provide a default
            final String resultAlias = performContext.getPeer().getAliasRegistry().getAlias(resultAdapter);
            final String resultStr = resultAlias != null ? resultAlias : resultTitle;
            performContext.getPeer().provideDefault(arg0Cell, resultStr);
        }

        return resultAdapter;
    }

}
