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


package org.apache.isis.viewer.scimpi.dispatcher.context;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.factory.InstanceFactory;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.Dispatcher;
import org.apache.isis.viewer.scimpi.dispatcher.ScimpiException;
import org.apache.isis.viewer.scimpi.dispatcher.action.PropertyException;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugView;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;


public abstract class RequestContext {
    private static final Logger LOG = Logger.getLogger(RequestContext.class);

    public enum Scope {
        GLOBAL, SESSION, INTERACTION, REQUEST
    };

    public enum Debug {
        ON, OFF, PAGE
    }

    public static Scope scope(String scopeName) {
        String name = scopeName.toUpperCase();
        if (name.equals(Scope.GLOBAL.toString())) {
            return Scope.GLOBAL;
        } else if (name.equals(Scope.SESSION.toString())) {
            return Scope.SESSION;
        } else if (name.equals(Scope.INTERACTION.toString())) {
            return Scope.INTERACTION;
        } else if (name.equals(Scope.REQUEST.toString())) {
            return Scope.REQUEST;
        }
        return null;
    }

    public static Scope scope(String scopeName, Scope defaultScope) {
        if (scopeName == null || scopeName.trim().equals("")) {
            return defaultScope;
        } else {
            return scope(scopeName);
        }
    }

    public static final String RESULT = "_result";
    public static final String ERROR = "_error";
    public static final String BACK_TO = "_back_to";
    private static final Map<String, Object> globalVariables = new HashMap<String, Object>();
    private static final Scope[] SCOPES = new Scope[] { Scope.REQUEST, Scope.INTERACTION, Scope.SESSION, Scope.GLOBAL }; 

    private ObjectMapping objectMapping;
    private VersionMapping versionMapping;
    private final Map<Scope, Map<String, Object>> variables;
    private final StringBuffer debugTrace = new StringBuffer(); 
    
    private String forwardTo;
    private String requestedFile;
    private String requestedParentPath;
    private AuthenticationSession session;
    private Debug debug;
    private String resourceFile;
    private String resourceParentPath;
    private ObjectAdapter collection;

    public RequestContext() {
        String className = IsisContext.getConfiguration().getString("scimpi.object-mapping.class",
                DefaultOidObjectMapping.class.getName());
        objectMapping = (ObjectMapping) InstanceFactory.createInstance(className, ObjectMapping.class);
        className = IsisContext.getConfiguration().getString("scimpi.version-mapping.class",
                DefaultVersionMapping.class.getName());
        versionMapping = (VersionMapping) InstanceFactory.createInstance(className, VersionMapping.class);
        variables = new HashMap<Scope, Map<String, Object>>();

        variables.put(Scope.GLOBAL, globalVariables);
        variables.put(Scope.SESSION, new HashMap<String, Object>());
        variables.put(Scope.INTERACTION, new HashMap<String, Object>());
        variables.put(Scope.REQUEST, new HashMap<String, Object>());
    }

    public void endSession() {
        objectMapping.endSession();
        variables.get(Scope.SESSION).clear();
        session = null;
        clearSession();
    }

    // //////////////////////////////////////////////////////////////////
    // Mapped objects
    // //////////////////////////////////////////////////////////////////

    public ObjectAdapter getMappedObject(String id) {
        if (id == null || id.trim().equals("") || id.trim().equals("null")) {
            return null;
        }
        if (id.equals("collection")) {
            return collection;
        }
        ObjectAdapter object = mappedObject(id);
        if (object == null) {
            throw new ScimpiException("No object for " + id);
        } else {
            return object;
        }
    }

    public ObjectAdapter getMappedObjectOrResult(String id) {
        return getMappedObjectOrVariable(id, RESULT);
    }
    
    public ObjectAdapter getMappedObjectOrVariable(String id, String name) {
        if (id == null) {
            id = (String) getVariable(name);
            if (id == null) {
                throw new ScimpiException("No variable for " + name);
            }
        }
        if (id.equals("collection")) {
            return collection;
        }
        return getMappedObject(id);
    }

    public String mapObject(ObjectAdapter object, String scopeName, Scope defaultScope) {
       // if (!object.getSpecification().containsFacet(EncodeableFacet.class)) {
        Scope scope = scopeName == null ? defaultScope : scope(scopeName);
        LOG.debug("mapping " + object + " " + scope);
        return objectMapping.mapObject(object, scope);
     //   }
    }

    private ObjectAdapter mappedObject(String id) {
        if (id != null && id.equals("")) {
            return null;
        }
        if (id == null) {
            id = RESULT;
        }

        ObjectAdapter mappedObject = objectMapping.mappedObject(id);
        if (mappedObject instanceof ObjectAdapter) {
            IsisContext.getPersistenceSession().resolveImmediately((ObjectAdapter) mappedObject);
        }
        return mappedObject;
    }

    public boolean isValid() {
        return true;
    }

    // //////////////////////////////////////////////////////////////////
    // Version
    // //////////////////////////////////////////////////////////////////
    

    public String mapVersion(ObjectAdapter object) {
        Version version = object.getVersion();
        return version == null ? "" : versionMapping.mapVersion(version);
    }
    
    public Version getVersion(String id) {
        if (id.equals("")) {
            return null;
        } else {
            return versionMapping.getVersion(id);
        }
    }

    // ////////////////////////////
    // Debug
    // ////////////////////////////
    public void append(DebugView view) {
        view.divider("context");
        view.appendRow("Parent request path", requestedParentPath);
        view.appendRow("Requested file", requestedFile);
        view.appendRow("Parent resource path", resourceParentPath);
        view.appendRow("Resource file", resourceFile);

        append(view, Scope.GLOBAL);
        append(view, Scope.SESSION);
        append(view, Scope.INTERACTION);
        append(view, Scope.REQUEST);
        view.endTable();

        view.startTable();
        objectMapping.append(view);
    }

    private void append(DebugView view, Scope scope) {
        Map<String, Object> map = variables.get(scope);
        Iterator<String> keys = new TreeSet(map.keySet()).iterator();
        if (keys.hasNext()) {
            view.divider(scope + " scoped variables");
            while (keys.hasNext()) {
                String key = keys.next();
                Object object = map.get(key);
                String mappedTo = "";
                /*
                if (object instanceof String) {
                    ObjectAdapter mappedObject = mappedObject((String) object);
                    mappedTo = mappedObject == null ? "" : " - " + mappedObject.toString();
                }
                */
                view.appendRow(key, object + mappedTo);
            }
        }
    }

    public void append(Request content, String list) {
        if (list.equals("variables")) {
            appendVariables(content, Scope.GLOBAL);
            content.appendHtml("\n");
            appendVariables(content, Scope.SESSION);
            content.appendHtml("\n");
            appendVariables(content, Scope.INTERACTION);
            content.appendHtml("\n");
            appendVariables(content, Scope.REQUEST);
        } else if (list.equals("mappings")) {
            objectMapping.appendMappings(content);
        }
    }

    private void appendVariables(Request content, Scope scope) {
        Map<String, Object> map = variables.get(scope);
        Iterator<String> names = new TreeSet(map.keySet()).iterator();
        if (names.hasNext()) {
            content.appendHtml(scope.toString() + "\n");
            while (names.hasNext()) {
                String name = names.next();
                try {
                    Object object = map.get(name);
                    String details = "";
                    if (object instanceof String) {
                        ObjectAdapter mappedObject = mappedObject((String) object);
                        if (mappedObject != null) {
                            details = mappedObject.toString();
                        }
                    }
                    content.appendHtml(name + " -> " + object + "  " + details + "\n");
                } catch (Exception e) {
                    content.appendHtml(name + " -> " + map.get(name) + "\n");
                    //content.appendHtml(e.printStackTrace())
                }
            }
        }
    }

    // ////////////////////////////
    // Variables
    // ////////////////////////////

    public void clearVariables(Scope scope) {
        variables.get(scope).clear();
    }

    public void changeScope(String name, Scope newScope) {
        for (int i = 0; i < SCOPES.length; i++) { 
            Map<String, Object> map = variables.get(SCOPES[i]); 
            Object object = map.get(name);
            if (object != null) {
                map.remove(name);
                addVariable(name, object, newScope);
                return;
            }
        }
    }

    public void clearVariable(String name, Scope scope) {
        name = name != null ? name : RESULT;
        variables.get(scope).remove(name);
    }

    public void addVariable(String name, Object value, String scope) {
        addVariable(name, value, scope(scope));
    }

    public void addVariable(String name, Object value, Scope scope) {
        name = name != null ? name : RESULT;
        removeExistingVariable(name);
        variables.get(scope).put(name, value);
    }

    private void removeExistingVariable(String name) { 
        for (int i = 0; i < SCOPES.length; i++) { 
            Map<String, Object> map = variables.get(SCOPES[i]); 
            Object object = map.get(name); 
            if (object != null) { 
                map.remove(name); 
                break; 
            } 
        } 
    } 
    
    public String getStringVariable(String name) {
        String value = (String) getVariable(name);
        if (value == null) {
            return null;
        } else {
            return replaceVariables(value, true);
        }
    }

    public Object getVariable(String name) {
        for (int i = 0; i < SCOPES.length; i++) { 
            Map<String, Object> map = variables.get(SCOPES[i]); 
            Object object = map.get(name);
            if (object != null) {
                return object;
            }
        }
        return null;
    }

    public String replaceVariables(String value, boolean ensureExists) {
        int start = value.indexOf("${");
        if (start == -1) {
            return value;
        } else {
            int end = value.indexOf('}');
            if (end == -1) {
                throw new PropertyException("No closing brace in " + value.substring(start));
            } else if (end < start) {
                throw new PropertyException("Closing brace before opening brace in " + value.substring(end));
            }
            String name = value.substring(start + 2, end);
            if (name != null) {
                Object replacementValue = getParameter(name);
                if (replacementValue == null) {
                    replacementValue = getVariable(name);
                }
                if (replacementValue == null) {
                    replacementValue = getBuiltIn(name);
                }
                if (replacementValue == null) {
                    // REVIEW should we have a special tag that shows that a variable must exist?
                    if (ensureExists) {
                        throw new PropertyException("No value for the variable " + value.substring(start, end + 1));
                    } else {
                        replacementValue = "";
                    }
                }
                value = value.substring(0, start) + replacementValue + value.substring(end + 1);
                return replaceVariables(value, ensureExists);
            } else {
                throw new PropertyException("No value for " + name);
            }
        }
    }

    private Object getBuiltIn(String name) {
        if (name.equals("_session")) {
            return getSessionId();
        } else if (name.equals("_context")) {
            return getContextPath();
        } else if (name.equals("_this")) {
            return resourceFile;
        } else if (name.equals("_directory")) {
            return resourceParentPath;
        } else if (name.equals("_base")) {
            return getUrlBase() + getContextPath() + resourceParentPath + resourceFile;
            // return "http://localhost:8080" + resourceParentPath + resourceFile;
        }
        return null;
    }

    public String encodedInteractionParameters() {
        StringBuffer buffer = new StringBuffer();
        Map<String, Object> map = variables.get(Scope.INTERACTION);
        Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            buffer.append("&" + entry.getKey() + "=" + entry.getValue());
        }
        return buffer.toString();
    }

    public String interactionFields() {
        StringBuffer buffer = new StringBuffer();
        Map<String, Object> map = variables.get(Scope.INTERACTION);
        Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> entry = iterator.next();
            buffer.append("<input type=\"hidden\" name=\"" + entry.getKey() + "\" value=\"" + entry.getValue() + "\">\n");
        }
        return buffer.toString();
    }

    protected abstract String getSessionId();

    public abstract void addCookie(String name, String value, int minutesUtilExpires);

    public abstract String getCookie(String name);

    // //////////////////////////////
    // Start/end request
    // //////////////////////////////
    public void endRequest() throws IOException {
        getWriter().close();
        objectMapping.clear(Scope.REQUEST);
        variables.get(Scope.REQUEST).clear();
        variables.get(Scope.INTERACTION).clear();
    }

    public void startRequest() {
        debugTrace.setLength(0); 
        objectMapping.reloadIdentityMap();
        String debugParameter = getParameter("debug");
        if (debugParameter != null) {
            if (debugParameter.equals("off")) {
                debug = Debug.OFF;
            } else if (debugParameter.equals("on")) {
                debug = Debug.ON;
            } else if (debugParameter.equals("page")) {
                debug = Debug.PAGE;
            }
        }
    }

    public abstract PrintWriter getWriter();

    // /////////////////////////////
    // Forwarding
    // /////////////////////////////
    public void forwardTo(String forwardTo) {
        this.forwardTo = "/" + forwardTo;
    }

    public String forwardTo() {
        String returnForwardTo = forwardTo;
        forwardTo = null;
        return returnForwardTo;
    }

    // /////////////////////////////
    // Parameters
    // /////////////////////////////
    public void addParameter(String name, String parameter) {
        if (name == null) {
            throw new ScimpiException("Name must be specified for parameter " + parameter);
        }
        addVariable(name, parameter, Scope.REQUEST);
    }

    public String getParameter(String name) {
        return (String) getVariable(name);
    }
    
    public Iterator<Entry<String, Object>> interactionParameters() {
        Map<String, Object> map = variables.get(Scope.REQUEST);
        Iterator<Entry<String, Object>> iterator = map.entrySet().iterator();
        return iterator;
    }

    // ///////////////////////////////////////
    // Requested file
    // ///////////////////////////////////////

    /**
     * The requested file is the file that the browser requested. This may or may not be the file that is
     * actually processed and returned; that is the {@link #getResourceFile()}.
     */
    public String getRequestedFile() {
        return requestedFile;
    }

    public void setRequestPath(String filePath) {
        setRequestPath(filePath, null);
    }

    public void setRequestPath(String filePath, String defaultGenericPath) {
        if (filePath == null) {
            defaultGenericPath = defaultGenericPath == null ? "" : defaultGenericPath;
            this.requestedFile = Dispatcher.GENERIC + defaultGenericPath + "." + Dispatcher.EXTENSION;
        } else if (filePath.startsWith("_generic")) {
            this.requestedParentPath = "/";
            LOG.debug("generic file, requested path cleared");
            this.requestedFile = filePath;
            LOG.debug("requested file set = " + filePath);

        } else {
            int lastSlash = filePath.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new ScimpiException("No slash in request path: " + filePath);
            }
            String path = filePath.substring(0, lastSlash + 1);
            LOG.debug("requested path set = " + path);
            this.requestedParentPath = path;

            String file = filePath.substring(lastSlash + 1);
            LOG.debug("requested file set = " + file);
            this.requestedFile = file;
        }
    }

    public void clearRequestedPath() {
        this.requestedParentPath = null;
        this.requestedFile = null;
    }

    /**
     * Returns the absolute file system path to the specified resource based on the path used for the current
     * request during the call to {@link #setRequestPath(String)}. The return path can then be used to access
     * the specified resource. If the resource has a leading slash (/) then that resource string is returned
     * as the path.
     */
    public String requestedFilePath(String resource) {
        if (resource.startsWith("/")) {
            return resource;
        } else {
            return requestedParentPath + resource;
        }
    }

    // ///////////////////////////////////////
    // Resource file
    // ///////////////////////////////////////

    /**
     * The resource file is the file on disk that is processed and returned to the browser. This may or may
     * not be the file that was actually requested by the browser; that is the {@link #getRequestedFile()}.
     */
    public String getResourceFile() {
        return resourceFile;
    }

    public void setResourcePath(String filePath) {
        if (filePath == null) {
            throw new ScimpiException("Path must be specified");
        } else {
            int lastSlash = filePath.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new ScimpiException("No slash in request path: " + filePath);
            }
            String path = /* getContextPath() + */filePath.substring(0, lastSlash + 1);
            LOG.debug("resource path set = " + path);
            this.resourceParentPath = path;

            String file = filePath.substring(lastSlash + 1);
            LOG.debug("resource file set = " + file);
            this.resourceFile = file;
        }
    }

    /**
     * Returns a uri for the specified resource based on the path used for the current request (as set up
     * during the call to {@link #setResourcePath(String)}). Such a uri when used by the browser will allow
     * access to the specified resource. If the resource has a leading slash (/) or the resource is for a
     * generic page (starts with "_generic") then that resource string is returned as the path.
     */
    public String fullUriPath(String resource) {
        if (resource.startsWith("/") || resource.startsWith("_generic")) {
            return resource;
        } else {
            return getContextPath() + resourceParentPath + resource;
        }
    }

    /**
     * Returns the absolute file system path to the specified resource based on the path used for the current
     * request (as set up during the call to {@link #setResourcePath(String)}). The return path can then be
     * used to access the specified resource. If the resource has a leading slash (/) or the resource is for a
     * generic page (starts with "_generic") then that resource string is returned as the path.
     */
    public String fullFilePath(String resource) {
        if (resource.startsWith("/") || resource.startsWith("_generic")) {
            return resource;
        } else {
            return resourceParentPath + resource;
        }
    }

    // //////////////////////////////////////////////////////////////////
    // 
    // //////////////////////////////////////////////////////////////////

    public String mapObject(ObjectAdapter object, Scope scope) {
        if (object.getOid() != null) {
            return objectMapping.mapObject(object, scope);
        } else if (object.getResolveState().isValue()) {
            return object.titleString();
        } else {
            collection = object;
            return "collection";
        }
    }

    public void unmapObject(ObjectAdapter object, Scope scope) {
        objectMapping.unmapObject(object, scope);
    }

    public abstract String findFile(String fileName);

    public abstract InputStream openStream(String path);

    public abstract String imagePath(ObjectAdapter object);

    public abstract String imagePath(ObjectSpecification specification);

    public abstract void redirectTo(String view);

    public abstract String getContextPath();

    public abstract String getUrlBase();

    public abstract String getQueryString();

    public abstract String clearSession();

    public void setSession(AuthenticationSession session) {
        this.session = session;
    }

    public AuthenticationSession getSession() {
        return session;
    }

    public Debug getDebug() {
        return debug;
    }

    public abstract String getUri();

    public void setStatus(int status) {}

    public void setContentType(String string) {}

    public boolean isDebug() { 
        return getDebug() == Debug.ON; 
    }
    
    public String getDebugTrace() { 
        return debugTrace.toString().replace('<', '[').replace('>', ']'); 
    } 

    public void appendDebugTrace(String line) { 
        debugTrace.append(line); 
    } 
}
