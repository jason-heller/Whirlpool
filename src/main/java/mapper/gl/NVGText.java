package mapper.gl;

import org.lwjgl.nanovg.NanoVG;

import lwjgui.font.Font;
import lwjgui.font.FontStyle;
import lwjgui.paint.Color;
import lwjgui.scene.Context;

public class NVGText {
	public long x, y;
	public String str;
	
	private float fontSize = 18;
	private Font font = Font.SANS;
	private FontStyle fontStyle = FontStyle.REGULAR;
	private Color textColor = Color.WHITE;
	
	private int hAlign = NanoVG.NVG_ALIGN_LEFT;
	private int vAlign = NanoVG.NVG_ALIGN_TOP;
	
	private float[] bounds = new float[4];
	
	public NVGText(String str, long x, long y) {
		this.x = x;
		this.y = y;
		this.str = str;
	}
	
	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}
	
	public float getFontSize() {
		return fontSize;
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public Font getFont() {
		return font;
	}
	
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}
	
	public Color getTextColor() {
		return textColor;
	}

	public void render(Context context) {
		if ( context == null )
			return;
		long vg = context.getNVG();
		
		String fontFace = font.getFont(fontStyle);

		// Setup font
		NanoVG.nvgFontSize(vg, fontSize);
		NanoVG.nvgFontFace(vg, fontFace);
		NanoVG.nvgTextAlign(vg, hAlign | vAlign);

		// Draw
		NanoVG.nvgBeginPath(vg);
		NanoVG.nvgFontBlur(vg,0);
		NanoVG.nvgFillColor(vg, textColor.getNVG());
		NanoVG.nvgText(vg, x, y, str);
	}
	
	public void calculateBounds(Context context) {
		if ( context == null )
			return;
		long vg = context.getNVG();
		
		String fontFace = font.getFont(fontStyle);
		
		NanoVG.nvgFontSize(vg, fontSize);
		NanoVG.nvgFontFace(vg, fontFace);
		NanoVG.nvgTextAlign(vg, hAlign | vAlign);
		NanoVG.nvgTextBounds(vg, 0, 0, str, bounds);
	}

	public void setAlignmentH(int hAlign) {
		this.hAlign = hAlign;
	}
	
	public float getWidth() {
		return bounds[2] - bounds[0];
	}

	public float getHeight() {
		return bounds[3] - bounds[1];
	}
	
	public float[] getBounds() {
		return bounds;
	}

	public boolean containsPoint(double px, double py) {
		double xOff = (hAlign == NanoVG.NVG_ALIGN_LEFT) ? 0 : getWidth();
		double yOff = (vAlign == NanoVG.NVG_ALIGN_TOP) ? 0 : getHeight();
		
		double dx = x - xOff;
		double dy = y - yOff;
		return px > dx && py > dy && px < dx + getWidth() && py < dy + getHeight();
	}
}
