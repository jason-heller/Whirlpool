package mapper.gui;

import lwjgui.geometry.Insets;
import lwjgui.geometry.Pos;
import lwjgui.paint.Color;
import lwjgui.scene.Node;
import lwjgui.scene.control.Label;
import lwjgui.scene.control.TextField;
import lwjgui.scene.layout.BorderPane;
import lwjgui.scene.layout.VBox;
import lwjgui.style.BackgroundSolid;
import mapper.App;
import mapper.editor.Editor;

public class TextInterface extends BorderPane {

	private TextField artistTextArea, nameTextArea, descTextArea;

	public TextInterface(Editor editor) {
		setBorder(new Insets(6));
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER_LEFT);
		
		artistTextArea = createField("Artist");
		nameTextArea = createField("Song Name");
		descTextArea = createField("Song Description");
		
		artistTextArea.setOnTextInput(event -> {
			editor.getData().setArtist(artistTextArea.getText());
		});
		nameTextArea.setOnTextInput(event -> {
			editor.getData().setName(nameTextArea.getText());
		});
		descTextArea.setOnTextInput(event -> {
			editor.getData().setDescription(descTextArea.getText());
		});
		
		
		vbox.getChildren().add(createLabel("Song Name"));
		vbox.getChildren().add(nameTextArea);
		vbox.getChildren().add(createLabel("Song Description"));
		vbox.getChildren().add(descTextArea);
		vbox.getChildren().add(createLabel("Artist"));
		vbox.getChildren().add(artistTextArea);
		
		VBox clicker = new VBox();
		
		clicker = new VBox();
		clicker.setBackgroundLegacy(null);
		clicker.setFillToParentHeight(true);
		clicker.setPrefWidth(App.WIDTH - 500);
		// clicker.setBackgroundLegacy(Color.RED);
		clicker.setAlignment(Pos.CENTER_RIGHT);
		
		//this.children.add(clicker);
		this.setRight(clicker);
		
		// HACK: Can't click through transparent stack panes
		clicker.setOnMousePressed(editor.getMousePressedEvent());
		clicker.setOnMouseReleased(editor.getMouseReleasedEvent());
		clicker.setOnMouseMoved(editor.getMouseMovedEvent());
		clicker.setOnMouseDragged(editor.getMouseDraggedEvent());
		//
		
		this.setLeft(vbox);
	}

	private Node createLabel(String string) {
		Label l = new Label(string);
		l.setTextFill(Color.WHITE);
		return l;
	}

	private TextField createField(String text) {
		TextField t = new TextField();
		t.setBackground(new BackgroundSolid(Color.BLACK));
		t.setPrefSize(260, 24);
		t.setFontFill(Color.WHITE);
		t.setCaretFill(Color.WHITE);
		return t;
	}
}
