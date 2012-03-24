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

package org.apache.isis.runtimes.dflt.objectstores.dflt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Test;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.CreateObjectCommand;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction.PersistenceCommand;

public class InMemoryObjectStoreTest_debug extends InMemoryObjectStoreTestAbstract {


    @Test
    public void debugTitle() throws Exception {

        // when
        final String debugTitle = store.debugTitle();
        
        // then
        assertThat(debugTitle, is("In-Memory Object Store"));
    }


    @Test
    public void debugXxx_whenHasCommand() throws Exception {

        // given
        final CreateObjectCommand command = store.createCreateObjectCommand(adapter1);
        store.execute(Collections.<PersistenceCommand> singletonList(command));

        // when
        final DebugString debug = new DebugString();
        store.debugData(debug);
        
        // then
        assertThat(debug.toString(), is("\nDomain Objects\n--------------\norg.apache.isis.runtimes.dflt.testsupport.domain.TestPojo\n   TOID:CUS#1:                Pojo#24 (object 1)\n\n\n"));
    }

    @Test
    public void testEmpty() throws Exception {
        
        // when
        final DebugString debug = new DebugString();
        store.debugData(debug);
        
        // then
        assertThat(debug.toString(), is("\nDomain Objects\n--------------\n\n"));
    }
}
