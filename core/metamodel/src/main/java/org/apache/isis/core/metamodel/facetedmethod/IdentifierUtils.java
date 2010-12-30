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


package org.apache.isis.core.metamodel.facetedmethod;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.ensure.Ensure;


public final class IdentifierUtils {

    private IdentifierUtils() {}

    /**
     * Factory method.
     * 
     * @see #toIdentityString(int)
     */
    public static Identifier fromIdentityString(final String asString) {
        Ensure.ensureThatArg(asString, is(not(nullValue())));
        final int indexOfHash = asString.indexOf("#");
        final int indexOfOpenBracket = asString.indexOf("(");
        final int indexOfCloseBracket = asString.indexOf(")");
        final String className = asString.substring(0, indexOfHash == -1 ? asString.length() : indexOfHash);
        if (indexOfHash == -1 || indexOfHash == (asString.length() - 1)) {
            return Identifier.classIdentifier(className);
        }
        String name = null;
        if (indexOfOpenBracket == -1) {
            name = asString.substring(indexOfHash + 1);
            return Identifier.propertyOrCollectionIdentifier(className, name);
        }
        List<String> parmList = new ArrayList<String>();
        name = asString.substring(indexOfHash + 1, indexOfOpenBracket);
        final String allParms = asString.substring(indexOfOpenBracket + 1, indexOfCloseBracket).trim();
        if (allParms.length() > 0) {
            // use StringTokenizer for .NET compatibility
            final StringTokenizer tokens = new StringTokenizer(allParms, ",", false);
            for (int i = 0; tokens.hasMoreTokens(); i++) {
                String nextParam = tokens.nextToken();
                parmList.add(nextParam);
            }
        }
        return Identifier.actionIdentifier(className, name, parmList.toArray(new String[]{}));
    }


}
