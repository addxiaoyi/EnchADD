package net.enchadd.mod.mixin;
import net.enchadd.mod.common.MobExperienceBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
@Mixin(LivingEntity.class)
abstract class MobExperienceMixin {
 @ModifyArg(method="dropExperience", at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"), index=2)
 private int enchadd$adjustDeathExperience(int experience) {
  LivingEntity victim=(LivingEntity)(Object)this;
  return MobExperienceBehavior.adjustDeathExperience(victim,victim.getLastHurtByPlayer(),experience);
 }
}
