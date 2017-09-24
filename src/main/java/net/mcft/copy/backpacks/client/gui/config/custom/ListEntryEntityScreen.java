package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.common.registry.EntityEntry;
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
import net.mcft.copy.backpacks.client.gui.config.EntryValueField;
import net.mcft.copy.backpacks.client.gui.config.IConfigEntry;
import net.mcft.copy.backpacks.client.gui.config.BaseEntryList.Entry.MoveButton;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.client.gui.control.GuiField;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;
import net.mcft.copy.backpacks.config.Status.Severity;
import net.mcft.copy.backpacks.config.custom.SettingListSpawn;
import net.mcft.copy.backpacks.config.custom.SettingListSpawn.BackpackEntityEntry;
import net.mcft.copy.backpacks.config.custom.SettingListSpawn.BackpackEntry;
import net.mcft.copy.backpacks.item.ItemBackpack;

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
		List<BackpackEntry> defaults = entry.map(EntryListSpawn.Entry::getValue)
			.flatMap(e -> SettingListSpawn.getDefaultValue().stream()
				.filter(f -> f.entityID.equals(e.entityID)).findAny())
			.map(e -> e.entries)
			.orElseGet(Collections::emptyList);
		
		// Title
		labelTitleEntityName = new GuiLabel("");
		labelTitleEntityName.setCenteredHorizontal();
		layoutTitle.addFixed(labelTitleEntityName);
		
		// Content
		entryEntityID = new EntryEntityID(this);
		listBackpack  = new EntryListBackpack(entries, defaults);
		
		listEntries.addFixed(entryEntityID);
		listEntries.addFixed(listBackpack);
		
		// Buttons
		buttonCancel = new GuiButton(I18n.format("config." + WearableBackpacks.MOD_ID + ".gui.cancel"));
		if (buttonCancel.getWidth() < 100) buttonCancel.setWidth(100);
		buttonCancel.setAction(this::cancelClicked);
		
		layoutButtons.addFixed(buttonDone);
		// If editing an existing entry ...
		if (entry.isPresent()) {
			layoutButtons.addFixed(buttonUndo); // Add "Undo Changes" button.
			// If defaults are available, add "Set to Default" button.
			if (!defaults.isEmpty()) layoutButtons.addFixed(buttonReset);
		// ... otherwise add just "Cancel" button.
		} else layoutButtons.addFixed(buttonCancel);
	}
	
	@Override
	protected void doneClicked() {
		BackpackEntityEntry value = new BackpackEntityEntry();
		value.entityID = entryEntityID.getValue().get();
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
	
	
	public static class EntryEntityID extends BaseEntry.Value<String> {
		
		private final ListEntryEntityScreen _owningScreen;
		
		public final String previousValue;
		public Optional<EntityEntry> entityEntry;
		
		public EntryEntityID(ListEntryEntityScreen owningScreen) {
			super(new EntryValueField.Text());
			setLabelAndTooltip("spawn.entityID");
			((EntryValueField.Text)control).setChangedAction(this::onChanged);
			
			_owningScreen = owningScreen;
			previousValue = _owningScreen._entry.map(e -> e.getValue().entityID).orElse("");
			setValue(previousValue);
			
			onChanged();
		}
		
		@Override
		public List<Status> getStatus() {
			return entityEntry.isPresent()    ? Collections.emptyList()
			     : getValue().get().isEmpty() ? Arrays.asList(Status.EMPTY)
			                                  : Arrays.asList(EntryListSpawn.STATUS_NOT_FOUND);
		}
		
		private void onChanged() {
			String entityID = getValue().get();
			entityEntry = EntryListSpawn.getEntityEntry(entityID);
			_owningScreen.labelTitleEntityName.setText(
				EntryListSpawn.getEntityEntryName(entityEntry, entityID));
		}
		
		// IConfigEntry implementation
		
		@Override
		public boolean isChanged() { return !previousValue.equals(getValue().get()); }
		@Override
		public void undoChanges() { setValue(previousValue); }
		
	}
	
	public static class EntryListBackpack extends BaseEntryList<BackpackEntry> {
		
		public static final int CHANCE_WIDTH = 42;
		
		public EntryListBackpack(List<BackpackEntry> previousValue, List<BackpackEntry> defaultValue) {
			super(300, previousValue, defaultValue);
			
			GuiLayout entryHeader = new GuiLayout(Direction.HORIZONTAL);
			entryHeader.setFillHorizontal();
			entryHeader.setHeight(DEFAULT_ENTRY_HEIGHT);
			entryHeader.setPaddingHorizontal(MoveButton.WIDTH - 8, DEFAULT_ENTRY_HEIGHT + 2);
			
				entryHeader.setSpacing(9, 2);
				entryHeader.addFixed(createLabel("spawn.chance"), CHANCE_WIDTH + 20);
				entryHeader.addWeighted(createLabel("spawn.backpack"));
				entryHeader.addWeighted(createLabel("spawn.lootTable"));
			
			insertFixed(0, entryHeader);
		}
		private static GuiLabel createLabel(String key) {
			key = "config." + WearableBackpacks.MOD_ID + "." + key;
			GuiLabel label = new GuiLabel(I18n.format(key), TextAlign.CENTER);
			label.setTooltip(Arrays.asList(I18n.format(key + ".tooltip").split("\\n")));
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
			
			public String id;
			public final GuiField fieldChance;
			public final GuiItem itemBackpack;
			public final GuiField fieldBackpack;
			public final GuiField fieldLootTable;
			
			public Entry(EntryListBackpack owningList) {
				super(owningList);
				
				fieldChance = new GuiField(CHANCE_WIDTH, DEFAULT_ENTRY_HEIGHT);
				fieldChance.setMaxLength(5);
				fieldChance.setCharValidator(Character::isDigit);
				itemBackpack = new GuiItem(18, 18);
				fieldBackpack = new GuiField(0, DEFAULT_ENTRY_HEIGHT);
				fieldBackpack.setChangedAction(this::onBackpackChanged);
				fieldLootTable = new GuiField(0, DEFAULT_ENTRY_HEIGHT);
				
				setSpacing(2, 2, -1, 2);
				addFixed(buttonMove);
				addFixed(fieldChance);
				addFixed(itemBackpack);
				addWeighted(fieldBackpack);
				addWeighted(fieldLootTable);
				addFixed(buttonRemove);
				
				setValue(BackpackEntry.DEFAULT);
				onBackpackChanged();
			}
			
			@Override
			public BackpackEntry getValue() {
				return new BackpackEntry(id,
					fieldBackpack.getText(),
					!fieldChance.getText().isEmpty() ? Integer.parseInt(fieldChance.getText()) : 0,
					fieldLootTable.getText());
			}
			@Override
			public void setValue(BackpackEntry value) {
				id = value.id;
				fieldChance.setText(Integer.toString(value.chance));
				fieldBackpack.setText(value.backpack);
				fieldLootTable.setText(value.lootTable);
				if (SettingListSpawn.getDefaultEntryIDs().contains(id)) {
					buttonMove.setEnabled(false);
					fieldBackpack.setEnabled(false);
					fieldLootTable.setEnabled(false);
					buttonRemove.setEnabled(false);
				}
				onBackpackChanged();
			}
			
			private void onBackpackChanged() {
				Item item = Item.getByNameOrId(fieldBackpack.getText());
				ItemStack backpack = (item != null) ? new ItemStack(item) : ItemStack.EMPTY;
				itemBackpack.setStack(backpack);
			}
			@Override
			public List<Status> getStatus() {
				List<Status> status = new ArrayList<Status>();
				if (fieldChance.getText().isEmpty()) status.add(Status.EMPTY);
				if (fieldBackpack.getText().isEmpty()) status.add(Status.EMPTY);
				else if (itemBackpack.getStack().isEmpty()) status.add(Status.WARN());
				else if (!(itemBackpack.getStack().getItem() instanceof ItemBackpack)) status.add(Status.ERROR());
				return status;
			}
			
			@Override
			public void draw(int mouseX, int mouseY, float partialTicks) {
				buttonMove.setEnabled(id == null);
				fieldBackpack.setEnabled(id == null);
				fieldLootTable.setEnabled(id == null);
				buttonRemove.setEnabled(id == null);
				
				// This is ugly but I'm too lazy to make it not so.
				fieldChance.setTextAndBorderColor(Severity.ERROR.foregroundColor, fieldChance.getText().isEmpty());
				int backpackColor = fieldBackpack.getText().isEmpty() ? Severity.ERROR.foregroundColor
					: itemBackpack.getStack().isEmpty() ? Severity.WARN.foregroundColor
					: !(itemBackpack.getStack().getItem() instanceof ItemBackpack) ? Severity.ERROR.foregroundColor
					: -1;
				fieldBackpack.setTextAndBorderColor(backpackColor, (backpackColor != -1));
				itemBackpack.setBorderColor((backpackColor != -1) ? backpackColor : GuiField.COLOR_BORDER_DEFAULT);
				
				super.draw(mouseX, mouseY, partialTicks);
			}
			
		}
		
		public static class GuiItem extends GuiElementBase {
			
			private ItemStack _stack = ItemStack.EMPTY;
			
			private int _colorBackground = 0xFF333333;
			private int _colorBorder     = GuiField.COLOR_BORDER_DEFAULT;
			
			public GuiItem(int width, int height)
				{ this(width, height, ItemStack.EMPTY); }
			public GuiItem(int width, int height, ItemStack stack)
				{ this(0, 0, width, height, stack); }
			public GuiItem(int x, int y, int width, int height, ItemStack stack) {
				setPosition(x, y);
				setSize(width, height);
				setStack(stack);
				setTooltip(0, Collections.emptyList()); // Just set delay.
			}
			
			public ItemStack getStack() { return _stack; }
			public void setStack(ItemStack value) { _stack = value; }
			
			@Override
			public List<String> getTooltip() {
				return !getStack().isEmpty()
					? getContext().getScreen().getItemToolTip(getStack())
					: null;
			}
			
			public void setBackgroundColor(int value) { _colorBackground = value; }
			public void setBorderColor(int value) { _colorBorder = value; }
			
			@Override
			public void draw(int mouseX, int mouseY, float partialTicks) {
				int w = getWidth();
				int h = getHeight();
				
				enableBlendAlphaStuffs();
				setRenderColorARGB(_colorBackground); drawRect(1, 1, w - 2, h - 2);
				setRenderColorARGB(_colorBorder);     drawOutline(0, 0, w, h);
				disableBlendAlphaStuffs();
				
				ItemStack stack = getStack();
				if (stack.isEmpty()) return;
				
				GlStateManager.enableDepth();
				GlStateManager.enableRescaleNormal();
				RenderHelper.enableGUIStandardItemLighting();
				getMC().getRenderItem().renderItemIntoGUI(stack, w / 2 - 8, h / 2 - 8);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.disableRescaleNormal();
				GlStateManager.disableDepth();
			}
			
		}
		
	}
	
}
