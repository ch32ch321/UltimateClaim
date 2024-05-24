package ultimateclaim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import ultimateclaim.commands.ClaimCommand;
import ultimateclaim.events.FireSpreadCallBack;

public class UltimateClaim implements ModInitializer {
	
    public static final Logger LOGGER = LoggerFactory.getLogger("ultimateclaim");
    private static LuckPerms luckPerms;

	@Override
	public void onInitialize() {
		
		ServerStartCallback.EVENT.register(server -> {
			if(FabricLoader.getInstance().isModLoaded("luckperms")) {
				luckPerms = LuckPermsProvider.get();
			}
		});
		
		ServerStopCallback.EVENT.register(server -> 
				luckPerms = null
				);
		CommandRegistrationCallback.EVENT.register(new ClaimCommand());
		
		FireSpreadCallBack.Event.register((world, pos) -> {
		return ActionResult.PASS;
		});
		
	}
	
	public static boolean hasPermission(ServerPlayerEntity player, String permission) {
	    if(luckPerms != null)
			return luckPerms.getPlayerAdapter(ServerPlayerEntity.class).getPermissionData(player).checkPermission(permission).asBoolean();
		return true;
	}
}