package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public interface FluidFlowCallBack {
	
	Event<FluidFlowCallBack> EVENT = EventFactory.createArrayBacked(FluidFlowCallBack.class, (listeners) ->(world, from, to) -> {
				for (FluidFlowCallBack event : listeners) {
					ActionResult result = event.fluidFlow(world, from, to);
					if(result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
	
	
	ActionResult fluidFlow(ServerWorld world, BlockPos from, BlockPos to);
}
