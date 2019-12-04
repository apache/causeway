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
package org.apache.isis.persistence.jdo.exceprecog;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;

public class ExceptionRecognizerForJDODataStoreException extends ExceptionRecognizerForType {

    public ExceptionRecognizerForJDODataStoreException() {
        super(Category.SERVER_ERROR,
                ofTypeExcluding(
                        javax.jdo.JDODataStoreException.class,
                        JdoNestedExceptionResolver::streamNestedExceptionsOf,
                        "NOT NULL check constraint"),
                prefix("Unable to save changes.  " +
                        "Does similar data already exist, or has referenced data been deleted?"));
    }
    


}
