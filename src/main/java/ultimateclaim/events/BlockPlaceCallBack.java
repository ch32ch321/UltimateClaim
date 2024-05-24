package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;

public interface BlockPlaceCallBack {
	
	Event<BlockPlaceCallBack> EVENT = EventFactory.createArrayBacked(BlockPlaceCallBack.class,
			(listeners) -> (block, player) -> {
		for (BlockPlaceCallBack event : listeners) {
			ActionResult result = event.place(block, player);
			if (result != ActionResult.PASS)
				return result;
		}
		return ActionResult.PASS;
	});
	
	ActionResult place(Block block, ServerPlayerEntity player);
	
}
