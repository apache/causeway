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
package org.apache.isis.client.kroviz.ui

import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.model.DiagramDM
import org.apache.isis.client.kroviz.core.model.meta.MetaClass
import org.apache.isis.client.kroviz.core.model.meta.MetaPackage
import org.apache.isis.client.kroviz.to.DomainType

object UmlDiagram {

    fun buildClass(dd: DiagramDM): String {
        val domainTypeList: Set<DomainType> = dd.classes
        //TODO properties needed to set type
        //val properties: Set<Property> = dd.properties
        val packages = mutableSetOf<MetaPackage>()
        domainTypeList.forEach { dt ->
            val cls = MetaClass(dt)
            val pkgName = cls.pkg.name
            var pkg = packages.find { p -> p.name == pkgName }
            if (pkg == null) {
                pkg = cls.pkg
                pkg.classes.add(cls)
                packages.add(pkg)
            } else {
                pkg.classes.add(cls)
            }
        }
        return PumlBuilder().with(packages)
    }

    fun buildSequence(logEntries: List<LogEntry>): String? {
        val first: LogEntry? = logEntries.find { le ->
            le.url.endsWith("/restful/")
        }
        if (first != null) {
            return PumlBuilder().with(first)
        }
        return null
    }

}
