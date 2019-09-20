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
package org.apache.isis.metamodel.specloader.validator;

import java.io.Serializable;
import java.util.Collection;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName="of")
public class MetaModelDeficiencies implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter private final Collection<String> validationErrors;

    public String getValidationErrorsAsString() {
        return concatenate(validationErrors);
    }

    // //////////////////////////////////////

    private static String concatenate(Collection<String> messages) {
        val buf = new StringBuilder();
        int i=0;
        for (val message : messages) {
            buf.append(++i).append(": ").append(message).append("\n");
        }
        return buf.toString();
    }
}
