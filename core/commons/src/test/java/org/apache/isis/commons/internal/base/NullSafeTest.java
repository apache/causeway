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

package org.apache.isis.commons.internal.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

public class NullSafeTest {

	@Test
	public void isEmptyString() throws Exception {
		Assert.assertThat(_NullSafe.isEmpty((String)null), is(true));
		Assert.assertThat(_NullSafe.isEmpty(""), is(true));
		Assert.assertThat(_NullSafe.isEmpty(" 12 aBc"), is(false));
	}
	
	@Test
	public void isEmptyCollection() throws Exception {
		Assert.assertThat(_NullSafe.isEmpty((Collection<?>)null), is(true));
		Assert.assertThat(_NullSafe.isEmpty(Collections.emptyList()), is(true));
		Assert.assertThat(_NullSafe.isEmpty(Arrays.asList(new String[] {"foo", "bar"})), is(false));
	}
	
	@Test
	public void absence() throws Exception {
		Assert.assertThat(_NullSafe.isAbsent(null), is(true));
		Assert.assertThat(_NullSafe.isAbsent(""), is(false));
	}
	
	@Test
	public void presence() throws Exception {
		Assert.assertThat(_NullSafe.isPresent(null), is(false));
		Assert.assertThat(_NullSafe.isPresent(""), is(true));
	}
	
	
	@Test
	public void emptyStreamWithArray() throws Exception {
		
		Assert.assertNotNull(_NullSafe.stream((String[])null));
		
		Assert.assertNotNull(_NullSafe.stream(_Strings.emptyArray));
		Assert.assertEquals(0L, _NullSafe.stream(_Strings.emptyArray).count());
	}
	
	@Test
	public void streamWithArray() throws Exception {
		Assert.assertThat(
				_NullSafe.stream(new String[] {"foo", "bar"})
				.collect(Collectors.joining("|")),
				is("foo|bar"));
	}
	
	@Test
	public void emptyStreamWithCollection() throws Exception {
		
		Assert.assertNotNull(_NullSafe.stream((List<?>)null));
		
		Assert.assertNotNull(_NullSafe.stream(Arrays.asList(_Strings.emptyArray)));
		Assert.assertEquals(0L, _NullSafe.stream(Arrays.asList(_Strings.emptyArray)).count());
	}
	
	@Test
	public void streamWithCollection() throws Exception {
		Assert.assertThat(
				_NullSafe.stream(Arrays.asList(new String[] {"foo", "bar"}))
				.collect(Collectors.joining("|")),
				is("foo|bar"));
	}
	
	@Test
	public void emptyStreamWithIterator() throws Exception {
		
		Assert.assertNotNull(_NullSafe.stream((Iterator<?>)null));
		
		Assert.assertNotNull(_NullSafe.stream(Arrays.asList(_Strings.emptyArray)).iterator());
		Assert.assertEquals(0L, _NullSafe.stream(Arrays.asList(_Strings.emptyArray).iterator()).count());
	}
	
	@Test
	public void streamWithIterator() throws Exception {
		Assert.assertThat(
				_NullSafe.stream(Arrays.asList(new String[] {"foo", "bar"}).iterator())
				.collect(Collectors.joining("|")),
				is("foo|bar"));
	}

	
	
}
