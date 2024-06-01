package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface PlayerMoveCallBack {
	
	Event<PlayerMoveCallBack> EVENT = EventFactory.createArrayBacked(PlayerMoveCallBack.class, 
			(listeners) -> (world, player, from, to) -> {
				for (PlayerMoveCallBack event : listeners) {
					ActionResult result = event.onMove(world, player, from, to);
					if(result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
	
	ActionResult onMove(World world, PlayerEntity player, BlockPos from, BlockPos to);

}
