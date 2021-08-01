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
package org.apache.isis.persistence.jdo.metamodel.menu;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.metadata.TypeMetadata;

import org.apache.isis.applib.IsisModuleApplib;
import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.annotation.RestrictTo;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.util.ZipWriter;
import org.apache.isis.applib.value.Blob;
import org.apache.isis.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.isis.persistence.jdo.applib.services.JdoSupportService;
import org.apache.isis.persistence.jdo.metamodel.IsisModulePersistenceJdoMetamodel;
import org.apache.isis.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.RequiredArgsConstructor;
import lombok.val;

@DomainService(
        logicalTypeName = JdoMetamodelMenu.LOGICAL_TYPE_NAME
)
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Prototyping"
)
@Named(JdoMetamodelMenu.LOGICAL_TYPE_NAME)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class JdoMetamodelMenu {

    public final static String LOGICAL_TYPE_NAME = IsisModulePersistenceJdoMetamodel.NAMESPACE + ".JdoMetamodelMenu";

    final JdoSupportService jdoSupport;
    final JdoFacetContext jdoFacetContext;

    public static abstract class ActionDomainEvent
    extends IsisModuleApplib.ActionDomainEvent<JdoMetamodelMenu> {}

    public static class DownloadJdoMetamodelDomainEvent extends ActionDomainEvent {}

    @Action(
            domainEvent = DownloadJdoMetamodelDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download JDO Metamodels (ZIP)",
            sequence="500.670.1")
    public Blob downloadMetamodels() {

        final byte[] zipBytes = zip();
        return Blob.of("jdo-metamodels", CommonMimeType.ZIP, zipBytes);
    }

    // -- HELPER

    private byte[] zip() {

        val pmFactory = getPersistenceManagerFactory();

        val zipWriter = ZipWriter.ofFailureMessage("Unable to create zip of jdo metamodels");

        pmFactory.getManagedClasses().stream()
        .filter(jdoFacetContext::isPersistenceEnhanced)
        .map(Class::getName)
        .map(pmFactory::getMetadata)
        .forEach(metadata->{
            val xmlString = metadata.toString();
            zipWriter.nextEntry(zipEntryNameFor(metadata), writer->writer.writeCharactersUtf8(xmlString));
        });
        return zipWriter.toBytes();
    }

    private String zipEntryNameFor(TypeMetadata metadata) {
        return metadata.getName() + ".xml";
    }

    private PersistenceManagerFactory getPersistenceManagerFactory() {
        return jdoSupport.getPersistenceManager().getPersistenceManagerFactory();
    }

}
