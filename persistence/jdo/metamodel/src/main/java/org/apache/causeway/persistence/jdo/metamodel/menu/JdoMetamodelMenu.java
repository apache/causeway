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
package org.apache.causeway.persistence.jdo.metamodel.menu;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.metadata.TypeMetadata;

import org.apache.causeway.applib.CausewayModuleApplib;
import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.DomainService;
import org.apache.causeway.applib.annotation.DomainServiceLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.util.ZipWriter;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.applib.value.NamedWithMimeType.CommonMimeType;
import org.apache.causeway.persistence.jdo.applib.services.JdoSupportService;
import org.apache.causeway.persistence.jdo.metamodel.CausewayModulePersistenceJdoMetamodel;
import org.apache.causeway.persistence.jdo.provider.entities.JdoFacetContext;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Named(CausewayModulePersistenceJdoMetamodel.NAMESPACE + ".JdoMetamodelMenu")
@DomainService()
@DomainServiceLayout(
        menuBar = DomainServiceLayout.MenuBar.SECONDARY,
        named = "Prototyping"
)
@javax.annotation.Priority(PriorityPrecedence.EARLY)
@RequiredArgsConstructor(onConstructor_ = { @Inject })
public class JdoMetamodelMenu {

    final JdoSupportService jdoSupport;
    final JdoFacetContext jdoFacetContext;

    public static abstract class ActionDomainEvent<T>
    extends CausewayModuleApplib.ActionDomainEvent<T> {}


    @Action(
            domainEvent = downloadMetamodels.ActionDomainEvent.class,
            semantics = SemanticsOf.NON_IDEMPOTENT, //disable client-side caching
            restrictTo = RestrictTo.PROTOTYPING
            )
    @ActionLayout(
            cssClassFa = "fa-download",
            named = "Download JDO Metamodels (ZIP)",
            sequence="500.670.1")
    public class downloadMetamodels{

        public class ActionDomainEvent extends JdoMetamodelMenu.ActionDomainEvent<downloadMetamodels> {}

        @MemberSupport public Blob act() {
            final byte[] zipBytes = zip();
            return Blob.of("jdo-metamodels", CommonMimeType.ZIP, zipBytes);
        }
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

    private String zipEntryNameFor(final TypeMetadata metadata) {
        return metadata.getName() + ".xml";
    }

    private PersistenceManagerFactory getPersistenceManagerFactory() {
        return jdoSupport.getPersistenceManager().getPersistenceManagerFactory();
    }

}
