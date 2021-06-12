package mapper.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import mapper.asset.AssetManager;
import mapper.asset.Texture;
import mapper.editor.EditorData;

public class SMImporter {
	
	private double sampleStart, sampleLength;
	private Map<String, List<String>> unhandledData = new HashMap<>();
	private List<String> noteData;
	
	public void saveSM(String filename, EditorData data) throws IOException {
		sampleStart = Double.NaN;
		sampleLength = Double.NaN;
		FileReader f = new FileReader(filename);
		BufferedReader in = new BufferedReader(f);
		
		String key = null;
		List<String> chunk = new ArrayList<>();
		Map<String, List<String>> unhandledData = new HashMap<>();
		
		String line;
		while ((line = in.readLine()) != null) {

			int hashTagIndex = line.indexOf('#');
			int semiColonIndex = line.indexOf(';');
			int colonIndex = line.indexOf(':');	// If the colon is on another line this will fail,
			// but it seems all SM/SSC files always have them on
			// the same line so this is okay for now.
			
			if (hashTagIndex != -1) {
				key = line.substring(hashTagIndex + 1, colonIndex).toUpperCase();
			}
			
			if (semiColonIndex != -1) {
				if (semiColonIndex != 0) {
					chunk.add(line.substring(colonIndex + 1, semiColonIndex));
				}
				
				parse(data, key, chunk);
				chunk.clear();
			} else {
				chunk.add(line);
			}
		}
		
		in.close();
		f.close();
		
		if (!Double.isNaN(sampleStart)) {
			data.setSampleStart(sampleStart);
			if (!Double.isNaN(sampleLength)) {
				data.setSampleEnd(sampleStart + sampleLength);
			}
		}

		// We save the note data parsing for absolute last, in case the BPM data/offset
		// data is for whatever reason after the note data, otherwise theres a chance
		// the notes are parsed before the tempo or offset is known
		parseNotes(data, chunk);
	}

	private void parse(EditorData data, String key, List<String> chunk) {
		if (key == null) {
			// Error
			System.err.println("Warning: Improperly parsed SM file");
			return;
		}
		
		switch(key) {
		case "TITLE":
			data.setName(chunk.get(0));
			break;
		case "SUBTITLE":
			data.setDescription(chunk.get(0));
			break;
		case "ARTIST":
			data.setArtist(chunk.get(0));
			break;
		case "MUSIC":
			data.setSongFile(chunk.get(0));
			break;
		case "OFFSET":
			data.setOffset(Double.parseDouble(chunk.get(0)));
			break;
		case "SAMPLESTART":
			sampleStart = Double.parseDouble(chunk.get(0));
			break;
		case "SAMPLELENGTH":
			sampleLength = Double.parseDouble(chunk.get(0));
			break;
		case "BANNER":
			loadTexture("banner", chunk.get(0));
			break;
		case "CDTITLE":
			loadTexture("cd_title", chunk.get(0));
			break;
		case "BACKGROUND":
			loadTexture("background", chunk.get(0));
			break;
		case "BPMS":
			for(String line : chunk) {
				String[] bpmTimeEq = line.replaceAll(" ", "").split(",");
				for(String bpmStr : bpmTimeEq) {
					if (!bpmStr.contains("="))
						continue;
					
					String[] bpmData = bpmStr.split("=");
					int bpm = Integer.parseInt(bpmData[0]);
					double seconds = Double.parseDouble(bpmData[1]);
					data.addBpmChange(seconds * 1000.0, bpm);
				}
			}
			break;
		case "NOTES":
			noteData = chunk;
			break;
			
		default:
			// Information not recognized by whirlpool.. just save it and move on
			unhandledData.put(key, chunk);
		}
	}

	private void parseNotes(EditorData data, List<String> chunk) {
		TreeMap<Double, Integer> bpms = data.getBpmChanges();
		Iterator<Entry<Double, Integer>> bpmIter = bpms.entrySet().iterator();
		
		double currentTime = data.getOffset();
		
		Entry<Double, Integer> currentBpm = bpmIter.next();
		
		//for
	}

	private void loadTexture(String key, String path) {
		String extension = path.substring(path.lastIndexOf('.' + 1)).toUpperCase();
		try {
			AssetManager.addTexture(key, path, extension);
		} catch (Exception e) {
			e.printStackTrace();
		}
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