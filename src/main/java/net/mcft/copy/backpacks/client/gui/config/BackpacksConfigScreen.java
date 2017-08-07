package net.mcft.copy.backpacks.client.gui.config;

import java.util.stream.Stream;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.*;
import net.mcft.copy.backpacks.client.gui.control.*;
import net.mcft.copy.backpacks.client.gui.test.GuiTestScreen;
import net.mcft.copy.backpacks.config.Setting;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;

@SideOnly(Side.CLIENT)
public class BackpacksConfigScreen extends GuiContainerScreen {
	
	private final GuiScreen _parentScreen;
	
	protected GuiButton buttonDone;
	protected GuiButton buttonReset;
	protected GuiButton buttonUndo;
	protected EntryList entryList;
	
	/** Creates a config GUI screen for Wearable Backpacks (and its GENERAL category). */
	public BackpacksConfigScreen(GuiScreen parentScreen) {
		this(parentScreen, (String)null);
		
		// Add all settings from the GENERAL category to the entry list.
		for (Setting<?> setting : WearableBackpacks.CONFIG.getSettings(Configuration.CATEGORY_GENERAL))
			addEntry(BaseEntrySetting.Create(this, setting));
		
		// After adding all settings from the GENERAL category, add its sub-categories.
		for (String cat : WearableBackpacks.CONFIG.getCategoryNames())
			if (!cat.equals(Configuration.CATEGORY_GENERAL))
				addEntry(new EntryCategory(this, cat));
	}
	
	/** Creates a config GUI screen for a sub-category. */
	public BackpacksConfigScreen(GuiScreen parentScreen, EntryCategory category) {
		this(parentScreen, category.getLanguageKey());
		
		// Add all settings for this category to the entry list.
		String cat = (category != null) ? category.category : Configuration.CATEGORY_GENERAL;
		for (Setting<?> setting : WearableBackpacks.CONFIG.getSettings(cat))
			addEntry(BaseEntrySetting.Create(this, setting));
	}
	
	public BackpacksConfigScreen(GuiScreen parentScreen, String title) {
		_parentScreen = parentScreen;
		
		container.add(new GuiButton(18, 18, "T") {
			{
				setRight(3); setTop(3);
				setAction(() -> display(new GuiTestScreen(BackpacksConfigScreen.this)));
			}
			@Override public boolean isVisible()
				{ return (super.isVisible() && GuiContext.DEBUG); }
		});
		
		container.add(new GuiLayout(Direction.VERTICAL) {{
			setFill();
			setSpacing(0);
			
			addFixed(new GuiLayout(Direction.VERTICAL) {{
				setFillHorizontal();
				setPaddingVertical(7);
				setSpacing(1);
				
				addFixed(new GuiLabel(WearableBackpacks.MOD_NAME) {{ setCenteredHorizontal(); }});
				if (title != null) addFixed(new GuiLabel(I18n.format(title)) {{ setCenteredHorizontal(); }});
			}});
			
			addWeighted(new EntryListScrollable(entryList = new EntryList()));
			
			addFixed(new GuiLayout(Direction.HORIZONTAL) {{
				setCenteredHorizontal();
				setPaddingVertical(3, 9);
				setSpacing(5);
				
				addFixed(buttonDone = new GuiButton(I18n.format("gui.done")));
				if (buttonDone.getWidth() < 100) buttonDone.setWidth(100);
				buttonDone.setAction(() -> doneClicked());
				
				addFixed(buttonUndo = new GuiButtonGlyph(GuiUtils.UNDO_CHAR, I18n.format("fml.configgui.tooltip.undoChanges")));
				addFixed(buttonReset = new GuiButtonGlyph(GuiUtils.RESET_CHAR, I18n.format("fml.configgui.tooltip.resetToDefault")));
				
				buttonUndo.setAction(() -> undoChanges());
				buttonReset.setAction(() -> setToDefault());
			}});
		}});
		
	}
	
	/** Returns whether any of this screen's entries were changed from their previous values. */
	public boolean isChanged() { return entryList.getEntries().anyMatch(BaseEntry::isChanged); }
	/** Returns whether all of this screen's entries are equal to their default values. */
	public boolean isDefault() { return entryList.getEntries().allMatch(BaseEntry::isDefault); }
	/** Returns whether all of this screen's entries represent a valid value. */
	public boolean isValid() { return entryList.getEntries().allMatch(BaseEntry::isValid); }
	
	/** Sets all of this screen's entries back to their previous values. */
	public void undoChanges() { entryList.getEntries().forEach(BaseEntry::undoChanges); }
	/** Sets all of this screen's entries to their default values. */
	public void setToDefault() { entryList.getEntries().forEach(BaseEntry::setToDefault); }
	
	/** Applies changes made to this screen's entries.
	 *  Called when clicking "Done" on the main config screen. */
	public ChangeRequiredAction applyChanges() {
		return entryList.getEntries()
			.map(e -> e.applyChanges())
			.max(ChangeRequiredAction::compareTo)
			.orElse(ChangeRequiredAction.None);
	}
	
	/** Called when the "Done" buttons is clicked. */
	protected void doneClicked() {
		// If this is the root config screen, apply the changes!
		if (!(_parentScreen instanceof BackpacksConfigScreen)) {
			applyChanges();
			WearableBackpacks.CONFIG.save();
		}
		GuiElementBase.display(_parentScreen);
	}
	
	/** Adds an entry to this screen's entry list. */
	public void addEntry(GuiElementBase entry) { entryList.addFixed(entry); }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		buttonDone.setEnabled(isValid());
		buttonUndo.setEnabled(isChanged());
		buttonReset.setEnabled(!isDefault());
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	
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
				.mapToInt(e -> e.getLabelWidth())
				.max().orElse(0);
			
			getEntries().forEach(e -> e.setLabelWidth(maxLabelWidth));
		}
		
		public Stream<GuiElementBase> getElements() { return children.stream(); }
		
		public Stream<BaseEntry> getEntries() { return getElements()
			.filter(BaseEntry.class::isInstance).map(BaseEntry.class::cast); }
		
	}
	
}
