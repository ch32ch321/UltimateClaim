package ultimateclaim.claim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import ultimateclaim.UltimateClaim;
import ultimateclaim.claim.region.ClaimCorners;
import ultimateclaim.claim.region.ClaimedChunk;
import ultimateclaim.claim.region.ClaimedRegion;
import ultimateclaim.claim.region.RegionCorners;
import ultimateclaim.member.ClaimMember;
import ultimateclaim.member.ClaimPerm;
import ultimateclaim.member.ClaimPermissions;
import ultimateclaim.member.ClaimRole;

public class Claim {
	
    private int id;
    private Text name = null;
    private ClaimMember owner;
    private final Set<ClaimMember> members = new HashSet<>();

    private final Set<ClaimedRegion> claimedRegions = new HashSet<>();
    private final Set<UUID> bannedPlayers = new HashSet<>();

    private BlockPos home = null;
    private boolean locked = false;
    private ServerWorld world;

    private final ClaimSettings claimSettings = new ClaimSettings()
            .setEnabled(ClaimSetting.HOSTILE_MOB_SPAWNING, true)
            .setEnabled(ClaimSetting.FIRE_SPREAD, true)
            .setEnabled(ClaimSetting.MOB_GRIEFING, true)
            .setEnabled(ClaimSetting.LEAF_DECAY, true)
            .setEnabled(ClaimSetting.PVP, false)
            .setEnabled(ClaimSetting.TNT, false);
    
    private ClaimPermissions memberPermissions = new ClaimPermissions()
            .setAllowed(ClaimPerm.BREAK, true)
            .setAllowed(ClaimPerm.INTERACT, true)
            .setAllowed(ClaimPerm.PLACE, true)
            .setAllowed(ClaimPerm.MOB_KILLING, true)
            .setAllowed(ClaimPerm.REDSTONE, true)
            .setAllowed(ClaimPerm.DOORS, true)
            .setAllowed(ClaimPerm.TRADING, true);

    private ClaimPermissions visitorPermissions = new ClaimPermissions()
            .setAllowed(ClaimPerm.BREAK, false)
            .setAllowed(ClaimPerm.INTERACT, false)
            .setAllowed(ClaimPerm.PLACE, false)
            .setAllowed(ClaimPerm.MOB_KILLING, false)
            .setAllowed(ClaimPerm.REDSTONE, false)
            .setAllowed(ClaimPerm.DOORS, true)
            .setAllowed(ClaimPerm.TRADING, false);

    //private PowerCell powerCell = new PowerCell(this);

    private BossBar bossBarVisitor = null;
    private BossBar bossBarMember = null;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public ClaimedChunk getFirstClaimedChunk() {
        if (!this.claimedRegions.isEmpty()) {
            return this.claimedRegions.iterator().next().getFirstClaimedChunk();
        } else {
            return null;
        }
    }

    public Text getName() {
        return this.name;
    }

    public void setName(Text name) {
        this.name = name;
        if (this.bossBarMember != null) {
            this.bossBarMember.setName(name);
        }
        if (this.bossBarVisitor != null) {
            this.bossBarVisitor.setName(name);
        }
    }

    public Text getDefaultName() {
        return this.owner.getName();
    }

    public BossBar getVisitorBossBar() {
        if (this.bossBarVisitor == null) {
            this.bossBarVisitor = new BossBar(UUID.randomUUID(), this.name, BossBar.Color.YELLOW, BossBar.Style.PROGRESS) {};
        }
        return this.bossBarVisitor;
    }

    public BossBar getMemberBossBar() {
        if (this.bossBarMember == null) {
            this.bossBarMember = new BossBar(UUID.randomUUID(), this.name, BossBar.Color.GREEN, BossBar.Style.PROGRESS) {};
        }
        return this.bossBarMember;
    }

    public ClaimMember getOwner() {
        return this.owner;
    }

    public ClaimMember setOwner(UUID owner) {
        return this.owner = new ClaimMember(this, owner, null, ClaimRole.OWNER, this.world.getServer());
    }

    public ClaimMember setOwner(PlayerEntity owner) {
        return this.owner = new ClaimMember(this, owner.getUuid(), owner.getName(), ClaimRole.OWNER, this.world.getServer());
    }

    public boolean transferOwnership(PlayerEntity newOwner) {
        if (newOwner.getUuid() == this.owner.getUniqueId()) {
            return false;
        }

        boolean wasNameChanged = this.name.equals(getDefaultName());

        removeMember(newOwner.getUuid());
        this.owner.setRole(ClaimRole.MEMBER);
        addMember(this.owner);
        setOwner(newOwner);
        if (!wasNameChanged) {
            setName(getDefaultName());
        }
        return true;
    }

    public Set<ClaimMember> getMembers() {
        return this.members;
    }

    public Set<ClaimMember> getOwnerAndMembers() {
        Set<ClaimMember> members = new HashSet<>(this.members);
        members.add(this.owner);
        return members;
    }

    public ClaimMember addMember(ClaimMember member) {
        // Removing the player if they already are a member (i.e. they are a visitor) to avoid conflict
        this.removeMember(member.getUniqueId());
        this.members.add(member);
        return member;
    }

    public ClaimMember addMember(PlayerEntity player, ClaimRole role) {
        ClaimMember newMember = new ClaimMember(this, player.getUuid(), player.getName(), role, this.world.getServer());
        return addMember(newMember);
    }

    public ClaimMember getMember(UUID uuid) {
        if (this.owner.getUniqueId().equals(uuid)) {
            return this.owner;
        }
        return this.members.stream()
                .filter(member -> member.getUniqueId().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * Search for a member by username
     *
     * @param name name to search
     * @return Member instance matching this username, if any
     */
    public ClaimMember getMember(Text name) {
        if (name == null) {
            return null;
        }
        if (name.equals(this.owner.getName())) {
            return this.owner;
        }
        return this.members.stream()
                .filter(member -> name.equals(member.getName()))
                .findFirst()
                .orElse(null);
    }

    public ClaimMember getMember(PlayerEntity player) {
        return getMember(player.getUuid());
    }

    public void removeMember(UUID uuid) {
        for (ClaimMember member : this.members) {
            if (member.getUniqueId().equals(uuid)) {
                this.members.remove(member);
                break;
            }
        }
    }

    public void removeMember(ClaimMember member) {
        this.members.remove(member);
    }

    public void removeMember(PlayerEntity player) {
        this.removeMember(player.getUuid());
    }

    public boolean playerHasPerms(PlayerEntity player, ClaimPerm claimPerm) {
        ClaimMember member = getMember(player);
        if (UltimateClaim.hasPermission((ServerPlayerEntity) player, "ultimateclaims.bypass.perms")
                || player.getUuid().equals(this.owner.getUniqueId())) {
            return true;
        }
        if (member == null) {
            return false;
        }
        return member.getRole() == ClaimRole.VISITOR && getVisitorPermissions().hasPermission(claimPerm)
                || member.getRole() == ClaimRole.MEMBER && getMemberPermissions().hasPermission(claimPerm);
    }

    public boolean isOwnerOrMember(PlayerEntity player) {
        if (player.getUuid().equals(this.owner.getUniqueId())) {
            return true;
        }
        ClaimMember member = getMember(player);
        if (member == null) {
            return false;
        }
        return member.getRole() == ClaimRole.MEMBER;
    }

    public boolean containsChunk(ChunkPos chunk) {
        final ServerWorld world = (ServerWorld) this.world;
        return this.claimedRegions.stream().anyMatch(r -> r.containsChunk(world, chunk));
    }

    public boolean containsChunk(ServerWorld world, ChunkPos pos) {
        return this.claimedRegions.stream().anyMatch(r -> r.containsChunk(world, pos));
    }

    public ClaimedRegion getPotentialRegion(ChunkPos chunk) {
        ClaimedChunk newChunk = new ClaimedChunk(this.world, chunk);
        return newChunk.getAttachedRegion(this);
    }

    public boolean addClaimedChunk(ChunkPos chunk, PlayerEntity player) {
        animateChunk(this.world, chunk, (ServerPlayerEntity) player, Blocks.EMERALD_BLOCK);
        return addClaimedChunk(this.world, chunk);
    }

    public boolean isNewRegion(ServerWorld world, ChunkPos pos) {
        ClaimedChunk newChunk = new ClaimedChunk(world, pos);
        return newChunk.getAttachedRegion(this) == null;
    }

    public boolean addClaimedChunk(ServerWorld world, ChunkPos pos) {
        ClaimedChunk newChunk = new ClaimedChunk(world, pos);
        ClaimedRegion region = newChunk.getAttachedRegion(this);
        if (region == null) {
            this.claimedRegions.add(new ClaimedRegion(newChunk, this));
            return true;
        } else {
            region.addChunk(newChunk);
            newChunk.mergeRegions(this);
        }
        return false;
    }

    public void addClaimedRegion(ClaimedRegion region) {
        this.claimedRegions.add(region);
    }

    public void removeClaimedRegion(ClaimedRegion region) {
        this.claimedRegions.remove(region);
    }

    public ClaimedChunk removeClaimedChunk(ChunkPos chunk, PlayerEntity player) {
        animateChunk(this.world, chunk, (ServerPlayerEntity) player, Blocks.REDSTONE_BLOCK);
        ClaimedChunk newChunk = getClaimedChunk(chunk, (ServerWorld) player.getWorld());
        for (ClaimedRegion region : new ArrayList<>(this.claimedRegions)) {
            List<ClaimedRegion> claimedRegions = region.removeChunk(newChunk);
            if (!claimedRegions.isEmpty()) {
                this.claimedRegions.remove(region);
                this.claimedRegions.addAll(claimedRegions);
            }
            if (region.getChunks().isEmpty()) {
                this.claimedRegions.remove(region);
                //UltimateClaims.getInstance().getDataHelper().deleteClaimedRegion(region);
            }
        }
        return newChunk;
    }

    public Set<ClaimedRegion> getClaimedRegions() {
        return Collections.unmodifiableSet(this.claimedRegions);
    }

    public ClaimedRegion getClaimedRegion(ChunkPos chunk, ServerWorld world) {
        for (ClaimedChunk claimedChunk : getClaimedChunks()) {
            if (claimedChunk.equals(chunk, world)) {
                return claimedChunk.getRegion();
            }
        }
        return null;
    }

    public List<ClaimedChunk> getClaimedChunks() {
        List<ClaimedChunk> chunks = new ArrayList<>();
        for (ClaimedRegion claimedRegion : this.claimedRegions) {
            chunks.addAll(claimedRegion.getChunks());
        }
        return chunks;
    }

    public ClaimedChunk getClaimedChunk(ChunkPos chunk, ServerWorld world) {
        for (ClaimedChunk claimedChunk : getClaimedChunks()) {
            if (claimedChunk.equals(chunk, world)) {
                return claimedChunk;
            }
        }
        return null;
    }

    public int getClaimSize() {
        return this.claimedRegions.stream().map(r -> r.getChunks().size()).mapToInt(Integer::intValue).sum();
    }

    public int getMaxClaimSize() {
        return getOwnerAndMembers().size()*5;
    }

    public void animateChunk(ServerWorld world, ChunkPos chunk, ServerPlayerEntity player, Block material) {
        int bx = chunk.x << 4;
        int bz = chunk.z << 4;

        BlockPos playerLocation = player.getBlockPos();
        for (int xx = bx; xx < bx + 16; ++xx) {
            for (int zz = bz; zz < bz + 16; ++zz) {
                for (int yy = playerLocation.getY() - 5; yy < playerLocation.getY() + 5; ++yy) {
                    BlockState block = world.getBlockState(new BlockPos(xx, yy, zz));
                    if (!block.isOpaque()) {
                        continue;
                    }

                    BlockPos blockLocation = new BlockPos(xx, yy, zz);
                    new Thread(() -> {
                    	Random random = new Random();
                    	try {
							Thread.sleep(random.nextLong(0, 200));
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
                    	world.getPlayers().stream()
                    		.filter(p -> p.getChunkPos().equals(chunk))
                    		.filter(p -> isOwnerOrMember(p))
                    		.forEach(p -> p.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockLocation, material.getDefaultState())));
                    	Thread thread = new Thread(() -> {
                    		try {
								Thread.sleep(random.nextLong(100, 200));
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
                    		world.getPlayers().stream()
		                		.filter(p -> p.getChunkPos().equals(chunk))
		                		.filter(p -> isOwnerOrMember(p))
		                		.forEach(p -> p.networkHandler.sendPacket(new BlockUpdateS2CPacket(blockLocation, block)));						});
						thread.start();
                    }).start();
                    
                        //player.sendBlockChange(blockLocation, material.createBlockData());
                        //Bukkit.getScheduler().runTaskLater(UltimateClaims.getInstance(), () -> player.sendBlockChange(blockLocation, block.getBlockData()), ThreadLocalRandom.current().nextInt(30) + 1);
                        //player.playSound(blockLocation, XSound.BLOCK_METAL_STEP.parseSound(), 1F, .2F);
                }
            }
        }
    }

    public List<RegionCorners> getCorners() {
        if (this.claimedRegions.size() <= 0) {
            return null;
        }

        List<RegionCorners> result = new ArrayList<>();

        for (ClaimedRegion region : this.claimedRegions) {
            RegionCorners regionCorners = new RegionCorners();
            for (ClaimedChunk cChunk : region.getChunks()) {
                double[] xArr = new double[2],
                        zArr = new double[2];

                int cX = cChunk.getX() * 16,
                        cZ = cChunk.getZ() * 16;

                xArr[0] = cX;
                zArr[0] = cZ;

                xArr[1] = cX + 16;
                zArr[1] = cZ + 16;

                regionCorners.addCorners(new ClaimCorners((WorldChunk) cChunk.getChunk(), xArr, zArr));
            }
            result.add(regionCorners);
        }

        return result;
    }

//    public boolean hasPowerCell() {
//        return this.powerCell.location != null;
//    }
//
//    public PowerCell getPowerCell() {
//        return this.powerCell;
//    }
//
//    public void setPowerCell(PowerCell powerCell) {
//        this.powerCell = powerCell;
//    }

    public ClaimPermissions getMemberPermissions() {
        return this.memberPermissions;
    }

    public void setMemberPermissions(ClaimPermissions memberPermissions) {
        this.memberPermissions = memberPermissions;
    }

    public ClaimPermissions getVisitorPermissions() {
        return this.visitorPermissions;
    }

    public void setVisitorPermissions(ClaimPermissions visitorPermissions) {
        this.visitorPermissions = visitorPermissions;
    }

    public void banPlayer(UUID uuid) {
        this.bannedPlayers.add(uuid);
    }

    public void unBanPlayer(UUID uuid) {
        this.bannedPlayers.remove(uuid);
    }

    public boolean isBanned(UUID uuid) {
        return this.bannedPlayers.contains(uuid);
    }

    public void destroy(ClaimDeleteReason reason) {
       

        this.claimedRegions.clear();

        //this.powerCell.destroy();

        UltimateClaim.getInstance().getClaimManager().removeClaim(this);

        // we've just unclaimed the chunk we're in, so we've "moved" out of the claim
        if (this.bossBarMember != null) {
            this.bossBarMember = null;
        }
        if (this.bossBarVisitor != null) {
            this.bossBarVisitor = null;
        }
        getOwnerAndMembers().forEach(m -> m.setPresent(false));
        this.members.clear();
    }

    public Set<UUID> getBannedPlayers() {
        return this.bannedPlayers;
    }

    public void setOwner(ClaimMember owner) {
        this.owner = owner;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public BlockPos getHome() {
        return this.home;
    }

    public void setHome(BlockPos home) {
        this.home = home;
    }

    public ClaimSettings getClaimSettings() {
        return this.claimSettings;
    }
    
    public void setWorld(ServerWorld world) {
		this.world = world;
	}
    
    public ServerWorld getWorld() {
		return world;
	}

//    public String getPowercellTimeRemaining() {
//        if (hasPowerCell()) {
//            return this.powerCell.getTimeRemaining();
//        } else {
//            return null;
//        }
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return this.id == ((Claim) obj).id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.id;
        return hash;
    }
    
    public JsonObject toJson() {
    	JsonObject json = new JsonObject();
    	
    	return json;
    }
    
}