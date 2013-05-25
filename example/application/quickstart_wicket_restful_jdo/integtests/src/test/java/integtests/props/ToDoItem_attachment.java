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
package integtests.props;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import integtests.AbstractIntegTest;

import java.nio.charset.Charset;
import java.util.List;

import javax.activation.MimeType;

import dom.todo.ToDoItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.value.Blob;

public class ToDoItem_attachment extends AbstractIntegTest {

    private ToDoItem toDoItem;
    private Blob attachment;

    @Before
    public void setUp() throws Exception {
        // given
        final List<ToDoItem> all = wrap(toDoItems).notYetComplete();
        toDoItem = wrap(all.get(0));

        // to reset after
        attachment = toDoItem.getAttachment();
    }

    @After
    public void tearDown() throws Exception {
        unwrap(toDoItem).setAttachment(attachment);
    }

    @Test
    public void happyCase() throws Exception {
        
        byte[] bytes = "{\"foo\": \"bar\"}".getBytes(Charset.forName("UTF-8"));
        final Blob newAttachment = new Blob("myfile.json", new MimeType("application/json"), bytes);
        
        // when
        toDoItem.setAttachment(newAttachment);
        
        // then
        assertThat(toDoItem.getAttachment(), is(newAttachment));
    }

    @Test
    public void canBeNull() throws Exception {
        
        // when
        toDoItem.setAttachment((Blob)null);
        
        // then
        assertThat(toDoItem.getAttachment(), is((Blob)null));
    }

    
}