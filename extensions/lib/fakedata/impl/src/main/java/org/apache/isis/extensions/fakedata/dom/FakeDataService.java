package org.apache.isis.extensions.fakedata.dom;

import java.util.Locale;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.repository.RepositoryService;

@DomainService(nature = NatureOfService.DOMAIN)
public class FakeDataService {

    private final Faker javaFaker;

    final RandomService randomService;
    final FakeValuesService fakeValuesService;

    public FakeDataService() {
        this(new Random());
    }

    FakeDataService(
            final Random random) {
        this(random, new Faker(random));
    }

    FakeDataService(
            final Random random,
            final Faker javaFaker) {
        this(random, javaFaker, new RandomService(random));
    }

    FakeDataService(
            final Random random,
            final Faker javaFaker,
            final RandomService randomService) {
        this(random, javaFaker, randomService, new FakeValuesService(Locale.ENGLISH, randomService));
    }

    FakeDataService(
            final Random random,
            final Faker javaFaker,
            final RandomService randomService,
            final FakeValuesService fakeValuesService) {
        this.javaFaker = javaFaker;
        this.randomService = randomService;
        this.fakeValuesService = fakeValuesService;
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
        this.jodaDateTimes = new J8DateTimes(this);
        this.jodaLocalDates = new J8LocalDates(this);
        this.jodaPeriods = new J8Periods(this);

        this.bigDecimals = new BigDecimals(this);
        this.bigIntegers = new BigIntegers(this);
        this.urls = new Urls(this);
        this.uuids = new Uuids(this);

        this.isisPasswords = new IsisPasswords(this);
        this.isisMoneys = new IsisMoneys(this);
        this.isisBlobs = new IsisBlobs(this);
        this.isisClobs = new IsisClobs(this);
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

    private J8DateTimes jodaDateTimes;
    private J8LocalDates jodaLocalDates;
    private J8Periods jodaPeriods;

    private BigDecimals bigDecimals;
    private BigIntegers bigIntegers;
    private Urls urls;
    private Uuids uuids;

    private IsisPasswords isisPasswords;
    private IsisMoneys isisMoneys;
    private IsisBlobs isisBlobs;
    private IsisClobs isisClobs;


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

    public J8LocalDates jodaLocalDates() {
        return jodaLocalDates;
    }

    public J8DateTimes jodaDateTimes() {
        return jodaDateTimes;
    }

    public J8Periods jodaPeriods() {
        return jodaPeriods;
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

    public IsisPasswords isisPasswords() {
        return isisPasswords;
    }

    public IsisMoneys isisMoneys() {
        return isisMoneys;
    }

    public IsisBlobs isisBlobs() {
        return isisBlobs;
    }

    public IsisClobs isisClobs() {
        return isisClobs;
    }

    @Inject ClockService clockService;

    @Inject RepositoryService repositoryService;

}