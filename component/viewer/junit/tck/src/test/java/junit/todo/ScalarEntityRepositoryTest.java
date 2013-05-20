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

package junit.todo;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import junit.AbstractTest;

import org.junit.Ignore;
import org.junit.Test;

import org.apache.isis.core.integtestsupport.legacy.Fixture;
import org.apache.isis.core.integtestsupport.legacy.Fixtures;
import org.apache.isis.core.tck.dom.scalars.PrimitiveValuedEntity;
import org.apache.isis.core.tck.fixture.scalars.PrimitiveValuedEntityFixture;

@Fixtures({ @Fixture(PrimitiveValuedEntityFixture.class) })
public class ScalarEntityRepositoryTest extends AbstractTest {

    @Test
    public void dummy() {
    }

    @Ignore
    @Test
    public void canFindAllItemsNotYetDone() throws Exception {
        final List<PrimitiveValuedEntity> foobarList = primitivesEntityRepository.list();
        assertThat(foobarList.size(), is(5));
    }

    @Ignore
    @Test
    public void canCreateScalarEntityItem() throws Exception {
        final PrimitiveValuedEntity newItem = primitivesEntityRepository.newEntity();
        assertThat(newItem, is(not(nullValue())));
        assertThat(getDomainObjectContainer().isPersistent(newItem), is(true));
    }

}
