package mapper.asset;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;

import fr.delthas.javamp3.Sound;
import mapper.al.AudioBuffer;
import mapper.al.AudioUtils;

public class AudioLoader {
	public static AudioBuffer loadOgg(String filename) {
		try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
			ShortBuffer pcm = AudioUtils.readOGG(filename, 32 * 1024, info);

			return new AudioBuffer(info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm,
					info.sample_rate());
		}
	}

	public static AudioBuffer loadMp3(String filename) throws Exception {
		Path path = new File(filename).toPath();
		try (Sound sound = new Sound(new BufferedInputStream(Files.newInputStream(path)))) {

			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int read = sound.decodeFullyInto(os);

			ByteBuffer data = BufferUtils.createByteBuffer(read);
			data.put(os.toByteArray()).flip();
			
			return new AudioBuffer(!sound.isStereo() ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, data,
					sound.getSamplingFrequency());
		}
	}

	public static AudioBuffer loadWav(String filename) throws Exception {
		File file = new File(filename);

		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
			AudioFormat format = audioInputStream.getFormat();
			
			int bytesPerFrame = format.getFrameSize();
			
			// Yeah
			if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
				bytesPerFrame = 1;
			}

			int numBytes = (int) (audioInputStream.getFrameLength() * bytesPerFrame);
			byte[] arrData = new byte[numBytes];
			try {
				int totalBytesRead = 0;

				while (totalBytesRead != -1) {
					totalBytesRead = audioInputStream.read(arrData);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			ByteBuffer data = BufferUtils.createByteBuffer(arrData.length);
			data.put(arrData);
			data.flip();
			
			int numChannels = format.getChannels();
			int sampleRate = (int) format.getSampleRate();
			
			return new AudioBuffer((numChannels == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, data, sampleRate);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
