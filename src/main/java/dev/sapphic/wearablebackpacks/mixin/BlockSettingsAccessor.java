package dev.sapphic.wearablebackpacks.mixin;

import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractBlock.Settings.class)
public interface BlockSettingsAccessor {
  @Invoker
  AbstractBlock.Settings callSounds(final BlockSoundGroup group);
}
