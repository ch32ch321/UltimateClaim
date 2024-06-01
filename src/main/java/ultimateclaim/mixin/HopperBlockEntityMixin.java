package ultimateclaim.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;
import ultimateclaim.events.HopperTransferCallBack;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
	
	@Inject(method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
			at = @At("HEAD"), 
			cancellable = true)
	private static void onTransfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side, CallbackInfoReturnable<ItemStack> info) {
		if (from instanceof HopperBlockEntity) {
			HopperBlockEntity fromHopper = (HopperBlockEntity) from;
			if (to instanceof ChestBlockEntity) {
				ChestBlockEntity toChest = (ChestBlockEntity) to;
				ActionResult result = HopperTransferCallBack.TO_CHEST.invoker().toChestTransfer(toChest, fromHopper, stack, slot, side);
				if(result != ActionResult.PASS)
					info.setReturnValue(stack);
			}
		}else if (from instanceof ChestBlockEntity) {
			ChestBlockEntity fromChest = (ChestBlockEntity) from;
			if (to instanceof HopperBlockEntity) {
				HopperBlockEntity toHopper = (HopperBlockEntity) to;
				ActionResult result = HopperTransferCallBack.FROM_CHEST.invoker().fromChestTransfer(toHopper, fromChest, stack, slot, side);
				if(result != ActionResult.PASS)
					info.setReturnValue(stack);
			}
		}
	}

}
