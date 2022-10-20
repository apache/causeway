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
package org.apache.causeway.testing.fakedata.applib.services;

import org.apache.commons.lang3.RandomUtils;

/**
 * Returns random <code>long</code> values, optionally constrained within a range,
 *
 * @since 2.0 {@index}
 */
public class Longs extends AbstractRandomValueGenerator {

    public Longs(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    public long any() {
        return fake.booleans().coinFlip()
                ?   RandomUtils.nextLong()
                : - RandomUtils.nextLong();
    }

    public long upTo(final long upTo) {
        return fake.randomService.nextLong(upTo);
    }

    public long between(final long min, final long max) {
        return min + fake.randomService.nextLong(max - min);
    }

}
