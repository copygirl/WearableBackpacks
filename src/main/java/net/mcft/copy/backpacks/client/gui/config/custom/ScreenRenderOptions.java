package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.entity.EntityLivingBase;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.api.BackpackRegistry.RenderOptions;
import net.mcft.copy.backpacks.client.gui.GuiElementBase;
import net.mcft.copy.backpacks.client.gui.GuiEntityRender;
import net.mcft.copy.backpacks.client.gui.config.BaseConfigScreen;
import net.mcft.copy.backpacks.client.gui.config.BaseEntry;
import net.mcft.copy.backpacks.client.gui.config.EntryValueField;
import net.mcft.copy.backpacks.client.gui.config.EntryValueSlider;
import net.mcft.copy.backpacks.client.gui.config.IConfigEntry;
import net.mcft.copy.backpacks.client.gui.config.IConfigValue;

@SideOnly(Side.CLIENT)
public class ScreenRenderOptions extends BaseConfigScreen {
	
	private IConfigValue<RenderOptions> _element;
	
	public final BaseEntry.Value<List<Double>> entryTranslate;
	public final BaseEntry.Value<Double> entryRotate;
	public final BaseEntry.Value<Double> entryScale;
	public final GuiEntityRender entityRender;
	
	public ScreenRenderOptions(IConfigValue<RenderOptions> element, Class<? extends EntityLivingBase> entityClass) {
		super(GuiElementBase.getCurrentScreen(), Stream.concat(
				((BaseConfigScreen)GuiElementBase.getCurrentScreen()).getTitleLines().stream(),
				Stream.of("config." + WearableBackpacks.MOD_ID + ".spawn.renderOptions")
			).toArray(String[]::new));
		_element = element;
		
		entityRender = new GuiEntityRender(8, 8, 100, 200, entityClass);
		entityRender.setYaw(145.0F);
		scrollableContent.add(entityRender);
		
		RenderOptions value = element.getValue().get();
		entryTranslate = new BaseEntry.Value<>(new EntryValueMulti<Double>(3, EntryValueField.Decimal.class),
		                                       Arrays.asList(value.x, value.y, value.z), null);
		entryRotate    = new BaseEntry.Value<>(new EntryValueSlider.RangeDouble(-180, 180, 5), value.rotate, null);
		entryScale     = new BaseEntry.Value<>(new EntryValueField.Decimal(), value.scale, null);
		
		entryTranslate.setLabelAndTooltip("spawn.translate");
		entryRotate.setLabelAndTooltip("spawn.rotate");
		entryScale.setLabelAndTooltip("spawn.scale");
		
		listEntries.addFixed(entryTranslate);
		listEntries.addFixed(entryRotate);
		listEntries.addFixed(entryScale);
		
		layoutButtons.addFixed(buttonDone);
		layoutButtons.addFixed(buttonUndo);
	}
	
	@Override
	protected void doneClicked() {
		List<Double> translate = entryTranslate.getValue().get();
		_element.setValue(new RenderOptions(
			translate.get(0), translate.get(1), translate.get(2),
			entryRotate.getValue().get(), entryScale.getValue().get()));
		GuiElementBase.display(parentScreen);
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		buttonDone.setEnabled(listEntries.getEntries().allMatch(IConfigEntry::isValid));
		buttonUndo.setEnabled(listEntries.getEntries().anyMatch(IConfigEntry::isChanged));
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
}
