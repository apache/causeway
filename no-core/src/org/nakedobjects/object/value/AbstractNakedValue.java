package org.nakedobjects.object.value;

import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.control.FieldAbout;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

// TODO remove ValueChanged stuff - to check with Dan first.
public abstract class AbstractNakedValue implements NakedValue {
    private final static Logger logger = Logger.getLogger(AbstractNakedValue.class);
    private transient About about;

    /**
     * @link aggregation
     * @associates <{NakedValueListener}>
     */
    private transient Vector nakedValueListeners = new Vector();

    public AbstractNakedValue() {
        super();
    }

    /**
     * Returns the objects About object. If none is set up then it returns the
     * default one, namely FieldAbout.READ_WRITE
     */
    public About about() {
        if (about == null) {
            return FieldAbout.READ_WRITE;
        } else {
            return about;
        }
    }

    /**
     * Registers an object as a listener of this naked value. Whenever the naked
     * value's state changes, then all listeners will be notified.
     * <p>
     * CAVEAT: only objects that are resolved and instantiated will be notified.
     * In general, this means that it is okay for the owning object of a naked
     * value to register itself as a listener, but not for other objects unless
     * it can be arranged for them to have already been resolved from the object
     * store.
     * </p>
     */
    public final void addNakedValueListener(NakedValueListener l) {
        nakedValueListeners.addElement(l);
    }

    /**
     * Throws an exception if the ojbect is null
     */
    protected void checkCanOperate() {}

    public String contextualTitle() {
        return null;
    }

    /**
     * Returns a deep copy of this object.
     * <p>
     * Alternatively, could have implemented <code>clone()</code>. However,
     * that is a much misunderstood method.
     * @see java.lang.Object#clone()
     */
    public final AbstractNakedValue deepCopy() {
        try {
            AbstractNakedValue clone = (AbstractNakedValue) getClass().newInstance();
            clone.restoreString(this.saveString());
            return clone;
        } catch (InstantiationException e) {
            throw new NakedObjectRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new NakedObjectRuntimeException(e);
        }
    }

    /**
     * Overridden version of <code>fireValueChanged(NakedValue)</code> that
     * passes <code>null</code> for the old value.
     * <p>
     * This was added since it was noted that some value objects are <i>not </i>
     * in fact serializable.
     * </p>
     * 
     * @see #fireValueChanged(NakedValue)
     */
    public final void fireValueChanged() {
        fireValueChanged(null);
    }

    /**
     * Allows subclasses (indeed any class) to notify listeners when they are
     * about to change the state of the naked value. Typical usage is:
     * <ul>
     * <li>call <code>deepCopy()</code> on self to obtain current (old) value
     * </li>
     * <li>modify self</li>
     * <li>call <code>notifyListenersChanged(NakedValue)</code> passing the
     * old value from first step.</li>
     * </ul>
     * 
     * @see #deepCopy()
     */
    public final void fireValueChanged(NakedValue oldValue) {
        //		getLogger().debug("fireValueChanged(): enter");
        try {
            NakedValueChangedEvent ev = new NakedValueChangedEvent(this, oldValue, this);
            int i = 0;
            for (Enumeration enum = nakedValueListeners(); enum.hasMoreElements();) {
                NakedValueListener l = (NakedValueListener) enum.nextElement();
                l.nakedValueChanged(ev);
                i++;
            }
        } finally {
            //			getLogger().debug("fireValueChanged(): exit (" + i + " listeners
            // notified)");
        }
    }

    public String getClassName() {
        return getClass().getName();
    }

    public String getShortClassName() {
        String name = getClassName();

        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * Returns enumeration of all registered naked value listeners
     */
    public final Enumeration nakedValueListeners() {
        return nakedValueListeners.elements();
    }

    /**
     * Removes an object as a listener of this naked value. The listener will no
     * longer be notified of changes to the naked value's state.
     */
    public final void removeNakedValueListener(NakedValueListener l) {
        nakedValueListeners.removeElement(l);
    }

    public void setAbout(About newAbout) {
        if (about != null) throw new IllegalStateException("The about object cannot be reset");
        about = newAbout;
    }

    /**
     * Returns a string representation of this object.
     * <p>
     * The specification of this string representation is not fixed, but, at the
     * time of writing, consists of <i>title [shortNakedClassName] </i>
     * </p>
     * 
     * @return string representation of object.
     */
    public String toString() {
        return title() + " [" + this.getClass().getName() + "]";
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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
