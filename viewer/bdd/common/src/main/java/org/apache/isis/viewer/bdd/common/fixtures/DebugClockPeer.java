package org.apache.isis.viewer.bdd.common.fixtures;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.StoryCell;

public class DebugClockPeer extends AbstractFixturePeer {

	private final CellBinding cellBinding;
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MMM-yyyy HH:mm");

	public DebugClockPeer(AliasRegistry aliasesRegistry,
			final CellBinding cellBinding) {
		super(aliasesRegistry, cellBinding);
		this.cellBinding = cellBinding;
	}

	public StoryCell getCurrentCell() {
		return cellBinding.getCurrentCell();
	}

	public String getFormattedClockTime() {
		final Calendar cal = Clock.getTimeAsCalendar();
		final String formattedDate = DATE_FORMAT
                .format(cal.getTime());
		return formattedDate;
	}

}
