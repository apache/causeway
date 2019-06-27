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
package org.apache.isis.runtime.system.transaction;

import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class PreAndPostValues {

    public static class Predicates {
        public final static Predicate<Map.Entry<?, PreAndPostValues>> SHOULD_AUDIT = 
            (Map.Entry<?, PreAndPostValues> input) -> {
                final PreAndPostValues papv = input.getValue();
                return papv.shouldAudit();
        };
    }

    private final Object pre;
    /**
     * Eagerly calculated because it could be that the object referenced ends up being deleted by the time that the xactn completes.
     */
    private final String preString;

    /**
     * Updated in {@link #setPost(Object)}
     */
    private Object post;
    /**
     * Updated in {@link #setPost(Object)}, along with {@link #post}.
     */
    private String postString;

    public static PreAndPostValues pre(Object preValue) {
        return new PreAndPostValues(preValue, null);
    }

    private PreAndPostValues(Object pre, Object post) {
        this.pre = pre;
        this.post = post;
        this.preString = ChangedObjectsServiceInternal.asString(pre);
    }

    /**
     * The object that was referenced before this object was changed
     * <p/>
     * <p/>
     * Note that this referenced object itself could end up being deleted in the course of the transaction; in which case use
     * {@link #getPreString()} which is the eagerly cached <tt>toString</tt> of said object.
     */
    public Object getPre() {
        return pre;
    }

    public String getPreString() {
        return preString;
    }

    public Object getPost() {
        return post;
    }

    public String getPostString() {
        return postString;
    }

    public void setPost(Object post) {
        this.post = post;
        this.postString = ChangedObjectsServiceInternal.asString(post);
    }

    @Override
    public String toString() {
        return getPre() + " -> " + getPost();
    }

    public boolean shouldAudit() {
        // don't audit objects that were created and then immediately deleted within the same xactn
        if (getPre() == IsisTransaction.Placeholder.NEW && getPost() == IsisTransaction.Placeholder.DELETED) {
            return false;
        }
        // but do always audit objects that have just been created or deleted
        if (getPre() == IsisTransaction.Placeholder.NEW || getPost() == IsisTransaction.Placeholder.DELETED) {
            return true;
        }
        // else - for updated objects - audit only if the property value has changed
        return !Objects.equals(getPre(), getPost());
    }
}
