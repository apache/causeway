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
package dom.todo;

import org.apache.isis.applib.annotation.PublishingChangeKind;
import org.apache.isis.applib.annotation.PublishingPayloadFactoryForObject;
import org.apache.isis.applib.services.publish.EventPayload;
import org.apache.isis.applib.services.publish.EventPayloadForObjectChanged;

public class ToDoItemChangedPayloadFactory implements PublishingPayloadFactoryForObject {

    public static class ToDoItemPayload extends EventPayloadForObjectChanged<ToDoItem> {

        public ToDoItemPayload(ToDoItem changed) {
            super(changed);
        }
        
        /**
         * Expose the item's {@link ToDoItem#getDescription() description} more explicitly
         * in the payload.
         */
        public String getDescription() {
            return getChanged().getDescription();
        }
    }

    @Override
    public EventPayload payloadFor(final Object changedObject, final PublishingChangeKind publishingChangeKind) {
        return new ToDoItemPayload((ToDoItem) changedObject);
    }

}
