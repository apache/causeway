/*
    Naked Objects - a framework that exposes behaviourally complete
    business objects directly to the user.
    Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.object.value;
import java.util.EventObject;

import org.nakedobjects.object.NakedValue;

public final class NakedValueChangedEvent extends EventObject {

	public NakedValueChangedEvent(final Object source, final NakedValue oldValue, final NakedValue newValue) {
        super(source);
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

	/**
     * returns (copy of) value before change
     */
    public NakedValue getOldValue(){
		return oldValue;
	}

	/**
     * returns new value; will be same as the object returned by
     * <code>getSource()</code>
     * 
     * @see EventObject#getSource()
     */
    public NakedValue getNewValue(){
		return newValue;
	}

	/**
	 * String representation of this event.
	 */
    public String toString() {
		StringBuffer buf = new StringBuffer();
		if (getNewValue() != null) {
			buf.append("newVal=").append(getNewValue());
			if (getOldValue() != null) {
				buf.append( ", ");
			}
		}
		if (getOldValue() != null) {
			buf.append("oldVal=").append(getOldValue());
		}
		return buf.toString();
	}

    private final NakedValue oldValue;
    private final NakedValue newValue;
}
