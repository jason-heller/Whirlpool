package mapper.asset;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glPixelStorei;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameterf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AssetManager {
	public static final String WORKING_DIR = System.getProperty("user.dir");
	public static final String ASSET_DIR = WORKING_DIR + "\\assets";
	
	private static Map<String, Asset> assets = new HashMap<>();
	
	private static void loadImages() throws Exception {
		addTexture("no_texture", ASSET_DIR + "\\no_texture.png");
		addTexture("no_background", ASSET_DIR + "\\no_background.png");
		addTexture("no_banner", ASSET_DIR + "\\no_banner.png");
		addTexture("no_cdtitle", ASSET_DIR + "\\no_cdtitle.png");
		
		addTexture("music", ASSET_DIR + "\\music.png");
		
		addTexture("notes", ASSET_DIR + "\\notes.png").atlas(1, 8);
		addTexture("receptors", ASSET_DIR + "\\receptors.png").atlas(4, 1);
		addTexture("hold_body", ASSET_DIR + "\\hold_body.png");
		addTexture("hold_cap", ASSET_DIR + "\\hold_cap.png");
		addTexture("roll_body", ASSET_DIR + "\\roll_body.png");
		addTexture("roll_cap", ASSET_DIR + "\\roll_cap.png");
		
		/*File f = new File(ASSET_DIR);
		String[] assetPathList = f.list();

        for (String pathname : assetPathList) {
        	final String pathLower = pathname.toLowerCase();
        	
            if (pathLower.endsWith(".png")) {
            	String name = pathLower.substring(0, pathname.length() - 4);
        	    
            	addTexture(name, ASSET_DIR + '\\' + pathname);
            }
        }*/
	}

	public static void init() {
		System.out.println("loading assets");
		try {
			loadImages();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void clean() {
		System.out.println("Cleaning assets");
		for(Asset asset : assets.values()) {
			asset.clean();
		}
	}
	
	public static Texture addTexture(String name, String path) throws Exception {
		return addTexture(name, path, "PNG");
	}
	
	public static Texture addTexture(String name, String path, String extension) throws Exception {
		ImageData imageData = ImageLoader.load(path, extension);

	    int id = glGenTextures();

	    glBindTexture(GL_TEXTURE_2D, id);
	    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

	    glTexImage2D(GL_TEXTURE_2D, 0, imageData.getFormat(), imageData.getWidth(), imageData.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, imageData.getBuffer());

	    Texture texture = new Texture(id, imageData.getWidth(), imageData.getHeight());
	    texture.setPath(path);
	    
	    if (assets.containsKey(name)) {
	    	assets.get(name).clean();
	    }
	    
	    assets.put(name, texture);
	    return texture;
	}

	public static Texture getTexture(String textureName) {
		return (Texture) assets.get(textureName);
	}
}
