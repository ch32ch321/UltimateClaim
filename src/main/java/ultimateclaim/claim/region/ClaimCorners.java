package ultimateclaim.claim.region;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;

/**
 * Holds coordinates for a claimed chunk to be used in {@link DynmapManager}
 */
public class ClaimCorners {
    private final WorldChunk chunk;

    public final String chunkID;
    public final double[] x, z;

    public ClaimCorners(WorldChunk chunk, double[] x, double[] z) {
        this.chunk = chunk;

        this.chunkID = chunk.getPos().x + ";" + chunk.getPos().z;
        this.x = x;
        this.z = z;
    }

	public ServerWorld getWorld() {
        return (ServerWorld) this.chunk.getWorld();
    }
}
