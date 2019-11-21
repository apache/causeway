package org.isisaddons.module.fakedata.dom;

import java.util.Locale;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.clock.ClockService;

@DomainService(
        nature = NatureOfService.DOMAIN
)
public class FakeDataService {

    private Faker javaFaker;

    Random random;
    RandomService randomService;
    FakeValuesService fakeValuesService;

    @Programmatic
    @PostConstruct
    public void init() {

        random = RandomUtils.JVM_RANDOM;
        javaFaker = new Faker(random);

        randomService = new RandomService(random);
        fakeValuesService = new FakeValuesService(Locale.ENGLISH, randomService);

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
        this.jodaDateTimes = new JodaDateTimes(this);
        this.jodaLocalDates = new JodaLocalDates(this);
        this.jodaPeriods = new JodaPeriods(this);

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

    private JodaDateTimes jodaDateTimes;
    private JodaLocalDates jodaLocalDates;
    private JodaPeriods jodaPeriods;

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
    @Programmatic
    public Faker javaFaker() { return javaFaker; }

    // //////////////////////////////////////

    @Programmatic
    public Names name() {
        return names;
    }

    @Programmatic
    public Comms comms() {
        return comms;
    }

    @Programmatic
    public Lorem lorem() {
        return lorem;
    }

    @Programmatic
    public Addresses addresses() {
        return addresses;
    }

    @Programmatic
    public CreditCards creditCards() {
        return creditCards;
    }

    @Programmatic
    public Books books() {
        return books;
    }

    // //////////////////////////////////////

    @Programmatic
    public Bytes bytes() {
        return bytes;
    }

    @Programmatic
    public Shorts shorts() {
        return shorts;
    }

    @Programmatic
    public Integers ints() {
        return integers;
    }

    @Programmatic
    public Longs longs() {
        return longs;
    }

    @Programmatic
    public Floats floats() {
        return floats;
    }

    @Programmatic
    public Doubles doubles() {
        return doubles;
    }

    @Programmatic
    public Chars chars() {
        return chars;
    }

    @Programmatic
    public Booleans booleans() {
        return booleans;
    }

    // //////////////////////////////////////

    @Programmatic
    public Strings strings() {
        return strings;
    }

    // //////////////////////////////////////

    @Programmatic
    public Collections collections() {
        return collections;
    }

    @Programmatic
    public Enums enums() {
        return enums;
    }

    // //////////////////////////////////////

    @Programmatic
    public JavaUtilDates javaUtilDates() {
        return javaUtilDates;
    }

    @Programmatic
    public JavaSqlDates javaSqlDates() {
        return javaSqlDates;
    }

    @Programmatic
    public JavaSqlTimestamps javaSqlTimestamps() {
        return javaSqlTimestamps;
    }

    @Programmatic
    public JodaLocalDates jodaLocalDates() {
        return jodaLocalDates;
    }

    @Programmatic
    public JodaDateTimes jodaDateTimes() {
        return jodaDateTimes;
    }

    @Programmatic
    public JodaPeriods jodaPeriods() {
        return jodaPeriods;
    }

    // //////////////////////////////////////

    @Programmatic
    public BigDecimals bigDecimals() {
        return bigDecimals;
    }

    @Programmatic
    public BigIntegers bigIntegers() {
        return bigIntegers;
    }

    @Programmatic
    public Urls urls() {
        return urls;
    }

    @Programmatic
    public Uuids uuids() {
        return uuids;
    }

    // //////////////////////////////////////

    @Programmatic
    public IsisPasswords isisPasswords() {
        return isisPasswords;
    }

    @Programmatic
    public IsisMoneys isisMoneys() {
        return isisMoneys;
    }

    @Programmatic
    public IsisBlobs isisBlobs() {
        return isisBlobs;
    }

    @Programmatic
    public IsisClobs isisClobs() {
        return isisClobs;
    }

    // //////////////////////////////////////

    // //////////////////////////////////////

    @Inject
    ClockService clockService;

    @Inject
    DomainObjectContainer container;

}