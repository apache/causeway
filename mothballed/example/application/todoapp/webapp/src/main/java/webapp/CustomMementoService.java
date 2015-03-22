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
package webapp;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.runtime.services.memento.MementoServiceDefault;


/**
 * Demonstrates how to register a replacement for one of the default framework-provided services.
 *
 * <p>
 *     In <code>isis.properties</code>, is registered using:
 * </p>
 *
 * <pre>
 *     isis.services = \
 *                     ...,
 *                     1:webapp.CustomMementoService,
 *                     ...,
 * </pre>
 * <p>
 *     that is, with a menuOrder of 1.
 * </p>
 */
public class CustomMementoService extends MementoServiceDefault {

    @Programmatic
    @Override
    public Memento create() {
        return super.create();
    }

    @Programmatic
    @Override
    public Memento parse(String str) {
        return super.parse(str);
    }

    @Programmatic
    @Override
    public boolean canSet(Object input) {
        return super.canSet(input);
    }
}
