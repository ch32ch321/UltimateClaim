package ultimateclaim.claim.region;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import ultimateclaim.claim.Claim;

public class ClaimedChunk {
    private ClaimedRegion claimedRegion;
    private final ServerWorld world;
    private final ChunkPos pos;

    public ClaimedChunk(ServerWorld world, ChunkPos pos) {
        this.world = world;
        this.pos = pos;
    }

    public Chunk getChunk() {
        return world.getChunk(getX(), getX());
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public int getX() {
        return this.pos.x;
    }

    public int getZ() {
        return this.pos.z;
    }

    public boolean isAttached(ClaimedChunk chunk) {
        if (!this.world.equals(chunk.getWorld())) {
            return false;
        } else if (chunk.getX() == this.pos.x - 1 && this.pos.z == chunk.getZ()) {
            return true;
        } else if (chunk.getX() == this.pos.x + 1 && this.pos.z == chunk.getZ()) {
            return true;
        } else if (chunk.getX() == this.pos.x && this.pos.z == chunk.getZ() - 1) {
            return true;
        } else {
            return chunk.getX() == this.pos.x && this.pos.z == chunk.getZ() + 1;
        }
    }

    public List<ClaimedChunk> getAttachedChunks() {
        List<ClaimedChunk> chunks = new ArrayList<>();

        for (ClaimedChunk chunk : this.claimedRegion.getChunks()) {
            if (isAttached(chunk)) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    public void mergeRegions(Claim claim) {
        for (ClaimedChunk chunk : claim.getClaimedChunks()) {
            ClaimedRegion region = chunk.getRegion();
            if (isAttached(chunk) && region != this.claimedRegion) {
                claim.removeClaimedRegion(region);
                //UltimateClaims.getInstance().getDataHelper().deleteClaimedRegion(region);
                this.claimedRegion.addChunks(region.getChunks());
                //UltimateClaims.getInstance().getDataHelper().updateClaimedChunks(region.getChunks());
            }
        }
    }

    @SuppressWarnings("removal")
	public BlockPos getCenter() {
        return this.getChunk().getPos().getCenterAtY(this.getChunk().getHighestNonEmptySectionYOffset());
    }

    public ClaimedRegion getAttachedRegion(Claim claim) {
        ClaimedChunk claimedChunk = claim.getClaimedChunks().stream().filter(c -> c.isAttached(this)).findFirst().orElse(null);
        return claimedChunk == null ? null : claimedChunk.getRegion();
    }


    public ClaimedRegion getRegion() {
        return this.claimedRegion;
    }

    public void setRegion(ClaimedRegion claimedRegion) {
        this.claimedRegion = claimedRegion;
    }
    
    public boolean equals(ClaimedChunk other) {
        return this.world.equals(other.world) && this.pos.x == other.pos.x && this.pos.z == other.pos.z;
    }
    
    public boolean equals(ChunkPos other, ServerWorld world) {
    	return this.world.equals(world) && this.pos.x == other.x && this.pos.z == other.z;
    }
}
