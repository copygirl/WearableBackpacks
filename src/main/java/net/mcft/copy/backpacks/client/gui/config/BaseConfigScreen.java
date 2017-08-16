package net.mcft.copy.backpacks.client.gui.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiContainerScreen;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.GuiLabel;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.GuiScrollable;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;

@SideOnly(Side.CLIENT)
public abstract class BaseConfigScreen extends GuiContainerScreen {
	
	public final GuiScreen parentScreen;
	public final List<String> titleLines;
	
	protected final GuiLayout layoutMain;
		protected final GuiLayout layoutTitle;
		protected final GuiScrollable scrollableContent;
			protected final EntryList listEntries;
		protected final GuiLayout layoutButtons;
			protected final GuiButton buttonDone;
	
	public BaseConfigScreen(GuiScreen parentScreen, String... titleLines) {
		this.parentScreen = parentScreen;
		
		layoutMain = new GuiLayout(Direction.VERTICAL);
		layoutMain.setFill();
		layoutMain.setSpacing(0);
			
			// Title
			layoutTitle = new GuiLayout(Direction.VERTICAL);
			layoutTitle.setFillHorizontal();
			layoutTitle.setPaddingVertical(7);
			layoutTitle.setSpacing(1);
			
			this.titleLines = Collections.unmodifiableList(Arrays.stream(titleLines)
				.filter(Objects::nonNull).map(I18n::format)
				.collect(Collectors.toList()));
			this.titleLines.forEach(line -> {
				GuiLabel title = new GuiLabel(line);
				title.setCenteredHorizontal();
				layoutTitle.addFixed(title);
			});
			
			// Content
			listEntries = new EntryList();
			scrollableContent = new EntryListScrollable(listEntries);
			
			// Buttons
			layoutButtons = new GuiLayout(Direction.HORIZONTAL);
			layoutButtons.setCenteredHorizontal();
			layoutButtons.setPaddingVertical(3, 9);
			layoutButtons.setSpacing(5);
		
				buttonDone = new GuiButton(I18n.format("gui.done"));
				if (buttonDone.getWidth() < 100) buttonDone.setWidth(100);
				buttonDone.setAction(this::doneClicked);
				
				layoutButtons.addFixed(buttonDone);

			layoutMain.addFixed(layoutTitle);
			layoutMain.addWeighted(scrollableContent);
			layoutMain.addFixed(layoutButtons);
		
		container.add(layoutMain);
		
	}
	
	/** Adds an entry to this screen's entry list. */
	public void addEntry(GuiElementBase entry)
		{ listEntries.addFixed(entry); }
	
	/** Called when the "Done" buttons is clicked. */
	protected void doneClicked()
		{ GuiElementBase.display(parentScreen); }
	
	
	public static class EntryListScrollable extends GuiScrollable {
		
		public final EntryList entryList;
		
		public EntryListScrollable(EntryList entryList) {
			super(Direction.VERTICAL);
			setFillHorizontal();
			getScrollbar().setAlign(Direction.HORIZONTAL, new GuiScrollable.ContentMax(4));
			add(this.entryList = entryList);
		}
		
		@Override
		protected void updateChildSizes(Direction direction) {
			super.updateChildSizes(direction);
			// TODO: This could be simplified if the Alignment class contained logic for position / sizing of elements.
			if ((direction == Direction.HORIZONTAL) && (entryList != null))
				entryList.setWidth(entryList.maxLabelWidth + 8 + getWidth() / 2);
		}
		
	}
	
	public static class EntryList extends GuiLayout {
		
		public int maxLabelWidth;
		
		public EntryList() {
			super(Direction.VERTICAL);
			setCenteredHorizontal();
			setPaddingVertical(4, 3);
			setExpand(Direction.HORIZONTAL, false);
		}
		
		@Override
		protected void updateChildSizes(Direction direction) {
			super.updateChildSizes(direction);
			
			maxLabelWidth = getEntries()
				.map(e -> e.getLabel())
				.filter(Objects::nonNull)
				.mapToInt(GuiLabel::getWidth)
				.max().orElse(0);
			
			getEntries()
				.map(e -> e.getLabel())
				.filter(Objects::nonNull)
				.forEach(l -> l.setWidth(maxLabelWidth));
		}
		
		public Stream<GuiElementBase> getElements() { return children.stream(); }
		
		public Stream<IConfigEntry> getEntries() { return getElements()
			.filter(IConfigEntry.class::isInstance).map(IConfigEntry.class::cast); }
		
	}
	
}
