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


package org.apache.isis.viewer.scimpi.dispatcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeSet;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceFactory;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.facets.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.viewer.scimpi.dispatcher.action.ActionAction;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Debug;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugAction;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugView;
import org.apache.isis.viewer.scimpi.dispatcher.edit.EditAction;
import org.apache.isis.viewer.scimpi.dispatcher.edit.RemoveAction;
import org.apache.isis.viewer.scimpi.dispatcher.logon.LogonAction;
import org.apache.isis.viewer.scimpi.dispatcher.logon.LogoutAction;
import org.apache.isis.viewer.scimpi.dispatcher.processor.HtmlFileParser;
import org.apache.isis.viewer.scimpi.dispatcher.processor.ProcessorLookup;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;
import org.apache.isis.viewer.scimpi.dispatcher.view.Snippet;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class Dispatcher {
    private static final String LOGIN = "login";
    public static final String ACTION = "_action";
    public static final String EDIT = "_edit";
    public static final String REMOVE = "_remove";
    public static final String GENERIC = "_generic";
    public static final String EXTENSION = "shtml";
    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(Dispatcher.class);
    public static final String COMMAND_ROOT = ".app";
    private Map<String, Action> actions = new HashMap<String, Action>();
    private Map<String, String> parameters = new HashMap<String, String>();
    private ProcessorLookup processors = new ProcessorLookup();
    private HtmlFileParser parser = new HtmlFileParser(processors);

    public void process(RequestContext context, String servletPath) {

        try {
        /*
         *  This is commented out untill the comment above is dealt with.  Once that is done this can be deleted.           
            if (session == null && IsisContext.getSession() == null && !servletPath.endsWith("logon.app")) {
                String username = getParameter("username");
                if (username == null) {
                    UserManager.startRequest(session);
                    context.setRequestPath("/" + LOGIN + "." + EXTENSION);
                    String contextPath = context.getContextPath();
                    String queryString = context.getQueryString();
                    context.addVariable("login-path", contextPath + servletPath + (queryString == null ? "" : "?" + queryString),
                            Scope.REQUEST);
                } else {
                    String password = getParameter("password");
                    session = UserManager.authenticate(new AuthenticationRequestPassword(username, password));
                    context.setSession(session);
                    context.setRequestPath(servletPath);
                    UserManager.startRequest(session);
                    context.startRequest();
                }
            } else {
                UserManager.startRequest(session);
                context.startRequest();
                context.setRequestPath(servletPath);
                processActions(context, servletPath);
            }
            */
            UserManager.startRequest(context);
            AuthenticationSession session = context.getSession(); 
            IsisContext.getPersistenceSession().getTransactionManager().startTransaction();
            context.startRequest();
            context.setRequestPath(servletPath);
            // TODO review how session should start 
   //         if (!newSession || servletPath.endsWith(context.getContextPath() + "/logon.app")) {
                // sessions should not start of with an action
                processActions(context, servletPath);
                boolean stillSameSession = context.getSession() == session;
                List<String> copyMessages = IsisContext.getMessageBroker().getMessages();
                List<String> copyWarnings = IsisContext.getMessageBroker().getWarnings();
                if (stillSameSession) {
                    IsisContext.getPersistenceSession().getTransactionManager().endTransaction();
                }
                IsisContext.getPersistenceSession().getTransactionManager().startTransaction();
                for (String message : copyMessages) {
                    IsisContext.getMessageBroker().addMessage(message);
                }
                for (String warning : copyWarnings) {
                    IsisContext.getMessageBroker().addWarning(warning);
                }
                
  //          }
            processView(context);
            IsisContext.getPersistenceSession().getTransactionManager().endTransaction();
        } catch (Throwable e) {
            LOG.error("process failed", e);
            generateErrorPage(e, context);
            IsisContext.getPersistenceSession().getTransactionManager().abortTransaction();

        } finally {
            try {
                context.endRequest();
            } catch (Exception e) {
                LOG.error("endRequest call failed", e);
            }
            try {
                UserManager.endRequest(context.getSession());
            } catch (Exception e) {
                LOG.error("endRequest call failed", e);
            }
        }
    }

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    private String getParameter(String name) {
        return parameters.get(name);
    }

    private void processActions(RequestContext context, String actionName) throws IOException {
        if (actionName.endsWith(COMMAND_ROOT)) {
            int pos = actionName.lastIndexOf('/');
            Action action = actions.get(actionName.substring(pos, actionName.length() - COMMAND_ROOT.length()));
            if (action == null) {
                throw new ScimpiException("No logic for " + actionName);
            }

            LOG.debug("processing " + action);
            action.process(context);
            String fowardTo = context.forwardTo();
            if (fowardTo != null) {
                processActions(context, fowardTo);
            }
        }
    }

    private void processView(RequestContext context) throws IOException {   
        String file = context.getRequestedFile();
        if (file == null) {
            LOG.warn("No view specified to process");
            return;
        }
        if (file.endsWith(COMMAND_ROOT)) {
            return;
        }
        file = determineFile(context, file);
        String fullPath = context.requestedFilePath(file);
        LOG.debug("processing file " + fullPath);
        context.setResourcePath(fullPath);

        context.setContentType("text/html");

        context.addVariable("title", "Untitled Page", Scope.REQUEST);
        Stack<Snippet> tags = loadPageTemplate(context, fullPath);
        Request request = new Request(file, context, tags, processors);
        request.appendDebug("processing " + fullPath); 
        try {
            request.processNextTag();
            List<String> messages = IsisContext.getMessageBroker().getMessages();
            if (messages.size() > 0) {
                // TODO write out all messages
                context.getWriter().println("Ignored messages");
            }
            List<String> warnings = IsisContext.getMessageBroker().getWarnings();
            if (warnings.size() > 0) {
                // TODO write out all warning
                context.getWriter().println("Ignored warnings");
            }
            IsisContext.getUpdateNotifier().clear();
        } catch (RuntimeException e) {
            IsisContext.getMessageBroker().getMessages();
            IsisContext.getMessageBroker().getWarnings();
            IsisContext.getUpdateNotifier().clear();
            throw e;
        }
        String page = request.popBuffer();
        context.getWriter().write(page);
        if (context.getDebug() == Debug.PAGE) {
            DebugView view = new DebugView(context.getWriter());
            view.startTable();
            context.append(view);
            view.endTable();
        }
    }

    private String determineFile(RequestContext context, String file) {
        String fileName = file.trim();
        if (fileName.startsWith(GENERIC)) {
            Object result = context.getVariable(RequestContext.RESULT);
            ObjectAdapter mappedObject = MethodsUtils.findObject(context, (String) result); // context.getMappedObjectOrResult((String)
            // result);
            if (mappedObject == null) {
                throw new ScimpiException("No object mapping for " + result);
            }
            if (fileName.equals(GENERIC + "." + EXTENSION)) {
                Facet facet = mappedObject.getSpecification().getFacet(CollectionFacet.class);
                if (facet != null) {
                    ObjectSpecification specification = mappedObject.getSpecification();
                    TypeOfFacet facet2 = (TypeOfFacet) specification.getFacet(TypeOfFacet.class);
                    file = findFileForSpecification(context, facet2.valueSpec(), "collection", EXTENSION);
                } else {
                    ObjectAdapter mappedObject2 = (ObjectAdapter) mappedObject;
                    if (mappedObject2.isTransient()) {
                        file = findFileForSpecification(context, mappedObject.getSpecification(), "edit", EXTENSION);
                    } else {
                        file = findFileForSpecification(context, mappedObject.getSpecification(), "object", EXTENSION);
                    }
                }
            } else if (fileName.equals(GENERIC + EDIT + "." + EXTENSION)) {
                file = findFileForSpecification(context, mappedObject.getSpecification(), "edit", EXTENSION);
            } else if (fileName.equals(GENERIC + ACTION + "." + EXTENSION)) {
                String method = context.getParameter("method");
                file = findFileForSpecification(context, mappedObject.getSpecification(), method, "action", EXTENSION);
            }
        }
        return file;
    }

    private String findFileForSpecification(RequestContext context, ObjectSpecification specification, String name, String extension) {
        return findFileForSpecification(context, specification, name, name, extension);
    }

    private String findFileForSpecification(
            RequestContext context,
            ObjectSpecification specification,
            String name,
            String defaultName,
            String extension) {
        
        String find = findFile(context, specification, name, extension);
        if (find == null) {
            find = "/generic/" + defaultName + "." + extension;
        }
        return find;
    }

    private String findFile(
            RequestContext context,
            ObjectSpecification specification,
            String name,
            String extension) {
        String className = specification.getShortName();
        String fileName = context.findFile("/" + className + "/" + name + "." + extension);
        if (fileName == null) {
            ObjectSpecification[] interfaces = specification.interfaces();
            for (int i = 0; i < interfaces.length; i++) {
                fileName = findFile(context, interfaces[i], name, extension);
                if (fileName != null) {
                    return fileName;
                }
            }
            if (specification.superclass() != null) {
                fileName = findFile(context, specification.superclass(), name, extension);
            }
        }
        return fileName;
    }

    private Stack<Snippet> loadPageTemplate(RequestContext context, String path) throws IOException, FileNotFoundException {
        // TODO cache stacks and check for them first
        copyParametersToVariableList(context);
        LOG.debug("parsing source " + path);
        return parser.parseHtmlFile(path, context);
    }

    private void copyParametersToVariableList(RequestContext context) {
    /*
     * Enumeration parameterNames = context.getParameterNames(); while (parameterNames.hasMoreElements()) {
     * String name = (String) parameterNames.nextElement(); if (!name.equals("view")) {
     * context.addVariable(name, context.getParameter(name), Scope.REQUEST); } }
     */
    }

    private void generateErrorPage(Throwable exception, RequestContext requestContext) {
        requestContext.setStatus(500);
        requestContext.setContentType("text/html");

        PrintWriter writer = requestContext.getWriter();
        DebugView errorView = new DebugView(writer);
        errorView.header();
        errorView.startTable();
        try {
            errorView.exception(exception);
            requestContext.append(errorView);
        } catch (RuntimeException e) {
            errorView.exception(e);
        }
        errorView.divider("Processing"); 
        errorView.appendDebugTrace(requestContext.getDebugTrace()); 
        errorView.endTable();
        errorView.footer();
        writer.close();
    }

    public void init(String dir) {
        addAction(new ActionAction());
        addAction(new DebugAction(this));
        addAction(new EditAction());
        addAction(new RemoveAction());
        addAction(new LogonAction());
        addAction(new LogoutAction());

        String configFile = getParameter("config");
        if (configFile != null) {
            File file = new File(dir, configFile);
            if (file.exists()) {
                loadConfigFile(file);
            } else {
                throw new ScimpiException("Configuration file not found: " + configFile);
            }
        }

        processors.init();
    }

    private void loadConfigFile(File file) {
        try {
            Document document;
            SAXReader reader = new SAXReader();
            document = reader.read(file);
            Element root = document.getRootElement();
            for (Iterator i = root.elementIterator(); i.hasNext();) {
                Element element = (Element) i.next();
          
                if (element.getName().equals("actions")) {
                    for (Iterator actions = element.elementIterator("action"); actions.hasNext();) {
                        Element action = (Element) actions.next();
                        String className = action.getText();
                        Action instance = (Action) InstanceFactory.createInstance(className);
                        addAction(instance);
                    }
                }
                
                if (element.getName().equals("processors")) {
                    for (Iterator processors = element.elementIterator("processor"); processors.hasNext();) {
                        Element processor = (Element) processors.next();
                        String className = processor.getText();
                        ElementProcessor instance = (ElementProcessor) InstanceFactory.createInstance(className);
                        this.processors.addElementProcessor(instance);
                    }
                }

            }
        } catch (MalformedURLException e) {
            throw new IsisException(e);
        } catch (DocumentException e) {
            throw new IsisException(e);
        }

    }

    private void addAction(Action action) {
        actions.put("/" + action.getName(), action);
        action.init();
    }

    public void debug(DebugView view) {
        view.divider("Actions");
        Iterator<String> names = new TreeSet(actions.keySet()).iterator();
        while (names.hasNext()) {
            String name = names.next();
            view.appendRow(name, actions.get(name));
        }
        Iterator<Action> iterator = actions.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().debug(view);
        }

        processors.debug(view);
    }
}

