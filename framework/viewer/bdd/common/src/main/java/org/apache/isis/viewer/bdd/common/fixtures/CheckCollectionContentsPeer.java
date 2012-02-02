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
package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.bdd.common.AliasRegistry;

public class CheckCollectionContentsPeer extends AbstractListFixturePeer {

    public CheckCollectionContentsPeer(final AliasRegistry aliasesRegistry, final String listAlias) {
        super(aliasesRegistry, listAlias);
    }

    /**
     * Returns <tt>true</tt> if collection contains specified alias.
     * 
     * <p>
     * If either the list alias is invalid, or the provided alias is
     * {@link #isValidAlias(String) invalid}, will return <tt>false</tt>.
     */
    public boolean contains(final String alias) {
        if (!isValidListAlias()) {
            return false;
        }

        final ObjectAdapter adapter = getAliasRegistry().getAliased(alias);
        if (adapter == null) {
            return false;
        }
        return collectionAdapters().contains(adapter);
    }

    /**
     * Returns <tt>true</tt> if collection does not contain specified alias.
     * 
     * <p>
     * If either the list alias is invalid, or the provided alias is
     * {@link #isValidAlias(String) invalid}, will return <tt>false</tt>.
     */
    public boolean doesNotContain(final String alias) {
        if (!isValidListAlias()) {
            return false;
        }
        final ObjectAdapter adapter = getAliasRegistry().getAliased(alias);
        if (adapter == null) {
            return false;
        }
        return !collectionAdapters().contains(adapter);
    }

    /**
     * Returns <tt>true</tt> if is empty.
     * 
     * @return <tt>false</tt> if the alias is invalid or does not represent a
     *         list
     */
    public boolean isEmpty() {
        if (!isValidListAlias()) {
            return false;
        }
        return collectionAdapters().size() == 0;
    }

    /**
     * Returns <tt>true</tt> if is not empty.
     * 
     * @return <tt>false</tt> if the alias is invalid or does not represent a
     *         list
     */
    public boolean isNotEmpty() {
        if (!isValidListAlias()) {
            return false;
        }

        return collectionAdapters().size() != 0;
    }

    /**
     * Returns <tt>true</tt> if collection has specified size.
     * 
     * @return <tt>false</tt> if the alias is invalid or does not represent a
     *         list
     */
    public boolean assertSize(final int size) {
        if (!isValidListAlias()) {
            return false;
        }
        return getSize() == size;
    }

    /**
     * Returns the size of the collection.
     * 
     * @return <tt>-1</tt> if the alias is invalid or does not represent a list.
     */
    public int getSize() {
        if (!isValidListAlias()) {
            return -1;
        }
        return collectionAdapters().size();
    }

}
