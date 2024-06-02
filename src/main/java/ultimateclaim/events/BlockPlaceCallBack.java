package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPlaceCallBack {
	
	public static Event<Before> BEFORE = EventFactory.createArrayBacked(Before.class,
			(listeners) -> (world, player, blockPos, state, blockEntity) -> {
		for (Before event : listeners) {
			ActionResult result = event.beforeBlockPlace(world, player, blockPos, state, blockEntity);
			if(result != ActionResult.PASS)
				return result;
		}
		return ActionResult.PASS;
	});
	public static Event<After> AFTER = EventFactory.createArrayBacked(After.class,
			(listeners) -> (world, player, blockPos, state, blockEntity) -> {
		for (After event : listeners) {
			event.afterBlockPlace(world, player, blockPos, state, blockEntity);
		}

	});
	
	public interface Before{
		ActionResult beforeBlockPlace(World world, PlayerEntity player, BlockPos blockPos, BlockState state,
				BlockEntity blockEntity);
	}
	public interface After{
	void afterBlockPlace(World world, PlayerEntity player, BlockPos blockPos, BlockState state,
			BlockEntity blockEntity);
	}
}
