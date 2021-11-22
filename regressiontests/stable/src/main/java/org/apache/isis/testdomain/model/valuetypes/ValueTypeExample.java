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
package org.apache.isis.testdomain.model.valuetypes;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.applib.value.LocalResourcePath;
import org.apache.isis.applib.value.Markup;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.applib.value.Password;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

public abstract class ValueTypeExample<T> {

    @Property
    public abstract T getValue();
    public abstract void setValue(T value);

    @Programmatic
    public abstract T getUpdateValue();

    @Action
    public final void updateValue(final T value) {
        setValue(value);
    }

    @Collection
    public final List<T> getValues() {
        return List.of(getValue(), getUpdateValue());
    }

    @SuppressWarnings("unchecked")
    @Programmatic
    public final Class<T> getValueType() {
        return (Class<T>) getValue().getClass();
    }

    // -- EXAMPLES - BASIC

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleBoolean",
            nature = Nature.BEAN)
    public static class ValueTypeExampleBoolean
    extends ValueTypeExample<Boolean> {

        @Property @Getter @Setter
        private Boolean value = Boolean.TRUE;

        @Getter
        private Boolean updateValue = Boolean.FALSE;

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleCharacter",
            nature = Nature.BEAN)
    public static class ValueTypeExampleCharacter
    extends ValueTypeExample<Character> {

        @Property @Getter @Setter
        private Character value = 'a';

        @Getter
        private Character updateValue = 'b';

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleString",
            nature = Nature.BEAN)
    public static class ValueTypeExampleString
    extends ValueTypeExample<String> {

        @Property @Getter @Setter
        private String value = "aString";

        @Getter
        private String updateValue = "anotherString";

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExamplePassword",
            nature = Nature.BEAN)
    public static class ValueTypeExamplePassword
    extends ValueTypeExample<Password> {

        @Property @Getter @Setter
        private Password value = Password.of("aPassword");

        @Getter
        private Password updateValue = Password.of("anotherPassword");

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleBufferedImage",
            nature = Nature.BEAN)
    public static class ValueTypeExampleBufferedImage
    extends ValueTypeExample<BufferedImage> {

        @Property @Getter @Setter
        private BufferedImage value = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);

        @Getter
        private BufferedImage updateValue = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleBlob",
            nature = Nature.BEAN)
    public static class ValueTypeExampleBlob
    extends ValueTypeExample<Blob> {

        @Property @Getter @Setter
        private Blob value = Blob.of("aBlob", CommonMimeType.BIN, new byte[] {1, 2, 3});

        @Getter
        private Blob updateValue = Blob.of("anotherBlob", CommonMimeType.BIN, new byte[] {3, 4});

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleClob",
            nature = Nature.BEAN)
    public static class ValueTypeExampleClob
    extends ValueTypeExample<Clob> {

        @Property @Getter @Setter
        private Clob value = Clob.of("aClob", CommonMimeType.TXT, "abc");

        @Getter
        private Clob updateValue = Clob.of("anotherClob", CommonMimeType.TXT, "ef");

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleLocalResourcePath",
            nature = Nature.BEAN)
    public static class ValueTypeExampleLocalResourcePath
    extends ValueTypeExample<LocalResourcePath> {

        @Property @Getter @Setter
        private LocalResourcePath value = new LocalResourcePath("img/a");

        @Getter
        private LocalResourcePath updateValue = new LocalResourcePath("img/b");

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleUrl",
            nature = Nature.BEAN)
    public static class ValueTypeExampleUrl
    extends ValueTypeExample<URL> {

        @Property @Getter @Setter
        private URL value = url("https://a.b.c");

        @Getter
        private URL updateValue = url("https://b.c.d");

        @SneakyThrows
        private static URL url(final String url) {
            return new URL(url);
        }

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleMarkup",
            nature = Nature.BEAN)
    public static class ValueTypeExampleMarkup
    extends ValueTypeExample<Markup> {

        @Property @Getter @Setter
        private Markup value = Markup.valueOf("aMarkup");

        @Getter
        private Markup updateValue = Markup.valueOf("anotherMarkup");

    }

    @DomainObject(
            logicalTypeName = "isis.testdomain.valuetypes.ValueTypeExampleUuid",
            nature = Nature.BEAN)
    public static class ValueTypeExampleUuid
    extends ValueTypeExample<UUID> {

        @Property @Getter @Setter
        private UUID value = UUID.randomUUID();

        @Getter
        private UUID updateValue = UUID.randomUUID();

    }


    // -- EXAMPLES - NUMBERS

//TODO  Byte
//TODO  Short
//TODO  Integer
//TODO  Long
//TODO  Float
//TODO  Double
//TODO    BigDecimal
//TODO    BigInteger


    // -- EXAMPLES - TEMPORAL

//TODO  Date
//TODO  DateTime
//TODO  LocalDate
//TODO  LocalDateTime
//TODO  LocalTime
//TODO  Time
//TODO  Timestamp
//TODO  OffsetDateTime
//TODO  OffsetTime
//TODO  ZonedDateTime


 // -- EXAMPLES - OTHER

//TODO    Bookmark
//TODO    OidDto

//TODO    ChangesDto
//TODO    CommandDto
//TODO    InteractionDto

//TODO    TreeNode


}
