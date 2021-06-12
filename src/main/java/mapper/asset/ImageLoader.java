package mapper.asset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.utils.PNGDecoder;

public class ImageLoader {
	public static ImageData load(String filename, String extension) throws Exception {
		switch(extension) {
		case "PNG":
			return loadPng(filename);
		case "JPG":
		case "JPEG":
		case "BMP":
        case "WEBMP":
        case "GIF":
			return loadViaBufferedImage(filename);
		}
		
		throw new Exception("Failed to load image: " + filename);
	}

	private static ImageData loadViaBufferedImage(String filename) throws Exception {
		BufferedImage image = null;
		image = ImageIO.read(new File(filename));
	    
		int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());

        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB
        
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS
	    return new ImageData(buffer, image.getWidth(), image.getHeight(), GL11.GL_RGBA);
	}

	static ImageData loadPng(String filename) throws FileNotFoundException, IOException {
		PNGDecoder decoder = new PNGDecoder(new FileInputStream(filename));
		ByteBuffer buffer = ByteBuffer.allocateDirect(4 * decoder.getWidth() * decoder.getHeight());

		decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.Format.RGBA);

		buffer.flip();
		return new ImageData(buffer, decoder.getWidth(), decoder.getHeight(), GL11.GL_RGBA);
	}
}
