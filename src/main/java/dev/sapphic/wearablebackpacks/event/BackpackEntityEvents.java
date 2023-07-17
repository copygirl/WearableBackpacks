package dev.sapphic.wearablebackpacks.event;

import dev.sapphic.wearablebackpacks.client.BackpackWearer;
import dev.sapphic.wearablebackpacks.inventory.WornBackpack;
import dev.sapphic.wearablebackpacks.item.BackpackItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public final class BackpackEntityEvents implements ModInitializer {
    public static final double MIN_REQUIRED_DISTANCE = 1.8;
    public static final double ANGLE_BOUNDS = 110;

    private static ActionResult tryPlaceBackpack(
            final PlayerEntity player, final World world, final Hand hand, final BlockHitResult hit
    ) {
        if (player.isSneaking() && player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty()) {
            final ItemStack stack = player.getEquippedStack(EquipmentSlot.CHEST);
            if (stack.getItem() instanceof BackpackItem) {
                final ItemPlacementContext context = new ItemPlacementContext(player, hand, stack, hit);
                if (((BackpackItem) stack.getItem()).place(context).isAccepted()) {
                    if (!player.getAbilities().creativeMode) {
                        stack.decrement(1);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }

    private static ActionResult tryOpenBackpack(
            final PlayerEntity self, final World world, final Hand hand, final Entity wearer,
            final @Nullable EntityHitResult hit
    ) {
        if (!(wearer instanceof LivingEntity)) {
            return ActionResult.PASS;
        }
        final ItemStack stack = ((LivingEntity) wearer).getEquippedStack(EquipmentSlot.CHEST);
        if ((stack.getItem() instanceof BackpackItem) && canOpenBackpack(self, (LivingEntity) wearer)) {
            if (world.isClient) {
                final float pitch = (self.world.random.nextFloat() * 0.1F) + 0.9F;
                self.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.5F, pitch);
            } else {
                self.openHandledScreen(WornBackpack.of((LivingEntity) wearer, stack));
                BackpackWearer.getBackpackState((LivingEntity) wearer).opened();
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    private static boolean canOpenBackpack(final PlayerEntity player, final LivingEntity entity) {
        if (player.distanceTo(entity) <= MIN_REQUIRED_DISTANCE) {
            final double theta = StrictMath.atan2(entity.getZ() - player.getZ(), entity.getX() - player.getX());
            //noinspection OverlyComplexArithmeticExpression
            final double angle = ((((Math.toDegrees(theta) - entity.bodyYaw - 90) % 360) + 540) % 360) - 180;
            return Math.abs(angle) < (ANGLE_BOUNDS / 2);
        }
        return false;
    }

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register(BackpackEntityEvents::tryPlaceBackpack);
        UseEntityCallback.EVENT.register(BackpackEntityEvents::tryOpenBackpack);
    }
}
