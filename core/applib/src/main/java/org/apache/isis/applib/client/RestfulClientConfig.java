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
package org.apache.isis.applib.client;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import org.apache.isis.applib.anyio.AnyIn;
import org.apache.isis.applib.anyio.AnyOut;
import org.apache.isis.applib.anyio.Try;

/**
 * 
 * @since 2.0.0-M2
 */
@XmlRootElement(name="restful-client-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class RestfulClientConfig {

    // --

    @XmlElement(name="restfulBase") 
    private String restfulBase;

    public String getRestfulBase() {
        return restfulBase;
    }

    public void setRestfulBase(String restfulBase) {
        this.restfulBase = restfulBase;
    }

    // --

    @XmlElement(name="useBasicAuth") 
    private boolean useBasicAuth;

    public boolean isUseBasicAuth() {
        return useBasicAuth;
    }

    public void setUseBasicAuth(boolean useBasicAuth) {
        this.useBasicAuth = useBasicAuth;
    }

    // --

    @XmlElement(name="restfulAuthUser")
    private String restfulAuthUser;

    public String getRestfulAuthUser() {
        return restfulAuthUser;
    }

    public void setRestfulAuthUser(String restfulAuthUser) {
        this.restfulAuthUser = restfulAuthUser;
    }

    // --

    @XmlElement(name="restfulAuthPassword")
    private String restfulAuthPassword;

    public String getRestfulAuthPassword() {
        return restfulAuthPassword;
    }

    public void setRestfulAuthPassword(String restfulAuthPassword) {
        this.restfulAuthPassword = restfulAuthPassword;
    }

    // --

    @XmlElement(name="useRequestDebugLogging") 
    private boolean useRequestDebugLogging;

    public boolean isUseRequestDebugLogging() {
        return useRequestDebugLogging;
    }

    public void setUseRequestDebugLogging(boolean useRequestDebugLogging) {
        this.useRequestDebugLogging = useRequestDebugLogging;
    }

    // -- MARSHALLING
    
    public static Marshaller createMarshaller() throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(RestfulClientConfig.class).createMarshaller();
        return marshaller;
    }
    
    public static Unmarshaller createUnmarshaller() throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(RestfulClientConfig.class).createUnmarshaller();
        return unmarshaller;
    }

    // -- READ
    
    /**
     * Tries to read the RestfulClientConfig from universal source {@code in}.
     * @param in - universal source {@link AnyIn}
     * @return
     */
    public static Try<RestfulClientConfig> tryRead(AnyIn in) {
        
        return in.tryApplyInputStream(is->{
            
            try {
                StreamSource source = new StreamSource(is);
                RestfulClientConfig clientConfig = createUnmarshaller()
                                    .unmarshal(source, RestfulClientConfig.class).getValue();
                
                return Try.success(clientConfig);
                
            } catch (JAXBException e) {
                
                return Try.failure(e);
            }
            
        });
        
    }
    
    // -- WRITE
    
    /**
     * Tries to write this RestfulClientConfig to universal sink {@code output}.
     * @param output - universal sink {@link AnyOut}
     * @return
     */
    public Try<Void> tryWrite(AnyOut output) {
        return output.tryApplyOutputStream(os->{
    
            try {

                createMarshaller().marshal(this, os);
                return Try.success(null);
                
            } catch (JAXBException e) {
                
                return Try.failure(e);
            }
            
        });
    }
    
    /**
     * Writes this RestfulClientConfig to universal sink {@code output}.
     * @param output - universal sink {@link AnyOut}
     * @throws Exception
     */
    public void write(AnyOut output) throws Exception {
        
        Try<Void> _try = tryWrite(output);
        _try.throwIfFailure();
        
    }
    

}
