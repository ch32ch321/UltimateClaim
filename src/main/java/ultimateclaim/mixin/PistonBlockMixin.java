package ultimateclaim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import ultimateclaim.events.PistonMoveBlockCallBack;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {
	
	@Inject(method = "move(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Z", at = @At("HEAD"), cancellable = true)
	private void move(World world, BlockPos pos, Direction dir, boolean retract, CallbackInfoReturnable<Boolean> info) {
		BlockPos blockPos = pos.offset(dir);
		BlockState defaultBlock = Blocks.AIR.getDefaultState();
		if (!retract && world.getBlockState(blockPos).isOf(Blocks.PISTON_HEAD)) {
			defaultBlock = world.getBlockState(blockPos);
			world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 20);
		}
		PistonHandler pistonHandler = new PistonHandler(world, pos, dir, retract);
		if (!pistonHandler.calculatePush())
		      return;
		for (BlockPos pushed : pistonHandler.getMovedBlocks()) {
			System.out.println(world.getBlockState(pushed).getBlock().getName().getString());
		}
		ActionResult result = PistonMoveBlockCallBack.EVENT.invoker().onMove((ServerWorld) world, pos, dir, pistonHandler.getMovedBlocks());
		if(result != ActionResult.PASS) {
			if(!retract)
				if(!defaultBlock.isAir())
					world.setBlockState(blockPos, defaultBlock, 20);
			info.cancel();
		}
	}
}
