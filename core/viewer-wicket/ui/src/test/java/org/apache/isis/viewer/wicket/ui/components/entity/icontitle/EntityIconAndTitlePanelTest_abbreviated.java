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
package org.apache.isis.viewer.wicket.ui.components.entity.icontitle;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class EntityIconAndTitlePanelTest_abbreviated {

    @Test
    public void notTruncated() throws Exception {
        assertThat(EntityIconAndTitlePanel.abbreviated("abcdef", 6), is("abcdef"));
    }

    @Test
    public void truncated() throws Exception {
        assertThat(EntityIconAndTitlePanel.abbreviated("abcdefg", 6), is("abc..."));
    }

    @Test
    public void notTruncatedAtEllipsesLimit() throws Exception {
        assertThat(EntityIconAndTitlePanel.abbreviated("abc", 3), is("abc"));
        assertThat(EntityIconAndTitlePanel.abbreviated("ab", 2), is("ab"));
        assertThat(EntityIconAndTitlePanel.abbreviated("a", 1), is("a"));
    }

    @Test
    public void truncatedAtEllipsesLimit() throws Exception {
        assertThat(EntityIconAndTitlePanel.abbreviated("abcd", 3), is(""));
        assertThat(EntityIconAndTitlePanel.abbreviated("abc", 2), is(""));
        assertThat(EntityIconAndTitlePanel.abbreviated("ab", 1), is(""));
    }

}
