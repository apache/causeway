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
package org.apache.isis.applib.services.bookmark;

import java.io.Serializable;
import java.util.Iterator;

import com.google.common.base.Splitter;

/**
 * String representation of any persistent object managed by the framework.
 * 
 * <p>
 * Analogous to the <tt>RootOid</tt>.
 */
public class Bookmark implements Serializable {

    private static final char SEPARATOR = ':';

    private static final long serialVersionUID = 1L;
    
    private final String objectType;
    private final String identifier;

    /**
     * Round-trip with {@link #toString()} representation.
     */
    public Bookmark(String toString) {
        this(Splitter.on(SEPARATOR).split(toString).iterator());
    }

    private Bookmark(Iterator<String> split) {
        this(split.next(), split.next());
    }

    public Bookmark(String objectType, String identifier) {
        this.objectType = objectType;
        this.identifier = identifier;
    }
    
    public String getObjectType() {
        return objectType;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
        result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Bookmark other = (Bookmark) obj;
        if (identifier == null) {
            if (other.identifier != null)
                return false;
        } else if (!identifier.equals(other.identifier))
            return false;
        if (objectType == null) {
            if (other.objectType != null)
                return false;
        } else if (!objectType.equals(other.objectType))
            return false;
        return true;
    }

    /**
     * The canonical form of the {@link Bookmark}, that is &quot;{@link #getObjectType() objectType}{@value #SEPARATOR}{@link #getIdentifier()}&quot;.
     * 
     * <p>
     * This is parseable by the {@link #Bookmark(String) string constructor}.
     */
    @Override
    public String toString() {
        return objectType + SEPARATOR + identifier;
    }
}
