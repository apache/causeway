package org.apache.causeway.viewer.graphql.viewer.controller;

import java.util.Optional;

import javax.inject.Inject;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.Clob;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.RequiredArgsConstructor;
import lombok.Value;

@RestController()
@RequestMapping("/graphql/object")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ResourceController {

    private final BookmarkService bookmarkService;
    private final ObjectManager objectManager;

    @GetMapping(value = "/{logicalTypeName}:{id}/{propertyId}/blobBytes")
    public ResponseEntity<byte[]> propertyBlobBytes(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String propertyId) {

        return valueOf(logicalTypeName, id, propertyId)
                .filter(Blob.class::isInstance)
                .map(Blob.class::cast)
                .map(blob -> ResponseEntity.ok()
                        .contentType(MediaType.asMediaType(MimeType.valueOf(blob.getMimeType().toString())))
                        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(blob.getName()).build().toString())
                        .contentLength(blob.getBytes().length)
                        .body(blob.getBytes()))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(value = "/{logicalTypeName}:{id}/{propertyId}/clobChars")
    public ResponseEntity<CharSequence> propertyClobChars(
            @PathVariable final String logicalTypeName,
            @PathVariable final String id,
            @PathVariable final String propertyId) {

        return valueOf(logicalTypeName, id, propertyId)
                .filter(Clob.class::isInstance)
                .map(Clob.class::cast)
                .map(clob -> ResponseEntity.ok()
                        .contentType(MediaType.asMediaType(MimeType.valueOf(clob.getMimeType().toString())))
                        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(clob.getName()).build().toString())
                        .contentLength(clob.getChars().length())
                        .body(clob.getChars()))
                .orElse(ResponseEntity.notFound().build());
    }

    private Optional<Object> valueOf(String logicalTypeName, String id, String propertyId) {
        return bookmarkService.lookup(Bookmark.forLogicalTypeNameAndIdentifier(logicalTypeName, id))
                .map(objectManager::adapt)
                .map(managedObject -> ManagedObjectAndPropertyIfAny.of(managedObject, managedObject.getSpecification().getProperty(propertyId)))
                .filter(ManagedObjectAndPropertyIfAny::isPropertyPresent)
                .map(ManagedObjectAndProperty::of)
                .map(ManagedObjectAndProperty::value)
                .map(ManagedObject::getPojo);
    }

    @Value(staticConstructor = "of")
    private static class ManagedObjectAndPropertyIfAny {
        ManagedObject owningObject;
        Optional<OneToOneAssociation> propertyIfAny;
        boolean isPropertyPresent() {
            return propertyIfAny.isPresent();
        }
    }

    private static class ManagedObjectAndProperty {
        private static ManagedObjectAndProperty of(ManagedObjectAndPropertyIfAny tuple) {
            return new ManagedObjectAndProperty(tuple);
        }
        private ManagedObjectAndProperty(ManagedObjectAndPropertyIfAny tuple) {
            this.owningObject = tuple.owningObject;
            this.property = tuple.propertyIfAny.orElse(null);
        }
        ManagedObject owningObject;
        OneToOneAssociation property;

        ManagedObject value() {
            return property.get(owningObject);
        }
    }


}
