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
package org.apache.causeway.core.metamodel.commons;

import lombok.experimental.UtilityClass;
import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

@UtilityClass
public class UtilStr {

    /**
     * String representation of bookmark for entities (otherwise empty string is returned).
     */
    public static String entityAsStr(Bookmark bookmark, SpecificationLoader specificationLoader) {
        var logicalTypeName = bookmark.getLogicalTypeName();
        var isEntity = specificationLoader
                            .lookupLogicalType(logicalTypeName)
                            .flatMap(specificationLoader::specForLogicalType)
                            .map(ObjectSpecification::isEntity)
                            .orElse(false);
        return isEntity
                    ? bookmark.stringify()
                    : "";
    }

    public static String namedArgStr(
            final String paramName,
            final Optional<ManagedObject> managedObjectIfany) {
        if (isSensitiveName(paramName)) {
            return "********";
        }
        if(managedObjectIfany.isEmpty()) {
            return "<none>";
        }
        var managedObject = managedObjectIfany.get();
        return namedArgStr(paramName, managedObject);
    }

    public static String namedArgStr(String paramName, ManagedObject managedObject) {
        if (isSensitiveName(paramName)) {
            return "********";
        }
        if(managedObject.getSpecialization().isEmpty()) {
            return "<none>";
        }
        return managedObject.getTitle();
    }

    private static boolean isSensitiveName(String name) {
        return name.equalsIgnoreCase("password") ||
                name.equalsIgnoreCase("secret") ||
                name.equalsIgnoreCase("apikey") ||
                name.equalsIgnoreCase("token");
    }

}
