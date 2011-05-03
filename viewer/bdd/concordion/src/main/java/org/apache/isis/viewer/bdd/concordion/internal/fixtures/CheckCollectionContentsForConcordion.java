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
import org.apache.isis.viewer.bdd.common.fixtures.CheckCollectionContentsPeer;

public class CheckCollectionContentsForConcordion extends AbstractFixture<CheckCollectionContentsPeer> {

    public CheckCollectionContentsForConcordion(final AliasRegistry aliasRegistry, final String listAlias) {
        super(new CheckCollectionContentsPeer(aliasRegistry, listAlias));
    }

    public String contains(final String alias) {
        if (!getPeer().isValidAlias(alias)) {
            return "unknown alias '" + alias + "'";
        }
        final boolean contains = getPeer().contains(alias);
        return contains ? "ok" : "does not contain '" + alias + "'";
    }

    public String doesNotContain(final String alias) {
        if (!getPeer().isValidAlias(alias)) {
            return "unknown alias '" + alias + "'";
        }
        final boolean doesNotContain = getPeer().doesNotContain(alias);
        return doesNotContain ? "ok" : "does contain";
    }

    public String isEmpty() {
        final boolean isEmpty = getPeer().isEmpty();
        return isEmpty ? "ok" : "not empty";
    }

    public String isNotEmpty() {
        final boolean isNotEmpty = getPeer().isNotEmpty();
        return isNotEmpty ? "ok" : "empty";
    }

    public String assertSize(final int size) {
        final boolean hasSize = getPeer().assertSize(size);
        return hasSize ? "ok" : "contains " + getPeer().getSize() + " objects";
    }

}
