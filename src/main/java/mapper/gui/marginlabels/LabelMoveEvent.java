package mapper.gui.marginlabels;

import lwjgui.event.Event;

public class LabelMoveEvent extends Event {
	public final double oldPos, newPos;
	
	public LabelMoveEvent(double oldPos, double newPos) {
		this.oldPos = oldPos;
		this.newPos = newPos;
	}

	public double getOldPos() {
		return oldPos;
	}
	
	public double getNewPos() {
		return newPos;
	}
}
