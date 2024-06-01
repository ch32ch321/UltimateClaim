package ultimateclaim.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;

public class HopperTransferCallBack {
	
	public static final Event<ToChest> TO_CHEST = EventFactory.createArrayBacked(ToChest.class, 
			(listeners) -> (chest, hopper, item, slot, side) ->{
				for (ToChest event : listeners) {
					ActionResult result = event.toChestTransfer(chest, hopper, item, slot, side);
					if(result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
	
	public static final Event<FromChest> FROM_CHEST = EventFactory.createArrayBacked(FromChest.class, 
			(listeners) -> (hopper, chest, item, slot, side) ->{
				for (FromChest event : listeners) {
					ActionResult result = event.fromChestTransfer(hopper, chest, item, slot, side);
					if(result != ActionResult.PASS)
						return result;
				}
				return ActionResult.PASS;
			});
	
	
	public interface ToChest{		
		ActionResult toChestTransfer(ChestBlockEntity chest, HopperBlockEntity hopper, ItemStack item, int slot, Direction side);
	}
	public interface FromChest{		
		ActionResult fromChestTransfer(HopperBlockEntity hopper, ChestBlockEntity chest, ItemStack item, int slot, Direction side);
	}
}
