package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public interface DragonEggTeleportCallBack {
	
	Event<DragonEggTeleportCallBack> EVENT = EventFactory.createArrayBacked(DragonEggTeleportCallBack.class, (listeners) -> (world, from, to) -> {
		for (DragonEggTeleportCallBack event : listeners) {
			ActionResult result = event.onTeleport(world, from, to);
			if(result != ActionResult.PASS)
				return result;
		}
		return ActionResult.PASS;
	});
	
	ActionResult onTeleport(ServerWorld world, BlockPos from, BlockPos to);

}
