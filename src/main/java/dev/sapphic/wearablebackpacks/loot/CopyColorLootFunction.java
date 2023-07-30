package dev.sapphic.wearablebackpacks.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import dev.sapphic.wearablebackpacks.Backpack;
import dev.sapphic.wearablebackpacks.Backpacks;
import dev.sapphic.wearablebackpacks.block.entity.BackpackBlockEntity;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class CopyColorLootFunction extends ConditionalLootFunction {
public static final Identifier ID = new Identifier(Backpacks.ID, "copy_color");

CopyColorLootFunction(final LootCondition[] conditions) {
  super(conditions);
}

public static Builder<?> builder() {
  return builder(CopyColorLootFunction::new);
}

static JsonSerializer<CopyColorLootFunction> serializer() {
  return new ConditionalLootFunction.Serializer<CopyColorLootFunction>() {
    @Override
    public CopyColorLootFunction fromJson(
            final JsonObject json, final JsonDeserializationContext context, final LootCondition[] conditions
    ) {
      return new CopyColorLootFunction(conditions);
    }
  };
}

@Override
public LootFunctionType getType() {
  return BackpackLootFunctions.COPY_COLOR;
}

@Override
public Set<LootContextParameter<?>> getRequiredParameters() {
  return ImmutableSet.of(LootContextParameters.BLOCK_ENTITY);
}

@Override
protected ItemStack process(final ItemStack stack, final LootContext context) {
  if (stack.getItem() instanceof BackpackItem) {
    final @Nullable BlockEntity be = context.get(LootContextParameters.BLOCK_ENTITY);
    if ((be instanceof BackpackBlockEntity) && ((Backpack) be).hasColor()) {
      ((DyeableItem) stack.getItem()).setColor(stack, ((Backpack) be).getColor());
    }
  }
  return stack;
}
}
