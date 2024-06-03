package ultimateclaim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import ultimateclaim.events.BlockPlaceCallBack;

@Mixin(BlockItem.class)
public class BlockItemMixin {
	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void placeBlock(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		ActionResult result = BlockPlaceCallBack.BEFORE.invoker().beforeBlockPlace(context.getWorld(), context.getPlayer(), context.getBlockPos(), state, context.getWorld().getBlockEntity(context.getBlockPos()));

		if (result != ActionResult.PASS) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;Lnet/minecraft/block/BlockState;)Z", at = @At(value = "TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void blockPlaced(ItemPlacementContext context, BlockState state, CallbackInfoReturnable<Boolean> cir) {
		BlockPlaceCallBack.AFTER.invoker().afterBlockPlace(context.getWorld(), context.getPlayer(), context.getBlockPos(), state, context.getWorld().getBlockEntity(context.getBlockPos()));
	}
}