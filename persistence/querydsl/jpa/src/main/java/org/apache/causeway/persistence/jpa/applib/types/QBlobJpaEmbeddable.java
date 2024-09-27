package org.apache.causeway.persistence.jpa.applib.types;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QBlobJpaEmbeddable is a Querydsl query type for BlobJpaEmbeddable
 *
 * <p>
 *     NOTE: This file was generated in the jpa-applib, and manually copied over. Regenerate using:
 * </p>
 * <p>
 *     <code>mvnd install -DskipTests -Dquerydsl -pl persistence/jpa/applib --also-make</code>
 * </p>
 *
 * <p>
 *     Note also that this Maven module does not use JPM (Jigsaw); it is an automatic module.  Without introducing
 *     a long-term dependency on
 *     possible
 * </p>
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QBlobJpaEmbeddable extends BeanPath<BlobJpaEmbeddable> {

    private static final long serialVersionUID = 1905953656L;

    public static final QBlobJpaEmbeddable blobJpaEmbeddable = new QBlobJpaEmbeddable("blobJpaEmbeddable");

    public final ArrayPath<byte[], Byte> bytes = createArray("bytes", byte[].class);

    public final StringPath mimeType = createString("mimeType");

    public final StringPath name = createString("name");

    public QBlobJpaEmbeddable(String variable) {
        super(BlobJpaEmbeddable.class, forVariable(variable));
    }

    public QBlobJpaEmbeddable(Path<BlobJpaEmbeddable> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBlobJpaEmbeddable(PathMetadata metadata) {
        super(BlobJpaEmbeddable.class, metadata);
    }

}

