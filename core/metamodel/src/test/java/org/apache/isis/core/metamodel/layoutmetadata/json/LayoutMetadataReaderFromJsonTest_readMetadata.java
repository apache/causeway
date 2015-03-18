/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.layoutmetadata.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.annotation.Render;
import org.apache.isis.core.metamodel.layoutmetadata.LayoutMetadata;

public class LayoutMetadataReaderFromJsonTest_readMetadata {
    
    private LayoutMetadataReaderFromJson reader;

    @Before
    public void setUp() throws Exception {
        reader = new LayoutMetadataReaderFromJson();
    }
    
    @Test
    public void happyCase() throws Exception {
        final LayoutMetadata metadata = reader.asLayoutMetadata(ExampleDomainObject.class);
        assertThat(metadata, is(not(nullValue())));
        assertThat(metadata.getColumns(), is(not(nullValue())));
        assertThat(metadata.getColumns().size(), is(4));
        assertThat(metadata.getColumns().get(0).span, is(3));
        assertThat(metadata.getColumns().get(1).span, is(4));
        assertThat(metadata.getColumns().get(2).span, is(0));
        assertThat(metadata.getColumns().get(3).span, is(5));
        
        assertThat(metadata.getColumns().get(0).memberGroups, is(not(nullValue())));
        assertThat(metadata.getColumns().get(0).memberGroups.size(), is(2));
        assertThat(metadata.getColumns().get(0).memberGroups.containsKey("General"), is(true));
        assertThat(metadata.getColumns().get(0).memberGroups.get("General").members, is(not(nullValue())));
        assertThat(metadata.getColumns().get(0).memberGroups.get("General").members.size(), is(3));
        assertThat(metadata.getColumns().get(0).memberGroups.get("General").members.containsKey("complete"), is(true));
        assertThat(metadata.getColumns().get(0).memberGroups.get("General").members.get("complete").actions, is(not(nullValue())));
        assertThat(metadata.getColumns().get(0).memberGroups.get("General").members.get("complete").actions.size(), is(2));
        assertThat(metadata.getColumns().get(0).memberGroups.get("General").members.get("complete").actions.containsKey("completed"), is(true));
        assertThat(metadata.getColumns().get(0).memberGroups.get("General").members.get("complete").propertyLayout.named, is(equalTo("the name of complete property")));
        assertThat(metadata.getColumns().get(0).memberGroups.get("General").members.get("complete").propertyLayout.namedEscaped, is(true));

        assertThat(metadata.getColumns().get(3).collections, is(not(nullValue())));
        assertThat(metadata.getColumns().get(3).collections.containsKey("dependencies"), is(true));
        assertThat(metadata.getColumns().get(3).collections.get("dependencies").actions, is(not(nullValue())));
        assertThat(metadata.getColumns().get(3).collections.get("dependencies").actions.size(), is(2));
        assertThat(metadata.getColumns().get(3).collections.get("dependencies").actions.containsKey("add"), is(true));
        assertThat(metadata.getColumns().get(3).collections.get("dependencies").paged, is(not(nullValue())));
        assertThat(metadata.getColumns().get(3).collections.get("dependencies").paged, is(not(nullValue())));
        assertThat(metadata.getColumns().get(3).collections.get("dependencies").render, is(not(nullValue())));
        assertThat(metadata.getColumns().get(3).collections.get("dependencies").render.value, is(nullValue()));
        
        assertThat(metadata.getColumns().get(3).collections.containsKey("similarItems"), is(true));
        assertThat(metadata.getColumns().get(3).collections.get("similarItems").render, is(not(nullValue())));
        assertThat(metadata.getColumns().get(3).collections.get("similarItems").render.value, is(Render.Type.LAZILY));

        assertThat(metadata.getActions(), is(not(nullValue())));
        assertThat(metadata.getActions().size(), is(2));
        assertThat(metadata.getActions().containsKey("delete"), is(true));

        assertThat(metadata.getColumns().get(0).memberGroups.get("Misc").members.get("versionSequence").propertyLayout.namedEscaped, is(false));
    }

}
