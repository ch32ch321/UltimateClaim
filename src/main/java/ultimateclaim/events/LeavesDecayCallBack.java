package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;

public interface LeavesDecayCallBack {
	
	Event<LeavesDecayCallBack> EVENT = EventFactory.createArrayBacked(LeavesDecayCallBack.class,
			listeners -> (leave, world) -> {
				for (LeavesDecayCallBack event : listeners) {
					ActionResult result = event.declay(leave, world);
					if (result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
	
	ActionResult declay(BlockState leave, ServerWorld world);
	
}
