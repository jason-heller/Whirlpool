package lwjgui.scene.control;

import lwjgui.geometry.Insets;
import lwjgui.paint.Color;
import lwjgui.style.Background;
import lwjgui.style.BackgroundSolid;
import lwjgui.style.BorderStyle;
import lwjgui.theme.Theme;

public class TransparentTextField extends TextInputControl {
	public TransparentTextField() {
		this("");
	}
	
	public TransparentTextField(String text) {
		super();
		
		this.setText(text);
		this.setPrefWidth(120);
		this.setBorderRadii(3);
		this.setBorderStyle(BorderStyle.NONE);
		this.setBorderWidth(0);
		this.setBorderColor(Color.WHITE);
		
		this.setBackground(new BackgroundSolid(Color.BLACK));
		this.setFontFill(Color.WHITE);
		
		this.internalScrollPane.setInternalPadding(new Insets(2,0,0,2));
	}
	
	@Override
	public double getPrefHeight() {
		return this.fontSize+8;
	}
	
	@Override
	public void setText(String text) {
		super.setText(text.replace("\n", "").replace("\t", ""));
	}

	@Override
	public String getElementType() {
		return "textfield";
	}
}
