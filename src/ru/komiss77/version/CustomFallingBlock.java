package ru.komiss77.version;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class CustomFallingBlock extends FallingBlockEntity {

  //private boolean jumping;
  //private float speed;
  //Vec3 move = Vec3.ZERO;
  float up;

  public CustomFallingBlock(ServerLevel level, double x, double y, double z, BlockState state) {
    super(level, x, y, z, state);
  }

  @Override
  public void tick() {
//if (1==1)return;
    //super.tick();
    //Ostrov.log_warn("FB tick "+time);
    //if (this.blockState.isAir()) {
    //  this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN); // CraftBukkit - add Bukkit remove cause
    //} else {
    //Block block = this.blockState.getBlock();
    //this.time++;
    this.applyGravity();

    //if (move != Vec3.ZERO) {
    //double d = move.lengthSqr();
    //if (d > 1.0E-7) {
    ////   this.setDeltaMovement(move);
    //   this.hurtMarked = true;
    //  move = move.scale(0.3);
    //}
    this.move(MoverType.SELF, this.getDeltaMovement());
    //}
//Ostrov.log_warn("move="+move);
    this.applyEffectsFromBlocks();
    // Paper start - Configurable falling blocks height nerf
      /*if (this.level().paperConfig().fixes.fallingBlockHeightNerf.test(v -> this.getY() > v)) {
        if (this.dropItem && this.level() instanceof final ServerLevel serverLevel && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
          this.spawnAtLocation(serverLevel, block);
        }
        this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.OUT_OF_WORLD);
        return;
      }*/
    // Paper end - Configurable falling blocks height nerf
    this.handlePortal();

    //up *= 0.3;
    //this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
     /* if (this.level() instanceof ServerLevel serverLevel && (this.isAlive() || this.forceTickAfterTeleportToDuplicate)) {
        BlockPos blockPos = this.blockPosition();
        boolean flag = this.blockState.getBlock() instanceof ConcretePowderBlock;
        boolean flag1 = flag && this.level().getFluidState(blockPos).is(FluidTags.WATER);
        double d = this.getDeltaMovement().lengthSqr();
        if (flag && d > 1.0) {
          BlockHitResult blockHitResult = this.level()
              .clip(
                  new ClipContext(
                      new Vec3(this.xo, this.yo, this.zo), this.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.SOURCE_ONLY, this
                  )
              );
          if (blockHitResult.getType() != HitResult.Type.MISS && this.level().getFluidState(blockHitResult.getBlockPos()).is(FluidTags.WATER)) {
            blockPos = blockHitResult.getBlockPos();
            flag1 = true;
          }
        }*/

      /*  if (!this.onGround() && !flag1) {
          if ((this.time > 100 && autoExpire) && (blockPos.getY() <= this.level().getMinY() || blockPos.getY() > this.level().getMaxY()) || (this.time > 600 && autoExpire)) { // Paper - Expand FallingBlock API
            if (this.dropItem && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
              this.spawnAtLocation(serverLevel, block);
            }

            this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DROP); // CraftBukkit - add Bukkit remove cause
          }
        } else {
          BlockState blockState = this.level().getBlockState(blockPos);
          this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
          if (!blockState.is(Blocks.MOVING_PISTON)) {
            if (!this.cancelDrop) {
              boolean canBeReplaced = blockState.canBeReplaced(
                  new DirectionalPlaceContext(this.level(), blockPos, Direction.DOWN, ItemStack.EMPTY, Direction.UP)
              );
              boolean flag2 = FallingBlock.isFree(this.level().getBlockState(blockPos.below())) && (!flag || !flag1);
              boolean flag3 = this.blockState.canSurvive(this.level(), blockPos) && !flag2;
              if (canBeReplaced && flag3) {
                if (this.blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                    && this.level().getFluidState(blockPos).getType() == Fluids.WATER) {
                  this.blockState = this.blockState.setValue(BlockStateProperties.WATERLOGGED, true);
                }

                // CraftBukkit start
                if (!org.bukkit.craftbukkit.event.CraftEventFactory.callEntityChangeBlockEvent(this, blockPos, this.blockState)) {
                  this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN); // SPIGOT-6586 called before the event in previous versions
                  return;
                }
                // CraftBukkit end
                if (this.level().setBlock(blockPos, this.blockState, Block.UPDATE_ALL)) {
                  serverLevel.getChunkSource()
                      .chunkMap
                      .sendToTrackingPlayers(this, new ClientboundBlockUpdatePacket(blockPos, this.level().getBlockState(blockPos)));
                  this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN); // CraftBukkit - add Bukkit remove cause
                  if (block instanceof Fallable fallable) {
                    fallable.onLand(this.level(), blockPos, this.blockState, blockState, this);
                  }

                  if (this.blockData != null && this.blockState.hasBlockEntity()) {
                    BlockEntity blockEntity = this.level().getBlockEntity(blockPos);
                    if (blockEntity != null) {
                      try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(
                          blockEntity.problemPath(), LOGGER
                      )) {
                        RegistryAccess registryAccess = this.level().registryAccess();
                        TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, registryAccess);
                        blockEntity.saveWithoutMetadata(tagValueOutput);
                        CompoundTag compoundTag = tagValueOutput.buildResult();
                        this.blockData.forEach((string, tag) -> compoundTag.put(string, tag.copy()));
                        blockEntity.loadWithComponents(TagValueInput.create(scopedCollector, registryAccess, compoundTag));
                      } catch (Exception var19) {
                        LOGGER.error("Failed to load block entity from falling block", (Throwable)var19);
                      }
                      blockEntity.setChanged();
                    }
                  }
                } else if (this.dropItem && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                  this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DROP); // CraftBukkit - add Bukkit remove cause
                  this.callOnBrokenAfterFall(block, blockPos);
                  this.spawnAtLocation(serverLevel, block);
                }
              } else {
                this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DROP); // CraftBukkit - add Bukkit remove cause
                if (this.dropItem && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                  this.callOnBrokenAfterFall(block, blockPos);
                  this.spawnAtLocation(serverLevel, block);
                }
              }
            } else {
              this.discard(org.bukkit.event.entity.EntityRemoveEvent.Cause.DESPAWN); // CraftBukkit - add Bukkit remove cause
              this.callOnBrokenAfterFall(block, blockPos);
            }
          }
        }
      }*/

    //this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    //}
  }
/*
  //отлом LivingEntity
  public void travelInAir(Vec3 travelVector) {
    BlockPos blockPosBelowThatAffectsMyMovement = this.getBlockPosBelowThatAffectsMyMovement();
    float friction = this.onGround() ? this.level().getBlockState(blockPosBelowThatAffectsMyMovement).getBlock().getFriction() : 1.0F;
    float f1 = friction * 0.91F;
    Vec3 vec3 = this.handleRelativeFrictionAndCalculateMovement(travelVector, friction);
    double d = vec3.y;
    //MobEffectInstance effect = this.getEffect(MobEffects.LEVITATION);
    //if (effect != null) {
   //   d += (0.05 * (effect.getAmplifier() + 1) - vec3.y) * 0.2;
   // } else
    if (!this.level().isClientSide() || this.level().hasChunkAt(blockPosBelowThatAffectsMyMovement)) {
      d -= this.getEffectiveGravity();
    } else if (this.getY() > this.level().getMinY()) {
      d = -0.1;
    } else {
      d = 0.0;
    }

    if (this.shouldDiscardFriction()) {
      this.setDeltaMovement(vec3.x, d, vec3.z);
    } else {
      float f2 = this instanceof FlyingAnimal ? f1 : 0.98F;
      this.setDeltaMovement(vec3.x * f1, d * f2, vec3.z * f1);
    }
  }

  private Vec3 handleRelativeFrictionAndCalculateMovement(Vec3 deltaMovement, float friction) {
    this.moveRelative(this.getFrictionInfluencedSpeed(friction), deltaMovement);
    this.setDeltaMovement(this.handleOnClimbable(this.getDeltaMovement()));
    this.move(MoverType.SELF, this.getDeltaMovement());
    Vec3 deltaMovement1 = this.getDeltaMovement();
    if ((this.horizontalCollision || this.jumping) && (this.onClimbable() || this.wasInPowderSnow && PowderSnowBlock.canEntityWalkOnPowderSnow(this))) {
      deltaMovement1 = new Vec3(deltaMovement1.x, 0.2, deltaMovement1.z);
    }

    return deltaMovement1;
  }

  private Vec3 handleOnClimbable(Vec3 deltaMovement) {
    if (this.onClimbable()) {
      this.resetFallDistance();
      float f = 0.15F;
      double d = Mth.clamp(deltaMovement.x, -0.15F, 0.15F);
      double d1 = Mth.clamp(deltaMovement.z, -0.15F, 0.15F);
      double max = Math.max(deltaMovement.y, -0.15F);
      //if (max < 0.0 && !this.getInBlockState().is(Blocks.SCAFFOLDING) && this.isSuppressingSlidingDownLadder() && this instanceof Player) {
      //  max = 0.0;
      //}

      deltaMovement = new Vec3(d, max, d1);
    }

    return deltaMovement;
  }

  private float getFrictionInfluencedSpeed(float friction) {
    return this.onGround() ? this.getSpeed() * (0.21600002F / (friction * friction * friction)) : this.getFlyingSpeed();
  }

  private float getFlyingSpeed() {
    return speed;//0.1f;
  }

  private float getSpeed() {
    return speed;//0.1f;
  }

  protected double getEffectiveGravity() {
    //boolean flag = this.getDeltaMovement().y <= 0.0;
    //return flag && this.hasEffect(MobEffects.SLOW_FALLING) ? Math.min(this.getGravity(), 0.01) : this.getGravity();
    return this.getGravity();
  }

  public boolean shouldDiscardFriction() {
    return false;//!this.frictionState.toBooleanOrElse(!this.discardFriction); // Paper - Friction API
  }

  public boolean onClimbable() {
    if (this.isSpectator()) {
      return false;
    } else {
      BlockPos blockPos = this.blockPosition();
      BlockState inBlockState = this.getInBlockState();
      if (inBlockState.is(BlockTags.CLIMBABLE)) {
        //this.lastClimbablePos = Optional.of(blockPos);
        return true;
      } else if (inBlockState.getBlock() instanceof TrapDoorBlock && this.trapdoorUsableAsLadder(blockPos, inBlockState)) {
        //this.lastClimbablePos = Optional.of(blockPos);
        return true;
      } else {
        return false;
      }
    }
  }

  private boolean trapdoorUsableAsLadder(BlockPos pos, BlockState state) {
    if (!state.getValue(TrapDoorBlock.OPEN)) {
      return false;
    } else {
      BlockState blockState = this.level().getBlockState(pos.below());
      return blockState.is(Blocks.LADDER) && blockState.getValue(LadderBlock.FACING) == state.getValue(TrapDoorBlock.FACING);
    }
  }


  public void setSpeed(float speed) {
    this.speed = speed;
  }
*/

}
