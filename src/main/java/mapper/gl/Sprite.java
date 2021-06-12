package mapper.gl;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;
import static org.lwjgl.opengl.GL11.glDrawArrays;

import org.joml.Vector4f;

import mapper.App;
import mapper.asset.AssetManager;
import mapper.asset.Texture;
import mapper.editor.Editor;

public class Sprite {
	private Texture texture;
	private float x, y;
	private float w = 1f, h = 1f;
	private float tx, ty;
	private float tw = 1f, th = 1f;
	private float rotation = 0f;
	private Vector4f color;
	
	private boolean fixed;
	
	public Sprite(String textureName, int x, int y) {
		this(AssetManager.getTexture(textureName), x, y);
	}
	
	public Sprite(Texture texture, int x, int y) {
		this.x = x;
		this.y = y;
		this.texture = texture;
		color = new Vector4f(1,1,1,1);
		
		if (texture.isAtlased()) {
			float s = 1f / texture.getRows();
			float t = 1f / texture.getCols();
			w = texture.getWidth() * s;
			h = texture.getHeight() * t;
			tw = s;
			th = t;
		} else {
			w = texture.getWidth();
			h = texture.getHeight();
		}
	}
	
	public void render(SpriteShader shader) {
		// TODO: Optimize this, batch texture swaps and stuff, ..maybe not remake the matrix every tick
		float yPos = y;
		if (fixed) {
			yPos += Editor.chartY;
		}
		shader.setPosTransform(x / (float)App.WIDTH, yPos / (float)App.HEIGHT, w / (float)App.WIDTH, h / (float)App.HEIGHT);
		shader.setTexTransform(tx, ty, tw, th);
		shader.setRotation(rotation);
		shader.setColor(color.x, color.y, color.z, color.w);
		texture.bind();
		glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
	}

	public Texture getTexture() {
		return texture;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public void setWidth(int w) {
		this.w = w;
	}
	
	public void setHeight(int h) {
		this.h = h;
	}

	public void setColor(Vector4f color) {
		this.color = color;
	}
	
	public Vector4f getColor() {
		return color;
	}

	public void setPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void fixed() {
		fixed = true;
	}
	
	public void relative() {
		fixed = false;
	}
	
	public boolean isFixed() {
		return fixed;
	}

	public void setTexture(String texName) {
		this.texture = AssetManager.getTexture(texName);
	}
	
	public boolean containsPoint(double px, double py) {
		return (px >= x - w && py >= y - h && px <= x + w && py <= y + h);
	}

	public void setAtlas(int row, int col) {
		tx = row;
		ty = col;
	}
}
