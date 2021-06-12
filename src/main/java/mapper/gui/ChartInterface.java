package mapper.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.joml.Vector4f;

import lwjgui.geometry.Insets;
import lwjgui.geometry.Pos;
import lwjgui.paint.Color;
import lwjgui.scene.control.Label;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.HBox;
import lwjgui.scene.layout.VBox;
import lwjgui.scene.layout.floating.FloatingPane;
import mapper.App;
import mapper.asset.AssetManager;
import mapper.asset.Texture;
import mapper.editor.Editor;
import mapper.editor.EditorData;
import mapper.editor.Measure;
import mapper.gl.MasterRenderer;
import mapper.gl.Sprite;
import mapper.gui.marginlabels.MarginLabelManager;

public class ChartInterface {
	private MasterRenderer renderer;
	private Editor editor;
	
	public static final int RECEPTOR_SIZE = 64;
	public static final int CHART_WIDTH = RECEPTOR_SIZE * 5;
	
	private Label songLabel, intervalLabel, timeLabel;
	
	private MarginLabelManager marginLabelManager;
	
	private Sprite banner, cdTitle, background;
	
	public ChartInterface(Editor editor, MasterRenderer renderer) {
		this.editor = editor;
		this.renderer = renderer;
		
		background = addSprite("no_background", App.WIDTH / 2, App.HEIGHT / 2, App.WIDTH, App.HEIGHT, Colors.WHITE);
		
		addBoard();
		addReceptors();
		
		// Music icon
		addSprite("music", App.WIDTH - 32, 40, 32, 32, Colors.WHITE);
		banner = addSprite("no_banner", (125 + 16), 64, 256, 80, Colors.WHITE);		// Hardcoded values BAD
		cdTitle = addSprite("no_cdtitle", (63 - 16), 150, 63, 63, Colors.WHITE);
		
		// Boxes
		VBox rhsInfo = new VBox();
		rhsInfo.setPrefSize(App.WIDTH, App.HEIGHT);
		rhsInfo.setAlignment(Pos.TOP_RIGHT);
		editor.getChildren().add(rhsInfo);
		//rhsInfo.setBackgroundLegacy(Color.GREEN);
		
		VBox lhsInfo = new VBox();
		lhsInfo.setPrefSize(300, 300);
		lhsInfo.setAlignment(Pos.BOTTOM_LEFT);
		//lhsInfo.setBackgroundLegacy(Color.BLACK);
		editor.getChildren().add(lhsInfo);
		
		FloatingPane artistInfoFBox = new FloatingPane();
		artistInfoFBox.setPrefSize(300, 300);
		artistInfoFBox.setAbsolutePosition(App.WIDTH - 280, 0);
		HBox artistInfo = new HBox();
		artistInfo.setAlignment(Pos.CENTER_LEFT);
		artistInfoFBox.getChildren().add(artistInfo);
		editor.getChildren().add(artistInfoFBox);
		
		BorderPane cInfo = new BorderPane();
		cInfo.setAlignment(Pos.TOP_CENTER);
		cInfo.setPrefSize(App.WIDTH, App.HEIGHT);
		//cInfo.setBackgroundLegacy(Color.RED);
		HBox cInfoHBox = new HBox();
		cInfo.setTop(cInfoHBox);
		editor.getChildren().add(cInfo);
		
		// Labels
		String songName = editor.getData().getSongFile();
		songLabel = new Label(songName == null ? "No Song Selected" : songName);
		songLabel.setPadding(new Insets(32, 64, 0, 0));
		songLabel.setTextFill(Color.WHITE);
		
		intervalLabel = new Label();
		intervalLabel.setFontSize(24);
		intervalLabel.setAlignment(Pos.CENTER);
		intervalLabel.setTextFill(Color.WHITE);
		intervalLabel.setPadding(new Insets(0,8,0,8));
		
		timeLabel = new Label("Time: 0");
		timeLabel.setFontSize(24);
		timeLabel.setAlignment(Pos.CENTER);
		timeLabel.setTextFill(Color.WHITE);
		
		rhsInfo.getChildren().add(songLabel);
		cInfoHBox.getChildren().add(intervalLabel);
		cInfoHBox.getChildren().add(timeLabel);
		
		marginLabelManager = new MarginLabelManager(editor);
	}
	
	private void addBoard() {
		addSprite("no_texture", App.WIDTH / 2, 0, CHART_WIDTH, App.HEIGHT * 2, Colors.BLACK_SLIGHTLY_TRANSPARENT);
	}

	private void addReceptors() {
		Texture t = AssetManager.getTexture("receptors");
		
		if (t == null) {
			System.err.println("ERR: Missing Texture for receptors");
			System.exit(-1);
		}
		
		int w = RECEPTOR_SIZE / 2;// t.getAtlasWidth();
		int h = RECEPTOR_SIZE;// t.getAtlasHeight();
		
		final int cx = App.WIDTH / 2;
		final int yPos = h;
		final int xOff = w;
		
		final float halfPI = (float) (Math.PI / 2f);
		addSprite(t, cx - xOff * 3, yPos, RECEPTOR_SIZE, RECEPTOR_SIZE, halfPI);
		addSprite(t, cx - xOff * 1, yPos, RECEPTOR_SIZE, RECEPTOR_SIZE, 0);
		addSprite(t, cx + xOff * 1, yPos, RECEPTOR_SIZE, RECEPTOR_SIZE, halfPI * 2f);
		addSprite(t, cx + xOff * 3, yPos, RECEPTOR_SIZE, RECEPTOR_SIZE, -halfPI);
		
	}

	private Sprite addSprite(Texture tex, int x, int y, int w, int h, float rot) {
		Sprite spr = new Sprite(tex, x, y);
		renderer.addSprite(spr);
		spr.setWidth(w);
		spr.setHeight(h);
		spr.setRotation(rot);
		spr.fixed();
		return spr;
	}
	
	private Sprite addSprite(String tex, int x, int y, int w, int h, Vector4f color) {
		Sprite spr = new Sprite(tex, x, y);
		renderer.addSprite(spr);
		spr.setWidth(w);
		spr.setHeight(h);
		spr.setColor(color);
		spr.fixed();
		return spr;
	}

	public void setSongName(String file) {
		String text = file.substring(file.lastIndexOf('\\') + 1);
		
		if (text.length() > 64)
			text = text.substring(0, 64);
		
		songLabel.setText(text);
	}
	
	public void update() {
		EditorData data = editor.getData();
		double start = Editor.chartY;// / Editor.chartScale;
		//double zoom = Editor.chartScale;
		double end = start + (App.HEIGHT/* * data.getScrollSpeed()*/);
		
		long msTime = (long)(Editor.chartTime);
		Date date = new Date(Math.abs(msTime));
		String formattedDate = new SimpleDateFormat("mm:ss.SSS").format(date);
		timeLabel.setText("Time: "+ (msTime < 0 ? "-" : "") + formattedDate);
		
		// Find all changes in BPM on screen
		TreeMap<Double, Integer> bpmChanges = data.getBpmChanges();
		
		Iterator<Entry<Double, Integer>> bpmChangesIter = bpmChanges.entrySet().iterator();
		Entry<Double, Integer> bpmChange = bpmChanges.firstEntry();

		List<Entry<Double, Integer>> changesOnScreen = new ArrayList<>();
		
		if (bpmChanges.size() < 2) {
			changesOnScreen.add(bpmChange);
		} else {
			bpmChangesIter.next();
			Entry<Double, Integer> nextBpmChange = null;
			
			while(bpmChangesIter.hasNext()) {
				nextBpmChange = bpmChangesIter.next();
				Double key = nextBpmChange.getKey();
				
				if (key >= start) {
					changesOnScreen.add(bpmChange);
					
					if (key >= end) {
						break;
					}
				}
				
				bpmChange = nextBpmChange;
			}
			
			changesOnScreen.add(nextBpmChange);
		}
		
		if (changesOnScreen.size() == 0) {
			changesOnScreen.add(bpmChange);
		}
		
		marginLabelManager.update(renderer, start, end);
		
		// Draw lines
		int entryIndex = 0;
		int bpm = changesOnScreen.get(0).getValue();
		double nextBpmChange = (changesOnScreen.size() < 2) ? Float.POSITIVE_INFINITY : changesOnScreen.get(1).getKey();
		nextBpmChange *= Editor.chartScale;
		double increment = (data.getMeasureSize(bpm) * Editor.chartScale) / 4.0;
		double i = start - (start % increment);
		int barCounter = (int)((start - changesOnScreen.get(0).getKey()) / increment) - 1;
		while(i < end) {
			barCounter = (barCounter + 1) % 4;
			if (i < 0) {
				i += increment;
				continue;
			}
			// Draw line
			Vector4f barColor = barCounter == 0 ? Colors.WHITE : Colors.GREY;
			renderer.drawRect(App.WIDTH / 2, (int)i, CHART_WIDTH, 3, barColor);
			
			// Increment to next spot
			double nextPos = i + increment;
			if (nextPos >= nextBpmChange) {
				i = nextBpmChange;
				
				bpm = changesOnScreen.get(entryIndex).getValue();
				increment = data.getMeasureSize(bpm) * Editor.chartScale;
				//System.out.println("BPM "+bpm+" INC "+increment+ " POS "+i);
				
				entryIndex++;
				nextBpmChange = (changesOnScreen.size() <= entryIndex) ? Float.POSITIVE_INFINITY
						: changesOnScreen.get(entryIndex).getKey();
			} else {
				i = nextPos;
			}
			
			// Draw notes
			for(Measure measure : data.getVisibleMeasures(start, end)) {
				measure.render(renderer);
			}
		}
	}

	public void setInterval(int interval) {
		String postFix = "th";
		if (interval == 32 || interval == 192) {
			postFix = "nd";
		}
		
		intervalLabel.setText("Snap: " + interval + postFix);
	}
	
	public Sprite getBanner() {
		return banner;
	}
	
	public Sprite getCdTitle() {
		return cdTitle;
	}
	
	public Sprite getBackground() {
		return background;
	}
	
	public MarginLabelManager getMarginLabelManager() {
		return marginLabelManager;
	}
}
