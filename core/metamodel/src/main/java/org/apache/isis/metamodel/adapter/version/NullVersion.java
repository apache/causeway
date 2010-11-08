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


package org.apache.isis.metamodel.adapter.version;

import java.util.Date;

import org.apache.isis.core.commons.exceptions.UnexpectedCallException;


public class NullVersion implements Version {

	
    public boolean different(final Version version) {
        return false;
    }

    public Version next(final String user, final Date time) {
        throw new UnexpectedCallException();
    }

    public String getUser() {
        return "";
    }

    public Date getTime() {
        return new Date();
    }

    public String sequence() {
        return "";
    }
}
