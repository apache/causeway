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

package org.apache.isis.runtimes.dflt.objectstores.nosql;

import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.core.commons.exceptions.UnexpectedCallException;

public class ExampleReferencePojo extends ExamplePojo {
    // {{ Reference1
    private ExampleValuePojo reference1;

    public ExampleValuePojo getReference1() {
        return reference1;
    }

    public void setReference1(final ExampleValuePojo reference1) {
        this.reference1 = reference1;
    }

    // }}

    // {{ Reference2
    private ExampleValuePojo reference2;

    public ExampleValuePojo getReference2() {
        return reference2;
    }

    public void setReference2(final ExampleValuePojo reference2) {
        this.reference2 = reference2;
    }

    // }}

    // {{ NotPersisted
    @NotPersisted
    public ExampleValuePojo getNotPersisted() {
        throw new UnexpectedCallException();
    }
    // }}

}
