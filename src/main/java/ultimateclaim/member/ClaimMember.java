package ultimateclaim.member;

import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import ultimateclaim.claim.Claim;

public class ClaimMember {
    private int id;
    private final Claim claim;
    private final UUID uuid;
    private Text lastName;
    private ClaimRole role;
    private boolean isPresent = false;
    private long playTime;
    private long memberSince = System.currentTimeMillis();
    private MinecraftServer server;

    public ClaimMember(Claim claim, UUID uuid, Text text, ClaimRole role, MinecraftServer server) {
        this.claim = claim;
        this.uuid = uuid;
        this.lastName = text;
        this.role = role;
        this.server = server;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public Claim getClaim() {
        return this.claim;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public Text getName() {
        return this.lastName;
    }

    public void setName(Text name) {
        this.lastName = name;
    }

    public ClaimRole getRole() {
        return this.role;
    }

    public void setRole(ClaimRole role) {
        this.role = role;
    }

    public boolean isPresent() {
        return this.isPresent;
    }

    public void setPresent(boolean present) {
        this.isPresent = present;
    }

    public long getPlayTime() {
        return this.playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public long getMemberSince() {
        return this.memberSince;
    }

    public void setMemberSince(long memberSince) {
        this.memberSince = memberSince;
    }

    public PlayerEntity getPlayer() {
        return this.server.getPlayerManager().getPlayer(this.uuid);
    }

    @SuppressWarnings("null")
	public void eject(BlockPos location) {
        PlayerEntity player;
        if (!this.isPresent || !((player = getPlayer()) == null)) {
            return;
        }
        BlockPos spawn = player.getWorld().getSpawnPos();
        if (spawn == null && location == null) {
            return;
        }
        if(location == null) {
        	player.teleport(spawn.getX(), spawn.getY(), spawn.getZ());
        }else {
        	player.teleport(location.getX(), location.getY(), location.getZ());
        }
        this.isPresent = false;
    }
}
