package minefantasy.mfr.entity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemExpireEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.Iterator;

public class EntityItemUnbreakable extends EntityItem {
    public EntityItemUnbreakable(World world) {
        super(world);
    }

    public EntityItemUnbreakable(World world, EntityItem parent) {
        super(world, parent.posX, parent.posY, parent.posZ, parent.getItem());
        this.mimicSpeed(parent);
        setPickupDelay(ObfuscationReflectionHelper.getPrivateValue(EntityItem.class, parent, "pickupDelay"));
        isImmuneToFire = true;
    }

    public void mimicSpeed(EntityItem parent) {
        this.motionX = parent.motionX;
        this.motionY = parent.motionY;
        this.motionZ = parent.motionZ;
    }

    @Override
    public boolean attackEntityFrom(DamageSource src, float dam) {
        if (isBreakable() && !src.isFireDamage()) {
            return super.attackEntityFrom(src, dam);
        }
        return false;
    }

    @Override
    public void onUpdate() {
        ItemStack stack = this.getDataWatcher().getWatchableObjectItemStack(10);
        if (stack != null && stack.getItem() != null) {
            if (stack.getItem().onEntityItemUpdate(this)) {
                return;
            }
        }

        if (getItem() == null) {
            setDead();
        } else {
            onEntityUpdate();

            int pickupDelay = ObfuscationReflectionHelper.getPrivateValue(EntityItem.class, this, "pickupDelay");
            if (pickupDelay > 0) {
                this.setPickupDelay(pickupDelay -1);
            }

            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;
            if (!isBreakable() && posY <= 1.0F) {
                motionY = 0D;
            } else {
                this.motionY -= 0.03999999910593033D;
            }
            //just a guess that this function is pushOutOfBlocks
            noClip = pushOutOfBlocks(posX,
                    (getCollisionBoundingBox().minY + getCollisionBoundingBox().maxY) / 2.0D,
                    posZ);

            //again just a guess
            this.move(MoverType.SELF, motionX, motionY, motionZ);//this.moveEntity(this.motionX, this.motionY, this.motionZ);

            boolean flag = (int) prevPosX != (int) posX || (int) prevPosY != (int) posY
                    || (int) prevPosZ != (int) posZ;

            if (flag || this.ticksExisted % 25 == 0) {
                IBlockState blockState = world.getBlockState(
                        new BlockPos(
                                MathHelper.floor(posX),
                                MathHelper.floor(posY),
                                MathHelper.floor(posZ)));
                if (blockState.getMaterial() == Material.LAVA) {
                    motionY = 0.20000000298023224D;
                    motionX = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
                    motionZ = (rand.nextFloat() - rand.nextFloat()) * 0.2F;
                }

                if (!this.world.isRemote) {
                    this.searchForOtherItemsNearby();
                }
            }

            float f = 0.98F;

            if (onGround) {

                IBlockState blockState = world.getBlockState(
                        new BlockPos(
                                MathHelper.floor(posX),
                                MathHelper.floor(getCollisionBoundingBox().minY) -1,
                                MathHelper.floor(posZ)));

                //parameters are all nulls because function ignores them and just returns slipperiness
                float splipperiness = blockState.getBlock().getSlipperiness(null, null, null, null);
                f = splipperiness * 0.98F;
            }

            this.motionX *= f;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= f;

            if (this.onGround) {
                this.motionY *= -0.5D;
            }

            int age = ObfuscationReflectionHelper.getPrivateValue(EntityItem.class, this, "age");
            ObfuscationReflectionHelper.setPrivateValue(EntityItem.class, this, age-1, "age");

            ItemStack item = getDataWatcher().getWatchableObjectItemStack(10);

            if (isBreakable()) {
                if (!this.world.isRemote && age >= lifespan) {
                    if (item != null) {
                        ItemExpireEvent event = new ItemExpireEvent(this,
                                (item.getItem() == null ? 6000 : item.getItem().getEntityLifespan(item, world)));
                        if (MinecraftForge.EVENT_BUS.post(event)) {
                            lifespan += event.getExtraLife();
                        } else {
                            this.setDead();
                        }
                    } else {
                        this.setDead();
                    }
                }
            }

            if (item != null && item.getCount() <= 0) {
                this.setDead();
            }
        }
    }

    private boolean isBreakable() {
        return false;
    }

    private void searchForOtherItemsNearby() {
        Iterator iterator = this.world
                .getEntitiesWithinAABB(EntityItem.class, getCollisionBoundingBox().expand(0.5D, 0.0D, 0.5D)).iterator();

        while (iterator.hasNext()) {
            EntityItem entityitem = (EntityItem) iterator.next();
            this.combineItems(entityitem);
        }
    }

    @Override
    public void playSound(String sound, float volume, float pitch) {
        if (sound.equalsIgnoreCase("random.fizz")) {
            return;
        }
        this.world.playSoundAtEntity(this, sound, volume, pitch);
    }
}
