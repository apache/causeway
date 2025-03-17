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
package org.apache.causeway.testing.fakedata.fixtures.demoapp.demomodule.dom;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.annotation.DomainObjectLayout;
import org.apache.causeway.applib.annotation.Editing;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Optionality;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Title;
import org.apache.causeway.applib.annotation.ValueSemantics;
import org.apache.causeway.applib.jaxb.PersistentEntityAdapter;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.applib.value.Password;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(
        schema = "libFakeDataFixture"
)
@DomainObject(editing = Editing.DISABLED)
@DomainObjectLayout(bookmarking = BookmarkPolicy.AS_ROOT)
@NoArgsConstructor
@XmlJavaTypeAdapter(PersistentEntityAdapter.class)
public class FakeDataDemoObjectWithAll implements Comparable<FakeDataDemoObjectWithAll> {

    public FakeDataDemoObjectWithAll(final String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY)
    @Version()
    @Getter
    long id;
    
    @Version
    private Long version;
    
    @Getter @Setter
    @jakarta.persistence.Column(nullable = false)
    @Title(sequence="1")
    @PropertyLayout(sequence="1")
    private String name;

    @Getter @Setter
    private boolean someBoolean;

    @Getter @Setter
    private byte someByte;

    @Getter @Setter
    private short someShort;

    @Getter @Setter
    @PropertyLayout(describedAs = "description of some int")
    private int someInt;

    @Getter @Setter
    private long someLong;

    @Getter @Setter
    private float someFloat;

    @Getter @Setter
    private double someDouble;

    @Getter @Setter
    private char someChar;

    @Column(nullable = true)
    @Getter @Setter
    private Boolean someBooleanWrapper;

    @Column(nullable = true)
    @Getter @Setter
    private Byte someByteWrapper;

    @Column(nullable = true)
    @Getter @Setter
    private Short someShortWrapper;

    @Column(nullable = true)
    @Getter @Setter
    private Integer someIntegerWrapper;

    @Column(nullable = true)
    @Getter @Setter
    private Long someLongWrapper;

    @Column(nullable = true)
    @Getter @Setter
    private Float someFloatWrapper;

    @Column(nullable = true)
    @Getter @Setter
    private Double someDoubleWrapper;

    @Column(nullable = true)
    @Getter @Setter
    private Character someCharacterWrapper;

    @Getter @Setter
    @Column(nullable = true)
    private String someString;

    @Column(nullable = true)
    @Getter @Setter
    @Property(optionality=Optionality.OPTIONAL)
    private Password somePassword;

//    @javax.jdo.annotations.Persistent(defaultFetchGroup="false", columns = {
//            @javax.jdo.annotations.Column(name = "someBlob_name"),
//            @javax.jdo.annotations.Column(name = "someBlob_mimetype"),
//            @javax.jdo.annotations.Column(name = "someBlob_bytes", jdbcType = "BLOB", sqlType = "LONGVARBINARY")
//    })
    @Getter @Setter
    @Property(optionality = Optionality.OPTIONAL)
    private Blob someBlob;

//    @javax.jdo.annotations.Persistent(defaultFetchGroup="false", columns = {
//            @javax.jdo.annotations.Column(name = "someClob_name"),
//            @javax.jdo.annotations.Column(name = "someClob_mimetype"),
//            @javax.jdo.annotations.Column(name = "someClob_chars", jdbcType = "CLOB", sqlType = "LONGVARCHAR")
//    })
    @Getter @Setter
    @Property(optionality=Optionality.OPTIONAL)
    private Clob someClob;

    @Column(nullable = true)
    @Getter @Setter
    private java.util.Date someJavaUtilDate;

    @Column(nullable = true)
    @Getter @Setter
    private java.sql.Date someJavaSqlDate;

    @Column(nullable = true)
    //@javax.jdo.annotations.Persistent(defaultFetchGroup="true")
    @Getter @Setter
    private org.joda.time.LocalDate someJodaLocalDate;

    @Column(nullable = true)
    //@javax.jdo.annotations.Persistent(defaultFetchGroup="true")
    @Getter @Setter
    private org.joda.time.DateTime someJodaDateTime;

    @Column(nullable = true)
    @Getter @Setter
    private java.sql.Timestamp someJavaSqlTimestamp;

    @Column(nullable = true)
    @Getter @Setter
    private BigInteger someBigInteger;

    @Column(nullable = true, length = 14, scale = 4)
    @Getter @Setter
    private BigDecimal someBigDecimal;

    @Column(nullable = true)
    @Getter @Setter
    private java.net.URL someUrl;

    @Column(nullable = true)
    @Getter @Setter
    private java.util.UUID someUuid;

    @Column(nullable = true)
    @Getter @Setter
    private EnumOf3 someEnumOf3;

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeBoolean(final boolean b) {
        setSomeBoolean(b);
        return this;
    }
    @MemberSupport public boolean default0UpdateSomeBoolean() {
        return isSomeBoolean();
    }

    @Action(semantics= SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeByte(final byte b) {
        setSomeByte(b);
        return this;
    }
    @MemberSupport public byte default0UpdateSomeByte() {
        return getSomeByte();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeShort(final short s) {
        setSomeShort(s);
        return this;
    }
    @MemberSupport public short default0UpdateSomeShort() {
        return getSomeShort();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeInt(final int i) {
        setSomeInt(i);
        return this;
    }
    @MemberSupport public int default0UpdateSomeInt() {
        return getSomeInt();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeLong(final long l) {
        setSomeLong(l);
        return this;
    }
    @MemberSupport public long default0UpdateSomeLong() {
        return getSomeLong();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeFloat(final float f) {
        setSomeFloat(f);
        return this;
    }
    @MemberSupport public float default0UpdateSomeFloat() {
        return getSomeFloat();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeDouble(final double d) {
        setSomeDouble(d);
        return this;
    }
    @MemberSupport public double default0UpdateSomeDouble() {
        return getSomeDouble();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeChar(final char i) {
        setSomeChar(i);
        return this;
    }
    @MemberSupport public char default0UpdateSomeChar() {
        return getSomeChar();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeBooleanWrapper(
            @Nullable
            final Boolean i) {
        setSomeBooleanWrapper(i);
        return this;
    }
    @MemberSupport public Boolean default0UpdateSomeBooleanWrapper() {
        return getSomeBooleanWrapper();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeBooleanWrapper() {
        setSomeBooleanWrapper(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeByteWrapper(final @Nullable Byte b) {
        setSomeByteWrapper(b);
        return this;
    }
    @MemberSupport public Byte default0UpdateSomeByteWrapper() {
        return getSomeByteWrapper();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeByteWrapper() {
        setSomeByteWrapper(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeShortWrapper(final @Nullable Short s) {
        setSomeShortWrapper(s);
        return this;
    }
    @MemberSupport public Short default0UpdateSomeShortWrapper() {
        return getSomeShortWrapper();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeShortWrapper() {
        setSomeShortWrapper(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeIntegerWrapper(final @Nullable Integer i) {
        setSomeIntegerWrapper(i);
        return this;
    }
    @MemberSupport public Integer default0UpdateSomeIntegerWrapper() {
        return getSomeIntegerWrapper();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeIntegerWrapper() {
        setSomeIntegerWrapper(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeLongWrapper(final @Nullable Long l) {
        setSomeLongWrapper(l);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeLongWrapper() {
        setSomeLongWrapper(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeFloatWrapper(final @Nullable Float f) {
        setSomeFloatWrapper(f);
        return this;
    }
    @MemberSupport public Float default0UpdateSomeFloatWrapper() {
        return getSomeFloatWrapper();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeFloatWrapper() {
        setSomeFloatWrapper(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeDoubleWrapper(final @Nullable Double d) {
        setSomeDoubleWrapper(d);
        return this;
    }
    @MemberSupport public Double default0UpdateSomeDoubleWrapper() {
        return getSomeDoubleWrapper();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeDoubleWrapper() {
        setSomeDoubleWrapper(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeCharacterWrapper(final @Nullable Character i) {
        setSomeCharacterWrapper(i);
        return this;
    }
    @MemberSupport public Character default0UpdateSomeCharacterWrapper() {
        return getSomeCharacterWrapper();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeCharacterWrapper() {
        setSomeCharacterWrapper(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeString(final @Nullable String i) {
        setSomeString(i);
        return this;
    }
    @MemberSupport public String default0UpdateSomeString() {
        return getSomeString();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeString() {
        setSomeString(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomePassword(final @Nullable Password password) {
        setSomePassword(password);
        return this;
    }
    @MemberSupport public Password default0UpdateSomePassword() {
        return getSomePassword();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomePassword() {
        setSomePassword(null);
        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeBlob(
            @Nullable
            final Blob blob) {
        setSomeBlob(blob);
        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeBlob() {
        setSomeBlob(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeClob(final @Nullable Clob clob) {
        setSomeClob(clob);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeClob() {
        setSomeClob(null);
        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeJavaUtilDate(final java.util.@Nullable Date i) {
        setSomeJavaUtilDate(i);
        return this;
    }
    @MemberSupport public java.util.Date default0UpdateSomeJavaUtilDate() {
        return getSomeJavaUtilDate();
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeJavaUtilDate() {
        setSomeJavaUtilDate(null);
        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeJavaSqlDate(final java.sql.@Nullable Date i) {
        setSomeJavaSqlDate(i);
        return this;
    }
    @MemberSupport public java.sql.Date default0UpdateSomeJavaSqlDate() {
        return getSomeJavaSqlDate();
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeJavaSqlDate() {
        setSomeJavaSqlDate(null);
        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeJodaLocalDate(final @Nullable LocalDate i) {
        setSomeJodaLocalDate(i);
        return this;
    }
    @MemberSupport public org.joda.time.LocalDate default0UpdateSomeJodaLocalDate() {
        return getSomeJodaLocalDate();
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeJodaLocalDate() {
        setSomeJodaLocalDate(null);
        return this;
    }

    @Column(nullable = true)
    @Getter @Setter
    private org.joda.time.LocalDateTime someJodaLocalDateTime;

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeJodaLocalDateTime(final LocalDateTime i) {
        setSomeJodaLocalDateTime(i);
        return this;
    }
    @MemberSupport public org.joda.time.LocalDateTime default0UpdateSomeJodaLocalDateTime() {
        return getSomeJodaLocalDateTime();
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeJodaLocalDateTime() {
        setSomeJodaLocalDateTime(null);
        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeJodaDateTime(final @Nullable DateTime i) {
        setSomeJodaDateTime(i);
        return this;
    }
    @MemberSupport public org.joda.time.DateTime default0UpdateSomeJodaDateTime() {
        return getSomeJodaDateTime();
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeJodaDateTime() {
        setSomeJodaDateTime(null);
        return this;
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeJavaSqlTimestamp(final @Nullable Timestamp i) {
        setSomeJavaSqlTimestamp(i);
        return this;
    }
    @MemberSupport public java.sql.Timestamp default0UpdateSomeJavaSqlTimestamp() {
        return getSomeJavaSqlTimestamp();
    }

    @Action(semantics = SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeJavaSqlTimestamp() {
        setSomeJavaSqlTimestamp(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeBigInteger(
            @Nullable
            final BigInteger d) {
        setSomeBigInteger(d);
        return this;
    }
    @MemberSupport public BigInteger default0UpdateSomeBigInteger() {
        return getSomeBigInteger();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeBigInteger() {
        setSomeBigInteger(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeBigDecimal(
            @Nullable @ValueSemantics(maxTotalDigits = 14, maxFractionalDigits = 4) final BigDecimal d) {
        setSomeBigDecimal(d);
        return this;
    }
    @MemberSupport public BigDecimal default0UpdateSomeBigDecimal() {
        return getSomeBigDecimal();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeBigDecimal() {
        setSomeBigDecimal(null);
        return this;
    }

    @Action(semantics= SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeUrl(final @Nullable URL i) {
        setSomeUrl(i);
        return this;
    }
    @MemberSupport public java.net.URL default0UpdateSomeUrl() {
        return getSomeUrl();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeUrl() {
        setSomeUrl(null);
        return this;
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeUuid(final @Nullable UUID i) {
        setSomeUuid(i);
        return this;
    }
    @MemberSupport public java.util.UUID default0UpdateSomeUuid() {
        return getSomeUuid();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeUuid() {
        setSomeUuid(null);
        return this;
    }

  //TODO[2249] deprecated
//    @Action(semantics=SemanticsOf.IDEMPOTENT)
//    public FakeDataDemoObjectWithAll updateSomeMoney(final @Nullable Money i) {
//        setSomeMoney(i);
//        return this;
//    }
//    public Money default0UpdateSomeMoney() {
//        return getSomeMoney();
//    }
//
//    @Action(semantics=SemanticsOf.IDEMPOTENT)
//    public FakeDataDemoObjectWithAll resetSomeMoney() {
//        setSomeMoney(null);
//        return this;
//    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll updateSomeEnumOf3(final @Nullable EnumOf3 i) {
        setSomeEnumOf3(i);
        return this;
    }
    @MemberSupport public EnumOf3 default0UpdateSomeEnumOf3() {
        return getSomeEnumOf3();
    }

    @Action(semantics=SemanticsOf.IDEMPOTENT)
    public FakeDataDemoObjectWithAll resetSomeEnumOf3() {
        setSomeEnumOf3(null);
        return this;
    }

    private static final Comparator<FakeDataDemoObjectWithAll> comparator =
            Comparator.nullsFirst(
                    Comparator.comparing(FakeDataDemoObjectWithAll::getName));

    @Override
    public int compareTo(final FakeDataDemoObjectWithAll other) {
        return comparator.compare(this, other);
    }

}
