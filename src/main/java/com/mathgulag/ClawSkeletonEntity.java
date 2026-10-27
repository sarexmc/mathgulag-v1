package com.mathgulag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.*;
import net.minecraft.world.World;

public class ClawSkeletonEntity extends HostileEntity {
    public ClawSkeletonEntity(EntityType<? extends HostileEntity> type, World world) { super(type, world); }
    public static DefaultAttributeContainer.Builder attrs() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 40)
            .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6)
            .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32);
    }
    @Override protected void initGoals() {
        goalSelector.add(1, new SwimGoal(this));
        goalSelector.add(2, new MeleeAttackGoal(this, 1.2, false));
        goalSelector.add(3, new WanderAroundFarGoal(this, 1.0));
        goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8f));
        goalSelector.add(5, new LookAroundGoal(this));
        targetSelector.add(1, new RevengeGoal(this));
        targetSelector.add(2, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.ENTITY_SKELETON_AMBIENT; }
    @Override protected SoundEvent getHurtSound(net.minecraft.entity.damage.DamageSource s) { return SoundEvents.ENTITY_SKELETON_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.ENTITY_SKELETON_DEATH; }
}
