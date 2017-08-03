package net.mcft.copy.backpacks.client.gui.test;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.*;
import net.mcft.copy.backpacks.client.gui.control.*;

@SideOnly(Side.CLIENT)
public class GuiTestScreen extends GuiContainerScreen {
	
	public GuiTestScreen(GuiScreen parentScreen) {
		container.add(new GuiLayout(Direction.VERTICAL) {{
			setCenteredHorizontal();
			setFillVertical(8);
			setSpacing(4);
			
			addFixed(new GuiButton(GuiButton.DEFAULT_WIDTH, "Test Alignment")
				{{ setAction(() -> display(new AlignmentScreen())); }});
			
			addFixed(new GuiLayout(Direction.HORIZONTAL) {{
				setCenteredHorizontal(GuiButton.DEFAULT_WIDTH);
				addFixed(new GuiLabel(" Layout: ") {{ setCenteredVertical(); }});
				addWeighted(new GuiButton("Test 1")
					{{ setAction(() -> display(new LayoutScreen1())); }});
				addWeighted(new GuiButton("Test 2")
					{{ setAction(() -> display(new LayoutScreen2())); }});
			}});
			
			addFixed(new GuiButton(GuiButton.DEFAULT_WIDTH, "Test Visibility / Enabled")
				{{ setAction(() -> display(new VisibilityEnabledScreen())); }});
			
			addFixed(new GuiButton(GuiButton.DEFAULT_WIDTH, "Test Controls")
				{{ setAction(() -> display(new ControlsScreen())); }});
			
			addWeighted(new GuiContainer()); // Filler space!
			
			addFixed(new GuiButton(GuiButton.DEFAULT_WIDTH, "Close")
				{{ setAction(() -> display(parentScreen)); }});
		}});
	}
	
	public class AlignmentScreen extends GuiContainerScreen {
		public AlignmentScreen() {
			for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				int x = i, y = j;
				// Can't use i and j directly because in the anonymous
				// class constructor they need to be "effectively final".
				// 
				// This would not be an issue here because we immediately
				// use these values, but in the case of delayed execution,
				// we would end up with the value 3 in place of i and j,
				// because the loops would have finished.
				
				if ((x == 1) && (y == 1)) continue;
				container.add(new GuiButton(12, 12) {{
					switch (x) {
						case 0: setLeft(4); break;
						case 1: setLeftRight(20); break;
						case 2: setRight(4); break;
					}
					switch (y) {
						case 0: setTop(4); break;
						case 1: setTopBottom(20); break;
						case 2: setBottom(4); break;
					}
				}});
			}
			
			for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				int x = i, y = j; // Same as above.
				container.add(new GuiButton(100, 20) {{
					switch (x) {
						case 0: setLeft(20); setText("Left"); break;
						case 1: setCenteredHorizontal(); setText("Center"); break;
						case 2: setRight(20); setText("Right"); break;
					}
					switch (y) {
						case 0: setTop(20); addText(" + Top"); break;
						case 1: setCenteredVertical(); addText(" + Middle"); break;
						case 2: setBottom(20); addText(" + Bottom"); break;
					}
					setAction(() -> display(GuiTestScreen.this));
				}});
			}
		}
	}
	
	public class LayoutScreen1 extends GuiContainerScreen {
		public LayoutScreen1() {
			container.add(new GuiLayout(Direction.VERTICAL) {{
				setFill(8);
				
				for (int i = 1; i <= 4; i++) {
					int weight = i * 10;
					addWeighted(new GuiLayout(Direction.HORIZONTAL) {{
						setFillHorizontal();
						addFixed(new GuiLabel(weight + "%") {{ setCenteredVertical(); }});
						addFixed(new GuiButton("<") {{ setCenteredVertical(); setSize(18, 18); }});
						for (int j = 1; j <= 3; j++)
							add(new GuiButton("Option " + j) {{ setTopBottom(0); }});
						addFixed(new GuiButton(">") {{ setCenteredVertical(); setSize(18, 18); }});
					}}, weight);
				}
				
				addFixed(new GuiButton(GuiButton.DEFAULT_WIDTH, "Back") {{
					setCenteredHorizontal();
					setAction(() -> display(GuiTestScreen.this));
				}});
			}});
		}
	}
	
	public class LayoutScreen2 extends GuiContainerScreen {
		private GuiLayout resizable;
		public LayoutScreen2() {
			container.add(new GuiLayout(Direction.VERTICAL) {{
				setCenteredHorizontal();
				setFillVertical(8);
				
				addFixed(new GuiLayout(Direction.HORIZONTAL) {{
					setFillHorizontal();
					addWeighted(new GuiLabel(" Adjust width: ") {{ setCenteredVertical(); }});
					addFixed(new GuiButton(20, "-") {{ setAction(() -> resizable.setWidth(resizable.getWidth() - 40)); }});
					addFixed(new GuiButton(20, "+") {{ setAction(() -> resizable.setWidth(resizable.getWidth() + 40)); }});
				}});
				
				addFixed(new GuiLayout(Direction.HORIZONTAL) {{
					resizable = this;
					setWidth(240);
					addFixed(new GuiLabel(" Variable Height: ") {{ setCenteredVertical(); }});
					for (int i = 1; i <= 3; i++)
						addWeighted(new GuiButton() {
							{ setCenteredVertical(); }
							@Override public void onSizeChanged(Direction direction)
								{ if (direction == Direction.HORIZONTAL) setHeight(getWidth()); } }, i);
				}});
				
				addFixed(new GuiLayout(Direction.HORIZONTAL) {{
					setFillHorizontal();
					addFixed(new GuiLabel(" Fixed Height: ") {{ setCenteredVertical(); }});
					addWeighted(new GuiButton());
				}});
				
				addWeighted(new GuiContainer()); // Filler space!
				
				addFixed(new GuiButton(GuiButton.DEFAULT_WIDTH, "Back") {{
					setCenteredHorizontal();
					setAction(() -> display(GuiTestScreen.this));
				}});
			}});
		}
	}
	
	public class VisibilityEnabledScreen extends GuiContainerScreen {
		public VisibilityEnabledScreen() {
			container.add(new GuiLayout(Direction.VERTICAL) {{
				setCenteredHorizontal();
				setFillVertical(8);
				
				addVisibilityEnabledControls(this, new GuiLayout(Direction.VERTICAL) {{
					setFillHorizontal(8);
					addFixed(new GuiButton("Filler") {{ setFillHorizontal(); }});
					addVisibilityEnabledControls(this, new GuiLayout(Direction.VERTICAL) {{
						setFillHorizontal(8);
						addFixed(new GuiButton("Filler") {{ setFillHorizontal(); }});
						addVisibilityEnabledControls(this, new GuiButton("Another Element") {{ setFillHorizontal(); }});
						addFixed(new GuiButton("Filler") {{ setFillHorizontal(); }});
					}});
					addFixed(new GuiButton("Filler") {{ setFillHorizontal(); }});
				}});
				
				addWeighted(new GuiContainer()); // Filler space!
				
				addFixed(new GuiButton(GuiButton.DEFAULT_WIDTH, "Back") {{
					setCenteredHorizontal();
					setAction(() -> display(GuiTestScreen.this));
				}});
			}});
		}
		
		private void addVisibilityEnabledControls(GuiLayout layout, GuiElementBase element) {
			layout.addFixed(new GuiLayout(Direction.HORIZONTAL) {{
				setFillHorizontal();
				addFixed(new GuiLabel(" Visible: ") {{ setCenteredVertical(); }});
				addWeighted(new GuiButton("yes") {{ setAction(() -> {
					element.setVisible(!element.isVisible());
					setText(element.isVisible() ? "yes" : "no");
				}); }});
				addFixed(new GuiLabel(" Enabled: ") {{ setCenteredVertical(); }});
				addWeighted(new GuiButton("yes") {{ setAction(() -> {
					element.setEnabled(!element.isEnabled());
					setText(element.isEnabled() ? "yes" : "no");
				}); }});
			}});
			layout.addFixed(element);
		}
	}
	
	public class ControlsScreen extends GuiContainerScreen {
		public ControlsScreen() {
			container.add(new GuiLayout(Direction.VERTICAL) {{
				setCenteredHorizontal();
				setFillVertical(8);
				
				addFixed(new GuiSlider(Direction.HORIZONTAL) {{
					setFillHorizontal();
					setRange(0, 10);
					setStepSize(1);
				}});
				
				addFixed(new GuiField() {{ setFillHorizontal(); }});
				
				addFixed(new GuiScrollable() {{
					setFillHorizontal();
					setHeight(100);
					setPadding(8);
					add(new GuiButton("Scroll!") {{ setPosition(0, 0); }});
					add(new GuiButton("You found me!") {{ setPosition(300, 200); }});
				}});
				
				addWeighted(new GuiContainer()); // Filler space!
				
				addFixed(new GuiButton(GuiButton.DEFAULT_WIDTH, "Back") {{
					setCenteredHorizontal();
					setAction(() -> display(GuiTestScreen.this));
				}});
			}});
			
			container.add(new GuiButton(8, 8, 100, 20, "Drag me!") {
				@Override public boolean canDrag() { return true; }
				@Override public void onDragged(int mouseX, int mouseY, int startX, int startY) {
					setPosition(((Alignment.Min)getHorizontalAlign()).min + mouseX - startX,
								((Alignment.Min)getVerticalAlign()).min   + mouseY - startY);
				}
			});
		}
	}
	
}
