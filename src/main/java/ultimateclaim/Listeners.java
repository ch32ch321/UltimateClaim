package ultimateclaim;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import ultimateclaim.claim.Claim;
import ultimateclaim.claim.ClaimManager;
import ultimateclaim.events.BlockPlaceCallBack;
import ultimateclaim.events.PlayerMoveCallBack;
import ultimateclaim.member.ClaimMember;
import ultimateclaim.member.ClaimPerm;
import ultimateclaim.member.ClaimRole;

public class Listeners {
	
	private ClaimManager manager;
	
	public Listeners() {
		this.manager = UltimateClaim.getInstance().getClaimManager();
		BlockPlaceCallBack.BEFORE.register(this::placeBlock);
		PlayerBlockBreakEvents.BEFORE.register(this::breakBlock);
		ServerPlayConnectionEvents.JOIN.register(this::join);
		PlayerMoveCallBack.EVENT.register(this::move);
	}
	
	private ActionResult placeBlock(World world, PlayerEntity player, BlockPos blockPos, BlockState state,
			BlockEntity blockEntity) {
		
		ChunkPos chunkPos = world.getChunk(blockPos).getPos();
		if(!manager.hasClaim(chunkPos))
			return ActionResult.PASS;
		Claim claimChunk = manager.getClaim(chunkPos);
		
		if(!claimChunk.playerHasPerms(player, ClaimPerm.PLACE) || claimChunk.isBanned(player.getUuid())) {
			player.sendMessage(Text.literal("Vous ne pouvez pas posser de block dans ce claim.").formatted(Formatting.RED));
			return ActionResult.FAIL;
		}
		return ActionResult.PASS;
	}
	
	private boolean breakBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity) {
		
		ChunkPos chunkPos = world.getChunk(pos).getPos();
		if(!manager.hasClaim(chunkPos))
			return true;
		Claim claimChunk = manager.getClaim(chunkPos);
		
		if(!claimChunk.playerHasPerms(player, ClaimPerm.BREAK) || claimChunk.isBanned(player.getUuid())) {
			player.sendMessage(Text.literal("Vous ne pouvez pas posser de block dans ce claim.").formatted(Formatting.RED));
			return false;
		}
		return true;
	}
	
	private void join(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		manager.getRegisteredClaims().stream()
			.map(claim -> claim.getMember(handler.getPlayer().getUuid()))
			.filter(Objects::nonNull)
			.forEach(member -> member.setName(handler.getPlayer().getName()));
	}
	
	private ActionResult move(World world, PlayerEntity player, BlockPos from, BlockPos to) {
		
		ChunkPos chunkFrom = world.getChunk(from).getPos();
        ChunkPos chunkTo = world.getChunk(to).getPos();
        if (chunkFrom == chunkTo)
            return ActionResult.PASS;
        
        if (manager.hasClaim(chunkFrom)) {
            Claim claim = manager.getClaim(chunkFrom);
            if (manager.getClaim(chunkTo) != claim) {
                ClaimMember member = claim.getMember(player.getUuid());
                if (member != null) {
                    if (member.getRole() == ClaimRole.VISITOR) {
                        claim.removeMember(member);
                    } else {
                        member.setPresent(false);
                    }
                }
                ((ServerPlayerEntity)player).networkHandler.sendPacket(BossBarS2CPacket.remove(claim.getMemberBossBar().getUuid()));
                ((ServerPlayerEntity)player).networkHandler.sendPacket(BossBarS2CPacket.remove(claim.getVisitorBossBar().getUuid()));
            }
        }
		
        if (manager.hasClaim(chunkTo)) {
            Claim claim = manager.getClaim(chunkTo);
            if (manager.getClaim(chunkFrom) != claim) {
                ClaimMember member = claim.getMember(player);
                if (member == null) {
                    if (claim.isLocked()) 
                        return ActionResult.FAIL;
                    if(claim.isBanned(player.getUuid()))
                    	return ActionResult.FAIL;
                }
                if (member != null) {
                    member.setPresent(true);
                }
                if (member != null
                        && claim.isBanned(member.getUniqueId())) {
                    return ActionResult.FAIL;
                }

                    if (member == null || member.getRole() == ClaimRole.VISITOR) {
                    	((ServerPlayerEntity)player).networkHandler.sendPacket(BossBarS2CPacket.add(claim.getVisitorBossBar()));
                    } else {
                    	((ServerPlayerEntity)player).networkHandler.sendPacket(BossBarS2CPacket.add(claim.getMemberBossBar()));
                    }
            }
        }
        
		return ActionResult.PASS;
	}
}
