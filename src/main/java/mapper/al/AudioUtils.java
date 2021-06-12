package mapper.al;

import static org.lwjgl.stb.STBVorbis.stb_vorbis_close;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_info;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_get_samples_short_interleaved;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_open_memory;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_stream_length_in_samples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;

import mapper.util.IOUtil;

public class AudioUtils {
	public static ShortBuffer readOGG(String filename, int bufferSize, STBVorbisInfo info) {
		ByteBuffer data;
		try {
			data = IOUtil.ioResourceToByteBuffer(filename, bufferSize);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		IntBuffer errCode = BufferUtils.createIntBuffer(1);
		long decoder = stb_vorbis_open_memory(data, errCode, null);
		
		if (decoder == 0) {
			throw new RuntimeException("Could not open OGG file \\'" + filename + "\\' (error " + errCode.get(0) + ")");
		}

		stb_vorbis_get_info(decoder, info);

		int channels = info.channels();
		ShortBuffer pcm = BufferUtils.createShortBuffer(stb_vorbis_stream_length_in_samples(decoder) * channels);

		stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
		stb_vorbis_close(decoder);

		return pcm;
	}
}
