package mapper.al;

import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.openal.EXTOffset;

import mapper.editor.Editor;

public class AudioBuffer {
	private int source;
	private final int bufferId;

	private boolean playing = false;

	public AudioBuffer(int format, ShortBuffer pcm, int sampleRate) {
		bufferId = alGenBuffers();
		source = alGenSources();

		alBufferData(bufferId, format, pcm, sampleRate);
		alSourcei(source, AL_BUFFER, bufferId);
		
		System.out.println(format + " ; " + pcm.capacity() + " ; " + sampleRate);
		
	}

	public AudioBuffer(int format, ByteBuffer pcm, int sampleRate) {
		bufferId = alGenBuffers();
		source = alGenSources();

		alBufferData(bufferId, format, pcm, sampleRate);
		alSourcei(source, AL_BUFFER, bufferId);

		System.out.println(format + " ; " + pcm.capacity() + " ; " + sampleRate);
		
	}

	public int getBufferId() {
		return bufferId;
	}

	public void clean() {
		alDeleteBuffers(bufferId);
	}

	public void toggle() {
		if (playing) {
			alSourceStop(source);
		} else {
			reposition();
			alSourcePlay(source);
		}

		playing = !playing;
	}

	public void reposition() {
		alSourcef(source, EXTOffset.AL_SEC_OFFSET, Editor.chartTime / 1000.0f);
	}
}
