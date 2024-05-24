package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface FireSpreadCallBack {
	
	Event<FireSpreadCallBack> Event = EventFactory.createArrayBacked(FireSpreadCallBack.class, 
			(listeners) -> (world, pos) -> {
				for (FireSpreadCallBack event : listeners) {
					ActionResult result = event.SpreadFire(world, pos);
					if (result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
	
	ActionResult SpreadFire(World world, BlockPos pos);
	
}
