package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.GuiLabel;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.GuiLabel.TextAlign;
import net.mcft.copy.backpacks.client.gui.config.BackpacksConfigScreen;
import net.mcft.copy.backpacks.client.gui.config.BaseConfigScreen;
import net.mcft.copy.backpacks.client.gui.config.BaseEntry;
import net.mcft.copy.backpacks.client.gui.config.BaseEntryList;
import net.mcft.copy.backpacks.client.gui.config.IConfigEntry;
import net.mcft.copy.backpacks.client.gui.config.BaseEntryList.Entry.MoveButton;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.client.gui.control.GuiField;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;
import net.mcft.copy.backpacks.config.Status.Severity;
import net.mcft.copy.backpacks.config.custom.SettingListSpawn.BackpackEntityEntry;
import net.mcft.copy.backpacks.config.custom.SettingListSpawn.BackpackEntry;

@SideOnly(Side.CLIENT)
public class ListEntryEntityScreen extends BaseConfigScreen {
	
	private final EntryListSpawn _owningList;
	private final Optional<EntryListSpawn.Entry> _entry;
	
	public final GuiLabel labelTitleEntityName;
	public final EntryEntityID entryEntityID;
	public final EntryListBackpack listBackpack;
	public final GuiButton buttonCancel;
	
	public ListEntryEntityScreen(EntryListSpawn owningList, Optional<EntryListSpawn.Entry> entry) {
		super(GuiElementBase.getCurrentScreen(),
			((BackpacksConfigScreen)GuiElementBase
				.getCurrentScreen()).titleLines.toArray(new String[0]));
		_owningList = owningList;
		_entry      = entry;
		
		List<BackpackEntry> entries = entry.map(EntryListSpawn.Entry::getValue)
			.map(e -> e.entries).orElseGet(Collections::emptyList);
		
		// Title
		labelTitleEntityName = new GuiLabel("");
		labelTitleEntityName.setCenteredHorizontal();
		layoutTitle.addFixed(labelTitleEntityName);
		
		// Content
		entryEntityID = new EntryEntityID(this);
		listBackpack  = new EntryListBackpack(entries, Collections.emptyList());
		
		listEntries.addFixed(entryEntityID);
		listEntries.addFixed(listBackpack);
		
		// Buttons
		buttonCancel = new GuiButton(translate("gui.cancel"));
		if (buttonCancel.getWidth() < 100) buttonCancel.setWidth(100);
		buttonCancel.setAction(this::cancelClicked);
		
		layoutButtons.addFixed(buttonDone);
		layoutButtons.addFixed(buttonUndo);
		// FIXME: Add if there's a default available.
		// layoutButtons.addFixed(buttonReset);
		layoutButtons.addFixed(buttonCancel);
	}
	
	@Override
	protected void doneClicked() {
		BackpackEntityEntry value = new BackpackEntityEntry();
		value.entityID = entryEntityID.field.getText();
		value.entries  = listBackpack.getValue();
		
		_entry.orElseGet(() -> (EntryListSpawn.Entry)_owningList.addEntry()).setValue(value);
		GuiElementBase.display(parentScreen);
	}
	
	protected void cancelClicked()
		{ GuiElementBase.display(parentScreen); }
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		buttonDone.setEnabled(listEntries.getEntries().allMatch(IConfigEntry::isValid));
		buttonUndo.setEnabled(listEntries.getEntries().anyMatch(IConfigEntry::isChanged));
		buttonReset.setEnabled(!listEntries.getEntries().allMatch(IConfigEntry::isDefault));
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	
	// TODO: Have this somehow extend EntryField?
	public static class EntryEntityID extends BaseEntry.Labelled {
		
		private final ListEntryEntityScreen _owningScreen;
		
		public final GuiField field;
		public Optional<EntityEntry> entityEntry;
		
		public EntryEntityID(ListEntryEntityScreen owningScreen) {
			label.setText(translate("spawn.entityID"));
			label.setTooltip(translateTooltip("spawn.entityID"));
			
			_owningScreen = owningScreen;
			
			field = new GuiField(0, ENTRY_HEIGHT, _owningScreen._entry
				.map(e -> e.getValue().entityID).orElse(""));
			field.setChangedAction(this::onChanged);
			
			setSpacing(4, 8, 6);
			addFixed(iconStatus);
			addFixed(label);
			addWeighted(field);
			addFixed(buttonUndo);
			
			onChanged();
		}
		
		@Override
		public List<Status> getStatus() {
			return entityEntry.isPresent()   ? Collections.emptyList()
			     : field.getText().isEmpty() ? Arrays.asList(Status.EMPTY)
			                                 : Arrays.asList(EntryListSpawn.STATUS_NOT_FOUND);
		}
		
		private void onChanged() {
			String entityID = field.getText();
			entityEntry = EntryListSpawn.getEntityEntry(entityID);
			_owningScreen.labelTitleEntityName.setText(
				EntryListSpawn.getEntityEntryName(entityEntry, entityID));
		}
		
		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			Severity severity = Status.getSeverity(getStatus());
			field.setTextAndBorderColor(severity.foregroundColor,
				(severity == Severity.WARN) || (severity == Severity.ERROR));
			super.draw(mouseX, mouseY, partialTicks);
		}
		
		// IConfigEntry implementation
		
		@Override
		public boolean isChanged() { return false; }
		@Override
		public boolean isDefault() { return true; }
		
		@Override
		public void undoChanges() {  }
		@Override
		public void setToDefault() {  }
		
		@Override
		public ChangeRequiredAction applyChanges()
			{ return ChangeRequiredAction.None; }
		
	}
	
	public static class EntryListBackpack extends BaseEntryList<BackpackEntry> {
		
		public static final int CHANCE_WIDTH = 42;
		
		public EntryListBackpack(List<BackpackEntry> previousValue, List<BackpackEntry> defaultValue) {
			super(300, previousValue, defaultValue);
			
			GuiLayout entryHeader = new GuiLayout(Direction.HORIZONTAL);
			entryHeader.setFillHorizontal();
			entryHeader.setHeight(ENTRY_HEIGHT);
			entryHeader.setPaddingHorizontal(MoveButton.WIDTH, ENTRY_HEIGHT);
			
				entryHeader.addFixed(createLabel("spawn.chance"), CHANCE_WIDTH);
				entryHeader.addWeighted(createLabel("spawn.backpack"));
				entryHeader.addWeighted(createLabel("spawn.lootTable"));
			
			insertFixed(0, entryHeader);
		}
		private static GuiLabel createLabel(String key) {
			GuiLabel label = new GuiLabel(translate(key), TextAlign.CENTER);
			label.setTooltip(translateTooltip(key));
			label.setBottom(2);
			return label;
		}
		
		@Override
		protected BaseEntryList.Entry<BackpackEntry> createListEntry()
			{ return new Entry(this); }
		
		@Override
		public ChangeRequiredAction applyChanges()
			{ return ChangeRequiredAction.None; }
		
		
		public static class Entry extends BaseEntryList.Entry<BackpackEntry> {
			
			public final GuiField fieldChance;
			public final GuiField fieldBackpack;
			public final GuiField fieldLootTable;
			
			private boolean _backpackValid = false;
			
			public Entry(EntryListBackpack owningList) {
				super(owningList);
				
				fieldChance = new GuiField(CHANCE_WIDTH, ENTRY_HEIGHT);
				fieldChance.setMaxLength(5);
				fieldChance.setCharValidator(Character::isDigit);
				fieldBackpack = new GuiField(0, ENTRY_HEIGHT);
				fieldBackpack.setChangedAction(this::onBackpackChanged);
				fieldLootTable = new GuiField(0, ENTRY_HEIGHT);
				
				addFixed(buttonMove);
				addFixed(fieldChance);
				addWeighted(fieldBackpack);
				addWeighted(fieldLootTable);
				addFixed(buttonRemove);
				
				setValue(BackpackEntry.DEFAULT);
				onBackpackChanged();
			}
			
			@Override
			public BackpackEntry getValue() {
				return new BackpackEntry(
					!fieldChance.getText().isEmpty() ? Integer.parseInt(fieldChance.getText()) : 0,
					fieldBackpack.getText(), fieldLootTable.getText());
			}
			@Override
			public void setValue(BackpackEntry value) {
				fieldChance.setText(Integer.toString(value.chance));
				fieldBackpack.setText(value.backpack);
				fieldLootTable.setText(value.lootTable);
			}
			
			private void onBackpackChanged() {
				_backpackValid = ForgeRegistries.ITEMS.containsKey(
					new ResourceLocation(fieldBackpack.getText()));
			}
			@Override
			public List<Status> getStatus() {
				List<Status> status = new ArrayList<Status>();
				if (fieldChance.getText().isEmpty()) status.add(Status.EMPTY);
				if (fieldBackpack.getText().isEmpty()) status.add(Status.EMPTY);
				else if (!_backpackValid) status.add(Status.WARN());
				return status;
			}
			
			@Override
			public void draw(int mouseX, int mouseY, float partialTicks) {
				// This is ugly but I'm too lazy to make it not so.
				fieldChance.setTextAndBorderColor(Severity.ERROR.foregroundColor, fieldChance.getText().isEmpty());
				if (fieldBackpack.getText().isEmpty()) fieldBackpack.setTextAndBorderColor(Severity.ERROR.foregroundColor, true);
				else if (!_backpackValid) fieldBackpack.setTextAndBorderColor(Severity.WARN.foregroundColor, true);
				else fieldBackpack.setTextAndBorderColor(-1, false);
				
				super.draw(mouseX, mouseY, partialTicks);
			}
			
		}
		
	}
	
	private static String translate(String key)
		{ return I18n.format("config." + WearableBackpacks.MOD_ID + "." + key); }
	private static List<String> translateTooltip(String key)
		{ return Arrays.asList(translate(key + ".tooltip").split("\\n")); }
	
}
