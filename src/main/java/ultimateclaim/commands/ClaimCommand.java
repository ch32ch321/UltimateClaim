package ultimateclaim.commands;

import java.util.UUID;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandManager.RegistrationEnvironment;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ChunkPos;
import ultimateclaim.UltimateClaim;
import ultimateclaim.claim.Claim;
import ultimateclaim.claim.ClaimBuilder;
import ultimateclaim.claim.ClaimDeleteReason;
import ultimateclaim.claim.ClaimManager;
import ultimateclaim.claim.region.ClaimedRegion;
import ultimateclaim.member.ClaimMember;
import ultimateclaim.member.ClaimRole;

public class ClaimCommand implements CommandRegistrationCallback {
	
	private ClaimManager manager;
	
	public ClaimCommand() {
		this.manager = UltimateClaim.getInstance().getClaimManager();
	}
	
	@Override
	public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
			RegistrationEnvironment environment) {
		dispatcher.register(CommandManager
				.literal("claim")
					.requires(source -> UltimateClaim.hasPermission(source.getPlayer(), "ultimateClaime.command.claim"))
					.executes(context -> helpCommand(context))
				.then(CommandManager
						.literal("create")
						.executes(context -> createClaim(context)))
				.then(CommandManager
						.literal("delete")
						.executes(context -> deleteClaim(context)))
				.then(CommandManager
						.literal("invite")
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.executes(context -> invite(context))))
				.then(CommandManager
						.literal("kick")
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.executes(context -> kick(context))))
				.then(CommandManager
						.literal("ban")
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.executes(context -> ban(context))))
				.then(CommandManager
						.literal("unban")
						.then(CommandManager.argument("player", EntityArgumentType.player())
								.executes(context -> unBan(context))))
				.then(CommandManager
						.literal("addChunk")
						.executes(context -> addChunk(context)))
				.then(CommandManager
						.literal("removeChunk")
						.executes(context -> removeChunk(context)))
				);
	}

	private int helpCommand(CommandContext<ServerCommandSource> context) {
		
		return Command.SINGLE_SUCCESS;
	}	

	private int createClaim(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		
		if(manager.hasClaim(player.getUuid())) {
			player.sendMessage(Text.literal("Vous possédez déjà un claime.").formatted(Formatting.RED));
			return 0;
		}
		
		Claim claim = new ClaimBuilder()
				.setWorld(player.getServerWorld())
                .setOwner(player)
                .build();
	 
		manager.addClaim(player, claim);
		player.sendMessage(Text.literal("Vous avez créer un claim.").formatted(Formatting.GREEN));
		return Command.SINGLE_SUCCESS;
	}

	private int deleteClaim(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		
		if(!manager.hasClaim(player.getUuid())) {
			player.sendMessage(Text.literal("Vous ne possédez pas ce claim").formatted(Formatting.RED));
			return 0;
		}
		Claim claim = manager.getClaim(player.getUuid());
		claim.destroy(ClaimDeleteReason.PLAYER);
		player.sendMessage(Text.literal("Votre claim a été détruit.").formatted(Formatting.GREEN));
		return Command.SINGLE_SUCCESS;
	}

	private int invite(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		EntitySelector selector = context.getArgument("player", EntitySelector.class);
		ServerPlayerEntity invitedPlayer = null;
		try {
			invitedPlayer = selector.getPlayer(context.getSource());
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		if(invitedPlayer == null) {
			player.sendMessage(Text.literal("Invalid Player").formatted(Formatting.RED));
			return 0;
		}
		if(!manager.hasClaim(player.getUuid())) {
			player.sendMessage(Text.literal("Vous ne possédez pas de claim").formatted(Formatting.RED));
			return 0;
		}
		if(!manager.getClaim(player.getChunkPos()).getOwner().getUniqueId().equals(player.getUuid())) {
			player.sendMessage(Text.literal("Vous n'êtes pas le propriétaire de ce claim.").formatted(Formatting.RED));
			return 0;
		}
		if (player.getUuid().equals(invitedPlayer.getUuid())) {
			player.sendMessage(Text.literal("Vous ne pouvez pas vous invitez vous-même.").formatted(Formatting.RED));
			return 0;
		}
		Claim claim = manager.getClaim(player.getUuid());
		
		UUID uuid = invitedPlayer.getUuid();
		
		if(claim.getMembers().stream()
				.filter(m -> m.getRole() == ClaimRole.MEMBER)
				.anyMatch(m -> m.getUniqueId().equals(uuid))) {
			player.sendMessage(Text.literal(invitedPlayer.getEntityName()+" est déjà dans votre claim.").formatted(Formatting.RED));
			return 0;
		}
		
		ClaimMember member = claim.addMember(invitedPlayer, ClaimRole.MEMBER);
		if(member.isPresent()) {
			invitedPlayer.networkHandler.sendPacket(BossBarS2CPacket.remove(claim.getVisitorBossBar().getUuid()));
			invitedPlayer.networkHandler.sendPacket(BossBarS2CPacket.add(claim.getMemberBossBar()));
		}
		player.sendMessage(Text.literal(invitedPlayer.getEntityName()+" est devenue un membre de votre claim.").formatted(Formatting.GREEN));
		return Command.SINGLE_SUCCESS;
	}

	private int kick(CommandContext<ServerCommandSource> context){
		ServerPlayerEntity player = context.getSource().getPlayer();
		EntitySelector selector = context.getArgument("player", EntitySelector.class);
		ServerPlayerEntity kickedPlayer = null;
		try {
			kickedPlayer = selector.getPlayer(context.getSource());
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		if(kickedPlayer == null) {
			player.sendMessage(Text.literal("Invalid Player").formatted(Formatting.RED));
			return 0;
		}		if(!manager.hasClaim(player.getUuid())) {
			player.sendMessage(Text.literal("Vous ne possédez pas de claim").formatted(Formatting.RED));
			return 0;
		}
		Claim claim = manager.getClaim(player.getUuid());
		if(claim.isOwnerOrMember(kickedPlayer)) {
			claim.removeMember(kickedPlayer.getUuid());
			kickedPlayer.networkHandler.sendPacket(BossBarS2CPacket.remove(claim.getMemberBossBar().getUuid()));
			kickedPlayer.networkHandler.sendPacket(BossBarS2CPacket.add(claim.getVisitorBossBar()));
		}
		return Command.SINGLE_SUCCESS;
	}

	private int ban(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		EntitySelector selector = context.getArgument("player", EntitySelector.class);
		ServerPlayerEntity bannedPlayer = null;
		try {
			bannedPlayer = selector.getPlayer(context.getSource());
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		if(bannedPlayer == null) {
			player.sendMessage(Text.literal("Invalid Player").formatted(Formatting.RED));
			return 0;
		}
		if(!manager.hasClaim(player.getUuid())) {
			player.sendMessage(Text.literal("Vous ne possédez pas de claim").formatted(Formatting.RED));
			return 0;
		}
		
		if(!manager.getClaim(player.getChunkPos()).getOwner().getUniqueId().equals(player.getUuid())) {
			player.sendMessage(Text.literal("Vous n'êtes pas le propriétaire de ce claim.").formatted(Formatting.RED));
			return 0;
		}
		Claim claim = manager.getClaim(player.getUuid());
		if(claim.isBanned(bannedPlayer.getUuid())) {
			player.sendMessage(Text.literal(bannedPlayer.getEntityName()+" est déjà ban de votre claim.").formatted(Formatting.RED));
			return 0;
		}
		claim.banPlayer(bannedPlayer.getUuid());
		player.sendMessage(Text.literal(bannedPlayer.getEntityName()+" est ban de votre claim.").formatted(Formatting.GREEN));
		return Command.SINGLE_SUCCESS;
	}
	
	private int unBan(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		EntitySelector selector = context.getArgument("player", EntitySelector.class);
		ServerPlayerEntity unBannedPlayer = null;
		try {
			unBannedPlayer = selector.getPlayer(context.getSource());
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
		if(unBannedPlayer == null) {
			player.sendMessage(Text.literal("Invalid Player").formatted(Formatting.RED));
			return 0;
		}
		if(!manager.hasClaim(player.getUuid())) {
			player.sendMessage(Text.literal("Vous ne possédez pas de claim").formatted(Formatting.RED));
			return 0;
		}
		
		if(!manager.getClaim(player.getChunkPos()).getOwner().getUniqueId().equals(player.getUuid())) {
			player.sendMessage(Text.literal("Vous n'êtes pas le propriétaire de ce claim.").formatted(Formatting.RED));
			return 0;
		}
		Claim claim = manager.getClaim(player.getUuid());
		if(!claim.isBanned(unBannedPlayer.getUuid())) {
			player.sendMessage(Text.literal(unBannedPlayer.getEntityName()+" n'est pas ban de votre claim.").formatted(Formatting.RED));
			return 0;
		}
		claim.unBanPlayer(unBannedPlayer.getUuid());
		player.sendMessage(Text.literal(unBannedPlayer.getEntityName()+" n'est plus ban de votre claim.").formatted(Formatting.GREEN));
		return Command.SINGLE_SUCCESS;
	}
	
	private int addChunk(CommandContext<ServerCommandSource> context) {
		PlayerEntity player = context.getSource().getPlayer();
		if(manager.hasClaim(player.getChunkPos())) {
			player.sendMessage(Text.literal("Ce chunk est déjà claim par "+manager.getClaim(player.getChunkPos()).getOwner().getName().getString()).formatted(Formatting.RED));
			return 0;
		}
		
		ChunkPos pos = player.getChunkPos();
		Claim claim;
		
		 if (manager.hasClaim(player.getUuid())) {
	            claim = manager.getClaim(player.getUuid());
	            
	            ClaimedRegion region = claim.getPotentialRegion(pos);
	            if(region == null && claim.getClaimedChunks().size() > 0) {
	            	player.sendMessage(Text.literal("Ce chunk n'est pas adjacent a vos claim").formatted(Formatting.RED));
	    			return 0;
	            }
	            
	            int maxClaimable = claim.getMaxClaimSize();
	            
	            if (claim.getClaimSize() >= maxClaimable) {
	            	player.sendMessage(Text.literal("Nombre maximum de chunk atteint pour votre claim, réessayer avec plus de menbre.").formatted(Formatting.RED));
	                return 0;
	            }
	            
	            claim.addClaimedChunk(pos, player);
	            
	            player.getServer().getPlayerManager().getPlayerList().stream()
			 	.filter(p -> p.getChunkPos().equals(pos))
			 	.forEach(p -> {
			 		ClaimMember member = claim.getMember(p);

	             if (member != null) {
	                 member.setPresent(true);
	                 p.networkHandler.sendPacket(BossBarS2CPacket.add(claim.getMemberBossBar()));
	             } else{
	                 member = claim.addMember(p, ClaimRole.VISITOR);
	                 p.networkHandler.sendPacket(BossBarS2CPacket.add(claim.getVisitorBossBar()));
	             } 
			 	});
	            player.sendMessage(Text.literal("Le chunk a été ajouter à votre claim.").formatted(Formatting.GREEN));
		 }else 
         	player.sendMessage(Text.literal("Vous n'êtes pas menbre d'un claim.").formatted(Formatting.RED));
		 
		return Command.SINGLE_SUCCESS;
	}

	private int removeChunk(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = context.getSource().getPlayer();
		ChunkPos pos = player.getChunkPos();
		if(!manager.hasClaim(player.getUuid())) {
			player.sendMessage(Text.literal("Vous ne possédez pas ce claim.").formatted(Formatting.RED));
			return 0;
		}
		if(!manager.hasClaim(pos)) {
			player.sendMessage(Text.literal("Ce chunk n'est pas dans un claim.").formatted(Formatting.RED));
			return 0;
		}
		Claim claim = manager.getClaim(player.getUuid());
		if(!manager.getClaim(player.getChunkPos()).getOwner().getUniqueId().equals(player.getUuid())) {
			player.sendMessage(Text.literal("Vous n'êtes pas le propriétaire de ce claim.").formatted(Formatting.RED));
			return 0;
		}
		
		 player.getServer().getPlayerManager().getPlayerList().stream()
		 	.filter(p -> p.getChunkPos().equals(pos))
		 	.forEach(p -> {
		 		ClaimMember member = claim.getOwnerAndMembers().stream().filter(m -> m.getUniqueId() == p.getUuid()).findFirst().orElse(null);

			 	if (member.getRole() == ClaimRole.VISITOR) {
	              claim.removeMember(member);
	              p.networkHandler.sendPacket(BossBarS2CPacket.remove(claim.getVisitorBossBar().getUuid()));
			 	} else{
	              member.setPresent(false);
	              p.networkHandler.sendPacket(BossBarS2CPacket.remove(claim.getVisitorBossBar().getUuid()));
			 	}	 
			 });
		
		 claim.removeClaimedChunk(pos, player);
         player.sendMessage(Text.literal("Le chunk a été suprimer de votre claim.").formatted(Formatting.GREEN));
		return Command.SINGLE_SUCCESS;
	}
}
