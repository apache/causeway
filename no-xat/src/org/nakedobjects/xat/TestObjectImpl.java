package org.nakedobjects.xat;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.reflect.AssociationSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.NakedObjectSpecificationException;
import org.nakedobjects.object.reflect.NameConvertor;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.reflect.ValueFieldSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;

import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;


public class TestObjectImpl extends AbstractTestObject implements TestObject {
    private TestObjectFactory factory;
    private Hashtable fields;
    private Session session;

    public TestObjectImpl(final Session session, final NakedObject object, final Hashtable viewCache, final TestObjectFactory factory) {
        this.session = session;
        this.factory = factory;
        setForObject(object);
        fields = new Hashtable();

        if (object != null) {
            viewCache.put(object, this);

            FieldSpecification[] a = object.getSpecification().getFields();

            for (int i = 0; i < a.length; i++) {
                FieldSpecification att = a[i];

                if (att instanceof AssociationSpecification) {
                    TestObject associatedView = null;

                    NakedObject associate = (NakedObject) a[i].get(object);
                    if (null != associate) {
                        // if object is not null, use the view in the cache
                        associatedView = (TestObject) viewCache.get(associate);
                    }

                    if (null == associatedView) {
                        associatedView = factory.createTestObject(session, associate, viewCache);
                        // this puts it into the viewCache
                    }

                    fields.put(a[i].getName(), associatedView);
                } else {
                    fields.put(att.getName(), factory.createTestValue(object, (ValueFieldSpecification) att));
                }
            }
        }
    }

    public TestObjectImpl(final Session session, final NakedObject object, final TestObjectFactory factory) {
        this(session, object, new Hashtable(), factory);
    }

    public void assertActionExists(final String name) {
        if (getAction(name) == null) {
            throw new NakedAssertionFailedError("Field '" + name + "' is not found in " + getForObject());
        }
    }


    public void assertActionExists(final String name, final TestNaked[] parameters) {
        if (getAction(name, parameters) == null) {
            throw new NakedAssertionFailedError("Field '" + name + "' is not found in " + getForObject());
        }
    }

    public void assertActionExists(final String name, final TestObject parameter) {
        if (getAction(name, new TestNaked[] { parameter }) == null) {
            throw new NakedAssertionFailedError("Field '" + name + "' is not found in " + getForObject());
        }
    }

    public void assertActionInvisible(String name) {
        assertActionInvisible(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionInvisible(String name, ActionSpecification action,TestNaked[] parameters) {
        boolean vetoed = action.getAbout(session, (NakedObject) getForObject(), nakedObjects(parameters)).canAccess().isVetoed();
        Assert.assertTrue("action '" + name + "' is visible", vetoed);
    }

    public void assertActionInvisible(String name, TestNaked[] parameters) {
        assertActionInvisible(name, getAction(name, parameters), parameters);
    }

    public void assertActionInvisible(String name, TestObject parameter) {
        TestNaked[] parameters = new TestNaked[] { parameter };
        assertActionInvisible(name, getAction(name, parameters), parameters);
    }

    public void assertActionUnusable(String name) {
        assertActionUnusable(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionUnusable(String name, ActionSpecification action, TestNaked[] parameters) {
        Naked[] parameterObjects = nakedObjects(parameters);
        boolean vetoed = action.getAbout(session, (NakedObject) getForObject(), parameterObjects).canUse().isVetoed();
        Assert.assertTrue("action '" + name + "' is usable", vetoed);
    }

    public void assertActionUnusable(String name, TestNaked[] parameters) {
        assertActionUnusable(name, getAction(name, parameters), parameters);
    }

    public void assertActionUnusable(String name, TestObject parameter) {
        TestNaked[] parameters = new TestNaked[] { parameter };
        assertActionUnusable(name, getAction(name, parameters), parameters);
    }

    public void assertActionUsable(String name) {
        assertActionUsable(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionUsable(String name, ActionSpecification action, final TestNaked[] parameters) {
        Naked[] paramaterObjects = nakedObjects(parameters);
        boolean allowed = action.getAbout(session, (NakedObject) getForObject(), paramaterObjects).canUse().isAllowed();
        assertTrue("action '" + name + "' is unusable", allowed);
    }

    public void assertActionUsable(String name, TestNaked[] parameters) {
        assertActionUsable(name, getAction(name, parameters), parameters);
    }

    public void assertActionUsable(String name, TestObject parameter) {
        TestNaked[] parameters = new TestNaked[] { parameter };
        assertActionUsable(name, getAction(name, parameters), parameters);
    }

    public void assertActionVisible(String name) {
        assertActionVisible(name, getAction(name), new TestNaked[0]);
    }

    private void assertActionVisible(String name, ActionSpecification action, TestNaked[] parameters) {
        boolean allowed = action.getAbout(session, (NakedObject) getForObject(), nakedObjects(parameters)).canAccess().isAllowed();
        assertTrue("action '" + name + "' is invisible", allowed);
    }

    public void assertActionVisible(String name, TestNaked[] parameters) {
        assertActionVisible(name, getAction(name, parameters), parameters);
    }

    public void assertActionVisible(String name, TestObject parameter) {
        assertActionVisible(name, getAction(name, new TestNaked[] { parameter }), new TestNaked[] { parameter });
    }

    /** @deprecated */
    public void assertCantInvokeAction(final String name) {
        assertActionUnusable(name);
    }

    /** @deprecated */
    public void assertCantInvokeAction(final String name, final TestObject parameter) {
        assertActionUnusable(name, parameter);
    }

    public void assertEmpty(final String fieldName) {
        assertEmpty(null, fieldName);
    }

    public void assertEmpty(final String message, final String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked actual = getField(fieldName);
        Object object = actual.getForObject();

        // TODO refactor to remove redundancy
        if (object instanceof NakedValue) {
            NakedValue value = ((NakedValue) object);
            if (!value.isEmpty()) {
                throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' to be empty, but contained "
                        + value.titleString().toString());
            }
        } else {
            if (object != null) {
                throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' to be empty, but contained "
                        + ((NakedObject) object).titleString().toString());
            }
        }
    }

    private void assertFalse(String message, boolean condition) {
        if (condition) {
            throw new NakedAssertionFailedError(message);
        }
    }

    public void assertFieldContains(final String fieldName, final NakedValue expectedValue) {
        assertFieldContains(null, simpleName(fieldName), expectedValue);
    }

    /**
     * Check that the specified field contains the expected value. If it does
     * not contain the expected value the test fails.
     * 
     * @group assert
     */
    public void assertFieldContains(final String fieldName, final String expectedValue) {
        assertFieldContains(null, simpleName(fieldName), expectedValue);
    }

    /**
     * Check that the specified field has the same value as the specified
     * NakedValue. If it differs the test fails. A note is added to the
     * documentation to explain that the specified field now has a specific
     * value.
     *  
     */
    public void assertFieldContains(final String message, final String fieldName, final NakedValue expectedValue) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked actual = getField(fieldName);
        NakedValue actualValue = ((NakedValue) actual.getForObject());

        if (!actualValue.isSameAs(expectedValue)) {
            throw new NakedAssertionFailedError(expected(message) + " value of " + expectedValue + " but got " + actualValue);
        }
    }

    /**
     * Check that the specified field contains the expected value. If it does
     * not contain the expected value the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     * @group assert
     */
    public void assertFieldContains(final String message, final String fieldName, final String expectedValue) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        Naked object = getField(fieldName).getForObject();

        if (object instanceof InternalCollection) {
            InternalCollection collection = (InternalCollection) object;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject element = collection.elementAt(i);
                if (element.titleString().toString().equals(expectedValue)) {
                    return;
                }
            }
            throw new NakedAssertionFailedError(expected(message) + " object titled '" + expectedValue + "' but could not find it in the internal collection");
        } else {
            String actualValue = object.titleString().toString();

            if (!actualValue.equals(expectedValue)) {
                throw new NakedAssertionFailedError(expected(message) + " value " + expectedValue + " but got " + actualValue);
            }
        }
    }

    /**
     * Check that the specified field contains the expected object (as
     * represented by the specifed view. If it does not contain the expected
     * object the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     * @group assert
     */
    public void assertFieldContains(final String message, final String fieldName, final TestObject expected) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        Naked actualObject = getField(fieldName).getForObject();

        if (expected == null) {
            if (actualObject instanceof InternalCollection) {
                int size = ((InternalCollection) actualObject).size();

                if (size > 0) {
                    throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' collection to contain zero elements, but found " + size);
                }
            } else if (actualObject != null) {
                throw new NakedAssertionFailedError(expected(message) + " an empty field, but found " + actualObject);
            }
        } else {
            Naked expectedObject = expected.getForObject();

            if (actualObject == null) {
                if (expectedObject != null)
                    throw new NakedAssertionFailedError(expected(message) + expectedObject + "  but found an empty field");
            } else if (actualObject instanceof InternalCollection) {
                if (!((InternalCollection) actualObject).contains((NakedObject) expectedObject)) {
                    throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' collection to contain " + expectedObject);
                }
            } else if (!actualObject.equals(expectedObject)) {
                throw new NakedAssertionFailedError(expected(message) + " object of " + expectedObject + " but got " + actualObject);
            }
        }
    }

    /**
     * Check that the specified field contains the expected object (as
     * represented by the specifed view. If it does not contain the expected
     * object the test fails.
     * 
     * @group assert
     */
    public void assertFieldContains(final String fieldName, final TestObject expectedView) {
        assertFieldContains(null, fieldName, expectedView);
    }

    public void assertFieldContainsType(final String fieldName, final String expectedType) {
        assertFieldContainsType(null, fieldName, expectedType);
    }

    public void assertFieldContainsType(final String message, final String fieldName, final String expectedType) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked actual = getField(fieldName);
        String actualType = ((Naked) actual.getForObject()).getSpecification().getShortName();

        if (!actualType.equals(expectedType)) {
            throw new NakedAssertionFailedError(expected(message) + " type " + expectedType + " but got " + actualType);
        }
    }

    public void assertFieldContainsType(final String message, final String fieldName, final String title,
            final String expectedType) {
        Naked object = getField(fieldName).getForObject();

        if (object instanceof InternalCollection) {
            InternalCollection collection = (InternalCollection) object;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject element = collection.elementAt(i);
                if (element.titleString().toString().equals(title)) {
                    if (!element.getSpecification().getShortName().equals(expectedType)) {
                        throw new NakedAssertionFailedError(expected(message) + " object " + title + " to be of type " + expectedType + " but was "
                                + element.getSpecification().getShortName());
                    }
                    return;
                }
            }
            throw new NakedAssertionFailedError(expected(message) + " object " + title + " but could not find it in the internal collection");
        }
    }

    public void assertFieldExists(final String fieldName) {
        try {
            ((NakedObject) getForObject()).getSpecification().getField(fieldName);
        } catch (NakedObjectSpecificationException e) {
            throw new NakedAssertionFailedError("No field called '" + fieldName + "' in " + getForObject().getClass().getName());
        }

    }

    public void assertFieldInvisible(final String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        boolean canAccess = field.canAccess(ClientSession.getSession(), (NakedObject) getForObject());
        assertFalse("Field '" + fieldName + "' is visible", canAccess);
    }

    public void assertFieldModifiable(String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldModifiable(fieldName, field);
    }

    private void assertFieldModifiable(String fieldName, FieldSpecification field) {
        boolean canAccess = field.canUse(session, (NakedObject) getForObject());
        assertTrue("Field '" + fieldName + "' is unmodifiable for " + session.getUser(), canAccess);
    }

    /**
     * Check that a field exists with the specified name, and it is read-only.
     * If it does not exist, or is writable, the test fails.
     * 
     * @deprecated
     */
    public void assertFieldReadOnly(final String fieldName) {
        assertFieldUnmodifiable(fieldName);
    }

    public void assertFieldUnmodifiable(final String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        boolean canAccess = field.canUse(session, (NakedObject) getForObject());
        assertFalse("Field '" + fieldName + "' is modifiable", canAccess);
    }

    public void assertFieldVisible(String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
    }

    private void assertFieldVisible(String fieldName, FieldSpecification field) {
        boolean canAccess = field.canAccess(session, (NakedObject) getForObject());
        assertTrue("Field '" + fieldName + "' is invisible", canAccess);
    }

    public void assertNotEmpty(final String fieldName) {
        assertNotEmpty(null, fieldName);
    }

    public void assertNotEmpty(final String message, final String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked actual = getField(fieldName);
        Object object = actual.getForObject();

        // TODO refactor to remove redundancy
        if (object instanceof NakedValue) {
            if (((NakedValue) object).isEmpty()) {
                throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' to contain something but it was empty");
            }
        } else {
            if (object == null) {
                throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' to contain something but it was empty");
            }
        }
    }

    /**
     * Check that the title of this object is the same as the expected title. If
     * it is not the same the test fails.
     * 
     * @group assert
     */
    public void assertTitleEquals(final String expectedTitle) {
        assertTitleEquals(null, expectedTitle);
    }

    /**
     * Check that the title of this object is the same as the expected title. If
     * it is not the same the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     * @group assert
     */
    public void assertTitleEquals(final String message, final String expectedTitle) {
        if (!getTitle().equals(expectedTitle)) {
            throw new NakedAssertionFailedError(expected(message) + " title of " + getForObject() + " as '" + expectedTitle + "' but got '" + getTitle()
                    + "'");
        }
    }

    private void assertTrue(String message, boolean condition) {
        if (!condition) {
            throw new NakedAssertionFailedError(message);
        }
    }

    public void assertType(final String expectedType) {
        assertType("", expectedType);
    }

    public void assertType(final String message, final String expectedType) {
        String actualType = getForObject().getSpecification().getShortName();

        if (!actualType.equals(expectedType)) {
            throw new NakedAssertionFailedError(expected(message) + " type " + expectedType + " but got " + actualType);
        }
    }

    /**
     * Drop the specified view (object) into the specified field.
     * 
     * <p>
     * If the field already contains an object then, as an object cannot be
     * dropped on a non-empty field, the test will fail.
     * </p>
     * 
     * @group action
     */
    public void associate(final String fieldName, final TestObject object) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        TestNaked targetField = getField(fieldName);

        if (targetField instanceof TestValue) {
            throw new IllegalActionError("drop(..) not allowed on value target field; use fieldEntry(..) instead");
        }

        if ((targetField.getForObject() != null) && !(targetField.getForObject() instanceof InternalCollection)) {
            throw new IllegalActionError("Field already contains an object: " + targetField.getForObject());
        }

        AssociationSpecification association = (AssociationSpecification) fieldFor(fieldName);
        NakedObject obj = (NakedObject) object.getForObject();

        if (association.getType() != null && !obj.getSpecification().isOfType(association.getType())) {
            throw new IllegalActionError("Can't drop a " + object.getForObject().getSpecification().getShortName() + " on to the " + fieldName
                    + " field (which accepts " + association.getType() + ")");
        }

        association.setAssociation((NakedObject) getForObject(), obj);
    }

    /**
     * Removes an existing object reference from the specified field. Mirrors
     * the 'Remove Reference' menu option that each object field offers by
     * default.
     * 
     * @group action
     */
    public void clearAssociation(final String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        TestObject targetField = (TestObject) fields.get(fieldName);

        if (targetField instanceof TestValue) {
            throw new IllegalActionError("set(..) not allowed on value target field; use fieldEntry(..) instead");
        } else {
            NakedObject ref = (NakedObject) fieldFor(fieldName).get((NakedObject) getForObject());
            if (ref != null) {
                ((OneToOneAssociationSpecification) fieldFor(fieldName)).clearAssociation((NakedObject) getForObject(), ref);
            }
        }
    }

    /**
     * Removes the existing object reference, which has the specified title,
     * from the specified multi-object field. Mirrors the 'Remove Reference'
     * menu option that each collection field offers by default.
     * 
     * @group action
     */
    public void clearAssociation(final String fieldName, final String title) {
        FieldSpecification assoc = fieldFor(fieldName);
        assertFieldVisible(fieldName, assoc);
        assertFieldModifiable(fieldName, assoc);

        TestObject viewToRemove = getField(fieldName, title);

        if (!(assoc instanceof OneToManyAssociationSpecification)) {
            throw new IllegalActionError("removeReference not allowed on target field " + fieldName);
        }

        Naked no = viewToRemove.getForObject();

        if (!(no instanceof NakedObject)) {
            throw new NakedAssertionFailedError("A NakedObject was to be removed from the InternalCollection, but found " + no);
        }

        ((OneToManyAssociationSpecification) assoc).clearAssociation((NakedObject) getForObject(), (NakedObject) no);
    }

    private final String expected(final String text) {
        return ((text == null) ? "E" : text + "; e") + "xpected";
    }

    /**
     * Enters text into an editable field. Data entered here is parsed in
     * exactly the same way as in GUI, and should therefore be given in the
     * correct form and formatted correctly. For fields that normally use some
     * other interaction, e.g. a checkbox, then the correct textual form must be
     * used (for the checkbox this is 'TRUE' and 'FALSE').
     */
    public void fieldEntry(final String fieldName, final String value) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        TestValue testField;
        Object view = fields.get(simpleName(fieldName));

        if (view instanceof TestValue) {
            testField = (TestValue) view;
            Naked valueObject = (Naked) fieldFor(fieldName).get((NakedObject) getForObject());
            if (valueObject == null) {
                throw new NakedAssertionFailedError("Field '" + fieldName
                        + "' contains null, but should contain an NakedValue object");
            }
            testField.setForObject(valueObject);
            testField.fieldEntry(value);
            NakedObject object = ((NakedObject) getForObject());
            object.getContext().getObjectManager().objectChanged(object);
        } else {
            throw new IllegalActionError("Can only make an entry (eg by keyboard) into a value field");
        }
    }

    private FieldSpecification fieldFor(final String fieldName) {
        FieldSpecification att = (FieldSpecification) ((NakedObject) getForObject()).getSpecification().getField(simpleName(fieldName));
        if (att == null) {
            throw new NakedAssertionFailedError("No field called '" + fieldName + "' in " + getForObject().getClass().getName());
        } else {
            return att;
        }
    }

    public ActionSpecification getAction(final String name) {
        ActionSpecification action = null;

        try {
            NakedObjectSpecification nakedClass = ((NakedObject) getForObject()).getSpecification();
            action = nakedClass.getObjectAction(ActionSpecification.USER, name);
        } catch (NakedObjectSpecificationException e) {
            throw new NakedAssertionFailedError(e.getMessage());
        }
        if (action == null) {
            throw new NakedAssertionFailedError("Method not found: " + name);
        }
        return action;
    }

    public ActionSpecification getAction(final String name, final TestNaked[] parameters) {
        final Naked[] parameterObjects = nakedObjects(parameters);
        final int noParameters = parameters.length;
        final NakedObjectSpecification[] parameterClasses = new NakedObjectSpecification[noParameters];
        for (int i = 0; i < noParameters; i++) {
            parameterClasses[i] = parameterObjects[i].getSpecification();
        }

        try {
            NakedObjectSpecification nakedClass = ((NakedObject) getForObject()).getSpecification();
            ActionSpecification action = nakedClass.getObjectAction(ActionSpecification.USER, simpleName(name), parameterClasses);
            if (action == null) {
                String parameterList = "";
                for (int i = 0; i < noParameters; i++) {
                    parameterList += (i > 0 ? ", " : "") + parameterClasses[i].getFullName();
                }
                throw new NakedAssertionFailedError("Method not found: " + name + "(" + parameterList + ")");
            }
            return action;
        } catch (NakedObjectSpecificationException e) {
            String targetName = getForObject().getSpecification().getShortName();
            String parameterList = "";
            for (int i = 0; i < noParameters; i++) {
                parameterList += (i > 0 ? ", " : "") + parameterClasses[i];
            }
            throw new NakedAssertionFailedError("Can't find action '" + name + "' with parameters (" + parameterList + ") on a " + targetName);
        }
    }

    /**
     * Returns the view, from within this collection, that has the specified
     * title.
     */
    public final TestObject getAssociation(final String title) {
        if (!(getForObject() instanceof NakedCollection)) {
            throw new IllegalActionError("selectByTitle will only select from a collection!");
        }

        NakedCollection collection = (NakedCollection) getForObject();

        Enumeration e = collection.elements();
        NakedObject object = null;
        int noElements = 0;

        while (e.hasMoreElements()) {
            NakedObject element = (NakedObject) e.nextElement();

            if (element.titleString().toString().indexOf(title) >= 0) {
                object = element;
                noElements++;
            }
        }

        if (noElements == 0) {
            throw new IllegalActionError("selectByTitle must find an object within " + collection);
        }

        if (noElements > 1) {
            throw new IllegalActionError("selectByTitle must select only one object within " + collection);
        }

        return factory.createTestObject(session, object);
    }

    public TestNaked getField(final String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked view = (TestNaked) fields.get(simpleName(fieldName));

        view.setForObject((Naked) fieldFor(fieldName).get((NakedObject) getForObject()));

        return view;
    }

    /**
     * Get the view for the object held within the named collection view, that
     * has the specified title.
     */
    public TestObject getField(final String fieldName, final String title) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        
        Naked object = getField(fieldName).getForObject();
        if (!(object instanceof InternalCollection)) {
            throw new IllegalActionError(
                    "getField(String, String) only allows an object to be selected from an InternalCollection");
        }

        Enumeration e = ((NakedCollection)object).elements();
        NakedObject selectedObject = null;
        int noElements = 0;
        while (e.hasMoreElements()) {
            NakedObject element = (NakedObject) e.nextElement();
            if (element.titleString().toString().equals(title)) {
                selectedObject = element;
                noElements++;
            }
        }

        if (noElements == 0) {
            throw new IllegalActionError("The field '" + fieldName + "' must contain an object titled '" + title + "' within it");
        } else if (noElements > 1) {
            throw new IllegalActionError("The field '" + fieldName + "' must only contain one object titled '" + title
                    + "' within it");
        }

        return factory.createTestObject(session, selectedObject);
    }

    /**
     * returns the title of the object as a String
     */
    public String getFieldTitle(final String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        if (getField(fieldName).getForObject() == null) {
            throw new IllegalActionError("No object to get title from in field " + fieldName + " within " + getForObject());
        }

        return getField(fieldName).getTitle();
    }

    /**
     * returns the title of the object as a String
     */
    public String getTitle() {
        if (getForObject() == null) {
            throw new IllegalActionError("??");
        }

        return getForObject().titleString().toString();
    }

    /**
     * Invokes this object's zero-parameter action method of the the given name.
     * This mimicks the right-clicking on an object and subsequent selection of
     * a menu item.
     */
    public TestObject invokeAction(final String name) {
        ActionSpecification action = getAction(name);
        assertActionUsable(name, action, new TestNaked[0]);
        assertActionVisible(name, action, new TestNaked[0]);

        NakedObject result = action.execute((NakedObject) getForObject());
        return ((result == null) ? null : factory.createTestObject(session, result));
    }

    public TestObject invokeAction(final String name, final TestNaked[] parameters) {
        ActionSpecification action = getAction(simpleName(name), parameters);

        Naked[] parameterObjects = nakedObjects(parameters);
        boolean allowed = action.getAbout(session, (NakedObject) getForObject(), parameterObjects).canUse().isAllowed();
        assertTrue("action '" + name + "' is unusable", allowed);

        allowed = action.getAbout(session, (NakedObject) getForObject(), parameterObjects).canAccess().isAllowed();
        assertTrue("action '" + name + "' is invisible", allowed);

        NakedObject result = action.execute((NakedObject) getForObject(), parameterObjects);
        if (result == null) {
            return null;
        } else {
            return factory.createTestObject(session, result);
        }

    }

    /**
     * Drop the specified view (object) onto this object and invoke the
     * corresponding <code>action</code> method. A new view representing the
     * returned object, if any is returned, from the invoked <code>action</code>
     * method is returned by this method.
     */
    public TestObject invokeAction(final String name, final TestObject parameter) {
        ActionSpecification action = getAction(name, new TestNaked[] { parameter });

        NakedObject dropObject = (NakedObject) parameter.getForObject();
        boolean allowed = action.getAbout(session, (NakedObject) getForObject(), dropObject).canUse().isAllowed();
        assertTrue("action '" + name + "' is unusable", allowed);

        allowed = action.getAbout(session, (NakedObject) getForObject(), dropObject).canAccess().isAllowed();
        assertTrue("action '" + name + "' is invisible", allowed);

        NakedObject result = action.execute((NakedObject) getForObject(), dropObject);
        if (result == null) {
            return null;
        } else {
            return factory.createTestObject(session, result);
        }
    }

    private Naked[] nakedObjects(TestNaked[] parameters) {
        Naked[] parameterObjects = new Naked[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            parameterObjects[i] = (Naked) parameters[i].getForObject();
        }
        return parameterObjects;
    }

    private String simpleName(final String name) {
        return NameConvertor.simpleName(name);
    }

    /**
     * Test the named field by calling fieldEntry with the specifed value and
     * then check the value stored is the same.
     */
    public void testField(String fieldName, String expectedValue) {
        testField(fieldName, expectedValue, expectedValue);
    }

    /**
     * Test the named field by calling fieldEntry with the set value and then
     * check the value stored against the expected value.
     */
    public void testField(String fieldName, String setValue, String expectedValue) {
        fieldEntry(fieldName, setValue);
        Assert.assertEquals("Field '" + fieldName + "' contains unexpected value", expectedValue, getField(fieldName).getTitle());
    }

    /**
     * Test the named field by calling fieldEntry with the set value and then
     * check the value stored against the expected value.
     */
    public void testField(String fieldName, TestObject expected) {
        associate(fieldName, expected);
        Assert.assertEquals(expected.getForObject(), getField(fieldName).getForObject());
    }

    public String toString() {
        return getForObject().toString();
    }
}