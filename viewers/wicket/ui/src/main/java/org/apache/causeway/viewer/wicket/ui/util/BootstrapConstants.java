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
package org.apache.causeway.viewer.wicket.ui.util;

import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.agilecoders.wicket.core.markup.html.bootstrap.behavior.CssClassNameRemover;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class BootstrapConstants {

    public enum ButtonModifier {
        SMALL,
        OUTLINE,
    }

    public enum ButtonSemantics {
        PRIMARY,
        SECONDARY,
        SUCCESS,
        DANGER,
        WARNING,
        INFO,
        LIGHT,
        DARK,
        ;
        public String buttonDefaultCss() {
            return "btn-" + name().toLowerCase();
        }
        public String buttonOutlineCss() {
            return "btn-outline-" + name().toLowerCase();
        }
        public String fullButtonCss(final @NonNull EnumSet<ButtonModifier> modifiers) {
            val sb = new StringBuilder();
            sb.append("btn ");
            if(modifiers.contains(ButtonModifier.OUTLINE)) {
                sb.append(buttonDefaultCss());
            } else {
                sb.append(buttonOutlineCss());
            }
            if(modifiers.contains(ButtonModifier.SMALL)) {
                sb.append(" btn-sm");
            }
            return sb.toString();
        }
        public String fullButtonCss() {
            return fullButtonCss(EnumSet.noneOf(ButtonModifier.class));
        }
        private static Stream<String> streamAllSemanticsNamesAndVariants() {
            return Stream.of(BootstrapConstants.ButtonSemantics.values())
                    .flatMap(bs->Stream.of(bs.buttonDefaultCss(), bs.buttonOutlineCss()));
        }
        public static CssClassNameRemover createButtonSemanticsRemover() {
            return new CssClassNameRemover(ButtonSemantics.streamAllSemanticsNamesAndVariants()
                    .collect(Collectors.toList()));
        }


    }

}
