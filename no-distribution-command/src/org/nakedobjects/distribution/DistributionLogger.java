package org.nakedobjects.distribution;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Session;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.utility.Logger;

import java.util.Vector;


public class DistributionLogger extends Logger implements Distribution {
    private static String padding = "      ";
    private static DataStructure dataStructure = new DataStructure();


    public static String dump(Data data) {
        StringBuffer str = new StringBuffer();
        dump(str, data, 1, new Vector());
        return str.toString();
    }

    public static String dump(Data[] data) {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            str.append("\n    [");
            str.append(i + 1);
            str.append("] ");
            dump(str, data[i], 3, new Vector());
        }
        return str.toString();
    }

    private static void dump(StringBuffer str, Data data, int indent, Vector complete) {
        if (data == null) {
            str.append("null");
        } else if (data instanceof NullData) {
            str.append("NULL (NullData object)");
        } else if (data instanceof ValueData) {
            ValueData valueData = ((ValueData) data);
            str.append("ValueData " + valueData.getType() + ":" + valueData.getValue());
        } else if (data instanceof ObjectData) {
            dumpObjectData(str, data, indent, complete);
        } else if (data instanceof CollectionData) {
            dumpCollectionData(str, data, indent, complete);
        } else if (data instanceof ReferenceData) {
            ReferenceData referenceData = (ReferenceData) data;
            str.append("ReferenceData " + referenceData.getType() + ":" + referenceData.getOid() + ":"
                    + referenceData.getVersion());
        } else {
            str.append("Unknown: " + data);
        }
    }

    private static void dumpCollectionData(StringBuffer str, Data data, int indent, Vector complete) {
        CollectionData objectData = ((CollectionData) data);
        str.append("CollectionData " + objectData.getType() + ":" + objectData.getOid() + ":"
                + (objectData.hasAllElements() ? "A" : "-") + ":" + objectData.getVersion());
        Object[] elements = objectData.getElements();
        for (int i = 0; elements != null && i < elements.length; i++) {
            str.append("\n");
            str.append(padding(indent));
            str.append(i + 1);
            str.append(") ");
            dump(str, (Data) elements[i], indent + 1, complete);
        }
    }

    private static void dumpObjectData(StringBuffer str, Data data, int indent, Vector complete) {
        ObjectData objectData = ((ObjectData) data);
        str.append("ObjectData " + objectData.getType() + ":" + objectData.getOid() + ":"
                + (objectData.hasCompleteData() ? "C" : "-") + ":" + objectData.getVersion());

        if(complete.contains(objectData)) {
            str.append(" (already detailed)");
            return;
        }
        
        complete.addElement(objectData);
        NakedObjectSpecification specification = NakedObjects.getSpecificationLoader().loadSpecification(objectData.getType());
        NakedObjectField[] fs = dataStructure.getFields(specification);
        Object[] fields = objectData.getFieldContent();
        for (int i = 0; fields != null && i < fields.length; i++) {
            str.append("\n");
            str.append(padding(indent));
            str.append(i + 1);
            str.append(") ");
            str.append(fs[i].getId());
            str.append(": ");
            dump(str, (Data) fields[i], indent + 1, complete);
        }
    }

    private static String indentedNewLine() {
        return "\n" + padding(2);
    }

    private static String padding(int indent) {
        int length = indent * 3;
        if (length > padding.length()) {
            padding += padding;
        }
        return padding.substring(0, length);
    }

    private final Distribution decorated;

    public DistributionLogger(final Distribution decorated, String fileName) {
        super(fileName, false);
        this.decorated = decorated;
    }

    public DistributionLogger(final Distribution decorated) {
        super(null, true);
        this.decorated = decorated;
    }

    public void abortTransaction(Session session) {
        log("abort transaction");
        decorated.abortTransaction(session);
    }

    public ObjectData[] allInstances(Session session, String fullName, boolean includeSubclasses) {
        log("all instances: " + fullName + (includeSubclasses ? "with subclasses" : ""));
        ObjectData[] allInstances = decorated.allInstances(session, fullName, includeSubclasses);
        log("  <-- instances: " + dump(allInstances));
        return allInstances;
    }

    public void clearAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associate) {
        log("clear association " + fieldIdentifier + indentedNewLine() + "target: " + dump(target) + indentedNewLine()
                + "nassociate: " + dump(associate));
        decorated.clearAssociation(session, fieldIdentifier, target, associate);
    }

    public void destroyObject(Session session, ReferenceData object) {
        log("destroy object: " + dump(object));
        decorated.destroyObject(session, object);
    }

    public void endTransaction(Session session) {
        log("end transaction");
        decorated.endTransaction(session);
    }

    public ResultData executeAction(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameters) {
        log("execute action " + actionIdentifier + "/" + actionType + indentedNewLine() + "target: " + dump(target)
                + indentedNewLine() + "parameters: " + dump(parameters));
        ResultData result;
        try {
            result = decorated.executeAction(session, actionType, actionIdentifier, target, parameters);
            log("  <-- returns: " + dump(result.getReturn()));
            log("  <-- persisted target: " + dump(result.getPersistedTarget()));
            log("  <-- persisted parameters: " + dump(result.getPersistedParameters()));
            log("  <-- updates: " + dump(result.getUpdates()));
        } catch (RuntimeException e) {
            log("  <-- exception: " +  e.getClass().getName() + " " + e.getMessage());
            throw e;
        }
        return result;
    }

    public ObjectData[] findInstances(Session session, InstancesCriteria criteria) {
        log("find instances " + criteria);
        ObjectData[] instances = decorated.findInstances(session, criteria);
        log(" <-- instances: " + dump(instances));
        return instances;
    }

    public Hint getActionHint(Session session, String actionType, String actionIdentifier, ObjectData target, Data[] parameters) {
        log("action hint - no details yet");
        return decorated.getActionHint(session, actionType, actionIdentifier, target, parameters);
    }

    protected Class getDecoratedClass() {
        return decorated.getClass();
    }

    public boolean hasInstances(Session session, String fullName) {
        log("has instances " + fullName);
        boolean hasInstances = decorated.hasInstances(session, fullName);
        log(" <-- instances: " + (hasInstances ? "yes" : "no"));
        return hasInstances;
    }

    public ObjectData makePersistent(Session session, ObjectData object) {
        log("make persistent " + dump(object));
        ObjectData result = decorated.makePersistent(session, object);
        log(" <-- data: " + dump(result));
        return result;
    }

    public int numberOfInstances(Session sessionId, String fullName) {
        log("number of instances of " + fullName);
        int numberOfInstances = decorated.numberOfInstances(sessionId, fullName);
        log("  <-- instances: " + numberOfInstances);
        return numberOfInstances;
    }
    
    public Data resolveField(Session session, ReferenceData data, String name) {
        log("resolve field " + name + " - " + dump(data));
        Data result = decorated.resolveField(session, data, name);
        log(" <-- data: " + dump(result));
        return result;
    }

    public ObjectData resolveImmediately(Session session, ReferenceData target) {
        log("resolve immediately" + dump(target));
        ObjectData result = decorated.resolveImmediately(session, target);
        log("  <-- data: " + dump(result));
        return result;
        
    }

    public void setAssociation(Session session, String fieldIdentifier, ReferenceData target, ReferenceData associate) {
        log("set association " + fieldIdentifier + indentedNewLine() + "target: " + dump(target) + indentedNewLine()
                + "associate: " + dump(associate));
        decorated.setAssociation(session, fieldIdentifier, target, associate);
    }

    public void setValue(Session session, String fieldIdentifier, ReferenceData target, Object value) {
        log("set value " + fieldIdentifier + indentedNewLine() + "target: " + dump(target) + indentedNewLine() + "value: "
                + value);
        decorated.setValue(session, fieldIdentifier, target, value);
    }

    public void startTransaction(Session session) {
        log("start transaction");
        decorated.startTransaction(session);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */