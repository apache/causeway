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
package org.apache.isis.core.transaction.changetracking;

import java.util.Objects;

import org.apache.isis.core.transaction.changetracking.events.IsisTransactionPlaceholder;

import lombok.Getter;

final class _PreAndPostValue {

    /**
     * The object that was referenced before this object was changed.
     *
     * @implNote this referenced object itself could end up being deleted in the course of the
     * transaction; in which case use
     * {@link #getPreString()} which is the eagerly cached <tt>toString</tt> of said object.
     */
    @Getter private final Object pre;

    /**
     * Eagerly calculated because it could be that the object referenced ends up being deleted
     * by the time that the transaction completes.
     */
    @Getter private final String preString;

    @Getter private final Object post;
    @Getter private final String postString;

    public static _PreAndPostValue pre(final Object preValue) {
        return new _PreAndPostValue(preValue);
    }

    private _PreAndPostValue(final Object pre) {
        this.pre = pre;
        this.preString = asString(pre);
        this.post = null;
        this.postString = null;
    }

    private _PreAndPostValue(final _PreAndPostValue pre, final Object post) {
        this.pre = pre.getPre();
        this.preString = pre.getPreString();
        this.post = post;
        this.postString = asString(post);
    }

    public _PreAndPostValue withPost(final Object post) {
        return new _PreAndPostValue(this, post);
    }

    @Override
    public String toString() {
        return getPre() + " -> " + getPost();
    }

    public boolean shouldPublish() {
        // don't audit objects that were created and then immediately deleted within the same xactn
        if (getPre() == IsisTransactionPlaceholder.NEW
                && getPost() == IsisTransactionPlaceholder.DELETED) {
            return false;
        }
        // but do always audit objects that have just been created or deleted
        if (getPre() == IsisTransactionPlaceholder.NEW
                || getPost() == IsisTransactionPlaceholder.DELETED) {
            return true;
        }
        // else - for updated objects - audit only if the property value has changed
        return !Objects.equals(getPre(), getPost());
    }

    // -- HELPER

    private static String asString(Object object) {
        return object != null
                ? object.toString()
                : null;
    }

}
