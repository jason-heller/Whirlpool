package mapper.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import mapper.asset.AssetManager;
import mapper.asset.Texture;
import mapper.editor.EditorData;
import mapper.editor.Measure;

public class SMExporter {
	private static BufferedWriter bwPtr;
	
	private static void writeln(String... strs) throws IOException {
		for(String s : strs) {
			bwPtr.write(s);
			bwPtr.newLine();
		}
	}
	
	public static void saveSM(String filename, EditorData data, boolean copyRelevantFiles) throws IOException {
		FileWriter f = new FileWriter(filename);
		BufferedWriter out = new BufferedWriter(f);
		bwPtr = out;
		
		writeln("#TITLE:" + data.getName() + ";");
		writeln("#SUBTITLE:" + data.getDescription() + ";");
		writeln("#ARTIST:" + data.getArtist() + ";");
		
		// Stuff I haven't implemented/wont use
		writeln("#TITLETRANSLIT:;  ", 
				"#SUBTITLETRANSLIT:;", 
				"#ARTISTTRANSLIT:;", 
				"#GENRE:;", 
				"#CREDIT:;");
		
		String songFile = fileNameOnly(data.getSongFile());
		writeln("#MUSIC:" + songFile + ";");
		writeln("#BANNER:" + getImage("banner") + ";");
		writeln("#BACKGROUND:" + getImage("background") + ";");
		writeln("#CDTITLE:" + getImage("cd_title") + ";");
		
		writeln("#SAMPLESTART:" + data.getSampleStart() + ";");
		writeln("#SAMPLELENGTH:" + (data.getSampleEnd() - data.getSampleStart()) + ";");
		writeln("#OFFSET:" + "0.000" + ";");
		
		writeln("#BPMS:" + writeBpms(data) + ";");
		
		writeln("#STOPS:;" + "", 
				"#BGCHANGES:;",
				"#FGCHANGES:;");

		writeln("#NOTES:", 
				"     dance-single:", 
				"     :", 
				"     Edit:",
				"     1:", 
				"     0,0,0,0,0:");
		
		final Collection<Measure> measures = data.getMeasures();

		Iterator<Measure> iter = measures.iterator();
		while(true) {
			Measure m = iter.next();
			List<String> lines = m.toOneInterval();
			for(String s : lines) {
				writeln(s);
			}
			
			if (iter.hasNext()) {
				writeln(",");
			} else {
				writeln(";");
				break;
			}
		}
		
		out.close();
		f.close();
	}

	private static String fileNameOnly(String filename) {
		return filename.substring(filename.lastIndexOf('\\') + 1);
	}

	private static String writeBpms(EditorData data) {
		TreeMap<Double, Integer> changes = data.getBpmChanges();
		String result = "";
		for(Entry<Double, Integer> change : changes.entrySet()) {
			result += change.getKey() + "=" + change.getValue() + ",";
		}
		return result.substring(0, result.length() - 1);
	}

	private static String getImage(String name) {
		Texture t = AssetManager.getTexture(name);
		if (t == null) {
			return "";
		}
		
		String path = t.getPath();
		return fileNameOnly(path);
	}
}