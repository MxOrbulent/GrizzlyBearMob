//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.aqupd.grizzlybear.entities;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Predicate;

import com.aqupd.grizzlybear.Main;
import com.aqupd.grizzlybear.ai.GrizzlyBearFishGoal;
import com.aqupd.grizzlybear.utils.AqLogger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.Durations;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.DefaultAttributeContainer.Builder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.MessageType;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IntRange;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class GrizzlyBearEntity extends AnimalEntity implements Angerable {
    private static final TrackedData<Boolean> WARNING;
    private static final TrackedData<Boolean> INRAGEMODE;
    private float lastWarningAnimationProgress;
    private float warningAnimationProgress;
    private int warningSoundCooldown;
    private static final IntRange ANGER_TIME_RANGE;
    private static final Ingredient LOVINGFOOD;
    private int angerTime;
    private UUID targetUuid;
    //angle is simply a float value used for drawing a particle circle around the bear, was used for debugging
    //It will remain incase we need to see the GENERIC FOLLOW RANGE of a bear again.
    //public float angle = 0f;
    private EntityAttributeModifier rageMovementSpeed;

    private int rageModeTimeInTicks;


    public GrizzlyBearEntity(EntityType<? extends GrizzlyBearEntity> entityType, World world) {
        super(entityType, world);
    }



    public boolean isInRageMode() {
        return this.dataTracker.get(INRAGEMODE);
    }

    private void setInRageMode(boolean rage) {
        this.dataTracker.set(INRAGEMODE, rage);
    }

    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return Main.GRIZZLYBEAR.create(world);
    }

    public boolean isBreedingItem(ItemStack stack) {
        return LOVINGFOOD.test(stack);
    }

    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        boolean bl = this.isBreedingItem(player.getStackInHand(hand));
        if (!bl && !player.shouldCancelInteraction()) {
            return ActionResult.success(this.world.isClient);
        } else {
            ActionResult actionResult = super.interactMob(player, hand);
            return actionResult;
        }
    }

    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new GrizzlyBearEntity.AttackGoal());
        this.goalSelector.add(1, new GrizzlyBearEntity.GrizzlyBearEscapeDangerGoal());
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0D));
        this.goalSelector.add(3, new TemptGoal(this, 1.0D, false, LOVINGFOOD));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.25D));
        this.goalSelector.add(5, new GrizzlyBearFishGoal(((GrizzlyBearEntity)(Object)this),1.0D,20));
        this.goalSelector.add(5, new WanderAroundGoal(this, 1.0D));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new GrizzlyBearEntity.GrizzlyBearRevengeGoal());
        this.targetSelector.add(2, new GrizzlyBearEntity.FollowPlayersGoal());
        this.targetSelector.add(3, new FollowTargetGoal<>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(4, new FollowTargetGoal(this, FoxEntity.class, 10, true, true, (Predicate) null));
        this.targetSelector.add(4, new FollowTargetGoal(this, RabbitEntity.class, 10, true, true, (Predicate) null));
        this.targetSelector.add(4, new FollowTargetGoal(this, ChickenEntity.class, 10, true, true, (Predicate) null));
        this.targetSelector.add(5, new UniversalAngerGoal(this, false));


    }

    public static Builder createGrizzlyBearAttributes() {

        return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, Main.GENERIC_MAX_HEALTH_CONFIG).add(EntityAttributes.GENERIC_FOLLOW_RANGE, Main.GENERIC_FOLLOW_RANGE_CONFIG).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, Main.GENERIC_MOVEMENT_SPEED_CONFIG).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, Main.GENERIC_ATTACK_DAMAGE_CONFIG);

    }

    public void readCustomDataFromNbt(NbtCompound tag) {
        super.readCustomDataFromNbt(tag);
        this.angerFromTag((ServerWorld)this.world, tag);
    }

    public void writeCustomDataToNbt(NbtCompound tag) {
        super.writeCustomDataToNbt(tag);
        this.writeAngerToNbt(tag);
    }

    public void chooseRandomAngerTime() {
        this.setAngerTime(ANGER_TIME_RANGE.choose(this.random));
    }

    public void setAngerTime(int ticks) {
        this.angerTime = ticks;
    }

    public int getAngerTime() {
        return this.angerTime;
    }

    public void setAngryAt(@Nullable UUID uuid) {
        this.targetUuid = uuid;
    }

    public UUID getAngryAt() {
        return this.targetUuid;
    }

    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? Main.GRIZZLY_BEAR_AMBIENT_BABY : Main.GRIZZLY_BEAR_AMBIENT;
    }
    //We can use this to implement a rage mechanic on hit.
    protected SoundEvent getHurtSound(DamageSource source) {
        if (!this.isInRageMode() && this.getHealth() < 15f && new Random().nextInt(3) == 1 && source.getAttacker() instanceof PlayerEntity && source.getAttacker().getEntityWorld().getDifficulty().getId() >= Main.DifficultyForRageMode && Main.DoUseRageMode) {
            this.setInRageMode(true);

            this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).addPersistentModifier(rageMovementSpeed);
            //If we want to spawn rage particles
            if (Main.DoSpawnRageParticles) {
                DefaultParticleType angryParticle = ParticleTypes.ANGRY_VILLAGER;
                //DustParticleEffect dust = new DustParticleEffect(0.7F,0.0F,0.0F,0.3F);
                //If some math wiz can do it, please fill in the correct math to obtain a position slightly above the bears eyes. You'd
                //probably need to do some maths with the model to get a corresponding position in the world relative to the bear etc.
                //double eyeXPos = 0D;
                //double eyeZPos = 0D;
                //End math wiz
                for(int i = 0; i < 14; ++i) {
                    double d = random.nextGaussian() * 0.75D;
                    double e = random.nextGaussian() * 0.75D;
                    double f = random.nextGaussian() * 0.75D;
                    ServerWorld serverworldyay = (ServerWorld) this.world;
                    serverworldyay.spawnParticles(angryParticle,this.getX(), this.getY() + 1D,this.getZ(),2,d,e,f,0.5D);

                    //serverworldyay.spawnParticles(dust,eyeXPos, this.getY() + 0.5D,eyeZPos,2,d,e,f,0.1D);
                }
            }
            //Playing the warning sound directly after eachother with different pitches produces an absolutely terrifying sound letting
            //players know that the bear is seriously pissed off.
            this.playSound(Main.GRIZZLY_BEAR_WARNING, 10.0F, 0.3F);
            this.playSound(Main.GRIZZLY_BEAR_WARNING, 10.0F, 0.5F);
            this.setRageModeTimeInTicks(Main.RageModeTimeInTicks);
        } else {
            //The code in this else statement is purely for debugging, do not change debug to true in main for a production environment (aka when not testing stuff).

            if (Main.DebugMod = true) {


            if (source.getAttacker() instanceof PlayerEntity) {
                source.getAttacker().getServer().getCommandManager().execute(source.getAttacker().getServer().getCommandSource(),"/say (AddedWith_getServer)DmgSource: "+source.getName() + " Attacker: " + source.getAttacker().getEntityName());

                source.getAttacker().getServer().getCommandManager().execute(source.getAttacker().
                            getServer().getCommandSource(), "/say (getServer)Attacker is instanceof PlayerEntity");
                    source.getAttacker().getServer().getCommandManager().execute(source.getAttacker().
                            getServer().getCommandSource(), "/say (getServer)Health of the bear is: "+this.getHealth());
                    source.getAttacker().getServer().getCommandManager().execute(source.getAttacker().
                            getServer().getCommandSource(), "/say (getServer)difficulty is " + source.getAttacker().getEntityWorld().getDifficulty().getName());

                }
            }


            }






        return Main.GRIZZLY_BEAR_HURT;
    }

    protected SoundEvent getDeathSound() {
        return Main.GRIZZLY_BEAR_DEATH;
    }

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(Main.GRIZZLY_BEAR_STEP, 0.15F, 1.0F);
    }

    protected void playWarningSound() {
        if (this.warningSoundCooldown <= 0) {
            this.playSound(Main.GRIZZLY_BEAR_WARNING, 1.0F, this.getSoundPitch());
            this.warningSoundCooldown = 40;
        }

    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(WARNING, false);
        this.dataTracker.startTracking(INRAGEMODE, false);

    }

    public void tick() {
        super.tick();
        if (this.world.isClient) {
            if (this.warningAnimationProgress != this.lastWarningAnimationProgress) {
                this.calculateDimensions();
            }

            this.lastWarningAnimationProgress = this.warningAnimationProgress;
            if (this.isWarning()) {
                this.warningAnimationProgress = MathHelper.clamp(this.warningAnimationProgress + 1.0F, 0.0F, 6.0F);
            } else {
                this.warningAnimationProgress = MathHelper.clamp(this.warningAnimationProgress - 1.0F, 0.0F, 6.0F);
            }
        }

        if (this.warningSoundCooldown > 0) {
            --this.warningSoundCooldown;
        }
        //Decreasing the number of ticks until rage mode ends, but only if the bear is not angry at anyone.
        if (this.getRageModeTimeInTicks() > 0 && this.getAngryAt() == null) {
            this.setRageModeTimeInTicks(this.getRageModeTimeInTicks() - 1);
            if (this.getRageModeTimeInTicks() == 0) {
                this.setInRageMode(false);
                this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).removeModifier(this.rageMovementSpeed.getId());
            }
        }

        if (!this.world.isClient) {
            this.tickAngerLogic((ServerWorld)this.world, true);
            if (this.isInRageMode() == true && this.getServer().getTicks() % 4==0 && this.getAngryAt() == null) {
                //We want the bear to get angry at any player that comes to close while it is in rage mode.
                if (((ServerWorld) this.world).getClosestPlayer(this.getX(),this.getY(),this.getZ(),this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).getValue() - 10.0D,true) != null) {
                    this.setAngryAt(((ServerWorld) this.world).getClosestPlayer(this.getX(),this.getY(),this.getZ(),this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).getValue() - 10.0D + 5.0D,true).getUuid());
                }

                }
            //workaround because sometimes the bear still get's angry at creative mode players.
            if (this.getAngryAt() != null) {
                if (this.getTarget() instanceof PlayerEntity) {
                    if (((PlayerEntity) this.getTarget()).isCreative()) {
                        this.setAngryAt(null);
                    }
                }
                //Debug code that helps show roughly the generic follow range. Do not remove.
                /*float radius = (float) this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE - 10.0D).getValue();

                //Code to draw aggro range
                DefaultParticleType parameters = ParticleTypes.FLAME;
                double x = (radius * Math.sin(angle));
                double z = (radius * Math.cos(angle));
                ServerWorld serverworldyay = (ServerWorld) this.world;
                serverworldyay.spawnParticles(parameters,this.getX()+x, this.getY() + 1D,this.getZ()+z,3,0D,0D,0D,0D);
                serverworldyay.spawnParticles(parameters,this.getX()+x, this.getY() + 1D,this.getZ()+z,1,0.01D,0.01D,0.01D,0D);
                serverworldyay.spawnParticles(parameters,this.getX()+x, this.getY() + 1D,this.getZ()+z,1,0.02D,0.02D,0.02D,0D);
                angle += 0.05;
                this.getServer().getCommandManager().execute(this.getServer().getCommandSource(), "/say I'M MAD AT UUID "+this.getAngryAt().toString()+"!!!");*/
                if (this.getServer().getPlayerManager().getPlayer(this.getAngryAt()) == null || ((ServerWorld) this.world).getClosestPlayer(this.getX(),this.getY(),this.getZ(),this.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).getValue() - 10.0D,true) == null && !(this.getTarget() instanceof PlayerEntity)) {

                    this.setAngryAt(null);
                }
                if (this.getTarget() != null) {
                    if (this.getTarget().isDead()) {
                        this.setAngryAt(null);
                    }

                }
            }

        }

    }

    public EntityDimensions getDimensions(EntityPose pose) {
        if (this.warningAnimationProgress > 0.0F) {
            float f = this.warningAnimationProgress / 6.0F;
            float g = 1.0F + f;
            return super.getDimensions(pose).scaled(1.0F, g);
        } else {
            return super.getDimensions(pose);
        }
    }

    public boolean tryAttack(Entity target) {
        boolean bl = target.damage(DamageSource.mob(this), (float)((int)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE)));
        if (bl) {
            this.dealDamage(this, target);
        }

        return bl;
    }

    public boolean isWarning() {
        return (Boolean)this.dataTracker.get(WARNING);
    }

    public void setWarning(boolean warning) {
        this.dataTracker.set(WARNING, warning);
    }

    @Environment(EnvType.CLIENT)
    public float getWarningAnimationProgress(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.lastWarningAnimationProgress, this.warningAnimationProgress) / 6.0F;
    }

    protected float getBaseMovementSpeedMultiplier() {
        return 0.98F;
    }

    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityTag) {
        if (entityData == null) {
            entityData = new PassiveData(1.0F);
        }
        double CalculatePercentageOfGenericMSForBuff = this.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).getValue()*Main.PercentageAsDoubleForRageModeMSSpeedBuff;

        this.rageMovementSpeed = new EntityAttributeModifier(UUID.randomUUID(),"grizzlybear_ragems",CalculatePercentageOfGenericMSForBuff,EntityAttributeModifier.Operation.ADDITION);
        return super.initialize(world, difficulty, spawnReason, (EntityData)entityData, entityTag);
    }

    static {
        WARNING = DataTracker.registerData(GrizzlyBearEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        INRAGEMODE = DataTracker.registerData(GrizzlyBearEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
        ANGER_TIME_RANGE = Durations.betweenSeconds(20, 39);
        LOVINGFOOD = Ingredient.ofItems(Items.COD, Items.SALMON, Items.SWEET_BERRIES);
    }

    public int getRageModeTimeInTicks() {
        return rageModeTimeInTicks;
    }

    public void setRageModeTimeInTicks(int rageModeTimeInTicks) {
        this.rageModeTimeInTicks = rageModeTimeInTicks;
    }

    class GrizzlyBearEscapeDangerGoal extends EscapeDangerGoal {
        public GrizzlyBearEscapeDangerGoal() {
            super(GrizzlyBearEntity.this, 2.0D);
        }

        public boolean canStart() {
            return !GrizzlyBearEntity.this.isBaby() && !GrizzlyBearEntity.this.isOnFire() ? false : super.canStart();
        }
    }

    class AttackGoal extends MeleeAttackGoal {
        public AttackGoal() {
            super(GrizzlyBearEntity.this, 1.25D, true);
        }

        protected void attack(LivingEntity target, double squaredDistance) {
            double d = this.getSquaredMaxAttackDistance(target);
            if (squaredDistance <= d && this.method_28347()) {
                this.method_28346();
                this.mob.tryAttack(target);
                GrizzlyBearEntity.this.setWarning(false);
            } else if (squaredDistance <= d * 2.0D) {
                if (this.method_28347()) {
                    GrizzlyBearEntity.this.setWarning(false);
                    this.method_28346();
                }

                if (this.method_28348() <= 10) {
                    GrizzlyBearEntity.this.setWarning(true);
                    GrizzlyBearEntity.this.playWarningSound();
                }
            } else {
                this.method_28346();
                GrizzlyBearEntity.this.setWarning(false);
            }

        }

        public void stop() {
            GrizzlyBearEntity.this.setWarning(false);
            super.stop();
        }

        protected double getSquaredMaxAttackDistance(LivingEntity entity) {
            return (double)(4.0F + entity.getWidth());
        }
    }

    class FollowPlayersGoal extends FollowTargetGoal<PlayerEntity> {
        public FollowPlayersGoal() {
            super(GrizzlyBearEntity.this, PlayerEntity.class, 20, true, true, (Predicate)null);
        }

        public boolean canStart() {
            if (GrizzlyBearEntity.this.isBaby()) {
                return false;
            } else {
                if (super.canStart()) {
                    List<GrizzlyBearEntity> list = GrizzlyBearEntity.this.world.getNonSpectatingEntities(GrizzlyBearEntity.class, GrizzlyBearEntity.this.getBoundingBox().expand(8.0D, 4.0D, 8.0D));
                    Iterator var2 = list.iterator();

                    while(var2.hasNext()) {
                        GrizzlyBearEntity grizzlyBearEntity = (GrizzlyBearEntity)var2.next();
                        if (grizzlyBearEntity.isBaby()) {
                            return true;
                        }
                    }
                }

                return false;
            }
        }

        protected double getFollowRange() {
            return super.getFollowRange() * 0.5D;
        }
    }

    class GrizzlyBearRevengeGoal extends RevengeGoal {
        public GrizzlyBearRevengeGoal() {
            super(GrizzlyBearEntity.this, new Class[0]);
        }

        public void start() {
            super.start();
            if (GrizzlyBearEntity.this.isBaby()) {
                this.callSameTypeForRevenge();
                this.stop();
            }

        }

        protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
            if (mob instanceof GrizzlyBearEntity && !mob.isBaby()) {
                super.setMobEntityTarget(mob, target);
            }

        }
    }
}
