package mapper.al;

import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;

import mapper.asset.AudioLoader;

public class AudioManager {
	private static long context;
	private static long device;
	
	private static AudioBuffer music = null;
	
	public static void init() {
		create();
	}
	
	public static void clean() {
		if (music != null) {
			music.clean();
		}
		destroy();
	}
	
	public static void setMainTrack(String filename, String extension) {
		if (music != null) {
			music.clean();
		}
		
		try {
			switch(extension) {
			case "OGG":
				music = AudioLoader.loadOgg(filename);
				break;
			case "MP3":
				music = AudioLoader.loadMp3(filename);
				break;
			case "WAV":
				music = AudioLoader.loadWav(filename);
				break;
			default:
				System.err.println("File extension not yet supported.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void toggleMainTrack() {
		if (music != null) {
			music.toggle();
		}
	}
	
	private static void create() {
		device = alcOpenDevice((ByteBuffer)null);
		ALCCapabilities deviceCaps = ALC.createCapabilities(device);

		context = alcCreateContext(device, (IntBuffer)null);
		alcMakeContextCurrent(context);
		AL.createCapabilities(deviceCaps);
		
		AL.setCurrentThread(null);
	}
	
	private static void destroy() {
		alcDestroyContext(context);
        alcCloseDevice(device);
	}
	
	public static void repositionMainTrack() {
		if (music == null)
			return;
		music.reposition();
	}

	public static AudioBuffer getMainTrack() {
		return music;
	}
}
