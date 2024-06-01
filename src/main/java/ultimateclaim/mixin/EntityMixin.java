package ultimateclaim.mixin;

import java.util.HashMap;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ultimateclaim.events.PlayerMoveCallBack;
import ultimateclaim.events.RideableMoveWithPlayerCallBack;

@Mixin(Entity.class)
public class EntityMixin {
	
	private HashMap<String, BlockPos> playersLastPos = new HashMap<>();
	
	@Inject(method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V", at = @At("INVOKE"), cancellable = true)
	private void move(MovementType movementType, Vec3d movement, CallbackInfo info) {
		Entity entity = (Entity)(Object)this;
		if(movement.x == 0 && movement.y == 0 && movement.z == 0) return;
		if (entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) entity;
			if(movementType.equals(MovementType.PLAYER)) {
				BlockPos pos = player.getBlockPos();
				if(playersLastPos.containsKey(player.getEntityName())) {
					BlockPos lastPos = playersLastPos.get(player.getEntityName());
					if(lastPos == pos) return;
					ActionResult result = PlayerMoveCallBack.EVENT.invoker().onMove(player.getWorld(), player, lastPos, pos);
					if(result != ActionResult.PASS) 
						player.teleport(player.getX()-movement.x, pos.getY(), player.getZ()-movement.z);
					playersLastPos.replace(player.getEntityName(),  player.getBlockPos());
				}else
					playersLastPos.put(player.getEntityName(), player.getBlockPos());
			}
		}else if(entity.hasPassengers()) {
			if (entity.getFirstPassenger() instanceof PlayerEntity) {
				PlayerEntity player = (PlayerEntity) entity.getFirstPassenger();
				BlockPos pos = player.getBlockPos();
				if(playersLastPos.containsKey(player.getEntityName())) {
					BlockPos lastPos = playersLastPos.get(player.getEntityName());
					if(lastPos == pos) return;
					ActionResult result = RideableMoveWithPlayerCallBack.EVENT.invoker().onMove(player.getWorld(), player, entity, lastPos, pos);
					if(result != ActionResult.PASS) {
						player.dismountVehicle();
						player.teleport(player.getX()-movement.x, player.getY()+0.5, player.getZ()-movement.z);
					}
					playersLastPos.replace(player.getEntityName(),  player.getBlockPos());
				}else
					playersLastPos.put(player.getEntityName(), player.getBlockPos());
			}
		}
	}
}
