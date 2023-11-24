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
package org.apache.isis.viewer.wicket.model.hints;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.Strings;

public interface UiHintContainer {

    String getHint(Component component, String attributeName);

    void setHint(Component component, String attributeName, String attributeValue);

    void clearHint(Component component, String attributeName);

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


        public static String hintPathFor(Component component) {
            final String fullHintPath = fullHintPathFor(component);
            final String firstPathComponent =
                    Strings.afterFirstPathComponent(fullHintPath, Component.PATH_SEPARATOR);
            return firstPathComponent;
        }

        private static String fullHintPathFor(Component component) {
            final PrependingStringBuffer buffer = new PrependingStringBuffer(32);
            for (Component c = component; c != null; c = c.getParent()) {
                if (buffer.length() > 0) {
                    buffer.prepend(Component.PATH_SEPARATOR);
                }
                final Class<? extends Component> aClass = c.getClass();
                if(HasUiHintDisambiguator.class.isAssignableFrom(aClass)) {
                    final HasUiHintDisambiguator hasUiHintDisambiguator = (HasUiHintDisambiguator) c;
                    buffer.prepend(hasUiHintDisambiguator.getHintDisambiguator());
                    buffer.prepend("-");
                }
                buffer.prepend(c.getId());
            }
            final String fullHintPath = buffer.toString();
            return fullHintPath;
        }


    }


}
