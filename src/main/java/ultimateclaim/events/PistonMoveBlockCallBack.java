package ultimateclaim.events;

import java.util.List;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface PistonMoveBlockCallBack {
	
	Event<PistonMoveBlockCallBack> EVENT = EventFactory.createArrayBacked(PistonMoveBlockCallBack.class, 
			(listeners) -> (world, pistonPos, direction, blocks) -> {
				for (PistonMoveBlockCallBack event : listeners) {
					ActionResult result = event.onMove(world, pistonPos, direction, blocks);
					if(result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
	
	ActionResult onMove(ServerWorld world, BlockPos pistonPos, Direction direction, List<BlockPos> blocks);
}
