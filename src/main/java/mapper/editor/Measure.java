package mapper.editor;

import static mapper.gui.ChartInterface.RECEPTOR_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import mapper.App;
import mapper.gl.MasterRenderer;
import mapper.gl.Sprite;
import mapper.gui.Colors;

public class Measure {
	
	private static final int[] X_POSITIONS = new int[] {
			App.WIDTH / 2 - (RECEPTOR_SIZE / 2 * 3),	
			App.WIDTH / 2 - (RECEPTOR_SIZE / 2 * 1),	
			App.WIDTH / 2 + (RECEPTOR_SIZE / 2 * 1),	
			App.WIDTH / 2 + (RECEPTOR_SIZE / 2 * 3)
	};
	
	private static final float[] ROTATIONS = new float[] {
			(float) (Math.PI / 2f),
			0f,
			2f * (float) (Math.PI / 2f),	
			-(float) (Math.PI / 2f)
	};
	
	private double size;
	private double startTime;
	private TreeMap<Integer, TreeMap<Integer, String>> notes;
	
	public Measure(double startTime, int bpm, EditorData data) {
		notes = new TreeMap<>();
		this.startTime = startTime;
		size = data.getMeasureSize(bpm);
	}
	
	public void edit(int column, int origIntervalId, double time) {
		double relativeTime = time - startTime;
		System.out.println(relativeTime + " / " + time + " : " + startTime);
		boolean success = false;
		
		final int num = Editor.INTERVALS.length;
		for(int i = 0; i < num; i++) {
			int interval = Editor.INTERVALS[i];
			double range = (size / interval);

			if ((relativeTime + .001) % range < .01) {
				TreeMap<Integer, String> snap = notes.get(i);
				if (snap ==  null) {
					snap = new TreeMap<Integer, String>();
					notes.put(i, snap);
				}
				int pos = (int)((relativeTime + .001) / (size / interval));
				String line = snap.get(pos);
				if (line == null) {
					line = "0000";
				}
				
				char newChar = line.charAt(column) == '0' ? '1' : '0';
				
				line = line.substring(0, column) + newChar + line.substring(column + 1, 4);
				snap.put(pos, line);
				success = true;
				break;
			}
		}
		
		if (!success) {
			System.out.println("failed to place " + column + " at " + time);
		}
	}
	
	public void render(MasterRenderer renderer) {
		for(int snap : notes.keySet()) {
			TreeMap<Integer, String> noteSection = notes.get(snap);
			
			for(Integer pos : noteSection.keySet()) {
				String line = noteSection.get(pos);
				
				for(int i = 0; i < 4; i++) {
					if (line.charAt(i) == '1') {
						int x = X_POSITIONS[i];
						int y = (int) (startTime + (pos * (size / Editor.INTERVALS[snap])));
						Sprite arrow = renderer.drawSprite("notes", x, (int) (y * Editor.chartScale), RECEPTOR_SIZE, RECEPTOR_SIZE, ROTATIONS[i], Colors.WHITE);
						arrow.setAtlas(0, snap);

					}
				}
			}
		}
	}

	public double getSize() {
		return size;
	}

	public List<String> toOneInterval() {
		int lcmId = getLcm();
		int lcm = Editor.INTERVALS[lcmId];
		
		TreeMap<Integer, String> output = new TreeMap<>();
		for(int snapId : notes.keySet()) {
			double snap = Editor.INTERVALS[snapId];
			for(Entry<Integer, String> entry : notes.get(snapId).entrySet()) {
				int pos = (int)((entry.getKey() / snap) * lcm);
				output.put(pos, entry.getValue());
			}
		}
		
		List<String> lines = new ArrayList<>();
		for(int i = 0; i < Editor.INTERVALS[lcmId]; i++) {
			String line = output.get(i);
			if (line == null)
				line = "0000";
			
			lines.add(line);
		}
		
		return lines;
	}
	
	private int getLcm() {
		int lcmId = notes.lastKey();
		for(int snapId : notes.keySet()) {
			int snap = Editor.INTERVALS[snapId];
			if (Editor.INTERVALS[lcmId] % snap != 0) {
				for(int i = lcmId + 1; i < Editor.INTERVALS.length; i++) {
					if (Editor.INTERVALS[i] % snap == 0) {
						lcmId = i;
						break;
					}
				}
			}
		}
		return lcmId;
	}

	public double getTime() {
		return startTime;
	}
	
	// 4, 8, 12, 16, 20, 24, 32, 48, 64, 96, 192
	/*private final int[] MUL4_INDICES = new int[] {0, 1, 3, 6, 8};
	private final int[] MUL12_INDICES = new int[] {2, 5, 7, 9, 10};
	private final int INTERVAL_20_INDEX = 4;
	private int getLcm() {
		// Get highest snap
		
		int lcmId = notes.lastKey();
		
		if (lcmId == INTERVAL_20_INDEX || lcmId == Editor.INTERVALS.length - 1) {
			return Editor.INTERVALS.length - 1;
		}
		
		int lcm = Editor.INTERVALS[lcmId];
		boolean lastSnapIsMul12 = lcm % 12 == 0;
		
		int len = notes.keySet().size() - 1;
		Iterator<Integer> iter = notes.keySet().iterator();
		for(int i = 0; i < len; i++) {
			int snap = Editor.INTERVALS[iter.next()];
			
			if (snap % lcm != 0) {
				if (lastSnapIsMul12) {
					for(int j = 0; j < 5; j++) {
						int potentialLcm = MUL12_INDICES[j];
						if (potentialLcm < lcm) continue;
						
						if (snap % potentialLcm == 0) {
							lcm = potentialLcm;
							lcmId = j;
							break;
						}
					}
				} else {
					for(int j = 0; j < 5; j++) {
						int potentialLcm = MUL4_INDICES[j];
						if (potentialLcm < lcm) continue;
						
						if (snap % potentialLcm == 0) {
							lcm = potentialLcm;
							lcmId = j;
							break;
						}
					}
				}
				
			}
		}
		
		return lcmId;
	}*/
}
