/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;

public interface UiHintContainer {

    String getHint(Component component, String key);
    
    void setHint(Component component, String key, String value);

    void clearHint(Component component, String key);

    public static class Util {
        private Util(){}

        public static UiHintContainer hintContainerOf(Component component) {
            return hintContainerOf(component, UiHintContainer.class);
        }

        public static <T extends UiHintContainer> T hintContainerOf(
                final Component component, final Class<T> additionalConstraint) {

            if(component == null) {
                return null;
            }
            IModel<?> model = component.getDefaultModel();
            if(model != null && additionalConstraint.isAssignableFrom(model.getClass())) {
                return additionalConstraint.cast(model);
            }
            return hintContainerOf(component.getParent(), additionalConstraint);
        }

    }
}
