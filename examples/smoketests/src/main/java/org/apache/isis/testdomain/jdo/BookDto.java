package org.apache.isis.testdomain.jdo;

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.isis.core.commons.internal.base._Bytes;
import org.apache.isis.core.commons.internal.base._Strings;

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
    
    public static BookDto from(Book book) {
        return BookDto.builder()
        .author(book.getAuthor())
        .description(book.getDescription())
        .isbn(book.getIsbn())
        .name(book.getName())
        .price(book.getPrice())
        .publisher(book.getPublisher())
        .build();
    }
   
    public Book toBook() {
       return Book.of(this.getName(), this.getDescription(), this.getPrice(), 
                this.getAuthor(), this.getIsbn(), this.getPublisher());   
    }

    public String encode() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(BookDto.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(this, sw);
        String newBookXml = sw.toString();
        
        String encoded = _Strings.convert(newBookXml, _Bytes.asCompressedUrlBase64, StandardCharsets.UTF_8);
        
        return encoded;
    }
    
    public static BookDto decode(String encoded) throws JAXBException {
        String bookXml = _Strings.convert(encoded, _Bytes.ofCompressedUrlBase64, StandardCharsets.UTF_8);
        
        JAXBContext jaxbContext = JAXBContext.newInstance(BookDto.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        BookDto bookDto = (BookDto) jaxbUnmarshaller.unmarshal(new StringReader(bookXml)); 
        
        return bookDto;
    }
    
}
