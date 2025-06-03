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

import java.util.Locale;
import java.util.Random;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.repository.RepositoryService;

/**
 * The main entry point for the fake data library, a domain service that provides the ability to obtain
 * a random value for multiple different types.
 *
 * <p>
 *     Also provides {@link #javaFaker() access} to the {@link Faker} class which provides many more random
 *     instances, some relating to movies and tv shows.
 * </p>
 *
 *
 * @since 2.0 {@index}
 */
@Service
@Named("causeway.testing.FakeDataService")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class FakeDataService {

    final ClockService clockService;
    final RepositoryService repositoryService;

    final Faker javaFaker;
    final RandomService randomService;
    final FakeValuesService fakeValuesService;

    @Inject
    public FakeDataService(
            final ClockService clockService,
            final RepositoryService repositoryService) {
        this(clockService, repositoryService, new Random());
    }

    protected FakeDataService(
            final ClockService clockService,
            final RepositoryService repositoryService,
            final Random random) {
        this.clockService = clockService;
        this.repositoryService = repositoryService;
        this.javaFaker = new Faker(random);
        this.randomService = new RandomService(random);
        this.fakeValuesService = new FakeValuesService(Locale.ENGLISH, randomService);
    }

    @PostConstruct
    public void init() {

        // (slightly refactored) wrappers for the javafaker subclasses
        this.names = new Names(this);
        this.comms = new Comms(this);
        this.lorem = new Lorem(this);
        this.addresses = new Addresses(this);
        this.creditCards = new CreditCards(this, fakeValuesService);
        this.books = new Books(this);

        this.strings = new Strings(this);
        this.bytes = new Bytes(this);
        this.shorts = new Shorts(this);
        this.integers = new Integers(this);
        this.longs = new Longs(this);
        this.floats = new Floats(this);
        this.doubles = new Doubles(this);
        this.chars = new Chars(this);
        this.booleans = new Booleans(this);

        this.collections = new Collections(this);
        this.enums = new Enums(this);

        this.javaUtilDates = new JavaUtilDates(this);
        this.javaSqlDates = new JavaSqlDates(this);
        this.javaSqlTimestamps = new JavaSqlTimestamps(this);

        this.zonedDateTimes = new ZonedDateTimes(this);
        this.offsetDateTimes = new OffsetDateTimes(this);
        this.localDates = new LocalDates(this);
        this.periods = new Periods(this);

        this.bigDecimals = new BigDecimals(this);
        this.bigIntegers = new BigIntegers(this);
        this.urls = new Urls(this);
        this.uuids = new Uuids(this);

        this.causewayPasswords = new CausewayPasswords(this);
        //this.causewayMoneys = new CausewayMoneys(this);
        this.causewayBlobs = new CausewayBlobs(this);
        this.causewayClobs = new CausewayClobs(this);
    }

    private Names names;
    private Comms comms;
    private Lorem lorem;
    private Addresses addresses;
    private CreditCards creditCards;
    private Books books;

    private Strings strings;
    private Bytes bytes;
    private Shorts shorts;
    private Integers integers;
    private Longs longs;
    private Floats floats;
    private Doubles doubles;
    private Chars chars;
    private Booleans booleans;

    private Collections collections;
    private Enums enums;

    private JavaUtilDates javaUtilDates;
    private JavaSqlDates javaSqlDates;
    private JavaSqlTimestamps javaSqlTimestamps;

    private ZonedDateTimes zonedDateTimes;
    private OffsetDateTimes offsetDateTimes;
    private LocalDates localDates;
    private Periods periods;

    private BigDecimals bigDecimals;
    private BigIntegers bigIntegers;
    private Urls urls;
    private Uuids uuids;

    private CausewayPasswords causewayPasswords;
    //private CausewayMoneys causewayMoneys;
    private CausewayBlobs causewayBlobs;
    private CausewayClobs causewayClobs;

    /**
     * Access to the full API of the underlying javafaker library.
     */
    public Faker javaFaker() { return javaFaker; }

    public Names name() {
        return names;
    }

    public Comms comms() {
        return comms;
    }

    public Lorem lorem() {
        return lorem;
    }

    public Addresses addresses() {
        return addresses;
    }

    public CreditCards creditCards() {
        return creditCards;
    }

    public Books books() {
        return books;
    }

    public Bytes bytes() {
        return bytes;
    }

    public Shorts shorts() {
        return shorts;
    }

    public Integers ints() {
        return integers;
    }

    public Longs longs() {
        return longs;
    }

    public Floats floats() {
        return floats;
    }

    public Doubles doubles() {
        return doubles;
    }

    public Chars chars() {
        return chars;
    }

    public Booleans booleans() {
        return booleans;
    }

    public Strings strings() {
        return strings;
    }

    public Collections collections() {
        return collections;
    }

    public Enums enums() {
        return enums;
    }

    public JavaUtilDates javaUtilDates() {
        return javaUtilDates;
    }

    public JavaSqlDates javaSqlDates() {
        return javaSqlDates;
    }

    public JavaSqlTimestamps javaSqlTimestamps() {
        return javaSqlTimestamps;
    }

    public LocalDates localDates() {
        return localDates;
    }

    public OffsetDateTimes offsetDateTimes() {
        return offsetDateTimes;
    }

    public ZonedDateTimes zonedDateTimes() {
        return zonedDateTimes;
    }

    public Periods periods() {
        return periods;
    }

    public BigDecimals bigDecimals() {
        return bigDecimals;
    }

    public BigIntegers bigIntegers() {
        return bigIntegers;
    }

    public Urls urls() {
        return urls;
    }

    public Uuids uuids() {
        return uuids;
    }

    public CausewayPasswords causewayPasswords() {
        return causewayPasswords;
    }

    public CausewayBlobs causewayBlobs() {
        return causewayBlobs;
    }

    public CausewayClobs causewayClobs() {
        return causewayClobs;
    }

}
