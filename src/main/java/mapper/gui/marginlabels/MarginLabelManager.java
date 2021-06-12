package mapper.gui.marginlabels;

import static mapper.gui.ChartInterface.CHART_WIDTH;
import static mapper.gui.ChartInterface.RECEPTOR_SIZE;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NanoVG;

import lwjgui.event.KeyEvent;
import lwjgui.event.MouseEvent;
import lwjgui.paint.Color;
import mapper.App;
import mapper.editor.Editor;
import mapper.editor.EditorData;
import mapper.gl.MasterRenderer;
import mapper.gl.NVGText;
import mapper.gui.ChartInterface;
import mapper.gui.Colors;;

public class MarginLabelManager {
		private MarginLabel firstLabel;		// Double-linked list
											// There shouldn't be too many labels, so this'll be efficient enough
		private Editor editor;
		private EditorData data;
		
		private String editInput = "";
		
		private MarginLabel hovering, editing;
		
		private boolean dragging;
		
		private double dragOffset = 0;
		private MarginLabel offsetLabel;
		private MarginLabel sampleStartLabel;
		private MarginLabel sampleEndLabel;
		
		public MarginLabelManager(Editor editor) {
			this.editor = editor;
			this.data = editor.getData();
			
			defaultLabels();
			
			
		}
		
		private void defaultLabels() {
			// Offset label
			String offsetTime = formatTime(data.getOffset());
			offsetLabel = addLabel("OFFSET", offsetTime, editor.timeToPosition(data.getOffset()));
			offsetLabel.setAlignment(MarginLabel.LEFT_ALIGNED);
			offsetLabel.setDeletable(false);
			
			offsetLabel.setOnMoveEvent(event -> {
				double pos = editor.positionToTime(event.newPos);
				offsetLabel.setValue(formatTime(pos));
				data.setOffset(pos);
			});
			
			// Sample Start label
			sampleStartLabel = addLabel("PREVIEW START", null, editor.timeToPosition(data.getSampleStart()));
			sampleStartLabel.setDeletable(false);
			sampleStartLabel.setOffset(128, 0);
			
			sampleStartLabel.setOnMoveEvent(event -> {
				data.setSampleStart(editor.positionToTime(event.newPos));
			});
			
			// Sample End label
			sampleEndLabel = addLabel("PREVIEW END", null, editor.timeToPosition(data.getSampleEnd()));
			sampleEndLabel.setDeletable(false);
			sampleEndLabel.setOffset(128, 0);
			
			sampleEndLabel.setOnMoveEvent(event -> {
				data.setSampleEnd(editor.positionToTime(event.newPos));
			});
		}
		
		public void addBpmLabel(double time, int bpm, boolean editing) {
			MarginLabel label = addLabel("BPM", bpm, time);
			label.setEditable(true);
			label.setOnMoveEvent(event -> {
				editor.moveBpm(event.oldPos, event.newPos);
			});
			label.setOnEditEvent(event -> {
				if (event.edit.equals("")) {
					return;
				}
				int newBpm = Integer.parseInt(event.edit);
				newBpm = Math.min(Math.max(newBpm, 4), 9999);
				// label.setValue(newBpm);
				editor.removeBpmChange(label.getPosition());
				removeLabel(label);
				editor.addBpmChange(label.getPosition(), newBpm, false);
				
			});
			
			if (editing) {
				editLabel(label);
			}
		}

		private String formatTime(double offset) {
			long msTime = (long)offset;
			Date date = new Date(Math.abs(msTime));
			return ((msTime < 0) ? "-" : "") + (new SimpleDateFormat("mm:ss.SSS").format(date).toString());
		}

		public MarginLabel addLabel(String label, Object value, double position) {
			MarginLabel newLabel = new MarginLabel(label, value, position);
			
			if (firstLabel == null) {
				firstLabel = newLabel;
				return newLabel;
			}
			
			MarginLabel currentLabel = firstLabel;
			while(currentLabel.getPosition() <= newLabel.getPosition()) {
				if (currentLabel.next != null) {
					currentLabel = currentLabel.next;
					continue;
				}
				break;
			}
			
			currentLabel.next = newLabel;
			newLabel.prev = currentLabel;
			
			return newLabel;
		}
		
		/*private void moveLabel(double oldPosition, double newPosition) {
			MarginLabel label = getLabelAt(oldPosition);
			label.move(newPosition);
		}*/
		
		public void removeLabel(MarginLabel label) {
			if (label.isDeletable()) {
				if (label.prev != null) {
					label.prev.next = label.next;
				}
				
				if(label.next != null) {
					label.next.prev = label.prev;
				}
				
				label.delete();
			}
		}

		public void update(MasterRenderer renderer, double start, double end) {
			double mouseX = editor.mouseX;
			double mouseY = editor.mouseY;
			
			double screenRatio = (App.HEIGHT / editor.getHeight());
			
			if (!dragging) {
				hovering = null;
			} else {
				NVGText nvgText = renderer.drawString(hovering.getText(), (long)mouseX, (long)(mouseY - 8.5));
				nvgText.setTextColor(Color.GRAY);
				int left = (App.WIDTH / 2);
				renderer.drawRect(left, (int) (start + (mouseY*screenRatio)), ChartInterface.CHART_WIDTH, 3, Colors.LT_BLUE);
				
			}
			
			// Wrapping update & render into one 'update' method to save on loop cycles
			MarginLabel label = firstLabel;
			while(label != null) {
				double pos = label.getPosition();
				
				// Should we process this label?
			if (pos >= start - (RECEPTOR_SIZE + 17) && pos <= end && (!dragging || hovering != label)) {

					String text = label.getText();
					
					// Determine position
					final int chartOffset = (App.WIDTH + (label.getAlignment() * CHART_WIDTH)) / 2;
					// final double start = Editor.chartY + RECEPTOR_SIZE;
					long x = (long)(label.getXOffset() + chartOffset);
					long y = (long)((((label.getPosition() + label.getYOffset()) * Editor.chartScale) - Editor.chartY) / screenRatio);
		
					// Draw label
					NVGText nvgText = renderer.drawString(text, x, y);
					if (label.getAlignment() < 0) {
						nvgText.setAlignmentH(NanoVG.NVG_ALIGN_RIGHT);
						nvgText.x -= 8;
					}
					
					nvgText.y -= 17;
					
					nvgText.calculateBounds(renderer.getLastContext());
					
					if (editing == label) {
						String blinker = (System.currentTimeMillis() % 1000 < 500) ? "|" : "";
						
						int w = (int)nvgText.getWidth() + 48;
						int h = (int)nvgText.getHeight();
						renderer.drawRect((int)x + (w/2), (int)label.getPosition() - (h/2), w, h, Colors.BLACK);
						nvgText.str = label.getLabel() + ": " + editInput + blinker;

					}
					// Handle dragging / deleting
					else if (nvgText.containsPoint(mouseX, mouseY)) {
						nvgText.setTextColor(Color.AQUA);
						if (!dragging) {
							hovering = label;
						}
					}
					
				}
				
				label = label.next;
			}
			
			//removeLabel(hovering);
			//hovering = null;
		}
		
		private MarginLabel getLabelAt(double position) {
			MarginLabel currentLabel = firstLabel;
			while(currentLabel != null) {
				double currentLabelPos = currentLabel.getPosition();
				if (currentLabelPos >= position) {
					return currentLabel;
				}
				
				currentLabel = currentLabel.next;
			}
			
			return currentLabel;
		}

		public void onMousePress(MouseEvent event) {
			if (!dragging && hovering != null) {
				if (event.button == 0) {
					dragging = true;
					double screenRatio = (App.HEIGHT / editor.getHeight());
					dragOffset = editor.mouseY - ((hovering.getPosition() - Editor.chartY) / screenRatio);
				} else if (hovering.isEditable()) {
					editLabel(hovering);
				}
			}
		}

		private void editLabel(MarginLabel label) {
			if (editing != null) {
				editing.edit(editInput);
				editing = null;
			}
			
			editing = label;
			editInput = "";
		}

		public void onMouseRelease(MouseEvent event) {
			if (dragging) {
				double screenRatio = (App.HEIGHT / editor.getHeight());
				
				hovering.move((Editor.chartY - dragOffset/* + RECEPTOR_SIZE*/) + (editor.mouseY * screenRatio));
				dragging = false;
				hovering = null;
			}
		}

		public boolean isEditing() {
			return editing != null;
		}

		public void onKeyPressed(KeyEvent event) {
			if (event.getKey() == GLFW.GLFW_KEY_BACKSPACE && editInput.length() > 0) {
				editInput = editInput.substring(0, editInput.length() - 1);
			} else if (event.getKey() == GLFW.GLFW_KEY_ENTER) {
				editing.edit(editInput);
				editing = null;
			} else {
				char c = (char)event.getKey();
				if (Character.isDigit(c))
						editInput += c;
			}
		}
}
