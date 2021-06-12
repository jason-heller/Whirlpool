package mapper.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import mapper.util.MSCounter;

public class EditorData {
	private String songFile;
	
	private TreeMap<Double, Integer> bpmChanges;
	private HashMap<Double, Integer> measureCount;
	private float scrollSpeed;
	
	public static final int DEFAULT_BPM = 120;
	private static final int DEFAULT_SCROLL_SPEED = 12;
	private static final double SPACING = 100.0;
	
	private double offset = 0.0;
	private double sampleStart = 0.0;
	private double sampleEnd = 100.0;
	
	private String name, artist, description;
	
	private TreeMap<Integer, Measure> measures;
	
	private MSCounter msCounter;
	
	public EditorData(String songFile, MSCounter msCounter) {
		this.songFile = songFile;
		
		measures = new TreeMap<>();
		
		bpmChanges = new TreeMap<>();
		measureCount = new HashMap<>();
		
		scrollSpeed = DEFAULT_SCROLL_SPEED;
		this.msCounter = msCounter;
	}
	
	public void addBpmChange(double time, int bpm) {
		int numMeasures = getMeasureAt(time);
		bpmChanges.put(time, bpm);
		measureCount.put(time, numMeasures);
	}
	
	public int removeBpmChange(double time) {
		int bpm = bpmChanges.remove(time);
		measureCount.remove(time);
		return bpm;
	}
	
	public int getMeasureAt(double time) {
		double timeOffset = time + .0001;

		Entry<Double, Integer> entry = getBpmEntry(timeOffset);

		if (entry == null) {
			return 0;
		}
		
		double lastBpmTime = entry.getKey();
		int prevMeasureCount = measureCount.get(lastBpmTime);
		double sizeScaled = getMeasureSize(entry.getValue()) * Editor.chartScale;
		int newMeasures = (int) Math.floor((timeOffset - lastBpmTime) / sizeScaled);
		return prevMeasureCount + newMeasures;
	}

	public void setSongFile(String file) {
		this.songFile = file;
	}
	
	public String getSongFile() {
		return songFile;
	}

	public TreeMap<Double, Integer> getBpmChanges() {
		return bpmChanges;
	}

	public float getScrollSpeed() {
		return scrollSpeed;
	}

	public double getMeasureSize(int bpm) {
		return ((60.0 * msCounter.getTargetFramerate() * scrollSpeed) / (bpm / 4));
	}
	
	public double getMeasureSizeAt(double chartY) {
		return getMeasureSize(getBpmAt(chartY));
	}

	public Entry<Double, Integer> getBpmEntry(double chartY) {
		if (bpmChanges.size() == 0)
			return null;
		
		Iterator<Entry<Double, Integer>> iter = bpmChanges.entrySet().iterator();
		Entry<Double, Integer> currentEntry = iter.next();
		while (iter.hasNext()) {
			Entry<Double, Integer> entry = iter.next();
			if (entry.getKey() >= chartY) {
				break;
			}

			currentEntry = entry;
		}

		return (currentEntry);
	}
	
	public int getBpmAt(double chartY) {
		return getBpmEntry(chartY).getValue();
	}
	
	public List<Measure> getVisibleMeasures(double start, double end) {
		List<Measure> visible = new ArrayList<>();

		for(Measure measure : measures.values()) {
			double size = measure.getSize() * Editor.chartScale;
			double time = measure.getTime() * Editor.chartScale;
			
			if (time + size >= start) {
				visible.add(measure);
			}
			
			if (time >= end) 
				break;
		}
		
		return visible;
	}

	public void editChart(int column, double time, int intervalId, boolean augmented) {
		int measureId = getMeasureAt(time * Editor.chartScale);

		Measure measure = measures.get(measureId);
		if (measure == null) {
			int bpm = getBpmAt(time);
			double sizeScaled = getMeasureSize(bpm);// * Editor.chartScale;
			double measureStartTime = time - (time % sizeScaled);

			measure = new Measure(measureStartTime, bpm, this);
			measures.put(measureId, measure);
		}
		
		measure.edit(column, intervalId, time);
	}

	public void setOffset(double offset) {
		this.offset = offset;
	}

	public void setSampleStart(double sampleStart) {
		this.sampleStart = sampleStart;
	}
	
	public void setSampleEnd(double sampleEnd) {
		this.sampleEnd = sampleEnd;
	}
	
	public double getSampleStart() {
		return sampleStart;
	}

	public double getSampleEnd() {
		return sampleEnd;
	}

	public Collection<Measure> getMeasures() {
		return measures.values();
	}

	public double getOffset() {
		return offset;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
