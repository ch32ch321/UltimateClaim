package ultimateclaim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import ultimateclaim.events.LeavesDecayCallBack;

@Mixin(LeavesBlock.class)
public class leaveBlockMixin {
	
	@Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
	private void randomTickDecay(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info) {
		ActionResult result = LeavesDecayCallBack.EVENT.invoker().declay(state, world);
		if(result != ActionResult.PASS)
			info.cancel();
	}

}
