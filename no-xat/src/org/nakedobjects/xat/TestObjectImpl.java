package org.nakedobjects.xat;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.TextEntryParseException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.NakedObjectAssociation;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.OneToManyAssociation;
import org.nakedobjects.object.reflect.OneToOneAssociation;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;

import java.util.Enumeration;
import java.util.Hashtable;

import junit.framework.Assert;

import org.apache.log4j.Logger;


public class TestObjectImpl extends AbstractTestObject implements TestObject {
    private static final Logger LOG = Logger.getLogger(TestObjectImpl.class);
    private Hashtable fields;

    /**
     * The object this mock is showing
     */
    private NakedObject forObject;
    private Hashtable existingTestObjects;

    /**
     * Return the object this mock is showing
     */
    public final Naked getForNaked() {
        return forObject;
    }

    public final Object getForObject() {
        return forObject.getObject();
    }

    public final void setForNaked(Naked object) {
        if (object == null || object instanceof NakedObject) {
            forObject = (NakedObject) object;
        } else {
            throw new IllegalArgumentException("Object must be a NakedObject");
        }
    }

    public TestObjectImpl(final Session session, final NakedObject object, final Hashtable viewCache,
            final TestObjectFactory factory) {
        super(session, factory);

        LOG.debug("created test object for " + object);
        setForNaked(object);
        existingTestObjects = viewCache;
    }

    public TestObjectImpl(final Session session, final NakedObject object, final TestObjectFactory factory) {
        this(session, object, new Hashtable(), factory);
    }

    public void assertEmpty(final String fieldName) {
        assertEmpty(null, fieldName);
    }

    public void assertEmpty(final String message, final String fieldName) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked actual = getField(fieldName);
        Naked object = actual.getForNaked();

        if (object != null && !forObject.isEmpty(field)) {
            throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' to be empty, but contained "
                    + object.titleString());
        }
    }

    private void assertFalse(String message, boolean condition) {
        if (condition) {
            throw new NakedAssertionFailedError(message);
        }
    }

    public void assertFieldContains(final String fieldName, final Object expected) {
        assertFieldContains(null, fieldName, expected);
    }

    /**
     * Check that the specified field contains the expected value. If it does
     * not contain the expected value the test fails.
     * 
     * @group assert
     */
    public void assertFieldContains(final String fieldName, final String expected) {
        assertFieldContains(null, fieldName, expected);
    }

    /**
     * Check that the specified field has the same value as the specified
     * NakedValue. If it differs the test fails. A note is added to the
     * documentation to explain that the specified field now has a specific
     * value.
     *  
     */
    public void assertFieldContains(final String message, final String fieldName, final Object expected) {
        TestNaked actual = retrieveField(fieldName);
        Naked actualValue = actual.getForNaked();

        if (actualValue == null && expected == null) {
            return;
        }

        if (actualValue == null && expected instanceof TestNakedNullParameter) {
            return;
        }

        if (actualValue.getObject().equals(expected)) {
            return;
        }

        throw new NakedAssertionFailedError(expected(message) + " value of " + expected + " but got " + actualValue.getObject());
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
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        Naked object = getField(fieldName).getForNaked();

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
            Naked expectedObject = expected.getForNaked();

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
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked actual = getField(fieldName);
        Naked nakedObject = actual.getForNaked();
        if (nakedObject == null && !(expected == null || expected.equals(""))) {
            throw new NakedAssertionFailedError(expected(message) + " type " + expected + " but got nothing");
        }
        String actualType = nakedObject.getSpecification().getShortName();
        if (!actualType.equals(expected)) {
            throw new NakedAssertionFailedError(expected(message) + " type " + expected + " but got " + actualType);
        }
    }

    public void assertFieldContainsType(final String message, final String fieldName, final String title, final String expected) {
        Naked object = getField(fieldName).getForNaked();

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

    public void assertFieldDoesNotContain(final String fieldName, final NakedObject expected) {
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
    public void assertFieldDoesNotContain(final String message, final String fieldName, final NakedObject notExpected) {
        TestNaked actual = retrieveField(fieldName);
        NakedObject actualValue = ((NakedObject) actual.getForNaked());

        if (notExpected == null) {
            if (actualValue == null) {
                throw new NakedAssertionFailedError(unexpected(message) + " empty field " + fieldName);
            }
        } else {
            if (actualValue != null && actualValue.isSameAs(notExpected)) {
                throw new NakedAssertionFailedError(unexpected(message) + " value " + notExpected + " in field " + fieldName);
            }
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
     * @param notExpected
     *                       Value the Field should not contain
     * @group assert
     */

    public void assertFieldDoesNotContain(final String message, final String fieldName, final String notExpected) {
        Naked object = retrieveNakedField(fieldName);
        if (object instanceof InternalCollection) {
            InternalCollection collection = (InternalCollection) object;
            for (int i = 0; i < collection.size(); i++) {
                NakedObject element = collection.elementAt(i);
                if (element.titleString().toString().equals(notExpected)) {
                    throw new NakedAssertionFailedError(unexpected(message) + " object titled '" + notExpected
                            + "'  was found in the collection in field " + fieldName);
                }
            }

        } else {
            String actualValue = object == null ? "" : object.titleString();

            if (actualValue.equals(notExpected)) {
                throw new NakedAssertionFailedError(unexpected(message) + " value " + notExpected + " in field " + fieldName);
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

    public void assertFieldDoesNotContain(final String message, final String fieldName, final TestObject notExpected) {
        Naked actualObject = retrieveNakedField(fieldName);

        if (notExpected == null) {
            if (actualObject instanceof InternalCollection) {
                int size = ((InternalCollection) actualObject).size();

                if (size == 0) {
                    throw new NakedAssertionFailedError(expected(message) + " '" + fieldName
                            + "' collection to contain one or more elements, but it was empty");
                }
            } else if (actualObject == null) {
                throw new NakedAssertionFailedError(expected(message) + " field to contain an object but it was empty");
            }
        } else {
            Naked notExpectedObject = notExpected.getForNaked();

            if (actualObject == null) {
                if (notExpectedObject == null) {
                    throw new NakedAssertionFailedError(expected(message) + " an object but the field " + fieldName
                            + "  is empty");
                }
            } else if (actualObject instanceof InternalCollection) {
                if (((InternalCollection) actualObject).contains((NakedObject) notExpectedObject)) {
                    throw new NakedAssertionFailedError(unexpected(message) + " entry in '" + fieldName + "' collection: "
                            + notExpectedObject);
                }
            } else if (actualObject.equals(notExpectedObject)) {
                throw new NakedAssertionFailedError(unexpected(message) + " object " + notExpectedObject + " in field "
                        + fieldName);
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
    public void assertFieldDoesNotContain(final String fieldName, final TestObject notExpected) {
        assertFieldDoesNotContain(null, fieldName, notExpected);
    }

    public void assertFieldEntryCantParse(String fieldName, String value) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        TestValue view = (TestValue) field(fieldName);
        if (!(view.getForNaked() instanceof NakedValue)) {
            throw new IllegalActionError("Can only make an entry (eg by keyboard) into a parsable field");
        }

        Naked valueObject = forObject.getField(field);
        //NakedObject valueObject = (NakedObject) field.get((NakedObject)
        // getForObject());
        if (valueObject == null) {
            throw new NakedAssertionFailedError("Field '" + fieldName
                    + "' contains null, but should contain an NakedValue object");
        }
        try {
            NakedValue nakedValue = (NakedValue) valueObject;
            if (nakedValue == null) {
                nakedValue = (NakedValue) field.getSpecification().acquireInstance();
            }
            nakedValue.parseTextEntry(value);
            throw new NakedAssertionFailedError("Value was unexpectedly parsed");
        } catch (TextEntryParseException expected) {} catch (InvalidEntryException e) {
            throw new NakedAssertionFailedError("Value was unexpectedly parsed");
        }

    }

    public void assertFieldEntryInvalid(String fieldName, String value) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        TestValue view = (TestValue) field(fieldName);
        if (!(view.getForNaked() instanceof NakedValue)) {
            throw new IllegalActionError("Can only make an entry (eg by keyboard) into a value field");
        }

//        Naked valueObject = forObject.getField(field);
        Naked valueObject = field.getSpecification().acquireInstance();
        if (valueObject == null) {
            throw new NakedAssertionFailedError("Field '" + fieldName
                    + "' contains null, but should contain an NakedValue object");
        }
        try {
            NakedValue nakedValue = (NakedValue) valueObject;
            if (nakedValue == null) {
                nakedValue = (NakedValue) field.getSpecification().acquireInstance();
            }
            nakedValue.parseTextEntry(value);

            Hint about = getForNaked().getHint(ClientSession.getSession(), field, nakedValue);
            boolean isAllowed = about.isValid().isAllowed();
            if (isAllowed) {
                throw new NakedAssertionFailedError("Value was unexpectedly validated");
            }
         } catch (InvalidEntryException expected) {}
    }

    public void assertFieldExists(final String fieldName) {
        try {
            ((NakedObject) getForNaked()).getSpecification().getField(fieldName);
        } catch (NakedObjectSpecificationException e) {
            throw new NakedAssertionFailedError("No field called '" + fieldName + "' in "
                    + getForNaked().getSpecification().getFullName());
        }

    }

    public void assertFieldInvisible(final String fieldName) {
        NakedObjectField field = fieldFor(fieldName);
        // boolean canAccess =
        // getForNaked().canAccess(ClientSession.getSession(), field);
        boolean canAccess = getForNaked().getHint(session, field, null).canAccess().isAllowed();
        assertFalse("Field '" + fieldName + "' is visible", canAccess);
    }

    public void assertFieldModifiable(String fieldName) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldModifiable(fieldName, field);
    }

    private void assertFieldModifiable(String fieldName, NakedObjectField field) {
        //boolean canUse = getForNaked().canUse(session, field);
        boolean canUse = getForNaked().getHint(session, field, null).canUse().isAllowed();
        assertTrue("Field '" + fieldName + "' in " + getForNaked() + " is unmodifiable", canUse);
    }

    public void assertFieldUnmodifiable(final String fieldName) {
        NakedObjectField field = fieldFor(fieldName);
        //boolean canUse = getForNaked().canUse(session, field);
        boolean canUse = getForNaked().getHint(session, field, null).canUse().isAllowed();
        assertFalse("Field '" + fieldName + "' is modifiable", canUse);
    }

    public void assertFieldVisible(String fieldName) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
    }

    private void assertFieldVisible(String fieldName, NakedObjectField field) {
        //     boolean canAccess = getForNaked().canAccess(session, field);
        boolean canAccess = getForNaked().getHint(session, field, null).canAccess().isAllowed();
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

        if (!collection.elementAt(0).equals(expected.getForNaked())) {
            throw new NakedAssertionFailedError(expected(message) + " object '" + expected.getForNaked() + "' but found '"
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
        if (!collection.elementAt(last).equals(expected.getForNaked())) {
            throw new NakedAssertionFailedError(expected(message) + " object '" + expected.getForNaked() + "' but found '"
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
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        //    TestNaked actual = getField(fieldName);
        //   NakedObject object = actual.getForObject();

        // TODO refactor to remove redundancy
        if (forObject.isEmpty(field)) {
            throw new NakedAssertionFailedError(expected(message) + " '" + fieldName + "' to contain something but it was empty");
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
            throw new NakedAssertionFailedError(expected(message) + " title of " + getForNaked() + " as '" + expectedTitle
                    + "' but got '" + getTitle() + "'");
        }
    }

    public void assertType(final String expected) {
        assertType("", expected);
    }

    public void assertType(final String message, final String expected) {
        String actualType = getForNaked().getSpecification().getShortName();

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
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        TestNaked targetField = getField(fieldName);

        if (targetField instanceof TestValue) {
            throw new IllegalActionError("drop(..) not allowed on value target field; use fieldEntry(..) instead");
        }

        if ((targetField.getForNaked() != null) && !(targetField.getForNaked() instanceof InternalCollection)) {
            throw new IllegalActionError("Field already contains an object: " + targetField.getForNaked());
        }

        NakedObjectAssociation association = (NakedObjectAssociation) fieldFor(fieldName);
        NakedObject obj = (NakedObject) object.getForNaked();

        if (association.getSpecification() != null && !obj.getSpecification().isOfType(association.getSpecification())) {
            throw new IllegalActionError("Can't drop a " + object.getForNaked().getSpecification().getShortName() + " on to the "
                    + fieldName + " field (which accepts " + association.getSpecification() + ")");
        }

        NakedObject nakedObject = (NakedObject) getForNaked();
        Hint about;
        if (association instanceof OneToOneAssociation) {
            assertEmpty(fieldName);
            about = nakedObject.getHint(session, association, obj);
        } else if (association instanceof OneToManyAssociation) {
            about = ((OneToManyAssociation) association).getHint(session, nakedObject, obj, true);
        } else {
            throw new NakedObjectRuntimeException();
        }

        if (about.canAccess().isVetoed()) {
            throw new IllegalActionError("Cannot access the field " + field);
        }

        if (about.canUse().isVetoed()) {
            throw new IllegalActionError("Cannot associate " + obj + " in the field " + field + " within " + nakedObject + ": "
                    + about.canUse().getReason());
        }

        nakedObject.setAssociation(association, obj);
    }

    /**
     * Removes an existing object reference from the specified field. Mirrors
     * the 'Remove Reference' menu option that each object field offers by
     * default.
     * 
     * @group action
     */
    public void clearAssociation(final String fieldName) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        TestObject targetField = (TestObject) field(fieldName);

        if (targetField instanceof TestValue) {
            throw new IllegalActionError("set(..) not allowed on value target field; use fieldEntry(..) instead");
        } else {
            NakedObject ref = (NakedObject) forObject.getField(field);
            if (ref != null) {
                getForNaked().clearAssociation((OneToOneAssociation) fieldFor(fieldName), ref);
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
        NakedObjectField assoc = fieldFor(fieldName);
        assertFieldVisible(fieldName, assoc);
        assertFieldModifiable(fieldName, assoc);

        TestObject viewToRemove = getField(fieldName, title);

        if (!(assoc instanceof OneToManyAssociation)) {
            throw new IllegalActionError("removeReference not allowed on target field " + fieldName);
        }

        Naked no = viewToRemove.getForNaked();

        if (!(no instanceof NakedObject)) {
            throw new NakedAssertionFailedError("A NakedObject was to be removed from the InternalCollection, but found " + no);
        }

        ((OneToManyAssociation) assoc).clearAssociation((NakedObject) getForNaked(), (NakedObject) no);
    }

    public boolean equals(Object obj) {
        if (obj instanceof TestObjectImpl) {
            TestObjectImpl object = (TestObjectImpl) obj;
            return object.getForNaked() == getForNaked();
        }
        return false;
    }

    private final String expected(final String text) {
        return ((text == null) ? "E" : text + "; e") + "xpected";
    }

    private final String unexpected(final String text) {
        return ((text == null) ? "U" : text + "; u") + "nexpected";
    }

    /**
     * Enters text into an editable field. Data entered here is parsed in
     * exactly the same way as in GUI, and should therefore be given in the
     * correct form and formatted correctly. For fields that normally use some
     * other interaction, e.g. a checkbox, then the correct textual form must be
     * used (for the checkbox this is 'TRUE' and 'FALSE').
     */
    public void fieldEntry(final String fieldName, final String textEntry) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);
        assertFieldModifiable(fieldName, field);

        NakedObject object = ((NakedObject) getForNaked());
        try {
            NakedValue value = object.getValue((OneToOneAssociation) field);
            if (value == null) {
                value = (NakedValue) field.getSpecification().acquireInstance();
            }
            value.parseTextEntry(textEntry);

            Hint hint = object.getHint(session, field, value);
            if (hint.isValid().isVetoed()) {
                throw new NakedAssertionFailedError("Value is not valid: " + textEntry);
            }

            object.setValue((OneToOneAssociation) field, value.getObject());
        } catch (TextEntryParseException e) {
            throw new IllegalActionError("");
        } catch (InvalidEntryException e) {
            throw new IllegalActionError("");
        }
        NakedObjects.getObjectManager().saveChanges();
    }

    private NakedObjectField fieldFor(final String fieldName) {
        NakedObjectField att = (NakedObjectField) ((NakedObject) getForNaked()).getSpecification()
                .getField(simpleName(fieldName));
        if (att == null) {
            throw new NakedAssertionFailedError("No field called '" + fieldName + "' in " + getForNaked().getClass().getName());
        } else {
            return att;
        }
    }

    /**
     * Returns the view, from within this collection, that has the specified
     * title.
     */
    public final TestObject getAssociation(final String title) {
        if (!(getForNaked() instanceof NakedCollection)) {
            throw new IllegalActionError("selectByTitle will only select from a collection!");
        }

        NakedCollection collection = (NakedCollection) getForNaked();

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
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        Naked object = getField(fieldName).getForNaked();
        if (!(object instanceof InternalCollection)) {
            new NakedAssertionFailedError(expected(message) + " a collection but got " + object);

        }
        return (InternalCollection) object;
    }

    private int getCollectionSize(String collectionName) {
        Naked object = getField(collectionName).getForNaked();
        if (object instanceof InternalCollection) {
            InternalCollection col = (InternalCollection) object;
            return col.size();
        } else {
            throw new NakedAssertionFailedError(collectionName + " is not a collection");
        }
    }

    private TestNaked field(final String fieldName) {
        if (fields == null) {
            fields = new Hashtable();

            Naked object = getForNaked();

            if (object != null) {
                existingTestObjects.put(object, this);

                NakedObjectField[] a = object.getSpecification().getFields();

                for (int i = 0; i < a.length; i++) {
                    NakedObjectField att = a[i];

                    if (att instanceof NakedObjectAssociation) {
                        TestNaked associatedView = null;

                        Naked associate;
                        associate = ((PojoAdapter) object).getField(att);
                        if (att.isValue()) {
                            associatedView = factory.createTestValue(session, (NakedValue) associate);
                        } else {
                            if (associate != null) {
                                // if object is not null, use the view in the
                                // cache
                                associatedView = (TestObject) existingTestObjects.get(associate);
                            }

                            if (associatedView == null) {
                                if (att.isCollection()) {
                                    associatedView = factory.createTestCollection(session, (NakedCollection) associate);
                                } else {
                                    associatedView = factory.createTestObject(session, (NakedObject) associate,
                                            existingTestObjects);
                                    // this puts it into the viewCache
                                }
                            }
                        }
                        fields.put(a[i].getName(), associatedView);
                    }
                }
            }
        }

        return (TestNaked) fields.get(simpleName(fieldName));
    }

    public TestNaked getField(final String fieldName) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        TestNaked testObject = field(fieldName);
        testObject.setForNaked(forObject.getField(fieldFor(fieldName)));
        return testObject;
    }

    public TestObject getFieldAsObject(final String fieldName) {
        return (TestObject) getField(fieldName);
    }

    public TestValue getFieldAsValue(final String fieldName) {
        return (TestValue) getField(fieldName);
    }

    /**
     * Get the view for the object held within the named collection view, that
     * has the specified title.
     */
    public TestObject getField(final String fieldName, final String title) {
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        Naked object = getField(fieldName).getForNaked();
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
        NakedObjectField field = fieldFor(fieldName);
        assertFieldVisible(fieldName, field);

        if (getField(fieldName).getForNaked() == null) {
            throw new IllegalActionError("No object to get title from in field " + fieldName + " within " + getForNaked());
        }

        return getField(fieldName).getTitle();
    }

    /**
     * returns the title of the object as a String
     */
    public String getTitle() {
        if (getForNaked() == null) {
            throw new IllegalActionError("??");
        }

        return getForNaked().titleString().toString();
    }

    private TestNaked retrieveField(String fieldName) {
        NakedObjectField field = fieldFor(fieldName);
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
        return retrieveField(fieldName).getForNaked();
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
        Assert.assertEquals(expected.getForNaked(), getField(fieldName).getForNaked());
    }

    public String toString() {
        return forObject == null ? null : forObject.toString();
    }

    protected Action getAction(String name, NakedObjectSpecification[] parameterClasses) {
        NakedObjectSpecification spec = ((NakedObject) getForNaked()).getSpecification();
        return spec.getObjectAction(Action.USER, name, parameterClasses);
    }
}