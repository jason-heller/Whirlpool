package mapper.asset;

import java.nio.ByteBuffer;

public class ImageData {
	private ByteBuffer buffer;
	private int width, height;
	private int format;
	
	public ImageData(ByteBuffer buffer, int width, int height, int format) {
		this.buffer = buffer;
		this.width = width;
		this.height = height;
		this.format = format;
	}
	
	public int getFormat() {
		return format;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}
}
