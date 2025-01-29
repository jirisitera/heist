package japi.heist.camera;

import japi.heist.Entities;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumSet;
import java.util.Optional;

public class CameraEntity extends GolemEntity implements VariantHolder<Optional<DyeColor>>, Monster {
  protected static final TrackedData<Direction> ATTACHED_FACE = DataTracker.registerData(CameraEntity.class, TrackedDataHandlerRegistry.FACING);
  protected static final TrackedData<Byte> PEEK_AMOUNT = DataTracker.registerData(CameraEntity.class, TrackedDataHandlerRegistry.BYTE);
  protected static final TrackedData<Byte> COLOR = DataTracker.registerData(CameraEntity.class, TrackedDataHandlerRegistry.BYTE);
  static final Vector3f SOUTH_VECTOR = Util.make(() -> {
    Vec3i vec3i = Direction.SOUTH.getVector();
    return new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
  });
  private static final Identifier COVERED_ARMOR_MODIFIER_ID = Identifier.ofVanilla("covered");
  private static final EntityAttributeModifier COVERED_ARMOR_BONUS = new EntityAttributeModifier(
      COVERED_ARMOR_MODIFIER_ID, 20.0, EntityAttributeModifier.Operation.ADD_VALUE
  );
  private float prevOpenProgress;
  private float openProgress;
  @Nullable
  private BlockPos prevAttachedBlock;
  private int teleportLerpTimer;
  
  public CameraEntity(EntityType<? extends CameraEntity> entityType, World world) {
    super(entityType, world);
    this.experiencePoints = 5;
    this.lookControl = new CameraEntity.ShulkerLookControl(this);
  }
  
  public static DefaultAttributeContainer.Builder createCameraAttributes() {
    return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 30.0);
  }
  
  private static float getExtraLength(float openProgress) {
    return 0.5F - MathHelper.sin((0.5F + openProgress) * (float) Math.PI) * 0.5F;
  }
  
  public static Box calculateBoundingBox(float scale, Direction facing, float extraLength, Vec3d pos) {
    return calculateBoundingBox(scale, facing, -1.0F, extraLength, pos);
  }
  
  public static Box calculateBoundingBox(float scale, Direction facing, float prevExtraLength, float extraLength, Vec3d pos) {
    Box box = new Box((-scale) * 0.5, 0.0, (-scale) * 0.5, scale * 0.5, scale, scale * 0.5);
    double d = Math.max(prevExtraLength, extraLength);
    double e = Math.min(prevExtraLength, extraLength);
    Box box2 = box.stretch(
            facing.getOffsetX() * d * scale, facing.getOffsetY() * d * scale, facing.getOffsetZ() * d * scale
        )
        .shrink(
            (-facing.getOffsetX()) * (1.0 + e) * scale,
            (-facing.getOffsetY()) * (1.0 + e) * scale,
            (-facing.getOffsetZ()) * (1.0 + e) * scale
        );
    return box2.offset(pos.x, pos.y, pos.z);
  }
  
  @Override
  protected void initGoals() {
    this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F, 0.02F, true));
    this.goalSelector.add(4, new CameraEntity.ShootBulletGoal());
    this.goalSelector.add(7, new CameraEntity.PeekGoal());
    this.goalSelector.add(8, new LookAroundGoal(this));
    this.targetSelector.add(1, new RevengeGoal(this, this.getClass()).setGroupRevenge());
    this.targetSelector.add(2, new CameraEntity.TargetPlayerGoal(this));
    this.targetSelector.add(3, new CameraEntity.TargetOtherTeamGoal(this));
  }
  
  @Override
  protected Entity.MoveEffect getMoveEffect() {
    return Entity.MoveEffect.NONE;
  }
  
  @Override
  public SoundCategory getSoundCategory() {
    return SoundCategory.HOSTILE;
  }
  
  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.ENTITY_SHULKER_AMBIENT;
  }
  
  @Override
  public void playAmbientSound() {
    if (!this.isClosed()) {
      super.playAmbientSound();
    }
  }
  
  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.ENTITY_SHULKER_DEATH;
  }
  
  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return this.isClosed() ? SoundEvents.ENTITY_SHULKER_HURT_CLOSED : SoundEvents.ENTITY_SHULKER_HURT;
  }
  
  @Override
  protected void initDataTracker(DataTracker.Builder builder) {
    super.initDataTracker(builder);
    builder.add(ATTACHED_FACE, Direction.DOWN);
    builder.add(PEEK_AMOUNT, (byte) 0);
    builder.add(COLOR, (byte) 16);
  }
  
  @Override
  protected BodyControl createBodyControl() {
    return new CameraEntity.ShulkerBodyControl(this);
  }
  
  @Override
  public void readCustomDataFromNbt(NbtCompound nbt) {
    super.readCustomDataFromNbt(nbt);
    this.setAttachedFace(Direction.byId(nbt.getByte("AttachFace")));
    this.dataTracker.set(PEEK_AMOUNT, nbt.getByte("Peek"));
    if (nbt.contains("Color", NbtElement.NUMBER_TYPE)) {
      this.dataTracker.set(COLOR, nbt.getByte("Color"));
    }
  }
  
  @Override
  public void writeCustomDataToNbt(NbtCompound nbt) {
    super.writeCustomDataToNbt(nbt);
    nbt.putByte("AttachFace", (byte) this.getAttachedFace().getId());
    nbt.putByte("Peek", this.dataTracker.get(PEEK_AMOUNT));
    nbt.putByte("Color", this.dataTracker.get(COLOR));
  }
  
  @Override
  public void tick() {
    super.tick();
    if (!this.getWorld().isClient && !this.hasVehicle() && !this.canStay(this.getBlockPos(), this.getAttachedFace())) {
      this.tryAttachOrTeleport();
    }
    
    if (this.tickOpenProgress()) {
      this.moveEntities();
    }
    
    if (this.getWorld().isClient) {
      if (this.teleportLerpTimer > 0) {
        this.teleportLerpTimer--;
      } else {
        this.prevAttachedBlock = null;
      }
    }
  }
  
  private void tryAttachOrTeleport() {
    Direction direction = this.findAttachSide(this.getBlockPos());
    if (direction != null) {
      this.setAttachedFace(direction);
    } else {
      this.tryTeleport();
    }
  }
  
  @Override
  protected Box calculateDefaultBoundingBox(Vec3d pos) {
    float f = getExtraLength(this.openProgress);
    Direction direction = this.getAttachedFace().getOpposite();
    return calculateBoundingBox(this.getScale(), direction, f, pos);
  }
  
  private boolean tickOpenProgress() {
    this.prevOpenProgress = this.openProgress;
    float f = this.getPeekAmount() * 0.01F;
    if (this.openProgress == f) {
      return false;
    } else {
      if (this.openProgress > f) {
        this.openProgress = MathHelper.clamp(this.openProgress - 0.05F, f, 1.0F);
      } else {
        this.openProgress = MathHelper.clamp(this.openProgress + 0.05F, 0.0F, f);
      }
      
      return true;
    }
  }
  
  private void moveEntities() {
    this.refreshPosition();
    float f = getExtraLength(this.openProgress);
    float g = getExtraLength(this.prevOpenProgress);
    Direction direction = this.getAttachedFace().getOpposite();
    float h = (f - g) * this.getScale();
    if (h > 0.0F) {
      for (Entity entity : this.getWorld()
          .getOtherEntities(
              this,
              calculateBoundingBox(this.getScale(), direction, g, f, this.getPos()),
              EntityPredicates.EXCEPT_SPECTATOR.and(entityX -> !entityX.isConnectedThroughVehicle(this))
          )) {
        if (!(entity instanceof CameraEntity) && !entity.noClip) {
          entity.move(
              MovementType.SHULKER,
              new Vec3d(h * direction.getOffsetX(), h * direction.getOffsetY(), h * direction.getOffsetZ())
          );
        }
      }
    }
  }
  
  @Override
  public boolean startRiding(Entity entity, boolean force) {
    if (this.getWorld().isClient()) {
      this.prevAttachedBlock = null;
      this.teleportLerpTimer = 0;
    }
    
    this.setAttachedFace(Direction.DOWN);
    return super.startRiding(entity, force);
  }
  
  @Override
  public void stopRiding() {
    super.stopRiding();
    if (this.getWorld().isClient) {
      this.prevAttachedBlock = this.getBlockPos();
    }
    
    this.prevBodyYaw = 0.0F;
    this.bodyYaw = 0.0F;
  }
  
  @Nullable
  @Override
  public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
    this.setYaw(0.0F);
    this.headYaw = this.getYaw();
    this.resetPosition();
    return super.initialize(world, difficulty, spawnReason, entityData);
  }
  
  @Override
  public void move(MovementType type, Vec3d movement) {
    if (type == MovementType.SHULKER_BOX) {
      this.tryTeleport();
    } else {
      super.move(type, movement);
    }
  }
  
  @Override
  public Vec3d getVelocity() {
    return Vec3d.ZERO;
  }
  
  @Override
  public void setVelocity(Vec3d velocity) {
  }
  
  @Override
  public void setPosition(double x, double y, double z) {
    BlockPos blockPos = this.getBlockPos();
    if (this.hasVehicle()) {
      super.setPosition(x, y, z);
    } else {
      super.setPosition(MathHelper.floor(x) + 0.5, MathHelper.floor(y + 0.5), MathHelper.floor(z) + 0.5);
    }
    
    if (this.age != 0) {
      BlockPos blockPos2 = this.getBlockPos();
      if (!blockPos2.equals(blockPos)) {
        this.dataTracker.set(PEEK_AMOUNT, (byte) 0);
        this.velocityDirty = true;
        if (this.getWorld().isClient && !this.hasVehicle() && !blockPos2.equals(this.prevAttachedBlock)) {
          this.prevAttachedBlock = blockPos;
          this.teleportLerpTimer = 6;
          this.lastRenderX = this.getX();
          this.lastRenderY = this.getY();
          this.lastRenderZ = this.getZ();
        }
      }
    }
  }
  
  @Nullable
  protected Direction findAttachSide(BlockPos pos) {
    for (Direction direction : Direction.values()) {
      if (this.canStay(pos, direction)) {
        return direction;
      }
    }
    
    return null;
  }
  
  boolean canStay(BlockPos pos, Direction direction) {
    if (this.isInvalidPosition(pos)) {
      return false;
    } else {
      Direction direction2 = direction.getOpposite();
      if (!this.getWorld().isDirectionSolid(pos.offset(direction), this, direction2)) {
        return false;
      } else {
        Box box = calculateBoundingBox(this.getScale(), direction2, 1.0F, pos.toBottomCenterPos()).contract(1.0E-6);
        return this.getWorld().isSpaceEmpty(this, box);
      }
    }
  }
  
  private boolean isInvalidPosition(BlockPos pos) {
    BlockState blockState = this.getWorld().getBlockState(pos);
    if (blockState.isAir()) {
      return false;
    } else {
      boolean bl = blockState.isOf(Blocks.MOVING_PISTON) && pos.equals(this.getBlockPos());
      return !bl;
    }
  }
  
  protected boolean tryTeleport() {
    if (!this.isAiDisabled() && this.isAlive()) {
      BlockPos blockPos = this.getBlockPos();
      
      for (int i = 0; i < 5; i++) {
        BlockPos blockPos2 = blockPos.add(
            MathHelper.nextBetween(this.random, -8, 8), MathHelper.nextBetween(this.random, -8, 8), MathHelper.nextBetween(this.random, -8, 8)
        );
        if (blockPos2.getY() > this.getWorld().getBottomY()
            && this.getWorld().isAir(blockPos2)
            && this.getWorld().getWorldBorder().contains(blockPos2)
            && this.getWorld().isSpaceEmpty(this, new Box(blockPos2).contract(1.0E-6))) {
          Direction direction = this.findAttachSide(blockPos2);
          if (direction != null) {
            this.detach();
            this.setAttachedFace(direction);
            this.playSound(SoundEvents.ENTITY_SHULKER_TELEPORT, 1.0F, 1.0F);
            this.setPosition(blockPos2.getX() + 0.5, blockPos2.getY(), blockPos2.getZ() + 0.5);
            this.getWorld().emitGameEvent(GameEvent.TELEPORT, blockPos, GameEvent.Emitter.of(this));
            this.dataTracker.set(PEEK_AMOUNT, (byte) 0);
            this.setTarget(null);
            return true;
          }
        }
      }
      
    }
    return false;
  }
  
  @Override
  public void updateTrackedPositionAndAngles(double x, double y, double z, float yaw, float pitch, int interpolationSteps) {
    this.bodyTrackingIncrements = 0;
    this.setPosition(x, y, z);
    this.setRotation(yaw, pitch);
  }
  
  @Override
  public boolean damage(ServerWorld world, DamageSource source, float amount) {
    if (this.isClosed()) {
      Entity entity = source.getSource();
      if (entity instanceof PersistentProjectileEntity) {
        return false;
      }
    }
    
    if (!super.damage(world, source, amount)) {
      return false;
    } else {
      if (this.getHealth() < this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
        this.tryTeleport();
      } else if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
        Entity entity = source.getSource();
        if (entity != null && entity.getType() == EntityType.SHULKER_BULLET) {
          this.spawnNewCamera();
        }
      }
      
      return true;
    }
  }
  
  private boolean isClosed() {
    return this.getPeekAmount() == 0;
  }
  
  private void spawnNewCamera() {
    Vec3d vec3d = this.getPos();
    Box box = this.getBoundingBox();
    if (!this.isClosed() && this.tryTeleport()) {
      int i = this.getWorld().getEntitiesByType(Entities.CAMERA, box.expand(8.0), Entity::isAlive).size();
      float f = (i - 1) / 5.0F;
      if (this.getWorld().random.nextFloat() >= f) {
        CameraEntity cameraEntity = Entities.CAMERA.create(this.getWorld(), SpawnReason.BREEDING);
        if (cameraEntity != null) {
          cameraEntity.setVariant(this.getVariant());
          cameraEntity.refreshPositionAfterTeleport(vec3d);
          this.getWorld().spawnEntity(cameraEntity);
        }
      }
    }
  }
  
  @Override
  public boolean isCollidable() {
    return this.isAlive();
  }
  
  public Direction getAttachedFace() {
    return this.dataTracker.get(ATTACHED_FACE);
  }
  
  private void setAttachedFace(Direction face) {
    this.dataTracker.set(ATTACHED_FACE, face);
  }
  
  @Override
  public void onTrackedDataSet(TrackedData<?> data) {
    if (ATTACHED_FACE.equals(data)) {
      this.setBoundingBox(this.calculateBoundingBox());
    }
    
    super.onTrackedDataSet(data);
  }
  
  private int getPeekAmount() {
    return this.dataTracker.get(PEEK_AMOUNT);
  }
  
  void setPeekAmount(int peekAmount) {
    if (!this.getWorld().isClient) {
      this.getAttributeInstance(EntityAttributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER_ID);
      if (peekAmount == 0) {
        this.getAttributeInstance(EntityAttributes.ARMOR).addPersistentModifier(COVERED_ARMOR_BONUS);
        this.playSound(SoundEvents.ENTITY_SHULKER_CLOSE, 1.0F, 1.0F);
        this.emitGameEvent(GameEvent.CONTAINER_CLOSE);
      } else {
        this.playSound(SoundEvents.ENTITY_SHULKER_OPEN, 1.0F, 1.0F);
        this.emitGameEvent(GameEvent.CONTAINER_OPEN);
      }
    }
    
    this.dataTracker.set(PEEK_AMOUNT, (byte) peekAmount);
  }
  
  public float getOpenProgress(float delta) {
    return MathHelper.lerp(delta, this.prevOpenProgress, this.openProgress);
  }
  
  @Override
  public void onSpawnPacket(EntitySpawnS2CPacket packet) {
    super.onSpawnPacket(packet);
    this.bodyYaw = 0.0F;
    this.prevBodyYaw = 0.0F;
  }
  
  @Override
  public int getMaxLookPitchChange() {
    return 180;
  }
  
  @Override
  public int getMaxHeadRotation() {
    return 180;
  }
  
  @Override
  public void pushAwayFrom(Entity entity) {
  }
  
  @Nullable
  public Vec3d getRenderPositionOffset(float tickDelta) {
    if (this.prevAttachedBlock != null && this.teleportLerpTimer > 0) {
      double d = (this.teleportLerpTimer - tickDelta) / 6.0;
      d *= d;
      d *= this.getScale();
      BlockPos blockPos = this.getBlockPos();
      double e = (blockPos.getX() - this.prevAttachedBlock.getX()) * d;
      double f = (blockPos.getY() - this.prevAttachedBlock.getY()) * d;
      double g = (blockPos.getZ() - this.prevAttachedBlock.getZ()) * d;
      return new Vec3d(-e, -f, -g);
    } else {
      return null;
    }
  }
  
  @Override
  protected float clampScale(float scale) {
    return Math.min(scale, 3.0F);
  }
  
  public Optional<DyeColor> getVariant() {
    return Optional.ofNullable(this.getColor());
  }
  
  public void setVariant(Optional<DyeColor> optional) {
    this.dataTracker.set(COLOR, optional.map(color -> (byte) color.getId()).orElse((byte) 16));
  }
  
  @Nullable
  public DyeColor getColor() {
    byte b = this.dataTracker.get(COLOR);
    return b <= 15 ? DyeColor.byId(b) : null;
  }
  
  static class ShulkerBodyControl extends BodyControl {
    public ShulkerBodyControl(MobEntity mobEntity) {
      super(mobEntity);
    }
    
    @Override
    public void tick() {
    }
  }
  
  static class TargetOtherTeamGoal extends ActiveTargetGoal<LivingEntity> {
    public TargetOtherTeamGoal(CameraEntity camera) {
      super(camera, LivingEntity.class, 10, true, false, (entity, world) -> entity instanceof Monster);
    }
    
    @Override
    public boolean canStart() {
      return this.mob.getScoreboardTeam() != null && super.canStart();
    }
    
    @Override
    protected Box getSearchBox(double distance) {
      Direction direction = ((CameraEntity) this.mob).getAttachedFace();
      if (direction.getAxis() == Direction.Axis.X) {
        return this.mob.getBoundingBox().expand(4.0, distance, distance);
      } else {
        return direction.getAxis() == Direction.Axis.Z
            ? this.mob.getBoundingBox().expand(distance, distance, 4.0)
            : this.mob.getBoundingBox().expand(distance, 4.0, distance);
      }
    }
  }
  
  class PeekGoal extends Goal {
    private int counter;
    
    @Override
    public boolean canStart() {
      return CameraEntity.this.getTarget() == null
          && CameraEntity.this.random.nextInt(toGoalTicks(40)) == 0
          && CameraEntity.this.canStay(CameraEntity.this.getBlockPos(), CameraEntity.this.getAttachedFace());
    }
    
    @Override
    public boolean shouldContinue() {
      return CameraEntity.this.getTarget() == null && this.counter > 0;
    }
    
    @Override
    public void start() {
      this.counter = this.getTickCount(20 * (1 + CameraEntity.this.random.nextInt(3)));
      CameraEntity.this.setPeekAmount(30);
    }
    
    @Override
    public void stop() {
      if (CameraEntity.this.getTarget() == null) {
        CameraEntity.this.setPeekAmount(0);
      }
    }
    
    @Override
    public void tick() {
      this.counter--;
    }
  }
  
  class ShootBulletGoal extends Goal {
    private int counter;
    
    public ShootBulletGoal() {
      this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }
    
    @Override
    public boolean canStart() {
      LivingEntity livingEntity = CameraEntity.this.getTarget();
      return livingEntity != null && livingEntity.isAlive() && CameraEntity.this.getWorld().getDifficulty() != Difficulty.PEACEFUL;
    }
    
    @Override
    public void start() {
      this.counter = 20;
      CameraEntity.this.setPeekAmount(100);
    }
    
    @Override
    public void stop() {
      CameraEntity.this.setPeekAmount(0);
    }
    
    @Override
    public boolean shouldRunEveryTick() {
      return true;
    }
    
    @Override
    public void tick() {
      if (CameraEntity.this.getWorld().getDifficulty() != Difficulty.PEACEFUL) {
        this.counter--;
        LivingEntity livingEntity = CameraEntity.this.getTarget();
        if (livingEntity != null) {
          CameraEntity.this.getLookControl().lookAt(livingEntity, 180.0F, 180.0F);
          double d = CameraEntity.this.squaredDistanceTo(livingEntity);
          if (d < 400.0) {
            if (this.counter <= 0) {
              this.counter = 20 + CameraEntity.this.random.nextInt(10) * 20 / 2;
              CameraEntity.this.getWorld()
                  .spawnEntity(new ShulkerBulletEntity(CameraEntity.this.getWorld(), CameraEntity.this, livingEntity, CameraEntity.this.getAttachedFace().getAxis()));
              CameraEntity.this.playSound(
                  SoundEvents.ENTITY_SHULKER_SHOOT, 2.0F, (CameraEntity.this.random.nextFloat() - CameraEntity.this.random.nextFloat()) * 0.2F + 1.0F
              );
            }
          } else {
            CameraEntity.this.setTarget(null);
          }
          
          super.tick();
        }
      }
    }
  }
  
  class ShulkerLookControl extends LookControl {
    public ShulkerLookControl(final MobEntity entity) {
      super(entity);
    }
    
    @Override
    protected void clampHeadYaw() {
    }
    
    @Override
    protected Optional<Float> getTargetYaw() {
      Direction direction = CameraEntity.this.getAttachedFace().getOpposite();
      Vector3f vector3f = direction.getRotationQuaternion().transform(new Vector3f(CameraEntity.SOUTH_VECTOR));
      Vec3i vec3i = direction.getVector();
      Vector3f vector3f2 = new Vector3f(vec3i.getX(), vec3i.getY(), vec3i.getZ());
      vector3f2.cross(vector3f);
      double d = this.x - this.entity.getX();
      double e = this.y - this.entity.getEyeY();
      double f = this.z - this.entity.getZ();
      Vector3f vector3f3 = new Vector3f((float) d, (float) e, (float) f);
      float g = vector3f2.dot(vector3f3);
      float h = vector3f.dot(vector3f3);
      return Math.abs(g) <= 1.0E-5F && Math.abs(h) <= 1.0E-5F
          ? Optional.empty()
          : Optional.of((float) (MathHelper.atan2(-g, h) * 180.0F / Math.PI));
    }
    
    @Override
    protected Optional<Float> getTargetPitch() {
      return Optional.of(0.0F);
    }
  }
  
  /**
   * A hostile target goal on players.
   */
  class TargetPlayerGoal extends ActiveTargetGoal<PlayerEntity> {
    public TargetPlayerGoal(final CameraEntity camera) {
      super(camera, PlayerEntity.class, true);
    }
    
    @Override
    public boolean canStart() {
      return CameraEntity.this.getWorld().getDifficulty() != Difficulty.PEACEFUL && super.canStart();
    }
    
    @Override
    protected Box getSearchBox(double distance) {
      Direction direction = ((CameraEntity) this.mob).getAttachedFace();
      if (direction.getAxis() == Direction.Axis.X) {
        return this.mob.getBoundingBox().expand(4.0, distance, distance);
      } else {
        return direction.getAxis() == Direction.Axis.Z
            ? this.mob.getBoundingBox().expand(distance, distance, 4.0)
            : this.mob.getBoundingBox().expand(distance, 4.0, distance);
      }
    }
  }
}
