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
package org.apache.isis.applib.services.background;

import java.util.Iterator;
import java.util.List;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.memento.MementoService;
import org.apache.isis.applib.services.memento.MementoService.Memento;

/**
 * A memento for an action invocation, to allow the details of an action invocation
 * to be captured and stored, then executed later.
 * 
 * <p>
 * Provided as a mechanism by which implementations of {@link BackgroundService} can 
 * hand-off work to the {@link BackgroundCommandService}.  This is used by the
 * default implementation of <tt>BackgroundServiceDefault</tt> in the <tt>isis-module-background</tt> module.
 * 
 * <p>
 * Implementation-wise this is a wrapper around {@link MementoService.Memento}, 
 * and abstracts away the details of the keys used to store the various pieces of
 * information stored in the underlying memento.
 */
public class ActionInvocationMemento {

    private final Memento memento;

    public ActionInvocationMemento(
            final MementoService mementoService, 
            final String actionId, 
            final Bookmark target, List<Class<?>> argTypes,
            final List<Object> args) {
        
        if(argTypes.size() != args.size()) {
            throw new IllegalArgumentException("argTypes and args must be same size");
        }
        
        this.memento = mementoService.create();
        memento.set("actionId", actionId);
        memento.set("target", target);
        memento.set("numArgs", args.size());
        
        final Iterator<Class<?>> iterArgTypes = argTypes.iterator();
        final Iterator<Object> iterArgs = args.iterator();
        int i=0;
        while(iterArgTypes.hasNext() && iterArgs.hasNext()) {
            memento.set("arg" + i + "Type", iterArgTypes.next().getName());
            memento.set("arg" + i         , iterArgs.next());
            i++;
        }
    }
    
    public ActionInvocationMemento(final MementoService mementoService, final String mementoStr) {
        this.memento = mementoService.parse(mementoStr);
    }
    
    // //////////////////////////////////////

    
    public String getActionId() {
        return memento.get("actionId", String.class);
    }

    /**
     * @deprecated - always returns <tt>null</tt>; use {@link #getActionId()} instead.
     */
    @Deprecated
    public String getTargetClassName() {
        return memento.get("targetClassName", String.class);
    }

    /**
     * @deprecated - always returns <tt>null</tt>; use {@link #getActionId()} instead.
     */
    @Deprecated
    public String getTargetActionName() {
        return memento.get("targetActionName", String.class);
    }
    
    public Bookmark getTarget() {
        return memento.get("target", Bookmark.class);
    }
    
    public int getNumArgs() {
        return memento.get("numArgs", Integer.class);
    }

    public Class<?> getArgType(final int num) throws ClassNotFoundException {
        String className = memento.get("arg" + num + "Type", String.class);
        if(className.equals("byte")) return byte.class;
        if(className.equals("short")) return short.class;
        if(className.equals("int")) return int.class;
        if(className.equals("long")) return long.class;
        if(className.equals("float")) return float.class;
        if(className.equals("double")) return double.class;
        if(className.equals("char")) return char.class;
        if(className.equals("boolean")) return boolean.class;
        return className != null? Thread.currentThread().getContextClassLoader().loadClass(className): null;
    }
    
    public <T> T getArg(final int num, final Class<T> type) {
        return memento.get("arg" + num, type);
    }
    
    // //////////////////////////////////////

    public String asMementoString() {
        return memento.asString();
    }

    // //////////////////////////////////////
    
    @Override
    public String toString() {
        return asMementoString();
    }


}
