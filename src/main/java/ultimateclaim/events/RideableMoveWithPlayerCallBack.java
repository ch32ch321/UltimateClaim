package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface RideableMoveWithPlayerCallBack {
	
	Event<RideableMoveWithPlayerCallBack> EVENT = EventFactory.createArrayBacked(RideableMoveWithPlayerCallBack.class, 
			(listeners) -> (world, passager, vehicule, from, to) ->  {
				for (RideableMoveWithPlayerCallBack event : listeners) {
					ActionResult result = event.onMove(world, passager, vehicule, from, to);
					if(result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
	
	ActionResult onMove(World world, PlayerEntity passager, Entity vehicule, BlockPos from, BlockPos to);

}
