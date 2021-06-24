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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
class _DomainObjectIcons {

    public Optional<byte[]> loadIcon(
            final @Nullable ManagedObject object) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(object)) {
            return Optional.empty();
        }

        val spec = object.getSpecification();

        return Optional.ofNullable(
                loadIcon(
                        spec.getCorrespondingClass(),
                        spec.getIconName(object)
                        ));
    }

    //TODO[2761] that's a naive implementation; refactor the Wicket Viewer's icon resolving logic into a reusable utility or service
    private byte[] loadIcon(
            final Class<?> domainClass,
            final String iconNameModifier) {

        val iconResourceName = _Strings.isNotEmpty(iconNameModifier)
                ? domainClass.getSimpleName() + "-" + iconNameModifier
                : domainClass.getSimpleName();

        try {
            final InputStream resource = _Resources
                    .load(domainClass, iconResourceName + ".png");
            return _Bytes.of(resource);

        } catch (IOException e) {
            return null;
        }
    }

}
