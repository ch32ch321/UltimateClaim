package ultimateclaim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import ultimateclaim.events.FluidFlowCallBack;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {
	
	@Inject(method = "canFlow(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/FluidState;Lnet/minecraft/fluid/Fluid;)Z", at = @At("HEAD"), cancellable = true)
	protected void canFlow(BlockView world, BlockPos fluidPos, BlockState fluidBlockState, Direction flowDirection, BlockPos flowTo, BlockState flowToBlockState, FluidState fluidState, Fluid fluid, CallbackInfoReturnable<Boolean> info) {
		ActionResult result = FluidFlowCallBack.EVENT.invoker().fluidFlow((ServerWorld) world, fluidPos, flowTo);
		if(result != ActionResult.PASS)
			info.setReturnValue(false); 
	}
	
}
