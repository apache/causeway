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
package org.apache.isis.testdomain.util.dto;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@XmlRootElement(name = "book")
@XmlAccessorType(XmlAccessType.PROPERTY)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BookDto {

    private String name;
    private String description;
    private double price;

    private String author;
    private String isbn;
    private String publisher;

    public static BookDto from(final IBook book) {
        return BookDto.builder()
        .author(book.getAuthor())
        .description(book.getDescription())
        .isbn(book.getIsbn())
        .name(book.getName())
        .price(book.getPrice())
        .publisher(book.getPublisher())
        .build();
    }

    @Programmatic
    public BookDtoBuilder asBuilder() {
        return BookDto.builder()
        .author(this.getAuthor())
        .description(this.getDescription())
        .isbn(this.getIsbn())
        .name(this.getName())
        .price(this.getPrice())
        .publisher(this.getPublisher());
    }

    public static Stream<BookDto> samples() {

        return Stream.of(
                BookDto.builder()
                .author("Frank Herbert")
                .description("A sample book for testing. (1)")
                .isbn("ISBN-A")
                .name("Dune")
                .price(39.)
                .publisher("Sample Publisher (1)")
                .build(),
                BookDto.builder()
                .author("Isaac Asimov")
                .description("A sample book for testing. (2)")
                .isbn("ISBN-B")
                .name("The Foundation")
                .price(29.)
                .publisher("Sample Publisher (2)")
                .build(),
                BookDto.builder()
                .author("Herbert George Wells")
                .description("A sample book for testing. (3)")
                .isbn("ISBN-C")
                .name("The Time Machine")
                .price(99.)
                .publisher("Sample Publisher (3)")
                .build()
                );
    }

    public static BookDto sample() {
        return samples().findFirst().orElseThrow();
    }


    @Programmatic
    public String encode() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(BookDto.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(this, sw);
        String newBookXml = sw.toString();

        String encoded = _Strings.convert(newBookXml, _Bytes.asCompressedUrlBase64, StandardCharsets.UTF_8);

        return encoded;
    }

    public static BookDto decode(final String encoded) throws JAXBException {
        String bookXml = _Strings.convert(encoded, _Bytes.ofCompressedUrlBase64, StandardCharsets.UTF_8);

        JAXBContext jaxbContext = JAXBContext.newInstance(BookDto.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        BookDto bookDto = (BookDto) jaxbUnmarshaller.unmarshal(new StringReader(bookXml));

        return bookDto;
    }


}
