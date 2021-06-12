package mapper.gui.marginlabels;

import lwjgui.event.Event;
import lwjgui.event.EventHandler;
import lwjgui.event.EventHelper;

public class MarginLabel {

	public static final int LEFT_ALIGNED = -1, RIGHT_ALIGNED = 1;
	
	private String label;
	private Object value;
	private double position;
	
	private int alignment = RIGHT_ALIGNED;
	
	private boolean deletable = true;
	private boolean editable = false;
	private boolean dragging = false;
	
	private boolean editing = false;
	
	private double xOffset, yOffset;
	
	protected EventHandler<LabelMoveEvent> labelMovedEvent;
	protected EventHandler<Event> labelDeletedEvent;
	protected EventHandler<LabelEditEvent> labelEditedEvent;
	
	protected MarginLabel prev, next;
	
	public MarginLabel(String label, Object value, double position) {
		this.label = label;
		this.value = value;
		this.position = position;
	}
	
	public String getText() {
		return label + ((value == null) ? "" : ": " + value.toString());
	}
	
	public double getPosition() {
		return position;
	}
	
	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public int getAlignment() {
		return alignment;
	}

	public void setOnMoveEvent(EventHandler<LabelMoveEvent> labelMovedEvent) {
		this.labelMovedEvent = labelMovedEvent;
	}
	
	public void setOnDeleteEvent(EventHandler<Event> labelDeletedEvent) {
		this.labelDeletedEvent = labelDeletedEvent;
	}
	
	public void setOnEditEvent(EventHandler<LabelEditEvent> labelEditedEvent) {
		this.labelEditedEvent = labelEditedEvent;
	}

	public void move(double newPosition) {
		LabelMoveEvent event = new LabelMoveEvent(position, newPosition);
		EventHelper.fireEvent(labelMovedEvent, event);

		position = newPosition;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setDeletable(boolean deletable) {
		this.deletable = deletable;
	}
	
	public boolean isDeletable() {
		return deletable;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void delete() {
		EventHelper.fireEvent(labelDeletedEvent, new Event());
	}

	public void setOffset(double xOffset, double yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}
	
	public double getXOffset() {
		return xOffset;
	}
	
	public double getYOffset() {
		return yOffset;
	}
	
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}
	
	public boolean isDragging() {
		return dragging;
	}
	
	public String getLabel() {
		return label;
	}

	public void edit(String editInput) {
		EventHelper.fireEvent(labelEditedEvent, new LabelEditEvent(editInput));
	}
}
