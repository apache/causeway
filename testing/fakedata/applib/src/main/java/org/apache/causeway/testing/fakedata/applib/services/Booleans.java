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

/**
 * Returns random <code>boolean</code> values with different probabilities.
 *
 * @since 2.0 {@index}
 */
public class Booleans extends AbstractRandomValueGenerator {

    public Booleans(final FakeDataService fakeDataService) {
        super(fakeDataService);
    }

    /**
     * Same as {@link #any()}, 50:50 chance of true or false.
     */
    public boolean coinFlip() {
        return any();
    }

    /**
     * Same as {@link #any()}, 50:50 chance of true or false.
     */
    public boolean either() {
        return any();
    }

    /**
     * A 1 in 6 chance of returning true.
     */
    public boolean diceRollOf6() {
        return fake.ints().upTo(6) == 5;
    }

    /**
     * 50:50 chance of true or false.
     */
    public boolean any() {
        return fake.randomService.nextDouble() < 0.5;
    }
}
