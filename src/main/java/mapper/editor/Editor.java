package mapper.editor;

import java.util.Map.Entry;

import org.lwjgl.glfw.GLFW;

import lwjgui.event.KeyEvent;
import lwjgui.event.ScrollEvent;
import lwjgui.scene.Window;
import lwjgui.scene.layout.OpenGLPane;
import mapper.App;
import mapper.al.AudioManager;
import mapper.asset.AssetManager;
import mapper.gl.MasterRenderer;
import mapper.gl.Sprite;
import mapper.gui.ChartInterface;
import mapper.util.MSCounter;

public class Editor extends OpenGLPane {
	private InputHandler tih;
	
	private EditorData data;
	private ChartInterface chart;
	
	//TODO: Remove all static vars in here
	public static double chartY = -ChartInterface.RECEPTOR_SIZE;
	public static long chartTime = 0;

	public static double chartScale = 1.0;
	
	private boolean moving = false;
	
	private int intervalIndex = 0;
	
	private MasterRenderer master;
	
	public double mouseX, mouseY;
	
	private MSCounter msCounter;
	
	public static final int[] INTERVALS = new int[] {
		4, 8, 12, 16, 20, 24, 32, 48, 64, 96, 192
	};
	
	public Editor(Window window) {
		super();
		this.setPrefSize(App.WIDTH, App.HEIGHT);
		
		msCounter = new MSCounter();
		
		master = new MasterRenderer(this, window.getContext());
		setRendererCallback(master);
		
		tih = new InputHandler();
		
		data = new EditorData(null, msCounter);
		chart = new ChartInterface(this, master);
		chart.setInterval(INTERVALS[intervalIndex]);
		
		addBpmChange(0, EditorData.DEFAULT_BPM, false);

		setOnKeyPressedAndRepeat( event -> {
			tih.processKeys(this, event);
		});
		
		setOnKeyReleased(event -> {
			tih.processKeyRelease(this, event);
		});
		
		setOnMouseScrolled(event -> {
			tih.processScroll(this, event);
		});
		
		setOnMousePressed(event -> {
			chart.getMarginLabelManager().onMousePress(event);
		});
		
		setOnMouseReleased(event -> {
			chart.getMarginLabelManager().onMouseRelease(event);
		});
		
		setOnMouseDragged(event -> {
			// GFX to show dragging perhaps?
		});
	}
	
	public void addBpmChange(double time, int bpm, boolean editing) {
		data.addBpmChange(time, bpm);
		chart.getMarginLabelManager().addBpmLabel(time, bpm, false);
	}

	public void removeBpmChange(double time) {
		data.removeBpmChange(time);
	}
	
	public void moveBpm(double oldPos, double newPos) {
		int bpm = data.removeBpmChange(oldPos);
		data.addBpmChange(newPos, bpm);
	}
	
	public void update() {
		msCounter.update();
		boolean musicRecheckHack = chartTime < 0;
		
		mouseX = Window.mouseX - this.getAbsolutePosition().x;
		mouseY = Window.mouseY - this.getAbsolutePosition().y;
		
		if (moving) {
			chartY += (data.getScrollSpeed() * chartScale) * msCounter.getLastFrameError();
		}
		
		chart.update();

		chartTime = positionToTime(chartY + ChartInterface.RECEPTOR_SIZE);
		
		if (moving && musicRecheckHack && chartTime >= 0) {
			AudioManager.toggleMainTrack();
		}
	}
	
	public MSCounter getMsCounter() {
		return msCounter;
	}
	
	/** Converts a Y-position on the chart to a time, note this is not one-to-one with timeToPosition() due to precision errors
	 * @param position the Y position on the chart
	 * @return the same position as a time (in milliseconds)
	 */
	public long positionToTime(double position) {
		double numTicks = position / (double)data.getScrollSpeed();
		double tps = (msCounter.getTargetFramerate()) * chartScale;
		return (long) (numTicks / tps * 1000.0);
	}
	
	/** Converts a timestamp on the chart to a position, note this is not one-to-one with positionToTime() due to precision errors
	 * @param timestamp on the chart
	 * @return the same time as a position (in units, scaled to match the current zoom)
	 */
	public double timeToPosition(double time) {
		double numSecs = time / 1000.0;
		double tps = (msCounter.getTargetFramerate()) * chartScale;
		double numTicks = numSecs * tps;
		double pos = numTicks * (double)data.getScrollSpeed();
		return pos;// + ChartInterface.RECEPTOR_SIZE;
	}

	/**
	 * Handles the inputs on the editor pane
	 */
	public class InputHandler {

		private boolean ctrlKey;

		public void processKeys(Editor editorPane, KeyEvent event) {
			// Return if consumed
			if (event.isConsumed()) return;
			
			if (chart.getMarginLabelManager().isEditing()) {
				chart.getMarginLabelManager().onKeyPressed(event);
				return;
			}
				
			
			int snap = intervalIndex;
			double timePos = (chartY + ChartInterface.RECEPTOR_SIZE) / Editor.chartScale;
			if (moving) {
				snap = INTERVALS.length - 1;	// Meh lol
				Entry<Double, Integer> bpmEntry = data.getBpmEntry(timePos + EPSILON);
				int bpm = bpmEntry.getValue();
				double measureSize = data.getMeasureSizeAt(bpm);
				double snapSize = measureSize / INTERVALS[snap];
				timePos = Math.floor(timePos / snapSize) * snapSize;
			}
			//timePos /= Editor.chartScale;
			
			switch(event.key) {
			case GLFW.GLFW_KEY_UP:
				shiftUp(INTERVALS[intervalIndex]);
				event.consume();
				break;
			case GLFW.GLFW_KEY_DOWN:
				shiftDown(INTERVALS[intervalIndex]);
				event.consume();
				break;
				
			case GLFW.GLFW_KEY_PAGE_UP:
				shiftUp(4f);
				event.consume();
				break;
			case GLFW.GLFW_KEY_PAGE_DOWN:
				shiftDown(4f);
				event.consume();
				break;
			case GLFW.GLFW_KEY_LEFT:
				intervalIndex--;
				if (intervalIndex == -1) {
					intervalIndex = INTERVALS.length - 1;
				}
				chart.setInterval(INTERVALS[intervalIndex]);
				event.consume();
				break;
			case GLFW.GLFW_KEY_RIGHT:
				intervalIndex++;
				if (intervalIndex == INTERVALS.length) {
					intervalIndex = 0;
				}
				chart.setInterval(INTERVALS[intervalIndex]);
				event.consume();
				break;
			case GLFW.GLFW_KEY_SPACE:
				if (AudioManager.getMainTrack() != null) {
					moving = !moving;
					if (Editor.chartTime >= 0.0) {
						AudioManager.toggleMainTrack();
						AudioManager.repositionMainTrack();
					}
				}
				event.consume();
				break;
			case GLFW.GLFW_KEY_1:
			case GLFW.GLFW_KEY_2:
			case GLFW.GLFW_KEY_3:
			case GLFW.GLFW_KEY_4:
				data.editChart(event.key - GLFW.GLFW_KEY_1, timePos, snap, false);
				break;

			case GLFW.GLFW_KEY_LEFT_CONTROL:
			case GLFW.GLFW_KEY_RIGHT_CONTROL:
				ctrlKey = true;
				break;
			}
		}

		public void processKeyRelease(Editor editor, KeyEvent event) {
			if (event.isConsumed()) return;
			if (event.key == GLFW.GLFW_KEY_LEFT_CONTROL || event.key == GLFW.GLFW_KEY_RIGHT_CONTROL) {
				ctrlKey = false;
			}
		}

		public void processScroll(Editor editor, ScrollEvent event) {
			if (event.isConsumed()) return;
			
			if (!ctrlKey) {
				if (event.y > 0) {
					shiftUp(INTERVALS[intervalIndex]);
				} else {
					shiftDown(INTERVALS[intervalIndex]);
				}
			} else {
				double oldPos = positionToTime(chartY + ChartInterface.RECEPTOR_SIZE);
				
				if (event.y > 0) {
					Editor.chartScale = Math.min(Editor.chartScale + .2, 2);
				} else {
					Editor.chartScale = Math.max(Editor.chartScale - .2, .2);
				}
				Editor.chartY = timeToPosition(oldPos);
				
				double measureSize = (data.getMeasureSizeAt(Editor.chartY) * chartScale) / INTERVALS[intervalIndex];
				Editor.chartY = Math.round(Editor.chartY / measureSize) * measureSize;
				Editor.chartY -= ChartInterface.RECEPTOR_SIZE;
				
				AudioManager.repositionMainTrack();
			}
			event.consume();
		}
	}
	
	private final double EPSILON = 0.01;

	
	private void shiftUp(float interval) {
		Entry<Double, Integer> bpmEntry = data.getBpmEntry(chartY - EPSILON);
		int bpm = bpmEntry.getValue();
		double measureSize = (data.getMeasureSizeAt(bpm) * chartScale) / interval;

		double cy = chartY + ChartInterface.RECEPTOR_SIZE;
		cy = (float)Math.floor((cy - EPSILON) / measureSize) * measureSize;
		chartY = cy - ChartInterface.RECEPTOR_SIZE;
		
		Entry<Double, Integer> newBpmEntry = data.getBpmEntry(chartY);
		if (bpm != newBpmEntry.getValue())
			chartY = bpmEntry.getKey();
		AudioManager.repositionMainTrack();
	}
	
	private void shiftDown(float interval) {
		int bpm = data.getBpmAt(chartY + EPSILON);
		double measureSize = (data.getMeasureSizeAt(bpm) * chartScale) / interval;
		
		double cy = chartY + ChartInterface.RECEPTOR_SIZE;
		cy = (float)Math.ceil((cy + EPSILON) / measureSize) * measureSize;
		chartY = cy - ChartInterface.RECEPTOR_SIZE;

		Entry<Double, Integer> newBpmEntry = data.getBpmEntry(chartY);
		if (bpm != newBpmEntry.getValue())
			chartY = newBpmEntry.getKey();
		AudioManager.repositionMainTrack();
	}

	public EditorData getData() {
		return data;
	}

	public void setAudio(String file) {
		data.setSongFile(file);
		chart.setSongName(file);
	}

	public void onImageDrop(String path, String name, String extension) throws Exception {
		Sprite banner = chart.getBanner();
		Sprite cdTitle = chart.getCdTitle();
		Sprite background = chart.getBackground();

		if (banner.containsPoint(mouseX, mouseY)) {
			AssetManager.addTexture("banner", path, extension);
			banner.setTexture("banner");
			
		} else if (cdTitle.containsPoint(mouseX, mouseY)) {
			AssetManager.addTexture("cd_title", path, extension);
			cdTitle.setTexture("cd_title");;
		} else {
			AssetManager.addTexture("background", path, extension);
			background.setTexture("background");
		}
	}

	
}
