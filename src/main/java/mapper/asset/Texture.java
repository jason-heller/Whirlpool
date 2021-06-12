package mapper.asset;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Texture implements Asset {
	
	private int id;
	private int width, height;
	private int rows, cols;
	
	private String path;

	public Texture(int id, int width, int height) {
		this.id = id;
		this.width = width;
		this.height = height;
	}

	public int getID() {
		return id;
	}

	@Override
	public void clean() {
		GL11.glDeleteTextures(id);
	}

	public void bind() {
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
	}

	public void atlas(int rows, int cols) {
		this.rows = rows;
		this.cols = cols;
	}
	
	public int getRows() {
		return rows;
	}
	
	public int getCols() {
		return cols;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public boolean isAtlased() {
		return rows != 0;
	}

	public int getAtlasWidth() {
		return width / rows;
	}
	
	public int getAtlasHeight() {
		return height / cols;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
}
