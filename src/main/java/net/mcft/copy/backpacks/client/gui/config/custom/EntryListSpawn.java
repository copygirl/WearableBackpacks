package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.GuiContainer;
import net.mcft.copy.backpacks.client.gui.GuiLabel;
import net.mcft.copy.backpacks.client.gui.config.BaseEntryList;
import net.mcft.copy.backpacks.client.gui.control.GuiButton;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;
import net.mcft.copy.backpacks.config.Status.Severity;
import net.mcft.copy.backpacks.config.custom.SettingListSpawn;
import net.mcft.copy.backpacks.config.custom.SettingListSpawn.BackpackEntityEntry;

@SideOnly(Side.CLIENT)
public class EntryListSpawn extends BaseEntryList<BackpackEntityEntry> {
	
	public static final Status STATUS_NOT_FOUND = Status.WARN("spawn", "entityNotFound");
	
	private final SettingListSpawn _setting;
	
	public EntryListSpawn(SettingListSpawn setting) {
		super(240, setting.get(), setting.getDefault());
		_setting = setting;
		
		GuiContainer entryLabel = new GuiContainer();
		entryLabel.setFillHorizontal();
		entryLabel.setHeight(ENTRY_HEIGHT);
			GuiLabel label = new GuiLabel(I18n.format(
				"config." + WearableBackpacks.MOD_ID + ".spawn.entries"));
			label.setCenteredHorizontal();
			label.setBottom(2);
			entryLabel.add(label);
		insertFixed(0, entryLabel);
	}
	
	@Override
	protected BaseEntryList.Entry<BackpackEntityEntry> createListEntry()
		{ return new Entry(this); }
	
	@Override
	protected void addButtonPressed()
		{ display(new ListEntryEntityScreen(this, Optional.empty())); }
	
	
	public static Optional<EntityEntry> getEntityEntry(String entityID) {
		return Optional.ofNullable(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityID)))
			.filter(entry -> EntityLivingBase.class.isAssignableFrom(entry.getEntityClass()));
	}
	public static String getEntityEntryName(Optional<EntityEntry> entry, String entityID)
		{ return entry.map(EntityEntry::getName).map(s -> "[" + s + "]").orElse(entityID); }
	
	
	public static class Entry extends BaseEntryList.Entry<BackpackEntityEntry> {
		
		public final GuiButton buttonEdit;
		private BackpackEntityEntry _value;
		private boolean _knownEntity;
		
		public Entry(EntryListSpawn owningList) {
			super(owningList);
			
			buttonEdit = new GuiButton(0, ENTRY_HEIGHT);
			buttonEdit.setAction(() -> { display(new ListEntryEntityScreen(owningList, Optional.of(this))); });
			
			addFixed(buttonMove);
			addWeighted(buttonEdit);
			addFixed(buttonRemove);
		}
		
		@Override
		public BackpackEntityEntry getValue() { return _value; }
		@Override
		public void setValue(BackpackEntityEntry value) {
			_value = value;
			Optional<EntityEntry> entry = getEntityEntry(_value.entityID);
			_knownEntity = entry.isPresent();
			buttonEdit.setText(getEntityEntryName(entry, _value.entityID));
		}
		
		@Override
		public List<Status> getStatus() {
			return _knownEntity ? Collections.emptyList()
			                    : Arrays.asList(STATUS_NOT_FOUND);
		}
		
		@Override
		public void draw(int mouseX, int mouseY, float partialTicks) {
			if (!_knownEntity) buttonEdit.setTextColor(Severity.WARN.foregroundColor);
			else buttonEdit.unsetTextColor();
			
			super.draw(mouseX, mouseY, partialTicks);
		}
		
	}
	
	
	// IConfigEntry implementation
	
	@Override
	public ChangeRequiredAction applyChanges() {
		if (!isChanged()) return ChangeRequiredAction.None;
		_setting.set(getValue());
		if (!_setting.requiresMinecraftRestart()) _setting.update();
		return _setting.getChangeRequiredAction();
	}
	
}
