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
package org.apache.isis.viewer.wicket.ui.pages.accmngt;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.wicket.util.cookies.CookieUtils;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.functions._Functions;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SuccessFeedbackCookieManager {

    /**
     * The name of a special cookie that is used as a temporary container for
     * stateless session scoped success feedback messages.
     */
    static final String FEEDBACK_COOKIE_NAME = "isis.feedback.success";


    /**
     * Store a cookie with name {@value #FEEDBACK_COOKIE_NAME} that is
     * used as a temporary container for stateless session scoped success feedback
     * messages.
     */
    public static void storeSuccessFeedback(final @Nullable String successFeedback) {
        val cookieUtils = new CookieUtils();
        if (_Strings.isNotEmpty(successFeedback)) {
            cookieUtils.save(FEEDBACK_COOKIE_NAME, _Strings.base64UrlEncode(successFeedback));
        } else {
            // if successFeedback is empty we interpret that as a cookie remove request
            drainSuccessFeedback(_Functions.noopConsumer());
        }

    }

    /**
     * Checks for a cookie with name {@value #FEEDBACK_COOKIE_NAME} that is
     * used as a temporary container for stateless session scoped success feedback
     * messages.
     */
    public static void drainSuccessFeedback(final @NonNull Consumer<String> onSuccessFeedback) {
        val cookieUtils = new CookieUtils();
        final String successFeedback = cookieUtils.load(FEEDBACK_COOKIE_NAME);
        if (_Strings.isNotEmpty(successFeedback)) {
            onSuccessFeedback.accept(_Strings.base64UrlDecode(successFeedback));
        }
        cookieUtils.remove(FEEDBACK_COOKIE_NAME);
    }

}
