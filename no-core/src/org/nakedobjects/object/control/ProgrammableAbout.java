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

package org.nakedobjects.object.control;

/**
 * @deprecated
 */
public class ProgrammableAbout implements About {
	   private final static long serialVersionUID = 1L;
    private String name;
    private String description;
    private boolean isAccessible;
    private boolean isAvailable;
    private StringBuffer unavailableReason;

    public ProgrammableAbout() {
        isAccessible = true;
        isAvailable = true;
    }

    private void appendReason(String reason) {
        if (unavailableReason == null) {
            unavailableReason = new StringBuffer();
        } else {
            unavailableReason.append("; ");
        }

        unavailableReason.append(reason);
    }

    public Permission canAccess() {
        if (isAccessible) {
            return Allow.DEFAULT;
        } else {
            return new Veto(unavailableReason.toString());
        }
    }

    public Permission canUse() {
        if (isAvailable) {
            return Allow.DEFAULT;
        } else {
            return new Veto((unavailableReason == null)                
                            ? "" : unavailableReason.toString());
        }
    }

    public void changeName(String name) {
        this.name = name;
    }

    public ProgrammableAbout changeNameIfAvailable(String name) {
        if (canUse().isAllowed()) {
            this.name = name;
        }

        return this;
    }

    public ProgrammableAbout changeNameIfUnavailable(String name) {
        if (canUse().isVetoed()) {
            this.name = name;
        }

        return this;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
/*
    public ProgrammableAbout makeAccessibleTo(Role role) {
        return this;
    }
*/
    public ProgrammableAbout makeAvailableOnCondition(boolean conditionMet) {
        isAvailable = isAvailable && conditionMet;

        return this;
    }

    public ProgrammableAbout makeAvailableOnCondition(boolean conditionMet, 
                                                      String reasonNotMet) {
        isAvailable = isAvailable && conditionMet;

        if (!conditionMet) {
            appendReason(reasonNotMet);
        }

        return this;
    }

/*
    public ProgrammableAbout makeAvailableTo(Role role) {
        return this;
    }

    public ProgrammableAbout makeInaccessibleTo(Role role) {
        return this;
    }
*/
    public ProgrammableAbout makeUnavailable(String reason) {
        isAvailable = false;
        appendReason(reason);

        return this;
    }
/*
    public ProgrammableAbout makeUnavailableTo(Role role) {
        return this;
    }
*/
    public void setDescription(String description) {
        this.description = description;
    }

	public String debug() {
		return "no details";
	}
}
