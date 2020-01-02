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
package org.apache.isis.testdomain.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.UnavailableSecurityManagerException;
import org.apache.shiro.config.Ini;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.LifecycleUtils;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.security.shiro.webmodule.WebModuleShiro.EnvironmentLoaderListenerForIsis;

import lombok.SneakyThrows;
import lombok.val;
import lombok.var;

/**
 * 
 * This class was initially copied over from the ApacheDS user Guide, however it has/had 
 * some glitches with inconsistent LocalThread (subjectThreadState) context.
 * <p>
 * IniSecurityManagerFactory was deprecated in Shiro 1.4, but we could not find a migration guide yet.
 *
 */
@SuppressWarnings("deprecation")
class AbstractShiroTest {

    private static ThreadState subjectThreadState;

    public AbstractShiroTest() {
    }

    /**
     * Allows subclasses to set the currently executing {@link Subject} instance.
     *
     * @param subject the Subject instance
     */
    protected void setSubject(Subject subject) {
        clearSubject();
        subjectThreadState = createThreadState(subject);
        subjectThreadState.bind();
    }

    protected Subject getSubject() {
        return SecurityUtils.getSubject();
    }

    protected ThreadState createThreadState(Subject subject) {
        return new SubjectThreadState(subject);
    }

    /**
     * Clears Shiro's thread state, ensuring the thread remains clean for future test execution.
     */
    protected void clearSubject() {
        doClearSubject();
    }

    private static void doClearSubject() {

        Subject subject = ThreadContext.getSubject();
        if(subject!=null) {
            ThreadContext.unbindSubject();
        }


        if (subjectThreadState != null) {
            subjectThreadState.clear();
            subjectThreadState = null;
        }
    }

    @SneakyThrows
    protected static void setSecurityManager(ServiceInjector serviceInjector, String iniResource) {
        
        val ini = Ini.fromResourcePath(iniResource);
        val factory = new IniSecurityManagerFactory(ini);
        val securityManager = factory.getInstance();

        var listener = new EnvironmentLoaderListenerForIsis(serviceInjector);
        listener.injectServicesIntoRealms(securityManager);
        
//debug        
//        ini.getSections().forEach(section->{
//            section.entrySet().forEach(es->{
//                System.out.println("" + es.getKey() + "=" +es.getValue());
//            });
//        });
       
        setSecurityManager(securityManager);
    }
    
    protected static void setSecurityManager(SecurityManager securityManager) {
        try {
            // guard against SecurityManager already being set
            getSecurityManager();
            throw new IllegalStateException("It seems a previous test, did not cleanup the its SecurityManager.");
        } catch (UnavailableSecurityManagerException e) {
            // happy case, fall through
        }
        SecurityUtils.setSecurityManager(securityManager);
        
        _Assert.assertEquals("expected same object", securityManager, SecurityUtils.getSecurityManager());
    }

    protected static SecurityManager getSecurityManager() {
        return SecurityUtils.getSecurityManager();
    }

    public static void tearDownShiro() {
        doClearSubject();
        try {
            SecurityManager securityManager = getSecurityManager();
            LifecycleUtils.destroy(securityManager);
        } catch (UnavailableSecurityManagerException e) {
            //we don't care about this when cleaning up the test environment
            //(for example, maybe the subclass is a unit test and it didn't
            // need a SecurityManager instance because it was using only
            // mock Subject instances)
        }
        SecurityUtils.setSecurityManager(null);
    }

}
