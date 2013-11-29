/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package webapp.prototyping;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.isis.applib.annotation.ActionSemantics;
import org.apache.isis.applib.annotation.Prototype;
import org.apache.isis.applib.annotation.ActionSemantics.Of;
import org.apache.isis.applib.annotation.MemberOrder;

public class ExternalLinksService {

    @Prototype
    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="Prototyping", sequence="93")
    public URL openIsisDocumentation() throws MalformedURLException {
        return new URL("http://isis.apache.org/documentation.html");
    }

    @Prototype
    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="Prototyping", sequence="94")
    public URL openProjectOnGithub() throws MalformedURLException {
        return new URL("https://github.com/apache/isis/tree/master/example/application/quickstart_wicket_restful_jdo/");
    }

}

