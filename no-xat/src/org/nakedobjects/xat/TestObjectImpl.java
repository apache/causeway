package org.nakedobjects.xat;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.ValueParseException;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.reflect.AssociationSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.NakedObjectSpecificationException;
import org.nakedobjects.object.reflect.OneToManyAssociationSpecification;
import org.nakedobjects.object.reflect.OneToOneAssociationSpecification;
import org.nakedobjects.object.reflect.ValueFieldSpecification;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;

import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;


public class TestObjectImpl extends AbstractTestObject implements TestObject {
    private Hashtable fields;

    /**
    The object this mock is showing
    */
   private Naked forObject;

   /**
    Return  the object this mock is showing
    */
   public final Naked getForObject() {
       return forObject;
   }
   
   
   public final void setForObject(Naked object) {
       forObject = object;
   }

   public NakedObjectSpecification getSpecification() {
       return forObject.getSpecification();
   }

    public TestObjectImpl(final Session session, final NakedObject object, final Hashtable viewCache,
            final TestObjectFactory factory) {
        super(session, factory);
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

    public void assertFieldContains(final String fieldName, final NakedValue expected) {
        assertFieldContains(null, simpleName(fieldName), expected);
    }

    /**
     * Check that the specified field contains the expected value. If it does
     * not contain the expected value the test fails.
     * 
     * @group assert
     */
    public void assertFieldContains(final String fieldName, final String expected) {
        assertFieldContains(null, simpleName(fieldName), expected);
    }

    /**
     * Check that the specified field has the same value as the specified
     * NakedValue. If it differs the test fails. A note is added to the
     * documentation to explain that the specified field now has a specific
     * value.
     *  
     */
    public void assertFieldContains(final String message, final String fieldName, final NakedValue expected) {
        TestNaked actual = retrieveField(fieldName);
        NakedValue actualValue = ((NakedValue) actual.getForObject());

        if (!actualValue.isSameAs(expected)) {
            throw new NakedAssertionFailedError(expected(message) + " value of " + expected + " but got " + actualValue);
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
    public void assertFieldContains(final String message, final String fieldName, final String expected) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        Naked object = getField(fieldName).getForObject();

        if (!(object instanceof InternalCollection)) {
            String actualValue = object.titleString().toString();

            if (!actualValue.equals(expected)) {
                throw new NakedAssertionFailedError(expected(message) + " value " + expected + " but got " + actualValue);
            }

        } else {
            InternalCollection collection = (InternalCollection) object;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject element = collection.elementAt(i);
                if (element.titleString().toString().equals(expected)) {
                    return;
                }
            }
            throw new NakedAssertionFailedError(expected(message) + " object titled '" + expected
                    + "' but could not find it in the internal collection");
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

        Naked actualObject = retrieveNakedField(fieldName);

        if (expected == null) {
            if (actualObject instanceof InternalCollection) {
                int size = ((InternalCollection) actualObject).size();

                if (size > 0) {
                    throw new NakedAssertionFailedError(expected(message) + " '" + fieldName
                            + "' collection to contain zero elements, but found " + size);
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
                    throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' collection to contain "
                            + expectedObject);
                }
            } else if (!actualObject.equals(expectedObject)) {
                throw new NakedAssertionFailedError(expected(message) + " object of " + expectedObject + " but got "
                        + actualObject);
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
    public void assertFieldContains(final String fieldName, final TestObject expected) {
        assertFieldContains(null, fieldName, expected);
    }

    public void assertFieldContainsType(final String fieldName, final String expected) {
        assertFieldContainsType(null, fieldName, expected);
    }

    public void assertFieldContainsType(final String message, final String fieldName, final String expected) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked actual = getField(fieldName);
        String actualType = ((Naked) actual.getForObject()).getSpecification().getShortName();

        if (!actualType.equals(expected)) {
            throw new NakedAssertionFailedError(expected(message) + " type " + expected + " but got " + actualType);
        }
    }

    public void assertFieldContainsType(final String message, final String fieldName, final String title, final String expected) {
        Naked object = getField(fieldName).getForObject();

        if (object instanceof InternalCollection) {
            InternalCollection collection = (InternalCollection) object;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject element = collection.elementAt(i);
                if (element.titleString().toString().equals(title)) {
                    if (!element.getSpecification().getShortName().equals(expected)) {
                        throw new NakedAssertionFailedError(expected(message) + " object " + title + " to be of type " + expected
                                + " but was " + element.getSpecification().getShortName());
                    }
                    return;
                }
            }
            throw new NakedAssertionFailedError(expected(message) + " object " + title
                    + " but could not find it in the internal collection");
        }
    }

    /**
     * Check that the specified field does not contain the expected value. If it
     * does contain the expected value the test fails.
     * 
     * @group assert
     */

    public void assertFieldDoesNotContain(final String fieldName, final NakedValue expected) {
        assertFieldDoesNotContain(null, simpleName(fieldName), expected);
    }

    /**
     * Check that the specified field does not contain the expected value. If it
     * does contain the expected value the test fails.
     * 
     * @group assert
     */
    public void assertFieldDoesNotContain(final String fieldName, final String expected) {
        assertFieldDoesNotContain(null, simpleName(fieldName), expected);
    }

    /**
     * Check that the specified field has the same value as the specified
     * NakedValue. If it differs the test fails. A note is added to the
     * documentation to explain that the specified field now has a specific
     * value.
     *  
     */
    public void assertFieldDoesNotContain(final String message, final String fieldName, final NakedValue expected) {
        TestNaked actual = retrieveField(fieldName);
        NakedValue actualValue = ((NakedValue) actual.getForObject());

        if (actualValue.isSameAs(expected)) {
            throw new NakedAssertionFailedError(expected(message) + " value of " + expected + " is the same as  " + actualValue);
        }
    }

    /**
     * Check that the specified field does not contain the expected value. If it
     * does contain the expected value the test fails.
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     * @param fieldName
     *                       Name of the Field to check
     * @param expected
     *                       Value the Field should not contain
     * @group assert
     */

    public void assertFieldDoesNotContain(final String message, final String fieldName, final String expected) {
        Naked object = retrieveNakedField(fieldName);
        if (object instanceof InternalCollection) {
            InternalCollection collection = (InternalCollection) object;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject element = collection.elementAt(i);
                if (element.titleString().toString().equals(expected)) {
                    throw new NakedAssertionFailedError(expected(message) + " object titled '" + expected
                            + "'  was found in the internal collection");
                }
            }

        } else {
            String actualValue = object.titleString().toString();

            if (actualValue.equals(expected)) {
                throw new NakedAssertionFailedError(expected(message) + " value " + expected + " is the same as " + actualValue);
            }
        }
    }

    /**
     * Added to confirm in negative tests that a Field does not contain the
     * expected value // This works for
     * 
     * @param message
     *                       text to add to the failure message, which is displayed after a
     *                       failure.
     * @param fieldName
     *                       Name of the field to check the value in *
     * @group assert
     */

    public void assertFieldDoesNotContain(final String message, final String fieldName, final TestObject expected) {
        Naked actualObject = retrieveNakedField(fieldName);

        if (expected == null) {
            if (actualObject instanceof InternalCollection) {
                int size = ((InternalCollection) actualObject).size();

                if (size == 0) {
                    throw new NakedAssertionFailedError(expected(message) + " '" + fieldName
                            + "' collection to contain one or more elements, but it was empty");
                }
            } else if (actualObject == null) {
                throw new NakedAssertionFailedError(expected(message) + " field to be " + actualObject + " but it was empty");
            }
        } else {
            Naked expectedObject = expected.getForObject();

            if (actualObject == null) {
                if (expectedObject == null)
                    throw new NakedAssertionFailedError(expected(message) + expectedObject + "  is an empty field");
            } else if (actualObject instanceof InternalCollection) {
                if (((InternalCollection) actualObject).contains((NakedObject) expectedObject)) {
                    throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' collection does contain "
                            + expectedObject);
                }
            } else if (actualObject.equals(expectedObject)) {
                throw new NakedAssertionFailedError(expected(message) + " object of " + expectedObject + " does contain "
                        + actualObject);
            }
        }
    }

    /**
     * Check that the specified field does not contain the expected object (as
     * represented by the specifed view. If it does contain the expected object
     * the test fails.
     * 
     * @group assert
     */
    public void assertFieldDoesNotContain(final String fieldName, final TestObject expected) {
        assertFieldDoesNotContain(null, fieldName, expected);
    }

    public void assertFieldEntryCantParse(String fieldName, String value) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        Object view = fields.get(simpleName(fieldName));

        if (!(view instanceof TestValue)) {
            throw new IllegalActionError("Can only make an entry (eg by keyboard) into a value field");
        }

        NakedValue valueObject = (NakedValue) field.get((NakedObject) getForObject());
        if (valueObject == null) {
            throw new NakedAssertionFailedError("Field '" + fieldName
                    + "' contains null, but should contain an NakedValue object");
        }
        try {
            valueObject.parse(value);
            throw new NakedAssertionFailedError("Value was unexpectedly parsed");
        } catch (ValueParseException expected) {}

    }

    public void assertFieldEntryInvalid(String fieldName, String value) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        Object view = fields.get(simpleName(fieldName));

        if (!(view instanceof TestValue)) {
            throw new IllegalActionError("Can only make an entry (eg by keyboard) into a value field");
        }

        NakedValue valueObject = (NakedValue) field.get((NakedObject) getForObject());
        if (valueObject == null) {
            throw new NakedAssertionFailedError("Field '" + fieldName
                    + "' contains null, but should contain an NakedValue object");
        }
        try {
            //            valueObject.parse(value);
            //        SimpleFieldAbout about = new SimpleFieldAbout(null, (NakedObject)
            // getForObject());
            ((ValueFieldSpecification) field).parseAndSave((NakedObject) getForObject(), value);
            throw new NakedAssertionFailedError("Value was unexpectedly validateds");
        } catch (InvalidEntryException expected) {}
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
        assertTrue("Field '" + fieldName + "' is unmodifiable for user " + session.getUser(), canAccess);
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

    public void assertFirstElementInField(String fieldName, String expected) {
        assertFirstElementInField("", fieldName, expected);
    }

    public void assertFirstElementInField(String message, String fieldName, String expected) {
        InternalCollection collection = getCollection(message, fieldName);
        if (collection.size() == 0) {
            throw new NakedAssertionFailedError(expected(message) + " a first element, but there are no elements");
        }

        if (!collection.elementAt(0).titleString().equals(expected)) {
            throw new NakedAssertionFailedError(expected(message) + " object titled '" + expected + "' but found '"
                    + collection.elementAt(0).titleString() + "'");
        }
    }

    public void assertFirstElementInField(String message, String fieldName, TestObject expected) {
        InternalCollection collection = getCollection(message, fieldName);
        if (collection.size() == 0) {
            throw new NakedAssertionFailedError(expected(message) + " a first element, but there are no elements");
        }

        if (!collection.elementAt(0).equals(expected.getForObject())) {
            throw new NakedAssertionFailedError(expected(message) + " object '" + expected.getForObject() + "' but found '"
                    + collection.elementAt(0) + "'");
        }
    }

    public void assertFirstElementInField(String fieldName, TestObject expected) {
        assertFirstElementInField("", fieldName, expected);
    }

    public void assertLastElementInField(String fieldName, String expected) {
        assertLastElementInField("", fieldName, expected);
    }

    public void assertLastElementInField(String message, String fieldName, String expected) {
        InternalCollection collection = getCollection(message, fieldName);
        if (collection.size() == 0) {
            throw new NakedAssertionFailedError(expected(message) + " a last element, but there are no elements");
        }

        int last = collection.size() - 1;
        if (!collection.elementAt(last).titleString().equals(expected)) {
            throw new NakedAssertionFailedError(expected(message) + " object titled '" + expected + "' but found '"
                    + collection.elementAt(last).titleString() + "'");
        }
    }

    public void assertLastElementInField(String message, String fieldName, TestObject expected) {
        InternalCollection collection = getCollection(message, fieldName);
        if (collection.size() == 0) {
            throw new NakedAssertionFailedError(expected(message) + " a first element, but there are no elements");
        }

        int last = collection.size() - 1;
        if (!collection.elementAt(last).equals(expected.getForObject())) {
            throw new NakedAssertionFailedError(expected(message) + " object '" + expected.getForObject() + "' but found '"
                    + collection.elementAt(last) + "'");
        }
    }

    public void assertLastElementInField(String fieldName, TestObject expected) {
        assertLastElementInField("", fieldName, expected);
    }

    /**
     * Test the named collection to determine no of Elements in contains If the
     * value noOfElements does not match the test fails
     */

    public void assertNoOfElements(String collectionName, int noOfElements) {
        int actualSize = getCollectionSize(collectionName);
        if (actualSize != noOfElements) {
            throw new NakedAssertionFailedError("Excepted " + collectionName + " to contain " + noOfElements
                    + " instead it contained " + actualSize);
        }
    }

    /**
     * Test the named collection to check that it does not contain the provided
     * number of Elements If the value noOfElements does match the Collection
     * size the test fails
     */

    public void assertNoOfElementsNotEqual(String collectionName, int noOfElements) {
        int actualSize = getCollectionSize(collectionName);
        if (actualSize == noOfElements) {
            throw new NakedAssertionFailedError("Excepted " + collectionName + " to contain " + noOfElements
                    + " instead it contained " + actualSize);
        }
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
                throw new NakedAssertionFailedError(expected(message) + " '" + fieldName
                        + "' to contain something but it was empty");
            }
        } else {
            if (object == null) {
                throw new NakedAssertionFailedError(expected(message) + " '" + fieldName
                        + "' to contain something but it was empty");
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
            throw new NakedAssertionFailedError(expected(message) + " title of " + getForObject() + " as '" + expectedTitle
                    + "' but got '" + getTitle() + "'");
        }
    }

    public void assertType(final String expected) {
        assertType("", expected);
    }

    public void assertType(final String message, final String expected) {
        String actualType = getForObject().getSpecification().getShortName();

        if (!actualType.equals(expected)) {
            throw new NakedAssertionFailedError(expected(message) + " type " + expected + " but got " + actualType);
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
        if(field instanceof OneToOneAssociationSpecification) {
            assertEmpty(fieldName);
        }
        
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
            throw new IllegalActionError("Can't drop a " + object.getForObject().getSpecification().getShortName()
                    + " on to the " + fieldName + " field (which accepts " + association.getType() + ")");
        }
        
        NakedObject nakedObject = (NakedObject) getForObject();
        About about;
        if(association instanceof OneToOneAssociationSpecification) {
            about = ((OneToOneAssociationSpecification) association).getAbout(session, nakedObject, obj);
        } else if(association instanceof OneToManyAssociationSpecification) {
            about = ((OneToManyAssociationSpecification) association).getAbout(session, nakedObject, obj, true);
        } else {
            throw new NakedObjectRuntimeException();
        }
        
        if(about.canAccess().isVetoed()) {
            throw new IllegalActionError("Cannot access the field " + field);
        }
        
        if(about.canUse().isVetoed()) {
            throw new IllegalActionError("Cannot associate " + obj + " in the field " + field + " within " + nakedObject + ": " + about.canUse().getReason());
        }
        
        association.setAssociation(nakedObject, obj);
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

    public boolean equals(Object obj) {
        if (obj instanceof TestObjectImpl) {
            TestObjectImpl object = (TestObjectImpl) obj;
            return object.getForObject() == getForObject();
        }
        return false;
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
            Naked valueObject = (Naked) field.get((NakedObject) getForObject());
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
        FieldSpecification att = (FieldSpecification) ((NakedObject) getForObject()).getSpecification().getField(
                simpleName(fieldName));
        if (att == null) {
            throw new NakedAssertionFailedError("No field called '" + fieldName + "' in " + getForObject().getClass().getName());
        } else {
            return att;
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

    private InternalCollection getCollection(String message, String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        Naked object = getField(fieldName).getForObject();
        if (!(object instanceof InternalCollection)) {
            new NakedAssertionFailedError(expected(message) + " a collection but got " + object);

        }
        return (InternalCollection) object;
    }

    private int getCollectionSize(String collectionName) {
        Naked object = getField(collectionName).getForObject();
        if (object instanceof InternalCollection) {
            InternalCollection col = (InternalCollection) object;
            return col.size();
        } else {
            throw new NakedAssertionFailedError(collectionName + " is not a collection");
        }
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

        Enumeration e = ((NakedCollection) object).elements();
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

    private TestNaked retrieveField(String fieldName) {
        FieldSpecification field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked object = getField(fieldName);
        return object;

    }

    /**
     * Check that the specified field contains the expected value. If it does
     * not contain the expected value the test fails.
     * 
     * @group assert
     */

    private Naked retrieveNakedField(String fieldName) {
        return retrieveField(fieldName).getForObject();
    }

    /**
     * Test the named field by calling fieldEntry with the specifed value and
     * then check the value stored is the same.
     */
    public void testField(String fieldName, String expected) {
        testField(fieldName, expected, expected);
    }

    /**
     * Test the named field by calling fieldEntry with the set value and then
     * check the value stored against the expected value.
     */
    public void testField(String fieldName, String setValue, String expected) {
        fieldEntry(fieldName, setValue);
        Assert.assertEquals("Field '" + fieldName + "' contains unexpected value", expected, getField(fieldName).getTitle());
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
    
    protected ActionSpecification getAction(NakedObjectSpecification nakedClass, String name) {
        return nakedClass.getObjectAction(ActionSpecification.USER, name);
    }
    
    protected ActionSpecification getAction(NakedObjectSpecification nakedClass, String name,
            NakedObjectSpecification[] parameterClasses) {
        return nakedClass.getObjectAction(ActionSpecification.USER, simpleName(name), parameterClasses);}
}