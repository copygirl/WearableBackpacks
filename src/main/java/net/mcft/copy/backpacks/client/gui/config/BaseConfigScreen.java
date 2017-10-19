package net.mcft.copy.backpacks.client.gui.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.mcft.copy.backpacks.client.gui.Alignment;
import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiContainerScreen;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.GuiLabel;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.GuiScrollable;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.client.gui.control.GuiButtonGlyph;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

@SideOnly(Side.CLIENT)
public abstract class BaseConfigScreen extends GuiContainerScreen {
	
	private final List<GuiLabel> _titleLabels;
	
	public final GuiScreen parentScreen;
	
	protected final GuiLayout layoutMain;
		protected final GuiLayout layoutTitle;
		protected final EntryListScrollable scrollableContent;
			protected final EntryList listEntries;
		protected final GuiLayout layoutButtons;
			protected final GuiButton buttonDone;
			protected final GuiButton buttonReset;
			protected final GuiButton buttonUndo;
	
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
			
			_titleLabels = Arrays.stream(titleLines)
				.filter(Objects::nonNull).map(I18n::format)
				.map(GuiLabel::new).collect(Collectors.toList());
			_titleLabels.forEach(GuiLabel::setCenteredHorizontal);
			_titleLabels.forEach(layoutTitle::addFixed);
			
			// Content
			listEntries = new EntryList();
			scrollableContent = new EntryListScrollable(listEntries);
			
			// Buttons
			layoutButtons = new GuiLayout(Direction.HORIZONTAL);
			layoutButtons.setCenteredHorizontal();
			layoutButtons.setPaddingVertical(3, 9);
			layoutButtons.setSpacing(5);
			
				buttonDone  = new GuiButton(I18n.format("gui.done"));
				buttonUndo  = new GuiButtonGlyph(GuiUtils.UNDO_CHAR, I18n.format("fml.configgui.tooltip.undoChanges"));
				buttonReset = new GuiButtonGlyph(GuiUtils.RESET_CHAR, I18n.format("fml.configgui.tooltip.resetToDefault"));
				if (buttonDone.getWidth() < 100) buttonDone.setWidth(100);
				
				buttonDone.setAction(this::doneClicked);
				buttonUndo.setAction(this::undoChanges);
				buttonReset.setAction(this::setToDefault);
			
			layoutMain.addFixed(layoutTitle);
			layoutMain.addWeighted(scrollableContent);
			layoutMain.addFixed(layoutButtons);
		
		container.add(layoutMain);
	}
	
	/** Adds an entry to this screen's entry list. */
	public void addEntry(GuiElementBase entry)
		{ listEntries.addFixed(entry); }
	
	public List<String> getTitleLines() {
		return Collections.unmodifiableList(_titleLabels.stream()
			.map(GuiLabel::getText).collect(Collectors.toList()));
	}
	public int getTitleLineCount() { return _titleLabels.size(); }
	public void setTitleLine(int index, String value)
		{ _titleLabels.get(index).setText(I18n.format(value)); }
	
	/** Returns whether any of this screen's entries were changed from their previous values. */
	public boolean isChanged() { return listEntries.getEntries().anyMatch(IConfigEntry::isChanged); }
	/** Returns whether all of this screen's entries are equal to their default values. */
	public boolean isDefault() { return listEntries.getEntries().allMatch(IConfigEntry::isDefault); }
	/** Returns whether all of this screen's entries represent a valid value. */
	public boolean isValid() { return listEntries.getEntries().allMatch(IConfigEntry::isValid); }
	
	/** Sets all of this screen's entries back to their previous values. */
	public void undoChanges() { listEntries.getEntries().forEach(IConfigEntry::undoChanges); }
	/** Sets all of this screen's entries to their default values. */
	public void setToDefault() { listEntries.getEntries().forEach(IConfigEntry::setToDefault); }
	
	/** Applies changes made to this screen's entries.
	 *  Called when clicking "Done" on the main config screen. */
	public ChangeRequiredAction applyChanges() {
		return listEntries.getEntries()
			.map(e -> e.applyChanges())
			.max(ChangeRequiredAction::compareTo)
			.orElse(ChangeRequiredAction.None);
	}
	
	/** Called when the "Done" button is clicked. */
	protected void doneClicked()
		{ GuiElementBase.display(parentScreen); }
	
	
	public static class EntryListScrollable extends GuiScrollable {
		
		public EntryList entryList;
		
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
			if ((direction != Direction.HORIZONTAL) || (entryList == null)) return;
			int minimumWidth = entryList.maxElementWidth;
			int dynamicWidth = entryList.maxLabelWidth + 8 + getWidth() / 2;
			entryList.setWidth(Math.max(minimumWidth, dynamicWidth));
		}
		
	}
	
	public static class EntryList extends GuiLayout {
		
		public int maxLabelWidth;
		public int maxElementWidth;
		
		public EntryList() {
			super(Direction.VERTICAL);
			setCenteredHorizontal();
			setPaddingVertical(4, 3);
			setExpand(Direction.HORIZONTAL, false);
		}
		
		@Override
		protected void updateChildSizes(Direction direction) {
			super.updateChildSizes(direction);
			
			maxLabelWidth = getEntryLabels()
				.mapToInt(GuiElementBase::getWidth)
				.max().orElse(0);
			getEntryLabels().forEach(l -> l.setWidth(maxLabelWidth));
			
			maxElementWidth = getElements()
				.filter(e -> !(e.getAlign(Direction.HORIZONTAL) instanceof Alignment.Both))
				.mapToInt(GuiElementBase::getWidth)
				.max().orElse(0);
		}
		
		public Stream<GuiElementBase> getElements() { return children.stream(); }
		
		public Stream<IConfigEntry> getEntries() {
			return getElements()
				.filter(IConfigEntry.class::isInstance)
				.map(IConfigEntry.class::cast);
		}
		public Stream<GuiLabel> getEntryLabels() {
			return getElements()
				.filter(BaseEntry.class::isInstance)
				.map(BaseEntry.class::cast)
				.filter(BaseEntry::hasLabel)
				.map(entry -> entry.label);
		}
		
	}
	
}
