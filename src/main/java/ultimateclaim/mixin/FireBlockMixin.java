package ultimateclaim.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.TntBlock;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import ultimateclaim.events.FireSpreadCallBack;

@Mixin(FireBlock.class)
public class FireBlockMixin {
	@Shadow
	private final Object2IntMap<Block> burnChances = (Object2IntMap<Block>)new Object2IntOpenHashMap();
	@Shadow
	private final Object2IntMap<Block> spreadChances = (Object2IntMap<Block>)new Object2IntOpenHashMap();
	
	@SuppressWarnings("unchecked")
	@Inject(at = @At("HEAD"), method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", cancellable = true)
	private void onSpreadFire(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo info) {
		FireBlock fire = (FireBlock) (Object) this;
		world.scheduleBlockTick(pos, fire, getFireTickDelay(world.random));
	    if (!world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK)) {
	    	info.cancel();
	    	return;
	    }
	    if (!state.canPlaceAt((WorldView)world, pos))
	    	world.removeBlock(pos, false);
	    BlockState blockState = world.getBlockState(pos.down());
	    boolean bl = blockState.isIn(world.getDimension().infiniburn());
	    int i = ((Integer)state.get((Property)fire.AGE)).intValue();
	    if (!bl && world.isRaining() && isRainingAround((World)world, pos) && random.nextFloat() < 0.2F + i * 0.03F) {
	      world.removeBlock(pos, false);
	      info.cancel();
	      return;
	    }
	    
	    ActionResult result = FireSpreadCallBack.Event.invoker().SpreadFire(world, pos);
	    if(result != ActionResult.PASS) {
	    	info.cancel();
	    	return;
	    }
	    
	    int j = Math.min(15, i + random.nextInt(3) / 2);
	    if (i != j) {
	      state = (BlockState)state.with((Property)fire.AGE, Integer.valueOf(j));
	      world.setBlockState(pos, state, 4);
	    } 
	    if (!bl) {
	      if (!areBlocksAroundFlammable((BlockView)world, pos)) {
	        BlockPos blockPos = pos.down();
	        if (!world.getBlockState(blockPos).isSideSolidFullSquare((BlockView)world, blockPos, Direction.UP) || i > 3)
	          world.removeBlock(pos, false); 
	        info.cancel();
	        return;
	      } 
	      if (i == 15 && random.nextInt(4) == 0 && !isFlammable(world.getBlockState(pos.down()))) {
	        world.removeBlock(pos, false);
	        info.cancel();
	        return;
	      } 
	    } 
	    boolean bl2 = world.getBiome(pos).isIn(BiomeTags.INCREASED_FIRE_BURNOUT);
	    int k = bl2 ? -50 : 0;
	    trySpreadingFire((World)world, pos.east(), 300 + k, random, i);
	    trySpreadingFire((World)world, pos.west(), 300 + k, random, i);
	    trySpreadingFire((World)world, pos.down(), 250 + k, random, i);
	    trySpreadingFire((World)world, pos.up(), 250 + k, random, i);
	    trySpreadingFire((World)world, pos.north(), 300 + k, random, i);
	    trySpreadingFire((World)world, pos.south(), 300 + k, random, i);
	    BlockPos.Mutable mutable;
	    int l;
	    for (mutable = new BlockPos.Mutable(), l = -1; l <= 1; l++) {
	      for (int m = -1; m <= 1; m++) {
	        for (int n = -1; n <= 4; n++) {
	          if (l != 0 || n != 0 || m != 0) {
	            int o = 100;
	            if (n > 1)
	              o += (n - 1) * 100; 
	            mutable.set((Vec3i)pos, l, n, m);
	            int p = getBurnChance((WorldView)world, (BlockPos)mutable);
	            if (p > 0) {
	              int q = (p + 40 + world.getDifficulty().getId() * 7) / (i + 30);
	              if (bl2)
	                q /= 2; 
	              if (q > 0 && random.nextInt(o) <= q && (
	                !world.isRaining() || !isRainingAround((World)world, (BlockPos)mutable))) {
	                int r = Math.min(15, i + random.nextInt(5) / 4);
	                world.setBlockState((BlockPos)mutable, getStateWithAge((WorldAccess)world, (BlockPos)mutable, r), 3);
	              } 
	            } 
	          } 
	        } 
	      } 
	    }
		info.cancel();
	}
	
	private static int getFireTickDelay(Random random) {
	    return 30 + random.nextInt(10);
	  }
	@Shadow
	protected boolean isRainingAround(World world, BlockPos pos) {
	    return (world.hasRain(pos) || world.hasRain(pos.west()) || world.hasRain(pos.east()) || world.hasRain(pos.north()) || world.hasRain(pos.south()));
	  }
	
	private int getSpreadChance(BlockState state) {
	    if (state.contains((Property)Properties.WATERLOGGED) && ((Boolean)state.get((Property)Properties.WATERLOGGED)).booleanValue())
	      return 0; 
	    return this.spreadChances.getInt(state.getBlock());
	  }
	
	private void trySpreadingFire(World world, BlockPos pos, int spreadFactor, Random random, int currentAge) {
	    int i = getSpreadChance(world.getBlockState(pos));
	    if (random.nextInt(spreadFactor) < i) {
	      BlockState blockState = world.getBlockState(pos);
	      if (random.nextInt(currentAge + 10) < 5 && !world.hasRain(pos)) {
	        int j = Math.min(currentAge + random.nextInt(5) / 4, 15);
	        world.setBlockState(pos, getStateWithAge((WorldAccess)world, pos, j), 3);
	      } else {
	        world.removeBlock(pos, false);
	      } 
	      Block block = blockState.getBlock();
	      if (block instanceof TntBlock)
	        TntBlock.primeTnt(world, pos); 
	    } 
	  }
	
	private boolean areBlocksAroundFlammable(BlockView world, BlockPos pos) {
	    for (Direction direction : Direction.values()) {
	      if (isFlammable(world.getBlockState(pos.offset(direction))))
	        return true; 
	    } 
	    return false;
	  }
	@Shadow
	protected boolean isFlammable(BlockState state) {
	    return (getBurnChance(state) > 0);
	  }
	
	private int getBurnChance(WorldView world, BlockPos pos) {
	    if (!world.isAir(pos))
	      return 0; 
	    int i = 0;
	    for (Direction direction : Direction.values()) {
	      BlockState blockState = world.getBlockState(pos.offset(direction));
	      i = Math.max(getBurnChance(blockState), i);
	    } 
	    return i;
	  }
	
	private int getBurnChance(BlockState state) {
		FireBlock fire = (FireBlock) (Object) this;
	    if (state.contains((Property)Properties.WATERLOGGED) && ((Boolean)state.get((Property)Properties.WATERLOGGED)).booleanValue())
	      return 0; 
	    return burnChances.getInt(state.getBlock());
	  }
	
	private BlockState getStateWithAge(WorldAccess world, BlockPos pos, int age) {
		FireBlock fire = (FireBlock) (Object) this;
		BlockState blockState = fire.getState((BlockView)world, pos);
		if (blockState.isOf(Blocks.FIRE))
			return (BlockState)blockState.with((Property)fire.AGE, Integer.valueOf(age)); 
		    return blockState;
	}
}