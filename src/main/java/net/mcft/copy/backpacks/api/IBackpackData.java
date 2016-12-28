package net.mcft.copy.backpacks.api;

import net.minecraft.nbt.NBTBase;

import net.minecraftforge.common.util.INBTSerializable;

/** Interface for classes which contain backpack data that may be stored on
 *  an equipped or placed backpack, for example one containing its items. */
public interface IBackpackData extends INBTSerializable<NBTBase> {  }
