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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebugTee;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.runtime.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionManager;
import org.apache.isis.viewer.scimpi.dispatcher.action.ActionAction;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Debug;
import org.apache.isis.viewer.scimpi.dispatcher.context.RequestContext.Scope;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugAction;
import org.apache.isis.viewer.scimpi.dispatcher.debug.DebugWriter;
import org.apache.isis.viewer.scimpi.dispatcher.debug.LogAction;
import org.apache.isis.viewer.scimpi.dispatcher.edit.EditAction;
import org.apache.isis.viewer.scimpi.dispatcher.edit.RemoveAction;
import org.apache.isis.viewer.scimpi.dispatcher.logon.LogonAction;
import org.apache.isis.viewer.scimpi.dispatcher.logon.LogoutAction;
import org.apache.isis.viewer.scimpi.dispatcher.processor.HtmlFileParser;
import org.apache.isis.viewer.scimpi.dispatcher.processor.ProcessorLookup;
import org.apache.isis.viewer.scimpi.dispatcher.processor.Request;
import org.apache.isis.viewer.scimpi.dispatcher.processor.TagProcessingException;
import org.apache.isis.viewer.scimpi.dispatcher.util.MethodsUtils;
import org.apache.isis.viewer.scimpi.dispatcher.view.Snippet;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;


public class Dispatcher {
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
        LOG.info("processing request " + servletPath);
        AuthenticationSession session = UserManager.startRequest(context);
        LOG.debug("exsiting session: " + session);
        
        IsisContext.getPersistenceSession().getTransactionManager().startTransaction();
        context.setRequestPath(servletPath);
        context.startRequest();
                
        try {
            processActions(context, false, servletPath);
            processTheView(context);
        } catch (ScimpiNotFoundException e) {
            if (context.isInternalRequest()) {
                LOG.error("invalid page request (from within application): "+ e.getMessage());
            } else {
                LOG.info("invalid page request (from outside application): "+ e.getMessage());
            }
            
            try {
                // TODO pick this up from configuration
                // context.raiseError(404);
                //context.setRequestPath("/error/notfound_404.shtml");
                IsisContext.getMessageBroker().addWarning("Failed to find page....");  // TODO complete message
                context.setRequestPath("/index.shtml");
                processTheView(context);
            } catch (IOException e1) {
                throw new ScimpiException(e);
            }
            
        } catch (NotLoggedInException e) {
            IsisContext.getMessageBroker().addWarning("You are not currently logged in! Please log in so you can continue.");
            context.setRequestPath("/login.shtml");
            try {
                processTheView(context);
            } catch (IOException e1) {
                throw new ScimpiException(e);
            }
            
        } catch (Throwable e) {
            LOG.debug(e.getMessage(), e);
            LOG.info("testing");
            
            DebugString error = new DebugString();
            generateErrorPage(e, context, error);
            String message = "failed while processing " + servletPath;
            LOG.error(message + "\n" + error + "\n" + message);
             
            PersistenceSession checkSession = IsisContext.getPersistenceSession();
            IsisTransactionManager transactionManager = checkSession.getTransactionManager();
            if (transactionManager.getTransaction() != null && transactionManager.getTransaction().getState().canAbort()) {
                transactionManager.abortTransaction();
                transactionManager.startTransaction();
            }

            Throwable ex = e instanceof TagProcessingException ? e.getCause() : e;
            if (ex instanceof ForbiddenException) {
                context.addVariable("_security_error", ex.getMessage(), Scope.REQUEST); 
                context.addVariable("_security_identifier", ((ForbiddenException) ex).getIdentifier(), Scope.REQUEST);
                context.addVariable("_security_roles", ((ForbiddenException) ex).getRoles(), Scope.REQUEST);

                // TODO allow these values to be got configuration
                // context.raiseError(403);
                // context.setRequestPath("/error/security_403.shtml");
                IsisContext.getMessageBroker().addWarning("You did not have the right permissions to perform this.....");
                context.setRequestPath("/index.shtml");
                try {
                    processTheView(context);
                } catch (IOException e1) {
                    throw new ScimpiException(e);
                }
            } else {
                // TODO allow these values to be got configuration
                // context.raiseError(500);    
                context.setRequestPath("/error/server_500.shtml");
                //IsisContext.getMessageBroker().addWarning("There was a error while processing this request....");   // TODO include the reference code so it can be looked up
                //context.setRequestPath("/index.shtml");
                try {
                    processTheView(context);
                } catch (IOException e1) {
                    throw new ScimpiException(e);
                }
            }
        } finally {
            try {
                UserManager.endRequest(context.getSession());
            } catch (Exception e1) {
                LOG.error("endRequest call failed", e1);
            }
        }
    }


    protected void processTheView(RequestContext context) throws IOException {
        IsisTransactionManager transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        if (transactionManager.getTransaction().getState().canFlush()) {
            transactionManager.flushTransaction();
        }
        processView(context);
        // Note - the session will have changed since the earlier call if a user has logged in or out in the action processing above.
        transactionManager = IsisContext.getPersistenceSession().getTransactionManager();
        if (transactionManager.getTransaction().getState().canCommit()) {
            IsisContext.getPersistenceSession().getTransactionManager().endTransaction();
        }
        
        context.endRequest();
        UserManager.endRequest(context.getSession());
    }


    private void generateErrorPage(Throwable exception, RequestContext requestContext, DebugString error) {
        if (IsisContext.getCurrentTransaction() != null) {
            List<String> messages =  IsisContext.getMessageBroker().getMessages();
            for (String message : messages) {
                requestContext.getWriter().append("<div class=\"message\">message: " + message + "</div>");
                error.appendln("message", message);
            }
            messages =  IsisContext.getMessageBroker().getWarnings();
            for (String message : messages) {
                requestContext.getWriter().append("<div class=\"message\">warning: " + message + "</div>");
                error.appendln("warning", message);
            }
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        writeErrorContent(requestContext, exception, error, new PrintWriter(out), false);
        
        PrintWriter writer;
        try {
            String ref = Long.toString(System.currentTimeMillis(), 36).toUpperCase();
            requestContext.addVariable("_error-ref", ref, Scope.INTERACTION);
            String directory = IsisContext.getConfiguration().getString(ConfigurationConstants.ROOT + "scimpi.error-snapshots", ".");
            writer = new PrintWriter(new File(directory, "error_" + ref + ".html"));
            writeErrorContent(requestContext, exception, new DebugString(), writer, true);
        } catch (FileNotFoundException e) {
            LOG.error("Failed to archive error page", e);
        }
    
        String replace = "";
        String withReplacement = "";
        String message = exception.getMessage();
        requestContext.addVariable("_error-message", message == null ? "" : message.replaceAll(replace, withReplacement), Scope.REQUEST);
        requestContext.addVariable("_error-details", out.toString().replaceAll(replace, withReplacement), Scope.REQUEST);
        requestContext.clearTransientVariables();
    }


    public void writeErrorContent(
            RequestContext requestContext,
            Throwable exception,
            DebugString error,
            PrintWriter writer,
            boolean includeHeader) {
        DebugBuilder errorView = new DebugTee(error, new DebugWriter(writer, includeHeader));
        try {
            errorView.appendException(exception);
            requestContext.append(errorView);
        } catch (RuntimeException e) {
            errorView.appendln("NOTE - an exception occurred while dumping an exception!");
            errorView.appendException(e);
        }
        errorView.appendTitle("Processing"); 
        errorView.appendln("<pre>" + requestContext.getDebugTrace() + "</pre>"); 
        errorView.close();
        writer.close();
    }

    
    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    private String getParameter(String name) {
        return parameters.get(name);
    }

    private void processActions(RequestContext context, boolean userLoggedIn, String actionName) throws IOException {
        if (actionName.endsWith(COMMAND_ROOT)) {
            int pos = actionName.lastIndexOf('/');
            Action action = actions.get(actionName.substring(pos, actionName.length() - COMMAND_ROOT.length()));
            if (action == null) {
                throw new ScimpiException("No logic for " + actionName);
            }

            LOG.debug("processing action: " + action);
            action.process(context);
            String fowardTo = context.forwardTo();
            if (fowardTo != null) {
                processActions(context, true, fowardTo);
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
            noteIfMessagesHaveNotBeenDisplay(context);
            IsisContext.getUpdateNotifier().clear();
        } catch (RuntimeException e) {
            IsisContext.getMessageBroker().getMessages();
            IsisContext.getMessageBroker().getWarnings();
            IsisContext.getUpdateNotifier().clear();
            throw e;
        }
        String page = request.popBuffer();
        PrintWriter writer = context.getWriter();
        writer.write(page);
        if (context.getDebug() == Debug.PAGE) {
            DebugWriter view = new DebugWriter(writer, false);
            context.append(view);
        }
    }

    public void noteIfMessagesHaveNotBeenDisplay(RequestContext context) {
        List<String> messages = IsisContext.getMessageBroker().getMessages();
        if (messages.size() > 0) {
            // TODO write out all messages
            context.getWriter().println("Note - messages existed but where not displayed");
        }
        List<String> warnings = IsisContext.getMessageBroker().getWarnings();
        if (warnings.size() > 0) {
            // TODO write out all warning
            context.getWriter().println("Note - warnings existed but where not displayed");
        }
    }

    private String determineFile(RequestContext context, String file) {
        String fileName = file.trim();
        if (fileName.startsWith(GENERIC)) {
            Object result = context.getVariable(RequestContext.RESULT);
            ObjectAdapter mappedObject = MethodsUtils.findObject(context, (String) result);
            if (mappedObject == null) {
                throw new ScimpiException("No object mapping for " + result);
            }
            if (fileName.equals(GENERIC + "." + EXTENSION)) {
                Facet facet = mappedObject.getSpecification().getFacet(CollectionFacet.class);
                if (facet != null) {
                    ObjectSpecification specification = mappedObject.getSpecification();
                    TypeOfFacet facet2 = specification.getFacet(TypeOfFacet.class);
                    file = findFileForSpecification(context, facet2.valueSpec(), "collection", EXTENSION);
                } else {
                    ObjectAdapter mappedObject2 = mappedObject;
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
        String className = specification.getShortIdentifier();
        String fileName = context.findFile("/" + className + "/" + name + "." + extension);
        if (fileName == null) {
            List<ObjectSpecification> interfaces = specification.interfaces();
            for (int i = 0; i < interfaces.size(); i++) {
                fileName = findFile(context, interfaces.get(i), name, extension);
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

    public void init(String dir) {
        addAction(new ActionAction());
        
        // TODO remove
        addAction(new DebugAction(this));
        addAction(new EditAction());
        addAction(new RemoveAction());
        addAction(new LogonAction());
        addAction(new LogoutAction());
        addAction(new LogAction());

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
        processors.addElementProcessor(new org.apache.isis.viewer.scimpi.dispatcher.view.debug.Debug(this));
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
                        Action instance = (Action) InstanceUtil.createInstance(className);
                        addAction(instance);
                    }
                }
                
                if (element.getName().equals("processors")) {
                    for (Iterator processors = element.elementIterator("processor"); processors.hasNext();) {
                        Element processor = (Element) processors.next();
                        String className = processor.getText();
                        ElementProcessor instance = (ElementProcessor) InstanceUtil.createInstance(className);
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

    public void debug(DebugBuilder debug) {
        debug.appendTitle("Actions");
        Set<String> keySet = actions.keySet();
        ArrayList<String> list = new ArrayList<String>(keySet);
        Collections.sort(list);
        for (String name : list) {
            debug.appendln(name, actions.get(name));            
        }
        /*
        new ArrayList<E>(actions.keySet().iterator())
        Iterator<String> names = Collections.sort().iterator();
        while (names.hasNext()) {
            String name = names.next();
            view.appendRow(name, actions.get(name));
        }
        */
        Iterator<Action> iterator = actions.values().iterator();
        while (iterator.hasNext()) {
            iterator.next().debug(debug);
        }

        processors.debug(debug);
    }
}

