package org.nakedobjects.object.snapshot;

import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.reflect.NakedClassException;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.Value;

import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Traverses object graph from specified root, so that an XML representation of
 * the graph can be returned.
 * 
 * Initially designed to allow snapshots to be easily created.
 * 
 * Typical use:
 * 
 * <pre>
 * SnapshotBuilder navigator = new SnapshotBuilder(customer); // where customer is a reference to an ANO
 * Element customerAsXml = navigator.toXml(); // returns customer's fields, titles of simple references, number of items in collections
 * navigator.navigate(&quot;placeOfBirth&quot;); // navigates to ANO represented by simple reference &quot;placeOfBirth&quot;
 * navigator.navigate(&quot;orders/product&quot;); // navigates to all Orders of Customer, and from them for their products 
 * 
 * </pre>
 * 
 * A possible enhancement (not yet implemented) might be to allow XPath
 * restrictions, perhaps with parameters. eg:
 * 
 * <pre>
 * navigator.navigate(&quot;orders[date.after(:beginningOfYear)]&quot;, new String[] { beginningOfYear.toString() });
 * </pre>
 */
public final class SnapshotBuilder {

    private static final Logger LOG = Logger.getLogger(SnapshotBuilder.class);

    /**
     * Represents a place in the graph to be navigated; really just wraps an object and a copy
     * of its XML together.  The copy of the XML is mutated as the graph of objects is navigated.
     */
    private static class Place {
        private final NakedObject object;
        private final Element element;
        
        Place(final NakedObject object, final Element element) {
            this.object = object;
            this.element = element;
        }
        
        public Element getElement() {
            return element;
        }
        
        public NakedObject getObject() {
            return object;
        }
        
     }
    
    private final Place rootPlace;
    private final NamespaceManager namespaceManager;

    /**
     * Start a snapshot at the root object, using own namespace manager.
     */
    public SnapshotBuilder(final SnapshotHelper rootObject) {
        this(rootObject, new NamespaceManager());
    }

    /**
     * Start a snapshot at the root object, using supplied namespace manager.
     */
    public SnapshotBuilder(final SnapshotHelper rootObject, final NamespaceManager namespaceManager) {
        this.namespaceManager = namespaceManager;
        this.rootPlace = new Place(rootObject, toXml(rootObject, namespaceManager));
    }

    public NakedObject getRootObject() {
        return rootPlace.getObject();
    }
    
    public NamespaceManager getNamespaceManager() {
        return namespaceManager;
    }
    
    
    private Element toXml(final SnapshotHelper object, NamespaceManager namespaceManager) {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element rootElement = namespaceManager.createNofElement(doc, "snapshot");

            doc.appendChild(rootElement);
            
            object.appendXml(namespaceManager, rootElement);
            return rootElement;
        } catch (ParserConfigurationException e) {
            e.printStackTrace(System.err);
            LOG.error("Unable to build snapshot", e);
        }
        return null;
    }


    public void include(final String path) {
        include(path, null);
    }
    public void include(final String path, final String annotation) {
        
        // tokenize into successive fields
        	Vector fieldNames = new Vector();
        	for(StringTokenizer tok = new StringTokenizer(path, "/"); tok.hasMoreTokens(); ) {
        	    fieldNames.addElement(tok.nextToken().toLowerCase());
        	}
        	
        	// navigate first field, from the root.
    	    includeField(rootPlace, fieldNames, annotation);
    }
    
    
    /**
     * @return true if able to navigate the complete vector of field names successfully; false if a
     *                   field could not be located or it turned out to be a value.
     */
    private boolean includeField(final Place place, Vector fieldNames, final String annotation) {

        // we use a copy of the path so that we can safely traverse collections without side-effects
		Vector fieldNamesOrig = fieldNames;
		fieldNames = new Vector();
		for(java.util.Enumeration enum = fieldNamesOrig.elements(); enum.hasMoreElements(); ) {
			fieldNames.addElement(enum.nextElement());
		}

        // see if we have any fields to process
        if (fieldNames.size() == 0) {
            return true;
        }

        // take the first field name from the list, and remove
        String fieldName = (String)fieldNames.elementAt(0);
        fieldNames.removeElementAt(0);
        
        // locate the field in the object's class
        NakedClass nakedClass = place.getObject().getNakedClass();
        Field field = null;
        try {
            // HACK: really want a NakedClass.hasField method to check first.
            field = nakedClass.getField(fieldName);
        } catch(NakedClassException ex) {
            return false;
        }
        
        // locate the corresponding XML element
        NodeList fieldElements = place.getElement().getElementsByTagNameNS("*", field.getName());
        if (fieldElements.getLength() != 1) {
            return false;
        }
        Element fieldElement =  (Element)fieldElements.item(0);
            
        if (fieldNames.size() == 0 && annotation != null) {
            // nothing left in the path, so we will apply the annotation now
            namespaceManager.setNofAttribute(fieldElement, "annotation", annotation);
        }
        

        if (field instanceof Value) {
            return false;
            
        } else if (field instanceof OneToOneAssociation) {
            OneToOneAssociation oneToOneAssociation = ((OneToOneAssociation) field);
            SnapshotHelper referencedObject = (SnapshotHelper)oneToOneAssociation.get(place.getObject()); // cast to ANO rather than NO because haven't (yet) put toXml() onto NO interface.

            if (referencedObject == null) {
                return true; // not a failure if the reference was null
            }

            return appendXmlThenIncludeRemaining(fieldElement, referencedObject, fieldNames, annotation);
        
        } else if (field instanceof OneToManyAssociation) {
            OneToManyAssociation oneToManyAssociation = (OneToManyAssociation) field;
            InternalCollection collection = (InternalCollection) oneToManyAssociation.get(place.getObject());
            
            boolean allFieldsNavigated = true;
            for (int i = 0; i < collection.size(); i++) {
                
                SnapshotHelper referencedObject = (SnapshotHelper)collection.elementAt(i); // cast because haven't (yet) put toXml() on the NO interface, only on ANO.

                allFieldsNavigated = allFieldsNavigated && appendXmlThenIncludeRemaining(fieldElement, referencedObject, fieldNames, annotation);
            }
            return allFieldsNavigated;
        }
        
        return false; // fall through, shouldn't get here but just in case.
    }

    private boolean appendXmlThenIncludeRemaining(Element fieldElement, SnapshotHelper referencedObject, final Vector fieldNames, final String annotation) {

        Element referencedElement = referencedObject.appendXml(namespaceManager, fieldElement);
        Place referencedPlace = new Place(referencedObject, referencedElement);

        return includeField(referencedPlace, fieldNames, annotation);
    }

    public Element toXml() {
        return rootPlace.getElement();
    }

}


/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */