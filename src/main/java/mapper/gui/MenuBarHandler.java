package mapper.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import lwjgui.collections.ObservableList;
import lwjgui.scene.Node;
import lwjgui.scene.Window;
import lwjgui.scene.control.Menu;
import lwjgui.scene.control.MenuBar;
import lwjgui.scene.control.MenuItem;
import lwjgui.scene.control.SeparatorMenuItem;
import mapper.App;
import mapper.editor.Editor;
import mapper.io.SMExporter;

public class MenuBarHandler {
	
	public static void buildMenuBar(Window window, ObservableList<Node> children, App app) {
		// Menu Bar
		MenuBar menuBar = new MenuBar();
		{
			// File Menu
			{
				Menu file = new Menu("File");
				
				/*MenuItem meNew = new MenuItem("New");
				file.getItems().add(meNew);
				meNew.setOnAction((event)->{
					app.newProject("New Song");
				});*/
				
				MenuItem meNew = new MenuItem("New");
				meNew.setOnAction((event)->{
					final JFileChooser fc = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Audio Files (.WAV/.MP3/.OGG)", "wav", "mp3", "ogg");
					fc.setFileFilter(filter);
					fc.setDialogTitle("Choose an audio file to step");
					int returnVal = fc.showOpenDialog(null);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File f = fc.getSelectedFile();
						String filename = f.getName();
						String ext = filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
						app.onAudioDrop(f.getAbsolutePath(), filename, ext);
					}
				});
				file.getItems().add(meNew);
	
				MenuItem meOpen = new MenuItem("Open");
				meOpen.setOnAction((event)->{
					final JFileChooser fc = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Stepmania Files", "sm", "ssc");
					fc.setFileFilter(filter);
					fc.setDialogTitle("Choose an sm/ssc file to open");
					int returnVal = fc.showOpenDialog(null);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						// Editor currentProj = app.getCurrentProject();
						File f = fc.getSelectedFile();
						String filename = f.getName();
						String ext = filename.substring(filename.lastIndexOf('.') + 1).toUpperCase();
						
						// TODO: Load SM/SSC files
					}
				});
				file.getItems().add(meOpen);
				
				MenuItem meSave = new MenuItem("Save");
				file.getItems().add(meSave);
				meSave.setOnAction((event)->{
					if (app.getCurrentProject() == null) {
						return;
					}
					Editor currentProj = app.getCurrentProject();
					String song = currentProj.getData().getSongFile();
					if (song == null) return;
					String filename = song.substring(0, song.lastIndexOf('.') + 1) + "sm";
					try {
						SMExporter.saveSM(filename, currentProj.getData(), false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				
				MenuItem meSaveAs = new MenuItem("Save As...");
				file.getItems().add(meSaveAs);
				meSaveAs.setOnAction((event)->{
					if (app.getCurrentProject() == null)
						return;
					final JFileChooser fc = new JFileChooser();
					int returnVal = fc.showSaveDialog(null);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						Editor currentProj = app.getCurrentProject();
						File f = fc.getSelectedFile();
						try {
							SMExporter.saveSM(f.getAbsolutePath(), currentProj.getData(), true);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				
				file.getItems().add(new SeparatorMenuItem());
				MenuItem mePref = new MenuItem("Preferences");
				//file.getItems().add(mePref);
				//file.getItems().add(new SeparatorMenuItem());
				
				MenuItem exit = new MenuItem("Exit");
				file.getItems().add(exit);
				
				menuBar.getItems().add(file);
				
				mePref.setOnAction((event)->{
					
				});
				
				exit.setOnAction((event)->{
					window.close();
				});
			}
			
			// Edit Menu
			{
				Menu edit = new Menu("Edit");
				//edit.getItems().add(new MenuItem("Redo"));
				menuBar.getItems().add(edit);
				
				MenuItem meTempoChange = new MenuItem("Add Tempo Change");
				edit.getItems().add(meTempoChange);
				meTempoChange.setOnAction((event)->{
					if (app.getCurrentProject() == null)
						return;
					app.getCurrentProject().addBpmChange(Editor.chartY + ChartInterface.RECEPTOR_SIZE, 120, true);
					//app.getCurrentProject().chart
				});
			}
			
			// Help Menu
			/*
			{
				Menu help = new Menu("Help");
				MenuItem click = new MenuItem("Click me!");
				help.getItems().add(click);
				menuBar.getItems().add(help);
				
				click.setOnAction((event)->{
					try {
						LWJGUIUtil.openURLInBrowser("https://github.com/orange451/LWJGUI");
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}*/
		}
		
		children.add(menuBar);
	}
}
