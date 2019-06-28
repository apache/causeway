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
package org.apache.isis.core.commons.lang;

import org.apache.isis.metamodel.commons.StringExtensions;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class StringUtils_RemoveLeadingWhiteSpace {

    @Test
    public void whenHasLeadingWhiteSpace() {
        final String removed = StringExtensions.removeLeadingWhiteSpace(" 	 foo");
        Assert.assertThat(removed, CoreMatchers.is("foo"));
    }

    @Test
    public void whenNoLeadingWhiteSpace() {
        final String removed = StringExtensions.removeLeadingWhiteSpace("foo");
        Assert.assertThat(removed, CoreMatchers.is("foo"));
    }

    @Test
    public void empty() {
        final String removed = StringExtensions.removeLeadingWhiteSpace("");
        Assert.assertThat(removed, CoreMatchers.is(""));
    }

    @Test
    public void whenNull() {
        final String removed = StringExtensions.removeLeadingWhiteSpace(null);
        Assert.assertThat(removed, CoreMatchers.is(CoreMatchers.nullValue()));
    }

}
