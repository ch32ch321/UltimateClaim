package ultimateclaim.claim;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import ultimateclaim.member.ClaimPermissions;
import ultimateclaim.member.ClaimRole;

public class ClaimBuilder {
    private final Claim claim;

    public ClaimBuilder() {
        this.claim = new Claim();
    }

    public ClaimBuilder setName(Text name) {
        this.claim.setName(name);
        return this;
    }

    public ClaimBuilder setOwner(PlayerEntity player) {
        this.claim.setOwner(player.getUuid()).setName(player.getName());
        if (this.claim.getName() == null) {
            this.claim.setName(this.claim.getDefaultName());
        }
        return this;
    }

    public ClaimBuilder addMembers(PlayerEntity... players) {
        for (PlayerEntity player : players) {
            this.claim.addMember(player, ClaimRole.MEMBER);
        }
        return this;
    }

    public ClaimBuilder addClaimedChunk(ChunkPos chunk, PlayerEntity player) {
        this.claim.addClaimedChunk(chunk, player);
        return this;
    }
    
    public ClaimBuilder setWorld(ServerWorld world) {
    	this.claim.setWorld(world);
    	return this;
    }

//    public ClaimBuilder setPowerCell(PowerCell powerCell) {
//        this.claim.setPowerCell(powerCell);
//        return this;
//    }

    public ClaimBuilder setMemberPermissions(ClaimPermissions memberPermissions) {
        this.claim.setMemberPermissions(memberPermissions);
        return this;
    }

    public ClaimBuilder setVisitorPermissions(ClaimPermissions visitorPermissions) {
        this.claim.setVisitorPermissions(visitorPermissions);
        return this;
    }

    public ClaimBuilder banPlayer(UUID... uuids) {
        for (UUID uuid : uuids) {
            this.claim.banPlayer(uuid);
        }
        return this;
    }

    public ClaimBuilder setLocked(boolean locked) {
        this.setLocked(locked);
        return this;
    }

    public ClaimBuilder setHome(BlockPos home) {
        this.setHome(home);
        return this;
    }

    public Claim build() {
        return this.claim;
    }
}
