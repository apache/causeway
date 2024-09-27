package org.apache.causeway.persistence.jpa.applib.types;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QClobJpaEmbeddable is a Querydsl query type for ClobJpaEmbeddable
 *
 * Regenerate using: <code>mvnd install -DskipTests -Dquerydsl -pl persistence/jpa/applib --also-make</code>
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QClobJpaEmbeddable extends BeanPath<ClobJpaEmbeddable> {

    private static final long serialVersionUID = -1035703943L;

    public static final QClobJpaEmbeddable clobJpaEmbeddable = new QClobJpaEmbeddable("clobJpaEmbeddable");

    public final StringPath chars = createString("chars");

    public final StringPath mimeType = createString("mimeType");

    public final StringPath name = createString("name");

    public QClobJpaEmbeddable(String variable) {
        super(ClobJpaEmbeddable.class, forVariable(variable));
    }

    public QClobJpaEmbeddable(Path<ClobJpaEmbeddable> path) {
        super(path.getType(), path.getMetadata());
    }

    public QClobJpaEmbeddable(PathMetadata metadata) {
        super(ClobJpaEmbeddable.class, metadata);
    }

}

