package lwjgui.scene.control;

import java.awt.Point;

import org.lwjgl.glfw.GLFW;

import lwjgui.collections.ObservableList;
import lwjgui.event.ActionEvent;
import lwjgui.event.EventHandler;
import lwjgui.event.EventHelper;
import lwjgui.event.MouseEvent;
import lwjgui.geometry.Insets;
import lwjgui.paint.Color;
import lwjgui.scene.Context;
import lwjgui.scene.Node;
import lwjgui.style.Background;
import lwjgui.style.BackgroundLinearGradient;
import lwjgui.style.BackgroundSolid;
import lwjgui.style.BlockPaneRenderer;
import lwjgui.style.BorderStyle;
import lwjgui.style.BoxShadow;
import lwjgui.style.ColorStop;
import lwjgui.theme.Theme;

public abstract class ButtonBase extends Labeled implements BlockPaneRenderer {
	protected EventHandler<ActionEvent> buttonEvent;
	protected EventHandler<ActionEvent> buttonInternalEvent;
	
	protected double textOffset;
	
	private Background background;
	private Color borderColor;
	private float[] borderRadii;
	private BorderStyle borderStyle;
	private ObservableList<BoxShadow> boxShadows = new ObservableList<>();
	
	public ButtonBase(String name) {
		super();
		this.setText(name);
		
		this.setMinSize(12, 24);
		this.setPadding(new Insets(4,6,4,6));
		
		this.setBorderRadii(2.5f);
		this.setBorderStyle(BorderStyle.SOLID);
		this.setBorderWidth(1);
		this.setBorderColor(Theme.current().getControlOutline());
		
		this.setText(name);
		this.setFontSize(16);
		
		// Fire the click event when we're clicked
		this.setOnMouseReleasedInternal( new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if ( isDisabled() )
					return;
				
				if ( event.button == 0 ) {
					EventHelper.fireEvent(buttonInternalEvent, new ActionEvent());
					EventHelper.fireEvent(buttonEvent, new ActionEvent());
				}
			}
		});
		
		// If the button is selected, and enter is pressed. Fire the click event
		this.setOnKeyPressedInternal( (event) -> {
			if ( event.getKey() == GLFW.GLFW_KEY_ENTER ) {
				if ( this.cached_context.isSelected(this) ) {
					EventHelper.fireEvent(buttonInternalEvent, new ActionEvent());
					EventHelper.fireEvent(buttonEvent, new ActionEvent());
				}
			}
		});
	}
	
	/**
	 * Set the background color of this node.
	 * <br>
	 * If set to null, then no background will draw.
	 * @param color
	 */
	public void setBackgroundLegacy(Color color) {
		setBackground( new BackgroundSolid(color) );
	}
	
	/**
	 * Set the background color of this node.
	 * <br>
	 * If set to null, then no background will draw.
	 * @param color
	 */	
	public void setBackground(Background color) {
		this.background = color;
	}
	
	/**
	 * Get the current background color of this node.
	 * @return
	 */
	public Background getBackground() {
		return this.background;
	}
	
	@Override
	public void setBorderStyle(BorderStyle style) {
		this.borderStyle = style;
	}

	@Override
	public BorderStyle getBorderStyle() {
		return this.borderStyle;
	}

	@Override
	public float[] getBorderRadii() {
		return borderRadii;
	}

	@Override
	public void setBorderRadii(float radius) {
		this.setBorderRadii(radius, radius, radius, radius);
	}

	@Override
	public void setBorderRadii(float[] radius) {
		this.setBorderRadii(radius[0], radius[1], radius[2], radius[3]);
	}

	@Override
	public void setBorderRadii(float cornerTopLeft, float cornerTopRight, float cornerBottomRight, float cornerBottomLeft) {
		this.borderRadii = new float[] {cornerTopLeft, cornerTopRight, cornerBottomRight, cornerBottomLeft};
	}

	@Override
	public void setBorderColor(Color color) {
		this.borderColor = color;
	}

	@Override
	public Color getBorderColor() {
		return this.borderColor;
	}

	@Override
	public ObservableList<BoxShadow> getBoxShadowList() {
		return this.boxShadows;
	}
	
	@Override
	public boolean isResizeable() {
		return false;
	}
	
	protected boolean isPressed() {
		if ( cached_context == null )
			return false;
		
		if ( isDisabled() )
			return false;
		
		return cached_context.isHovered(this) && GLFW.glfwGetMouseButton(cached_context.getWindowHandle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
	}
	
	@Override
	protected void position(Node parent) {
		defaultStyle();
		super.position(parent);
	}
	
	private void defaultStyle() {
		Context context = this.cached_context;
		if ( context == null )
			return;
		
		// Text color?
		if ( isDisabled() ) {
			this.setTextFill(Theme.current().getShadow());
		} else {
			this.setTextFill(Theme.current().getText());
		}
		
		
		// SETUP BUTTON COLOR
		Color buttonColor = isPressed()?Theme.current().getControlOutline():((context.isHovered(this)&&!isDisabled())?Theme.current().getControlHover():Theme.current().getControl());
		this.setBackground(new BackgroundLinearGradient(90, new ColorStop(buttonColor, 0), new ColorStop(Theme.current().getControlOutline(), 3)));
		
		// SETUP BUTTON OUTLINE
		Color outlineColor = (context.isSelected(this)&&context.isFocused()&&!isDisabled())?Theme.current().getSelection():Theme.current().getControlOutline();
		this.setBorderColor(outlineColor);
		
		// Weird inset outline???
		this.getBoxShadowList().clear();
		if ( !this.isDisabled() ) {
			Color c2 = Theme.current().getControlAlt();
			if ( context.isHovered(this) )
				c2 = Theme.current().getControlHover().darker();
			if ( isPressed() )
				c2 = buttonColor.darker();
			this.getBoxShadowList().add(new BoxShadow(0, 0, 2, 1, c2, true));
		}
		
		// SETUP SELECTION GRAPHIC
		if ( context.isSelected(this) && context.isFocused() ) {
			Color sel = Theme.current().getSelection();
			if ( isDisabled() )
				sel = Theme.current().getSelectionPassive();

			this.getBoxShadowList().add(new BoxShadow(0, 0, 4, 0, sel.alpha(0.8f)));
			this.getBoxShadowList().add(new BoxShadow(0, 0, 1.5f, 2, sel.alpha(0.2f), true));
		}
	}
	
	@Override
	public void render(Context context) {
		if ( context == null )
			return;
		
		if ( !isVisible() )
			return;
		
		clip(context, 0);
		
		// Apply CSS
		this.stylePush();
		{
			// Render standard pane
			BlockPaneRenderer.render(context, this);
			
			// Draw children
			this.offset(textOffset, 0);
			super.render(context);
			this.offset(-textOffset, 0);
		}
		this.stylePop();
	}
	
	protected Point getDrawSize() {
		return new Point((int)getWidth(), (int)getHeight());
	}

	public void setOnAction(EventHandler<ActionEvent> event) {
		this.buttonEvent = event;
	}
	
	public EventHandler<ActionEvent> getOnAction() {
		return this.buttonEvent;
	}

	protected void setOnActionInternal(EventHandler<ActionEvent> event) {
		this.buttonInternalEvent = event;
	}

}
