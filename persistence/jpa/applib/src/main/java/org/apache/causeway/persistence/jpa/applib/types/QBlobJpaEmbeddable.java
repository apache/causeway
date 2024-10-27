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
package org.apache.causeway.persistence.jpa.applib.types;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.types.dsl.ArrayPath;
import com.querydsl.core.types.dsl.BeanPath;
import com.querydsl.core.types.dsl.StringPath;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * QBlobJpaEmbeddable is a Querydsl query type for BlobJpaEmbeddable
 *
 * <p>
 *     Regenerate using:
 * </p>
 * <p>
 *     <code>mvnd install -DskipTests -Dquerydsl -pl persistence/jpa/applib --also-make</code>
 * </p>
 *
 * <p>
 *     And then copy up to source code.  This is a workaround due to an incompatibility between annotation processors
 *     and JPM (Jigsaw).
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
