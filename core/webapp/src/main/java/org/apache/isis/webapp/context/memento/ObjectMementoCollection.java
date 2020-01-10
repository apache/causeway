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
package org.apache.isis.webapp.context.memento;

import java.util.ArrayList;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;

import lombok.Getter;
import lombok.Value;

/**
 * 
 * @since 2.0
 *
 */
@Value(staticConstructor = "of")
public final class ObjectMementoCollection implements ObjectMemento {

    private static final long serialVersionUID = 1L;

    private final ArrayList<ObjectMemento> container; 
    @Getter(onMethod = @__({@Override})) private final ObjectSpecId objectSpecId;

    @Override
    public String asString() {
        return getContainer().toString();
    }

    @Override
    public Bookmark asHintingBookmarkIfSupported() {
        throw _Exceptions.notImplemented(); // please unwrap at call-site
    }

    @Override
    public Bookmark asBookmarkIfSupported() {
        throw _Exceptions.notImplemented(); // please unwrap at call-site
    }    

    public ArrayList<ObjectMemento> unwrapList() {
        return getContainer();
    }


}
