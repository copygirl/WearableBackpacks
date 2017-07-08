package net.mcft.copy.backpacks.client.gui.test;

import net.minecraft.client.gui.GuiScreen;

import net.mcft.copy.backpacks.client.config.BackpacksGuiConfig;
import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiContainer;
import net.mcft.copy.backpacks.client.gui.GuiContainerScreen;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.client.gui.control.GuiLabel;

public class GuiTestScreen extends GuiContainerScreen {
	
	public GuiTestScreen(GuiScreen parentScreen) {
		
		container.add(new GuiLayout(Direction.VERTICAL) {{
			setSpacing(4);
			setFill(8);
			
			addFixed(new GuiButton("Show Config GUI") {{
				setHorizontalCentered();
				setAction(() -> display(new BackpacksGuiConfig(GuiTestScreen.this)));
			}});
			
			addFixed(new GuiButton("Test Alignment") {{
				setHorizontalCentered();
				setAction(() -> display(new AlignmentScreen()));
			}});
			
			addFixed(new GuiLayout(Direction.HORIZONTAL) {{
				setHorizontalCentered(200);
				addFixed(new GuiLabel(" Layout: ") {{ setVerticalCentered(); }});
				addWeighted(new GuiButton("Test 1")
					{{ setAction(() -> display(new LayoutScreen1())); }});
				addWeighted(new GuiButton("Test 2")
					{{ setAction(() -> display(new LayoutScreen2())); }});
			}});
			
			addWeighted(new GuiContainer()); // Filler space!
			
			addFixed(new GuiButton("Close") {{
				setHorizontalCentered();
				setAction(() -> display(parentScreen));
			}});
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
				container.add(new GuiButton() {{
					setSize(12, 12);
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
				container.add(new GuiButton() {{
					setSize(100, 20);
					switch (x) {
						case 0: setLeft(20); setText("Left"); break;
						case 1: setHorizontalCentered(); setText("Center"); break;
						case 2: setRight(20); setText("Right"); break;
					}
					switch (y) {
						case 0: setTop(20); addText(" + Top"); break;
						case 1: setVerticalCentered(); addText(" + Middle"); break;
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
						setLeftRight(0);
						addFixed(new GuiLabel(weight + "%") {{ setVerticalCentered(); }});
						addFixed(new GuiButton("<") {{ setVerticalCentered(); setSize(18, 18); }});
						for (int j = 1; j <= 3; j++)
							add(new GuiButton("Option " + j) {{ setTopBottom(0); }});
						addFixed(new GuiButton(">") {{ setVerticalCentered(); setSize(18, 18); }});
					}}, weight);
				}
				
				addFixed(new GuiButton("Back") {{
					setHorizontalCentered();
					setAction(() -> display(GuiTestScreen.this));
				}});
			}});
			
		}
	}
	
	public class LayoutScreen2 extends GuiContainerScreen {
		GuiLayout resizable = null;
		public LayoutScreen2() {
			
			container.add(new GuiLayout(Direction.VERTICAL) {{
				setHorizontalCentered();
				setTopBottom(8);
				
				addFixed(new GuiLayout(Direction.HORIZONTAL) {{
					setLeftRight(0);
					addWeighted(new GuiLabel(" Adjust width: ") {{ setVerticalCentered(); }});
					addFixed(new GuiButton(20, "-") {{ setAction(() -> resizable.setWidth(resizable.getWidth() - 40)); }});
					addFixed(new GuiButton(20, "+") {{ setAction(() -> resizable.setWidth(resizable.getWidth() + 40)); }});
				}});
				
				addFixed(new GuiLayout(Direction.HORIZONTAL) {{
					resizable = this;
					setWidth(240);
					addFixed(new GuiLabel(" Variable Height: ") {{ setVerticalCentered(); }});
					for (int i = 1; i <= 3; i++)
						addWeighted(new GuiButton() {
							{ setVerticalCentered(); }
							@Override public void onSizeChanged(Direction direction)
								{ if (direction == Direction.HORIZONTAL) setHeight(getWidth()); } }, i);
				}});
				
				addFixed(new GuiLayout(Direction.HORIZONTAL) {{
					setLeftRight(0);
					addFixed(new GuiLabel(" Fixed Height: ") {{ setVerticalCentered(); }});
					addWeighted(new GuiButton());
				}});
				
				addWeighted(new GuiContainer()); // Filler space!
				
				addFixed(new GuiButton("Back") {{
					setHorizontalCentered();
					setAction(() -> display(GuiTestScreen.this));
				}});
			}});
			
		}
	}
	
}
