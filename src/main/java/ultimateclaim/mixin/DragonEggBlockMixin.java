package ultimateclaim.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import ultimateclaim.events.DragonEggTeleportCallBack;

@Mixin(DragonEggBlock.class)
public class DragonEggBlockMixin {
	
	@Inject(method = "teleport(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", at = @At("HEAD"), cancellable = true)
	private void teleport(BlockState state, World world, BlockPos pos, CallbackInfo info) {
	    WorldBorder worldBorder = world.getWorldBorder();
	    for (int i = 0; i < 1000; i++) {
	      BlockPos blockPos = pos.add(world.random.nextInt(16) - world.random.nextInt(16), world.random
	          .nextInt(8) - world.random.nextInt(8), world.random.nextInt(16) - world.random.nextInt(16));
	      if (world.getBlockState(blockPos).isAir() && worldBorder.contains(blockPos)) {
	    	  
	    	  ActionResult result = DragonEggTeleportCallBack.EVENT.invoker().onTeleport((ServerWorld) world, pos, blockPos);
	    	  if(result != ActionResult.PASS) {
	    		  info.cancel();
	    		  return;
	    	  }
	    	  
	          world.setBlockState(blockPos, state, 2);
	          world.removeBlock(pos, false);
	          info.cancel();
	      } 
	    } 
	  }

}
