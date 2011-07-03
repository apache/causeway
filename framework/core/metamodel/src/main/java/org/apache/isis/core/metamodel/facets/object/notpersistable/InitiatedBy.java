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

package org.apache.isis.core.metamodel.facets.object.notpersistable;

import org.apache.isis.applib.annotation.NotPersistable;
import org.apache.isis.applib.marker.NonPersistable;
import org.apache.isis.applib.marker.ProgramPersistable;
import org.apache.isis.core.metamodel.facets.EnumerationAbstract;

public final class InitiatedBy extends EnumerationAbstract {

    public static InitiatedBy USER = new InitiatedBy(0, "USER", "User");
    public static InitiatedBy USER_OR_PROGRAM = new InitiatedBy(1, "USER_OR_PROGRAM", "User or Program");

    private InitiatedBy(final int num, final String nameInCode, final String friendlyName) {
        super(num, nameInCode, friendlyName);
    }

    public static InitiatedBy decodeBy(final NotPersistable.By by) {
        if (by == NotPersistable.By.USER) {
            return USER;
        }
        if (by == NotPersistable.By.USER_OR_PROGRAM) {
            return USER_OR_PROGRAM;
        }
        return null;
    }

    public static InitiatedBy forCorrespondingMarkerSubType(final Class<?> cls) {
        InitiatedBy initiatedBy = null;
        if (ProgramPersistable.class.isAssignableFrom(cls)) {
            initiatedBy = USER;
        } else if (NonPersistable.class.isAssignableFrom(cls)) {
            initiatedBy = USER_OR_PROGRAM;
        }
        return initiatedBy;
    }

}
