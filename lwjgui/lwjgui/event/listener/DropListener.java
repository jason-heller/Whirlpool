package lwjgui.event.listener;

/**
 * This listener is invoked when the window is focused/unfocused.
 */

public abstract class DropListener implements EventListener {
	public abstract void invoke(long window, int count, long names);
	
	public EventListenerType getEventListenerType() {
		return EventListenerType.DROP_LISTENER;
	}
}
