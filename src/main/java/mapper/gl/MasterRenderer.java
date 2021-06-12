package mapper.gl;

import java.util.LinkedList;

import org.joml.Vector4f;

import lwjgui.gl.Renderer;
import lwjgui.scene.Context;
import mapper.editor.Editor;

public class MasterRenderer implements Renderer {

	private SpriteRenderer sprRenderer;
	private Context lastContext = null;
	
	private LinkedList<NVGText> nvgTexts;
	
	public MasterRenderer(Editor editor, Context context) {
		sprRenderer = new SpriteRenderer(editor, context);
		nvgTexts = new LinkedList<>();
	}

	@Override
	public void render(Context context) {
		lastContext = context;
		sprRenderer.render(context);
		
		for(NVGText text : nvgTexts) {
			text.render(context);
		}
		
		nvgTexts.clear();
	}

	public void addSprite(Sprite spr) {
		sprRenderer.addSprite(spr);
	}

	// Rects are techinically untextures temp-sprites, scaled and colored. Works well enough
	public Sprite drawRect(int x, int y, int width, int height, Vector4f color) {
		return drawSprite("no_texture", x, y, width, height, color);
	}
	
	public Sprite drawSprite(String tex, int x, int y, int width, int height, float rotation, Vector4f color) {
		return sprRenderer.addTempSprite(tex, x, y, width, height, rotation, color);
	}
	
	public Sprite drawSprite(String tex, int x, int y, int width, int height, Vector4f color) {
		return sprRenderer.addTempSprite(tex, x, y, width, height, 0f, color);
	}

	public NVGText drawString(String str, long x, long y) {
		NVGText text = new NVGText(str, x, y);
		nvgTexts.add(text);
		return text;
	}

	public Sprite drawSprite(String tex, int x, int y) {
		return sprRenderer.addTempSprite(tex, x, y);
	}

	public Context getLastContext() {
		return lastContext;
	}
}
