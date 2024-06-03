package ultimateclaim.claim;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

public class ClaimManager {
    // Owner, Claim
    private final Map<UUID, Claim> registeredClaims = new HashMap<>();

    public Claim addClaim(UUID owner, Claim claim) {
        return this.registeredClaims.put(owner, claim);
    }

    public Claim addClaim(PlayerEntity owner, Claim claim) {
        return addClaim(owner.getUuid(), claim);
    }

    public void addClaims(Map<UUID, Claim> claims) {
        this.registeredClaims.putAll(claims);
    }

    public boolean hasClaim(UUID owner) {
        return this.registeredClaims.containsKey(owner);
    }

    public boolean hasClaim(PlayerEntity owner) {
        return hasClaim(owner.getUuid());
    }

    public boolean hasClaim(ChunkPos chunk) {
        return this.registeredClaims
                .values()
                .stream()
                .anyMatch(claim -> claim.containsChunk(chunk));
    }

    public Claim getClaim(UUID owner) {
        return this.registeredClaims.get(owner);
    }

    public Claim getClaim(PlayerEntity owner) {
        return getClaim(owner.getUuid());
    }

    public Claim getClaim(ChunkPos chunk) {
        return this.registeredClaims.values().stream()
                .filter(claim -> claim.containsChunk(chunk)).findFirst().orElse(null);
    }

    public Claim getClaim(ServerWorld world, ChunkPos pos) {
        return this.registeredClaims.values().stream()
                .filter(claim -> claim.containsChunk(world, pos)).findFirst().orElse(null);
    }

    public List<Claim> getClaims(PlayerEntity player) {
        return this.registeredClaims.values().stream().filter(c -> c.isOwnerOrMember(player)).collect(Collectors.toList());
    }

    public void removeClaim(Claim claim) {
        this.registeredClaims.remove(claim.getOwner().getUniqueId());
    }

    public Collection<Claim> getRegisteredClaims() {
        return this.registeredClaims.values();
    }
}
