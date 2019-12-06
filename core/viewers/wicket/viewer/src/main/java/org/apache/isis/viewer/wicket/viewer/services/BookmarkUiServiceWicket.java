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
package org.apache.isis.viewer.wicket.viewer.services;

import javax.inject.Named;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.services.bookmarkui.BookmarkUiService;
import org.apache.isis.viewer.wicket.viewer.integration.AuthenticatedWebSessionForIsis;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisWicketViewer.BookmarkUiServiceWicket")
@Order(OrderPrecedence.HIGH)
@Primary
@Log4j2
public class BookmarkUiServiceWicket implements BookmarkUiService {

    @Override
    public void clear() {
        final AuthenticatedWebSessionForIsis session = AuthenticatedWebSessionForIsis.get();
        if (session == null) {
            return;
        }
        session.getBreadcrumbModel().clear();
        session.getBookmarkedPagesModel().clear();
    }
}
