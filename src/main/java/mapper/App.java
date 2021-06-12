package mapper;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;

import lwjgui.LWJGUIApplication;
import lwjgui.collections.ObservableList;
import lwjgui.event.listener.DropListener;
import lwjgui.event.listener.EventListener;
import lwjgui.event.listener.WindowCloseListener;
import lwjgui.geometry.Insets;
import lwjgui.scene.Node;
import lwjgui.scene.Scene;
import lwjgui.scene.Window;
import lwjgui.scene.control.Tab;
import lwjgui.scene.control.TabPane;
import lwjgui.scene.layout.StackPane;
import lwjgui.scene.layout.VBox;
import mapper.al.AudioManager;
import mapper.asset.AssetManager;
import mapper.editor.Editor;
import mapper.gui.MenuBarHandler;
import mapper.gui.TextInterface;

public class App extends LWJGUIApplication {
	
	public static final int WIDTH   = 1280;
	public static final int HEIGHT  = 720;

	private Window window;
	private TabPane tabPane;
	
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(String[] args, Window window) {
		AssetManager.init();
		
		AudioManager.init();
		
		this.window = window;
		VBox background = new VBox();
		ObservableList<Node> children = background.getChildren();
		MenuBarHandler.buildMenuBar(window, children, this);
		
		// Tab Pane
		tabPane = new TabPane();
		tabPane.setPrefHeight(100);
		tabPane.setFillToParentHeight(true);
		tabPane.setFillToParentWidth(true);
		
		children.add(tabPane);
		
		
		window.setScene(new Scene(background, WIDTH, HEIGHT));
		window.center();
		window.show();
		window.setTitle("Whirlpool [WORK IN PROGRESS]");

		window.addEventListener(new OnDropEvent(this));
		window.addEventListener(new OnCloseEvent(this));
		
	}
	
	public void clean() {
		AssetManager.clean();
		AudioManager.clean();
	}

	public Editor newProject(String name) {
		Tab tab = new Tab(name);
		tabPane.getTabs().add(tab);
		
		StackPane stackPane = new StackPane();
		stackPane.setFillToParentHeight(true);
		stackPane.setFillToParentWidth(true);
		stackPane.setPadding(new Insets(4));
		tab.setContent(stackPane);
		
		Editor editorPane = new Editor(window);
		editorPane.setFillToParentHeight(true);
		editorPane.setFillToParentWidth(true);
		stackPane.getChildren().add(editorPane);
		
		TextInterface textInfo = new TextInterface(editorPane);
		textInfo.setFillToParentHeight(true);
		textInfo.setFillToParentWidth(true);
		stackPane.getChildren().add(textInfo);
		return editorPane;
	}
	
	public Editor getCurrentProject() {
		Tab current = tabPane.getCurrentTab();
		if (current == null) 
			return null;
		
		StackPane stackPane = (StackPane)current.getContent();
		return (Editor) stackPane.getChildren().get(0);
	}
	
	public Tab getCurrentTab() {
		return tabPane.getCurrentTab();
	}

	public void onAudioDrop(String filename, String name, String extension) {
		if (tabPane.getTabs().size() == 0) {
			Editor proj = newProject(name);
			proj.setAudio(filename);
		} else {
			Editor proj = getCurrentProject();
			proj.setAudio(filename);
			getCurrentTab().setText(name);
		}
		
		AudioManager.setMainTrack(filename, extension);
	}

	public void onImageDrop(String path, String name, String extension) {
		Editor editor = this.getCurrentProject();
		if (editor == null) return;
		try {
			editor.onImageDrop(path, name, extension);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}



class OnDropEvent extends DropListener implements EventListener  {
	private App app;
	
	public OnDropEvent(App app) {
		this.app = app;
	}
	

	@Override
	public EventListenerType getEventListenerType() {
		return EventListenerType.DROP_LISTENER;
	}

	@Override
	public void invoke(long window, int count, long names) {
		PointerBuffer nameBuffer = MemoryUtil.memPointerBuffer(names, count);
		
		for (int i = 0; i < count; i++) {
	        final String path = MemoryUtil.memUTF8(MemoryUtil.memByteBufferNT1(nameBuffer.get(i)));
	        
	        int periodPos = path.lastIndexOf(".");
	        int slashPos = path.lastIndexOf('\\');
	        
	        final String name = path.substring(slashPos + 1);
	        final String ext = path.substring(periodPos + 1).toUpperCase();
	        
	        switch(ext) {
	        case "MP3":
	        case "OGG":
	        case "WAV":
	        	app.onAudioDrop(path, name, ext);
	        	break;
	        	
	        case "SM":
	        case "SSC":
	        	break;
	        	
	        case "PNG":
	        case "JPG":
	        case "JPEG":
	        case "BMP":
	        case "WEBMP":
	        case "GIF":
	        case "TIFF":
	        	app.onImageDrop(path, name, ext);
	        	break;
	        }
	    }
	}
}

class OnCloseEvent extends WindowCloseListener implements EventListener  {
	private App app;
	
	public OnCloseEvent(App app) {
		this.app = app;
	}
	
	@Override
	public EventListenerType getEventListenerType() {
		return EventListenerType.WINDOW_CLOSE_LISTENER;
	}


	@Override
	public void invoke(long window) {
		app.clean();
	}

}