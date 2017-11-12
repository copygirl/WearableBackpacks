package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackRegistry.BackpackEntityEntry;
import net.mcft.copy.backpacks.api.BackpackRegistry.BackpackEntry;
import net.mcft.copy.backpacks.api.BackpackRegistry.ColorRange;
import net.mcft.copy.backpacks.api.BackpackRegistry.RenderOptions;
import net.mcft.copy.backpacks.client.gui.Direction;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.GuiLabel;
import net.mcft.copy.backpacks.client.gui.GuiLayout;
import net.mcft.copy.backpacks.client.gui.GuiLabel.TextAlign;
import net.mcft.copy.backpacks.client.gui.config.BaseConfigScreen;
import net.mcft.copy.backpacks.client.gui.config.BaseEntry;
import net.mcft.copy.backpacks.client.gui.config.BaseEntryList;
import net.mcft.copy.backpacks.client.gui.config.EntryCategory;
import net.mcft.copy.backpacks.client.gui.config.EntryValueField;
import net.mcft.copy.backpacks.client.gui.config.IConfigEntry;
import net.mcft.copy.backpacks.client.gui.config.IConfigValue;
import net.mcft.copy.backpacks.client.gui.config.BaseEntryList.Entry.MoveButton;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.client.gui.control.GuiButtonIcon;
import net.mcft.copy.backpacks.client.gui.control.GuiField;
import net.mcft.copy.backpacks.client.gui.control.GuiButtonIcon.Icon;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;
import net.mcft.copy.backpacks.config.Status.Severity;
import net.mcft.copy.backpacks.item.ItemBackpack;
import net.mcft.copy.backpacks.misc.util.NbtUtils;

@SideOnly(Side.CLIENT)
public class ScreenEntityEntry extends BaseConfigScreen {
	
	public static final long UPDATE_TIMESPAN = 1600;
	
	private final EntryListEntities _owningList;
	private final Optional<EntryListEntities.Entry> _entry;
	
	public final EntryEntityID entryEntityID;
	public final EntryButtonRenderOptions entryRenderOptions;
	public final EntryListBackpack listBackpack;
	public final GuiButton buttonCancel;
	public final boolean isDefault;
	
	public ScreenEntityEntry(EntryListEntities owningList, Optional<EntryListEntities.Entry> entry) {
		super(GuiElementBase.getCurrentScreen(), Stream.concat(
				((BaseConfigScreen)GuiElementBase.getCurrentScreen()).getTitleLines().stream().skip(1),
				Stream.of("< this is where the entity name will go >")
			).toArray(String[]::new));
		_owningList = owningList;
		_entry      = entry;
		
		Optional<BackpackEntityEntry> backpackEntry = entry.map(EntryListEntities.Entry::getValue);
		isDefault = backpackEntry.map(e -> e.isDefault).orElse(false);
		List<BackpackEntry> entries  = backpackEntry.map(BackpackEntityEntry::getEntries).orElseGet(Collections::emptyList);
		List<BackpackEntry> defaults = entries.stream().filter(e -> e.isDefault).collect(Collectors.toList());
		
		// Content
		entryEntityID = new EntryEntityID(this);
		
		entryRenderOptions = new EntryButtonRenderOptions(entryEntityID,
			_entry.map(e -> e.getValue().renderOptions), this::getBackpackEntries);
		entryRenderOptions.setLabelAndTooltip("entity.renderOptions");
		listBackpack = new EntryListBackpack(entries, defaults);
		
		listEntries.addFixed(entryEntityID);
		listEntries.addFixed(entryRenderOptions);
		listEntries.addFixed(listBackpack);
		
		if (isDefault) {
			entryEntityID.setEnabled(false);
			entryRenderOptions.setEnabled(false);
		}
		
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
	
	private List<BackpackEntry> getBackpackEntries() {
		List<BackpackEntry> backpacks = listBackpack.getEntries().map(e -> e.getValue())
			.filter(e -> (Item.getByNameOrId(e.backpack) != null))
			.collect(Collectors.toCollection(ArrayList::new));
		if (backpacks.isEmpty()) backpacks.add(BackpackEntry.DEFAULT);
		return backpacks;
	}
	
	@Override
	protected void doneClicked() {
		BackpackEntityEntry value = new BackpackEntityEntry(
			entryEntityID.getValue().get(),
			entryRenderOptions.getValue().get(),
			listBackpack.getValue(), isDefault);
		
		_entry.orElseGet(() -> (EntryListEntities.Entry)_owningList.addEntry()).setValue(value);
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
		
		private final ScreenEntityEntry _owningScreen;
		
		public Optional<EntityEntry> entityEntry;
		
		public EntryEntityID(ScreenEntityEntry owningScreen) {
			super(new EntryValueField.Text(), owningScreen._entry
				.map(e -> e.getValue().entityID), Optional.empty());
			setLabelAndTooltip("entity.entityID");
			((EntryValueField.Text)control).setChangedAction(this::onChanged);
			_owningScreen = owningScreen;
			onChanged();
		}
		
		@Override
		public List<Status> getStatus() {
			return entityEntry.isPresent()    ? Collections.emptyList()
			     : getValue().get().isEmpty() ? Arrays.asList(Status.EMPTY)
			                                  : Arrays.asList(EntryListEntities.STATUS_NOT_FOUND);
		}
		
		private void onChanged() {
			String entityID = getValue().get();
			entityEntry = EntryListEntities.getEntityEntry(entityID);
			_owningScreen.setTitleLine(_owningScreen.getTitleLineCount() - 1,
			                           EntryListEntities.getEntityEntryName(entityEntry, entityID));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public static class EntryButtonRenderOptions extends BaseEntry implements IConfigValue<RenderOptions> {
		
		private final Optional<RenderOptions> _previousValue;
		private RenderOptions _value;
		
		public EntryButtonRenderOptions(EntryEntityID entityID, Optional<RenderOptions> value,
		                                Supplier<List<BackpackEntry>> backpacks) {
			_previousValue = value;
			_value = value.orElse(RenderOptions.DEFAULT);
			
			GuiButton button = new GuiButton(EntryCategory.BUTTON_WIDTH);
			String languageKey = "config." + WearableBackpacks.MOD_ID + ".entity.renderOptions";
			button.setText(I18n.format(languageKey));
			button.setTooltip(formatTooltip(languageKey, languageKey + ".tooltip", null, null));
			button.setAction(() -> display(new ScreenRenderOptions(this, backpacks.get(),
				entityID.entityEntry.map(e -> (Class<? extends EntityLivingBase>)e.getEntityClass()).orElse(null))));
			
			setSpacing(6);
			addWeighted(button);
			addFixed(buttonUndo);
		}
		
		@Override
		public List<Status> getStatus() {
			List<Status> status = new ArrayList<Status>();
			if (!getValue().isPresent()) status.add(Status.INVALID);
			return status;
		}
		
		// IConfigValue implementation
		
		@Override
		public Optional<RenderOptions> getValue() { return Optional.of(_value); }
		@Override
		public void setValue(RenderOptions value) { _value = value; }
		
		// IConfigEntry implementation
		
		@Override
		public boolean isChanged() { return !_previousValue.equals(getValue()); }
		@Override
		public boolean isDefault() { return false; }
		
		@Override
		public void undoChanges() { _previousValue.ifPresent(this::setValue); }
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
			entryHeader.setHeight(DEFAULT_ENTRY_HEIGHT);
			entryHeader.setPaddingHorizontal(MoveButton.WIDTH - 8, (DEFAULT_ENTRY_HEIGHT + 2) * 2);
			
				entryHeader.setSpacing(9, 2);
				entryHeader.addFixed(createLabel("entity.chance"), CHANCE_WIDTH + 20);
				entryHeader.addWeighted(createLabel("entity.backpack"));
				entryHeader.addWeighted(createLabel("entity.lootTable"));
			
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
			
			private static final String HEX_DIGITS = "0123456789ABCDEF";
			
			private static final Icon ICON_COLOR_ON =
				new Icon(BaseEntry.TEXTURE_CONFIG_ICONS, 0, 16, 16, 16);
			private static final Icon ICON_COLOR_OFF =
				new Icon(BaseEntry.TEXTURE_CONFIG_ICONS, 16, 16, 16, 16);
			private static final Icon ICON_COLOR_PICK =
				new Icon(BaseEntry.TEXTURE_CONFIG_ICONS, 32, 16, 16, 16);
			
			public String id;
			private boolean _isDefault;
			private long _lastUpdateTime = Long.MIN_VALUE;
			private ItemStack _backpack;
			
			public final GuiField fieldChance;
			public final GuiItem itemBackpack;
			public final GuiField fieldBackpack;
			public final GuiField fieldLootTable;
			public final GuiButtonIcon switchColorPick;
			
			public final GuiButtonIcon switchColorOn;
			public final GuiLabel labelColor;
			public final GuiField fieldColorMin;
			public final GuiLabel labelColorCenter;
			public final GuiField fieldColorMax;
			
			
			public Entry(EntryListBackpack owningList) {
				super(owningList);
				
				int h = DEFAULT_ENTRY_HEIGHT;
				fieldChance = new GuiField(CHANCE_WIDTH, h);
				fieldChance.setMaxLength(5);
				fieldChance.setCharValidator(Character::isDigit);
				itemBackpack = new GuiItem(h, h);
				fieldBackpack = new GuiField(0, h);
				fieldBackpack.setChangedAction(this::updateBackpackItem);
				fieldLootTable = new GuiField(0, h);
				switchColorPick = new GuiButtonIcon(h, h, ICON_COLOR_PICK);
				switchColorPick.setSwitch();
				switchColorPick.setAction(this::onColorPickPressed);
				
				switchColorOn = new GuiButtonIcon(h, h, ICON_COLOR_ON);
				switchColorOn.setSwitch();
				switchColorOn.setAction(this::onColorOnPressed);
				labelColor = new GuiLabel(" Min/max:");
				labelColor.setCenteredVertical();
				fieldColorMin = new GuiField(50, h);
				fieldColorMin.setCharValidator(c -> (HEX_DIGITS.indexOf(Character.toUpperCase(c.charValue())) != -1));
				labelColorCenter = new GuiLabel("/");
				labelColorCenter.setCenteredVertical();
				fieldColorMax = new GuiField(50, h);
				fieldColorMax.setCharValidator(c -> (HEX_DIGITS.indexOf(Character.toUpperCase(c.charValue())) != -1));
				
				setSpacing(2, 2, -1, 2);
				addFixed(buttonMove);
				addFixed(fieldChance);
				addFixed(itemBackpack);
				addWeighted(fieldBackpack);
				addWeighted(fieldLootTable);
				addFixed(switchColorPick);
				addFixed(buttonRemove);
				
				setValue(BackpackEntry.DEFAULT);
			}
			
			@Override
			public BackpackEntry getValue() {
				ColorRange colorRange = switchColorOn.isSwitchOn()
					? new ColorRange(
						!fieldColorMin.getText().isEmpty() ? Integer.parseInt(fieldColorMin.getText(), 16) : 0,
						!fieldColorMax.getText().isEmpty() ? Integer.parseInt(fieldColorMax.getText(), 16) : 0)
					: null;
				return new BackpackEntry(id,
					fieldBackpack.getText(),
					!fieldChance.getText().isEmpty() ? Integer.parseInt(fieldChance.getText()) : 0,
					fieldLootTable.getText(), colorRange, _isDefault);
			}
			@Override
			public void setValue(BackpackEntry value) {
				id = value.id;
				_isDefault = value.isDefault;
				
				fieldChance.setText(Integer.toString(value.chance));
				fieldBackpack.setText(value.backpack);
				fieldLootTable.setText(value.lootTable);
				
				boolean on = (value.colorRange != null);
				switchColorOn.setSwitch(on);
				switchColorOn.setIcon(on ? ICON_COLOR_ON : ICON_COLOR_OFF);
				fieldColorMin.setEnabled(on);
				fieldColorMax.setEnabled(on);
				fieldColorMin.setText(String.format("%06X",
					on ? value.colorRange.min : ColorRange.DEFAULT.min));
				fieldColorMax.setText(String.format("%06X",
					on ? value.colorRange.max : ColorRange.DEFAULT.max));
				
				Arrays.asList(buttonMove, fieldBackpack, fieldLootTable, switchColorPick, buttonRemove)
					.forEach(e -> e.setEnabled(!_isDefault));
				updateBackpackItem();
			}
			
			private void updateBackpackItem() {
				Item item = Item.getByNameOrId(fieldBackpack.getText());
				_backpack = (item != null) ? new ItemStack(item) : ItemStack.EMPTY;
				if (switchColorOn.isSwitchOn()) {
					boolean isValid = (fieldColorMin.getText().length() == 6) &&
					                  (fieldColorMax.getText().length() == 6);
					int minColor = isValid ? Integer.parseInt(fieldColorMin.getText(), 16) : 0xFF0000;
					int maxColor = isValid ? Integer.parseInt(fieldColorMax.getText(), 16) : 0xFF0000;
					ColorRange range = new ColorRange(minColor, maxColor);
					int color        = range.isValid() ? range.getRandom() : 0xFF0000;
					NbtUtils.set(_backpack, color, "display", "color");
				}
				itemBackpack.setStack(_backpack);
			}
			
			private void onColorPickPressed() {
				if (switchColorPick.isSwitchOn()) {
					remove(fieldBackpack);
					remove(fieldLootTable);
					setSpacing(2);
					insertFixed(3, switchColorOn);
					insertFixed(4, labelColor);
					insertWeighted(5, fieldColorMin);
					insertFixed(6, labelColorCenter);
					insertWeighted(7, fieldColorMax);
				} else {
					remove(switchColorOn);
					remove(labelColor);
					remove(fieldColorMin);
					remove(labelColorCenter);
					remove(fieldColorMax);
					setSpacing(2, 2, -1, 2);
					insertWeighted(3, fieldBackpack);
					insertWeighted(4, fieldLootTable);
				}
			}
			
			private void onColorOnPressed() {
				boolean on = switchColorOn.isSwitchOn();
				switchColorOn.setIcon(on ? ICON_COLOR_ON : ICON_COLOR_OFF);
				fieldColorMin.setEnabled(on);
				fieldColorMax.setEnabled(on);
				updateBackpackItem();
			}
			
			@Override
			public List<Status> getStatus() {
				List<Status> status = new ArrayList<Status>();
				if (fieldChance.getText().isEmpty()) status.add(Status.EMPTY);
				if (fieldBackpack.getText().isEmpty()) status.add(Status.EMPTY);
				else if (itemBackpack.getStack().isEmpty()) status.add(Status.WARN());
				else if (!(itemBackpack.getStack().getItem() instanceof ItemBackpack)) status.add(Status.INVALID);
				else if (switchColorOn.isSwitchOn()) {
					if ((fieldColorMin.getText().length() != 6) ||
					    (fieldColorMax.getText().length() != 6) ||
					    !new ColorRange(Integer.parseInt(fieldColorMin.getText(), 16),
					                    Integer.parseInt(fieldColorMax.getText(), 16)).isValid())
						status.add(Status.INVALID);
				}
				return status;
			}
			
			@Override
			public void draw(int mouseX, int mouseY, float partialTicks) {
				long currentTime = Minecraft.getSystemTime();
				if (currentTime > _lastUpdateTime + UPDATE_TIMESPAN) {
					updateBackpackItem();
					_lastUpdateTime = currentTime;
				}
				
				setTextAndBorderColorIf(fieldChance.getText().isEmpty(), fieldChance, Severity.ERROR.foregroundColor);
				
				int backpackColor = fieldBackpack.getText().isEmpty() ? Severity.ERROR.foregroundColor
					: itemBackpack.getStack().isEmpty() ? Severity.WARN.foregroundColor
					: !(itemBackpack.getStack().getItem() instanceof ItemBackpack) ? Severity.ERROR.foregroundColor
					: -1;
				setTextAndBorderColorIf((backpackColor != -1), fieldBackpack, backpackColor);
				if (backpackColor != -1) itemBackpack.setBorderColor(backpackColor);
				else itemBackpack.resetBorderColor();
				
				boolean colorOn = switchColorOn.isSwitchOn();
				boolean valid = !colorOn ||
					(fieldColorMin.getText().length() == 6) &&
					(fieldColorMax.getText().length() == 6) &&
					new ColorRange(Integer.parseInt(fieldColorMin.getText(), 16),
					               Integer.parseInt(fieldColorMax.getText(), 16)).isValid();
				setTextAndBorderColorIf(!valid, fieldColorMin, Severity.ERROR.foregroundColor);
				setTextAndBorderColorIf(!valid, fieldColorMax, Severity.ERROR.foregroundColor);
				int color = !colorOn ? GuiUtils.getColorCode('8', true)
				             : valid ? GuiUtils.getColorCode('7', true)
				                     : Severity.ERROR.foregroundColor;
				labelColor.setColor(color);
				labelColorCenter.setColor(color);
				
				super.draw(mouseX, mouseY, partialTicks);
			}
			
			private static void setTextAndBorderColorIf(boolean condition, GuiField field, int color) {
				if (condition) { field.setTextColor(color); field.setBorderColor(color); }
				else { field.resetTextColor(); field.resetBorderColor(); }
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
					: Collections.emptyList();
			}
			
			public void setBackgroundColor(int value) { _colorBackground = value; }
			public void resetBackgroundColor() { setBackgroundColor(0xFF333333); }
			
			public void setBorderColor(int value) { _colorBorder = value; }
			public void resetBorderColor() { setBorderColor(GuiField.COLOR_BORDER_DEFAULT); }
			
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
