package org.nakedobjects.object.snapshot;

import org.nakedobjects.object.NakedClass;

import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Manages namespaces (URIs) and their aliases.
 */
public class NamespaceManager {


    /**
     * URI representing the namespace of the in-built xmlns namespace as defined by w3.org.
     *   
     * The NamespaceManager will not allow any namespaces with this URI to be added.
     */
    public static final String W3_ORG_XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    /**
     * Namespace alias for {@link #W3_ORG_XMLNS_URI}.
     *  
     * The NamespaceManager  will not allow any namespace to use this alias.
     */
    public static final String W3_ORG_XMLNS_ALIAS = "xmlns";
    
    /**
     * URI representing the namespace of NakedObject framework's metamodel.
     * 
     * The NamespaceManager  will not allow any namespaces with this URI to be added.
     */
    public static final String NOF_METAMODEL_NS_URI = "http://www.nakedobjects.org/ns/0.1/metamodel/";
    /**
     * Namespace alias for {@link #NOF_METAMODEL_NS_URI}.
     * 
     * The NamespaceManager will not allow any namespace to use this alias.
     */
    public static final String NOF_METAMODEL_NS_ALIAS = "nof";

    /**
     * The prefix of the URI to use for application namespaces if none explicitly supplied in the
     * constructor.
     */
    public final static String DEFAULT_URI_PREFIX = "http://www.nakedobjects.org/ns/app/";
    /**
     * The prefix of the alias to use if none explicitly supplied in the constructor.
     */
    public final static String DEFAULT_ALIAS_PREFIX = "app";
    
    private Hashtable aliasByUriMap = new Hashtable();
    private String uriPrefix;
    private String aliasPrefix;
    private int index = 0;
    
    public NamespaceManager() {
        this(DEFAULT_URI_PREFIX, DEFAULT_ALIAS_PREFIX);
    }

    /**
     * @param uriPrefix     the prefix for the application namespace's URIs
     * @param aliasPrefix the prefix for the application namespace's alias
     */
    public NamespaceManager(String uriPrefix, final String aliasPrefix) {
        uriPrefix = trailingSlash(uriPrefix);
        if (W3_ORG_XMLNS_URI.equals(uriPrefix)) {
            throw new IllegalArgumentException("URI reserved for w3.org namespace.");
        }
        if (W3_ORG_XMLNS_ALIAS.equals(aliasPrefix)) {
            throw new IllegalArgumentException("Alias reserved for w3.org.");
        }
        if (NOF_METAMODEL_NS_URI.equals(uriPrefix)) {
            throw new IllegalArgumentException("URI reserved for NOF metamodel namespace.");
        }
        if (NOF_METAMODEL_NS_ALIAS.equals(aliasPrefix)) {
            throw new IllegalArgumentException("Alias reserved for NOF metamodel namespace.");
        }
        this.uriPrefix = uriPrefix;
        this.aliasPrefix = aliasPrefix;
    }
    
    
    /**
     * The base of the Uri in use.  All namespaces are concatenated with this. 
     * 
     * 
     * The namespace string will be the concatenation of the plus the
     * package name of the class of the object being referenced.
     * 
     * If not specified in the constructor, then {@link #DEFAULT_URI_PREFIX} is used.
     */
    public String getUriPrefix() {
        return uriPrefix;
    }
    
    /**
     * The default alias, used as the basis for application namespaces managed by this
     * NamespaceManager.
     * 
     * If more than one namespace is managed and no hints are supplied, then the aliases are
     * kept unique by appending '1', '2', '3' and so on.
     * 
     * So, if the <code>NamespaceManager</code> is instantiated using "app" as its base prefix,
     * and no hints are supplied, then the prefixes will be 'app', 'app1', 'app2', 'app3' and so on.
     * 
     * If not specified in the constructor, then {@link #DEFAULT_ALIAS_PREFIX} is used.
     */
    public String getAliasPrefix() {
        return aliasPrefix;
    }

    
    /**
     * Whether a namespace is known for the (package of) the class with the given name.
     */
    public boolean managingNamespaceFor(final String fullyQualifiedClassName) {
        String uri =  getUri(fullyQualifiedClassName);
        String alias = (String)aliasByUriMap.get(uri);
        return (alias != null);
    }

    /**
     * @see #manageNamespaceFor(String, String)
     */
    public boolean managingNamespaceFor(final NakedClass nakedClass) {
        return managingNamespaceFor(nakedClass.fullName());
    }

    /**
     * @see #manageNamespaceFor(String, String)
     */
    public boolean managingNamespaceFor(final Class clazz) {
        return managingNamespaceFor(clazz.getName());
    }


    /**
     * Returns a namespace alias for the class.
     */
    public String getAlias(String fullyQualifiedClassName) {
        String uri =  getUri(fullyQualifiedClassName);
        String alias = (String)aliasByUriMap.get(uri);
        if (alias == null) {
            manageNamespaceFor(fullyQualifiedClassName);
            return getAlias(fullyQualifiedClassName); // should get one next time
        }
        return alias;
    }

    /**
     * @see #getAlias(String)
     */
    public String getAlias(final Class clazz) {
        return getAlias(clazz.getName());
    }

    /**
     * @see #getAlias(String)
     */
    public String getAlias(final NakedClass nakedClass) {
        return getAlias(nakedClass.fullName());
    }

    /**
     * Returns the namespace URI for the class.
     */
    public String getUri(final String fullyQualifiedClassName) {
        return getUriPrefix() +packageNameFor(fullyQualifiedClassName); 
    }

    /**
     * @see #getUri(String)
     */
    public String getUri(final NakedClass nakedClass) {
        return getUri(nakedClass.fullName());
    }

    /**
     * @see #getUri(String)
     */
    public String getUri(final Class clazz) {
        return getUri(clazz.getName());
    }

    /**
     * Returns the qualified name (for example, <code>app:Customer</code>) for the given class.
     */
    public String getQname(final String fullyQualifiedClassName) {
        return getAlias(fullyQualifiedClassName) + ":" + classNameFor(fullyQualifiedClassName);
    }

    /**
     * @see #getQname(String)
     */
    public String getQname(final NakedClass nakedClass) {
        return getQname(nakedClass.fullName());
    }

    /**
     * @see #getQname(String)
     */
    public String getQname(final Class clazz) {
        return getQname(clazz.getName());
    }

    /**
     * Utility method that returns the package name for the supplied fully qualified class name, or
     * <code>default</code> if  the class is in no namespace / in the default namespace.
     * 
     * cf 'dirname' in Unix.
     */
    public String packageNameFor(String fullyQualifiedClassName) {
        int fullNameLastPeriodIdx = fullyQualifiedClassName.lastIndexOf('.');
        if (fullNameLastPeriodIdx > 0) {
            return fullyQualifiedClassName.substring(0, fullNameLastPeriodIdx);
        } else {
            return "default"; // TODO: should provide a better way to specify namespace.
        }
    }
    
    /**
     * Utility method that returns just the class's name for the supplied fully qualified class name.
     * 
     * cf 'basename' in Unix.
     */
    public String classNameFor(String fullyQualifiedClassName) {
        int fullNameLastPeriodIdx = fullyQualifiedClassName.lastIndexOf('.');
        if (fullNameLastPeriodIdx > 0 && fullNameLastPeriodIdx < fullyQualifiedClassName.length()) {
            return fullyQualifiedClassName.substring(fullNameLastPeriodIdx+1);
        } else {
            return fullyQualifiedClassName;
        }
    }

    private String trailingSlash(final String str) {
        return str.endsWith("/")?str:str+"/";
    }

    private String nextAlias() {
        if (index == 0) {
            index++;
            return aliasPrefix;
        } else {
            index++;
            return aliasPrefix + index;
        }
    }


    /**
     * Helper method used to create a namespace entry for the class name if not yet known.
     * 
     * The uri of the resultant namespace will be the supplied base Uri concatenated with the
     * package name of the supplied class name.  The alias associated with this namespace
     * will be the supplied alias if that alias is unused, otherwise the default alias will be used to
     * derive a unique alias.
     */
    private void manageNamespaceFor(String fullyQualifiedClassName) {
        String uri = getUri(fullyQualifiedClassName);
        String alias = (String)aliasByUriMap.get(uri);
        if (alias == null) {
            // can't find a prefix for this uri, so generate one
            alias = nextAlias();
            aliasByUriMap.put(uri, alias);
        }
    }

    /**
     * Adds a namespace declaration (<code>xmlns:app</code> or similar) for the class
     * provided to the root element of the document that owns this element. 
     */
    public void addNamespaceIfRequired(Element element, String fullyQualifiedClassName) {
        if (!managingNamespaceFor(fullyQualifiedClassName)) {
            manageNamespaceFor(fullyQualifiedClassName);
            String uri = getUri(fullyQualifiedClassName);
            String alias = getAlias(fullyQualifiedClassName);
            addNamespace(element, alias, uri);
        }
    }

    /**
     * Returns the root element for the element by looking up the owner document for the element,
     * and from that its document element.
     * 
     * If no document element exists, just returns the supplied document.
     */
    public Element rootElementFor(final Element element) {
        Document doc = element.getOwnerDocument();
        if (doc == null) {
            return element;
        }
        Element rootElement = doc.getDocumentElement();
        if (rootElement == null) {
            return element;
        }
        return rootElement;
    }
    

    /**
     * Creates an element in the NOF namespace, adding the definition of the namespace to the
     * root element of the document if required,
     */
    public Element createNofElement(Document doc, String elementName) {
        Element element = doc.createElementNS(NOF_METAMODEL_NS_URI, NOF_METAMODEL_NS_ALIAS+":" + elementName);
        addNofNamespace(element);
        return element;
    }

    private void addNofNamespace(final Element element) {
        rootElementFor(element).setAttributeNS(W3_ORG_XMLNS_URI, W3_ORG_XMLNS_ALIAS+":"+NOF_METAMODEL_NS_ALIAS, NOF_METAMODEL_NS_URI);
    }

    public void setNofAttribute(Element element, String attributeName, String attributeValue) {
        element.setAttributeNS(NOF_METAMODEL_NS_URI, NOF_METAMODEL_NS_ALIAS+":" + attributeName, attributeValue);
    }

    public String getNofAttribute(Element element, String attributeName) {
        return element.getAttributeNS(NOF_METAMODEL_NS_URI, attributeName);
    }

    public void addNamespace(Element element, String alias, String nsUri) {
        rootElementFor(element).setAttributeNS(W3_ORG_XMLNS_URI, W3_ORG_XMLNS_ALIAS+":" + alias, nsUri);
    }

    public void addXmlnsNamespace(Element element) {
        rootElementFor(element).setAttributeNS(W3_ORG_XMLNS_URI,
                NOF_METAMODEL_NS_ALIAS+":"+ NOF_METAMODEL_NS_ALIAS,
                NOF_METAMODEL_NS_URI);
     }


}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/