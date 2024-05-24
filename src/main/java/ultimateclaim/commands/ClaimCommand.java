package ultimateclaim.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import ultimateclaim.UltimateClaim;

public class ClaimCommand implements CommandRegistrationCallback {

	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
			RegistrationEnvironment environment) {
		dispatcher.register(CommandManager
				.literal("claim")
				.requires(source -> UltimateClaim.hasPermission(source.getPlayer(), "ultimateClaime.command.claim"))
				.executes(context -> {
					return Command.SINGLE_SUCCESS;
				}));

	}

}
