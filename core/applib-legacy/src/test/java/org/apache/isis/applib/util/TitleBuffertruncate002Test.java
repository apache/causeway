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
package org.apache.isis.applib.util;

import org.junit.Assert;
import org.junit.rules.ExpectedException;
import org.junit.Test;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.diffblue.deeptestutils.CompareWithFieldList;
import com.diffblue.deeptestutils.Reflector;

public class TitleBuffertruncate002Test {

  @org.junit.Rule
  public ExpectedException thrown = ExpectedException.none();

  /* testedClasses: org/apache/isis/applib/util/TitleBuffer.java */
  /*
   * Test generated by Diffblue Deeptest.
   * This test case covers:
   * conditional line 447 branch to line 450
   * org/apache/isis/applib/util/TitleBuffer.java:457: loop: 1 iterations
   * iteration 1
   * conditional line 453 branch to line 454
   * conditional line 454 branch to line 457
   * conditional line 459 branch to line 464
   */

  @Test
  public void org_apache_isis_applib_util_TitleBuffer_truncate_002_bc48a3caeb173bd3() throws Throwable {

    org.apache.isis.applib.util.TitleBuffer retval;
    {
      /* Arrange */
      org.apache.isis.applib.util.TitleBuffer param_1 = (org.apache.isis.applib.util.TitleBuffer) Reflector.getInstance("org.apache.isis.applib.util.TitleBuffer");
      Reflector.setField(param_1, "title", new StringBuilder("`"));
      int noWords = 1;

      /* Act */
      retval = param_1.truncate(noWords);
    }
    {
      /* Assert result */
      Assert.assertNotNull(retval);
      Assert.assertNotNull(((StringBuilder) Reflector.getInstanceField(retval, "title")));
      Assert.assertNotNull(((StringBuilder) Reflector.getInstanceField(retval, "title")));
      Assert.assertEquals("`", ((StringBuilder) Reflector.getInstanceField(retval, "title")).toString());
    }
  }
}
