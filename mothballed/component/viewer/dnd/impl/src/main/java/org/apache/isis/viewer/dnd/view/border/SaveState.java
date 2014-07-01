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

package org.apache.isis.viewer.dnd.view.border;

class SaveState {
    StringBuffer missingFields = new StringBuffer();
    StringBuffer invalidFields = new StringBuffer();

    void addMissingField(final String parameterName) {
        if (missingFields.length() > 0) {
            missingFields.append(", ");
        }
        missingFields.append(parameterName);
    }

    void addInvalidField(final String parameterName) {
        if (invalidFields.length() > 0) {
            invalidFields.append(", ");
        }
        invalidFields.append(parameterName);
    }

    String getMessage() {
        String error = "";
        if (missingFields.length() > 0) {
            if (error.length() > 0) {
                error += "; ";
            }
            error += "Fields needed: " + missingFields;
        }
        if (invalidFields.length() > 0) {
            if (error.length() > 0) {
                error += "; ";
            }
            error += "Invalid fields: " + invalidFields;
        }
        return error;
    }
}
