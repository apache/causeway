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
package org.apache.isis.persistence.jdo.datanucleus.mixins;

import java.io.IOException;

import javax.inject.Inject;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.metadata.TypeMetadata;
import javax.xml.bind.JAXBException;

import org.datanucleus.enhancement.Persistable;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ParameterLayout;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.mixins.layout.LayoutMixinConstants;
import org.apache.isis.applib.value.Clob;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.persistence.jdo.applib.services.JdoSupportService;

import lombok.RequiredArgsConstructor;

/**
 * Provides the ability to download the JDO
 * <a href="http://www.datanucleus.org/products/datanucleus/jdo/metadata_xml.html">class metadata</a>
 * as XML.
 *
 * @since 2.0 {@index}
 */
@Action(
        domainEvent = Persistable_downloadJdoMetadata.ActionDomainEvent.class,
        semantics = SemanticsOf.SAFE,
        restrictTo = RestrictTo.PROTOTYPING
        )
@ActionLayout(
        cssClassFa = "fa-download",
        position = ActionLayout.Position.PANEL_DROPDOWN
        )
@RequiredArgsConstructor
public class Persistable_downloadJdoMetadata {

    @Inject JdoSupportService jdoSupport;

    private final Persistable persistable;

    public static class ActionDomainEvent extends org.apache.isis.applib.IsisModuleApplib.ActionDomainEvent<Persistable_downloadJdoMetadata> {}

    @MemberOrder(name = LayoutMixinConstants.METADATA_LAYOUT_GROUPNAME, sequence = "710.1")
    public Clob act(
            @ParameterLayout(named = "File name")
            final String fileName) throws JAXBException, IOException {

        final Class<? extends Persistable> objClass = persistable.getClass();
        final String objClassName = objClass.getName();

        final TypeMetadata metadata = getPersistenceManagerFactory().getMetadata(objClassName);
        final String xml = metadata.toString();

        return new Clob(_Strings.asFileNameWithExtension(fileName, "jdo"), "text/xml", xml);
    }

    public String default0Act() {
        return _Strings.asFileNameWithExtension(persistable.getClass().getName(), "jdo");
    }

    private PersistenceManagerFactory getPersistenceManagerFactory() {
        return jdoSupport.getPersistenceManager().getPersistenceManagerFactory();
    }

}
