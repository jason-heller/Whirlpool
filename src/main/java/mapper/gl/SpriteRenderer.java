package mapper.gl;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryStack.stackMallocFloat;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.net.MalformedURLException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import lwjgui.gl.Renderer;
import lwjgui.scene.Context;
import mapper.App;
import mapper.editor.Editor;
import mapper.gui.Colors;

public class SpriteRenderer implements Renderer {
	private SpriteShader shader;
	
	private Editor editor;
	
	private int vao;
	private int vbo;
	
	private List<Sprite> sprites;
	private static List<Sprite> tempSprList;
	
	public SpriteRenderer(Editor editor, Context context) {
		shader = null;
		try {
			shader = new SpriteShader();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		this.editor = editor;
		
		sprites = new ArrayList<>();
		tempSprList = new ArrayList<>();
		
		// Setup geometry
		int vertSize = 2; // vec3 in shader
		int texSize = 2; // vec2 in shader
		int colorSize = 4; // vec4 in shader
		int size = vertSize + texSize + colorSize; // Stride length
		int verts = 4; // Number of vertices
		int bytes = Float.BYTES; // Bytes per element (float)
		
		stackPush();
		{
			// Initial vertex data
			FloatBuffer buffer = stackMallocFloat(verts * size);
			
			buffer.put(0.5f).put(0.5f);		// Vert 4 position
			buffer.put(new float[] {1.0f, 1.0f});		// Vert 4 texture
			buffer.put(new float[] {1.0f,1.0f,1.0f,1.0f}); // Vert 4 color
			
			buffer.put(-0.5f).put(0.5f);		// Vert 3 position
			buffer.put(new float[] {0.0f, 1.0f});		// Vert 3 texture
			buffer.put(new float[] {1.0f,1.0f,1.0f,1.0f}); // Vert 3 color
			
			buffer.put(0.5f).put(-0.5f);		// Vert 2 position
			buffer.put(new float[] {1.0f, 0.0f});		// Vert 2 texture
			buffer.put(new float[] {1.0f,1.0f,1.0f,1.0f}); // Vert 2 color
			
			buffer.put(-0.5f).put(-0.5f);		// Vert 1 position
			buffer.put(new float[] {0.0f, 0.0f});		// Vert 1 texture
			buffer.put(new float[] {1.0f,1.0f,1.0f,1.0f}); // Vert 1 color
			buffer.flip();

			// Generate buffers
			vbo = glGenBuffers();
			vao = glGenVertexArrays();

			// Upload Vertex Buffer
			glBindBuffer(GL_ARRAY_BUFFER, vbo);
			glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

			// Set attributes (automatically stored to currently bound VAO)
			glBindVertexArray(vao);
			glEnableVertexAttribArray(0); // layout 0 shader
			glEnableVertexAttribArray(1); // layout 1 shader
			glEnableVertexAttribArray(2); // layout 2 shader
			int vertOffset = 0;
			glVertexAttribPointer( 0, vertSize,  GL_FLOAT, false, size*bytes, vertOffset );
			int texOffset = vertSize*bytes;
			glVertexAttribPointer( 1, texSize,   GL_FLOAT, false, size*bytes, texOffset );
			int colorOffset = texOffset + texSize*bytes;
			glVertexAttribPointer( 2, colorSize, GL_FLOAT, false, size*bytes, colorOffset );

			// Unbind
			glBindBuffer(GL_ARRAY_BUFFER, 0);
			glBindVertexArray(0);
		}
		stackPop();
	}
	
	public void addSprite(Sprite s) {
		sprites.add(s);
	}

	@Override
	public void render(Context context) {
		editor.update();
		
		GL11.glClearColor(Colors.DK_GREY.x, Colors.DK_GREY.y, Colors.DK_GREY.z, 1f);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		shader.bind();
		shader.projectOrtho( 0.0f, (float) (Editor.chartY / App.HEIGHT), 1.0f, 1.0f );
		
		glBindVertexArray(vao);
		
		for(Sprite sprite : sprites) {
			sprite.render(shader);
		}
		
		// Temporary sprites
		for(Sprite sprite : tempSprList) {
			sprite.render(shader);
		}
		
		tempSprList.clear();
	}

	public Sprite addTempSprite(String tex, int x, int y, int width, int height, float rotation, Vector4f color) {
		Sprite sprite = addTempSprite(tex, x, y);
		sprite.setWidth(width);
		sprite.setHeight(height);
		sprite.setColor(color);
		sprite.setRotation(rotation);
		return sprite;
	}
	
	public Sprite addTempSprite(String tex, int x, int y) {
		Sprite sprite = new Sprite(tex, x, y);
		tempSprList.add(sprite);
		
		return sprite;
	}
}
