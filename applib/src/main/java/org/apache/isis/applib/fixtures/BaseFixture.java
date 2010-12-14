package org.apache.isis.applib.fixtures;

import org.apache.isis.applib.AbstractContainedObject;
import org.apache.isis.applib.clock.Clock;

abstract class BaseFixture extends AbstractContainedObject implements InstallableFixture  {

    private final FixtureType fixtureType;
    FixtureClock clock = null;

    public BaseFixture(FixtureType fixtureType) {
        this.fixtureType = fixtureType;
        try {
            clock = FixtureClock.initialize();
        } catch(IllegalStateException ex) {
            clock = null;
            System.err.println(ex.getMessage());
            System.err.println("calls to change date or time will be ignored");
        }
    }

    ///////////////////////////////////////////////////
    // FixtureType
    ///////////////////////////////////////////////////
    
    /**
     * As specified in constructor.
     */
    @Override
    public final FixtureType getType() {
        return fixtureType;
    }


    ///////////////////////////////////////////////////
    // FixtureClock
    ///////////////////////////////////////////////////

    /**
     * Will print warning message and do nothing if {@link FixtureClock} could not be {@link FixtureClock#initialize() initialized}.
     */
    public void setDate(final int year, final int month, final int day) {
        if (shouldIgnoreCallBecauseNoClockSetup("setDate()")) return;
        clock.setDate(year, month, day);
    }

    /**
     * Will print warning message and do nothing if {@link FixtureClock} could not be {@link FixtureClock#initialize() initialized}.
     */
    public void setTime(final int hour, final int minute) {
        if (shouldIgnoreCallBecauseNoClockSetup("setTime()")) return;
        clock.setTime(hour, minute);
    }


    /**
     * The {@link Clock} singleton, downcast to {@link FixtureClock}.
     * 
     * <p>
     * Will return <tt>null</tt> if {@link FixtureClock} could not be {@link FixtureClock#initialize() initialized}.
     */
    public FixtureClock getFixtureClock() {
        return clock;
    }

    /**
     * Will print warning message and do nothing if {@link FixtureClock} could not be {@link FixtureClock#initialize() initialized}.
     */
    public void resetClock() {
    	if (shouldIgnoreCallBecauseNoClockSetup("resetClock()")) return;
        clock.reset();
    }

    boolean shouldIgnoreCallBecauseNoClockSetup(String methodName) {
    	if (clock == null) {
    		System.err.println("clock not set, call to " + methodName + " ignored");
    		return true;
    	}
    	return false;
    }
    // }}

}