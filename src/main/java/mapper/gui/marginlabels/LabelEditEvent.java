package mapper.gui.marginlabels;

import lwjgui.event.Event;

public class LabelEditEvent extends Event {
	public final String edit;
	
	public LabelEditEvent(String edit) {
		this.edit = edit;
	}
}
