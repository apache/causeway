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

package org.apache.isis.metamodel.adapter.loader;

import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.MetaModelContextAware;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.Value;
import lombok.val;

/**
 * @since 2.0
 */
public interface ObjectLoader {

    ManagedObject loadObject(ObjectLoadRequest objectLoadRequest);

    // -- REQUEST
    
    @Value(staticConstructor = "of")
    public static class ObjectLoadRequest {
        ObjectSpecification objectSpecification;
        String objectIdentifier;
    }
    
    // -- HANDLER
    
    public interface Handler extends MetaModelContextAware {
        boolean isHandling(ObjectLoadRequest objectLoadRequest);
        ManagedObject loadObject(ObjectLoadRequest objectLoadRequest);
    }

    // -- BUILDER
    
    public interface ObjectLoaderBuilder {
        ObjectLoaderBuilder add(Handler handler);
        ObjectLoader build();
    }

    public static ObjectLoaderBuilder builder(MetaModelContext metaModelContext) {
        return new ObjectLoader_Builder(metaModelContext);
    }

    public static ObjectLoader buildDefault(MetaModelContext metaModelContext) {
        val objectLoader = ObjectLoader.builder(metaModelContext)
                .add(new ObjectLoader_builtinHandlers.GuardAgainstNull())
                .add(new ObjectLoader_builtinHandlers.LoadService())
                .add(new ObjectLoader_builtinHandlers.LoadValue())
                .add(new ObjectLoader_builtinHandlers.LoadViewModel())
                .add(new ObjectLoader_builtinHandlers.LoadEntity())
                .add(new ObjectLoader_builtinHandlers.LoadOther())
                .build();
        return objectLoader;
    }
    
}
