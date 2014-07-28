#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
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

import org.apache.isis.applib.annotation.*;
import org.apache.isis.applib.annotation.ActionSemantics.Of;

@DomainService(menuOrder = "40.3")
public class ExternalLinksService {

    public static enum ExternalLink {
        ISIS_DOCUMENTATION("Apache Isis docs", "http://isis.apache.org/documentation.html"),
        PROJECT_ON_GITHUB("Project source code on Github", "https://github.com/apache/isis/tree/master/example/application/quickstart_wicket_restful_jdo/");
        
        private final String title;
        private final String url;
        
        private ExternalLink(final String title, final String url) {
            this.title = title;
            this.url = url;
        }
        
        public URL open() throws MalformedURLException {
            return new URL(url);
        }
        
        public String toString() {
            return title;
        }
    }
    
    @Prototype
    @ActionSemantics(Of.SAFE)
    @MemberOrder(name="Prototyping", sequence="91.1")
    public URL goToDocs(@Named("Link") ExternalLink link) throws MalformedURLException {
        return link.open();
    }

}

