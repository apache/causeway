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
package org.apache.isis.core.metamodel.methods;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.function.IntFunction;

import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.commons.StringExtensions;

public final class MethodLiteralConstants {

    // -- PREFIXES

    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";

    public static final String DEFAULT_PREFIX = "default";
    public static final String CHOICES_PREFIX = "choices";
    public static final String AUTO_COMPLETE_PREFIX = "autoComplete";

    public static final String HIDE_PREFIX = "hide";
    public static final String DISABLE_PREFIX = "disable";
    public static final String VALIDATE_PREFIX = "validate";

    public static final String NAMED_PREFIX = "named"; // dynamic naming
    public static final String DESCRIBED_PREFIX = "described"; // dynamic description

    public static final String CREATED_PREFIX = "created";
    public static final String LOADED_PREFIX = "loaded";
    public static final String SAVED_PREFIX = "saved";
    public static final String SAVING_PREFIX = "saving";
    public static final String PERSISTED_PREFIX = "persisted";
    public static final String PERSISTING_PREFIX = "persisting";
    public static final String DELETING_PREFIX = "deleting";
    public static final String REMOVING_PREFIX = "removing";
    public static final String UPDATED_PREFIX = "updated";
    public static final String UPDATING_PREFIX = "updating";

    // -- LITERALS

    public static final String DISABLED = "disabled"; // for batch disabling all members
    public static final String TITLE = "title";
    public static final String TO_STRING = "toString";

    public static final String CSS_CLASS_PREFIX = "cssClass";
    public static final String HIDDEN_PREFIX = "hidden";
    public static final String ICON_NAME_PREFIX = "iconName";
    public static final String LAYOUT_METHOD_NAME = "layout";

    @FunctionalInterface
    public static interface SupportingMethodNameProviderForAction {
        @Nullable String getActionSupportingMethodName(Method actionMethod, String prefix, boolean isMixin);
    }

    @FunctionalInterface
    public static interface SupportingMethodNameProviderForParameter {
        @Nullable String getParameterSupportingMethodName(Method actionMethod, String prefix, boolean isMixin, int paramNum);

        /** paramNum to param-supporting-method name provider */
        default IntFunction<String> providerForParam(final Method actionMethod, final String prefix, final boolean isMixin) {
            return paramNum->getParameterSupportingMethodName(actionMethod, prefix, isMixin, paramNum);
        }
    }

    @FunctionalInterface
    public static interface SupportingMethodNameProviderForPropertyAndCollection {
        /** automatically deals with properties getters and actions */
        @Nullable String getMemberSupportingMethodName(Member member, String prefix, boolean isMixin);
    }

    // -- SUPPORTING METHOD NAMING CONVENTION

    public static final Can<SupportingMethodNameProviderForAction> NAMING_ACTIONS = Can.of(
            (final Method actionMethod, final String prefix, final boolean isMixin)->
                prefix + StringExtensions.asCapitalizedName(actionMethod.getName()),
            (final Method actionMethod, final String prefix, final boolean isMixin)->
                isMixin
                    // prefix only notation is restricted to mixins
                    ? prefix
                    : null
            );
    public static final Can<SupportingMethodNameProviderForParameter> NAMING_PARAMETERS = Can.of(
            (final Method actionMethod, final String prefix, final boolean isMixin, final int paramNum)->
                prefix + paramNum + StringExtensions.asCapitalizedName(actionMethod.getName()),
            (final Method actionMethod, final String prefix, final boolean isMixin, final int paramNum)->
                isMixin
                    // no action name reference notation is restricted to mixins
                    ? prefix + StringExtensions.asCapitalizedName(actionMethod.getParameters()[paramNum].getName())
                    : null
            );
    public static final Can<SupportingMethodNameProviderForPropertyAndCollection> NAMING_PROPERTIES_AND_COLLECTIONS = Can.of(
            (final Member member, final String prefix, final boolean isMixin)->
                prefix + getCapitalizedMemberName(member),
            (final Member member, final String prefix, final boolean isMixin)->
                isMixin
                    // prefix only notation is restricted to mixins
                    ? prefix
                    : null
            );

    // -- HELPER

    private static String getCapitalizedMemberName(final Member member) {
        if(member instanceof Method) {
            final Method method = (Method)member;
            if(method.getParameterCount()>0) {
                // definitely an action not a getter
                return StringExtensions.asCapitalizedName(method.getName());
            }
            // either a no-arg action or a getter
            final String capitalizedName =
                    StringExtensions.asJavaBaseNameStripAccessorPrefixIfRequired(member.getName());
            return  capitalizedName;
        }
        // must be a field then
        final String capitalizedName =
                StringExtensions.asCapitalizedName(member.getName());
        return capitalizedName;
    }


}
