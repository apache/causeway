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
package org.apache.isis.client.kroviz.utils

object TestUtils {

    fun execute() {
        //given
        val inputXml =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><bs3:grid xmlns:cpt=\"http://isis.apache.org/applib/layout/component\" xmlns:lnk=\"http://isis.apache.org/applib/layout/links\" xmlns:bs3=\"http://isis.apache.org/applib/layout/grid/bootstrap3\"><bs3:row><bs3:col span=\"10\" unreferencedActions=\"true\"><cpt:domainObject><cpt:link><lnk:rel>urn:org.restfulobjects:rels/element</lnk:rel><lnk:method>GET</lnk:method><lnk:href>http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=</lnk:href><lnk:type>application/json;profile=\"urn:org.restfulobjects:repr-types/object\"</lnk:type></cpt:link></cpt:domainObject><cpt:action cssClassFa=\"fa fa-fw fa-mask\" cssClassFaPosition=\"LEFT\" id=\"impersonate\"><cpt:named>Impersonate</cpt:named><cpt:link><lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel><lnk:method>GET</lnk:method><lnk:href>http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/impersonate</lnk:href><lnk:type>application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"</lnk:type></cpt:link></cpt:action><cpt:action cssClassFa=\"fa fa-fw fa-mask\" cssClassFaPosition=\"LEFT\" id=\"impersonateWithRoles\"><cpt:named>Impersonate With Roles</cpt:named><cpt:link><lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel><lnk:method>GET</lnk:method>"
        //when
        val outputXml = XmlHelper.format(inputXml)
        console.log("[TU.execute]")
        console.log(outputXml)
    }

}
