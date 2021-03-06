package minefantasy.mfr.mechanics;

import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import minefantasy.mfr.MineFantasyReborn;
import minefantasy.mfr.api.material.CustomMaterial;
import minefantasy.mfr.config.ConfigHardcore;
import minefantasy.mfr.init.CustomArmourListMFR;
import minefantasy.mfr.init.CustomToolListMFR;
import minefantasy.mfr.item.armour.ItemCustomArmour;
import minefantasy.mfr.item.weapon.ItemWeaponMFR;
import minefantasy.mfr.util.XSTRandom;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Items;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;

public class MonsterUpgrader {
	public static final String zombieArmourNBT = "MF_ZombieArmour";
	private static final float zombieWepChance = 10F;
	private static final float zombieKnightChance = 200F;
	private static final float zombieBruteChance = 200F;
	private static final float creeperJockeyChance = 60F;
	private static final float witchRiderChance = 100F;

	private static XSTRandom random = new XSTRandom();

	public void upgradeMob(EntityLivingBase mob) {
		int diff = mob.world.getDifficulty().getDifficultyId();

		if (ConfigHardcore.upgradeZombieWep) {
			if (mob instanceof AbstractSkeleton) {
				if ((mob) instanceof EntityWitherSkeleton) {
					giveEntityWeapon(mob, "Diamond", random.nextInt(8));
				} else if (CombatMechanics.swordSkeleton && random.nextInt(3) == 0) {
					mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
							CustomToolListMFR.STANDARD_SWORD.construct("Bronze", "OakWood"));
					((EntitySkeleton) mob).setCombatTask();
				}
			} else if (mob instanceof EntityZombie) {
				String tier = "Iron";
				if (mob instanceof EntityPigZombie) {
					tier = "Obsidian";
					giveEntityWeapon(mob, tier, random.nextInt(7));
					if (mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
						mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
					}
				} else {
					if (mob.getHeldItemMainhand() != null
							&& mob.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.IRON_SWORD) {
						giveEntityWeapon(mob, tier, 0);
						if (mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
							mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
						}
					} else {
						float mod = diff >= 2 ? ConfigHardcore.zombieWepChance * 2
								: diff < 1 ? ConfigHardcore.zombieWepChance / 2 : ConfigHardcore.zombieWepChance;
						float chance = random.nextFloat() * 100F * mod;
						if (chance >= (100F - zombieWepChance)) {
							giveEntityWeapon(mob, tier, random.nextInt(5));
							if (mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE) != null) {
								mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
							}
						}
					}
				}
				if (random.nextFloat() * (zombieKnightChance) < diff) {
					createZombieKnight((EntityZombie) mob);
				} else if (random.nextFloat() * (zombieBruteChance) < diff) {
					createZombieBrute((EntityZombie) mob);
				} else if (ConfigHardcore.fastZombies) {
					mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
				}
			}
		} else if (mob instanceof EntitySpider) {
			if (!mob.isBeingRidden()) {
				if (random.nextFloat() * (witchRiderChance) < diff) {
					EntityWitch rider = new EntityWitch(mob.world);
					rider.setPosition(mob.posX, mob.posY, mob.posZ);
					mob.world.spawnEntity(rider);
					rider.startRiding(mob);
				} else if (random.nextFloat() * (creeperJockeyChance) < diff) {
					EntityCreeper rider = new EntityCreeper(mob.world);
					rider.setPosition(mob.posX, mob.posY, mob.posZ);
					mob.world.spawnEntity(rider);
					rider.startRiding(mob);
				}
			}
		} else {
			if (mob.getHeldItemMainhand() != null
					&& mob.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.IRON_SWORD) {
				giveEntityWeapon(mob, "iron", 0);
			}
		}
		if (mob instanceof EntityPigZombie) {
			mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
		}
	}

	private void createZombieKnight(EntityZombie mob) {
		if (mob.isChild())
			return;
		String tier = "steel";
		int lootId = 0;
		if (mob instanceof EntityPigZombie) {
			lootId = 1;
			tier = "encrusted";
		}
		mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
		mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
				CustomToolListMFR.STANDARD_GREATSWORD.construct(tier, "OakWood"));
		setArmour(mob, 1, tier);
		mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F);
		mob.getEntityData().setInteger("MF_LootDrop", lootId);
	}

	private void setArmour(EntityLivingBase mob, int id, String tier) {
		if (id == 1) {
			ItemStack boots = ((ItemCustomArmour)CustomArmourListMFR.STANDARD_PLATE_BOOTS).construct(tier);
			ItemStack legs = ((ItemCustomArmour)CustomArmourListMFR.STANDARD_PLATE_LEGS).construct(tier);
			ItemStack chest = ((ItemCustomArmour)CustomArmourListMFR.STANDARD_PLATE_CHEST).construct(tier);
			ItemStack helmet = ((ItemCustomArmour)CustomArmourListMFR.STANDARD_PLATE_HELMET).construct(tier);

			if (boots != null)
				mob.setItemStackToSlot(EntityEquipmentSlot.FEET, boots);
			if (legs != null)
				mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, legs);
			if (chest != null)
				mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, chest);
			if (helmet != null)
				mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, helmet);
			return;
		}
		
		ItemStack boots = ((ItemCustomArmour)CustomArmourListMFR.STANDARD_CHAIN_BOOTS).construct(tier);
		ItemStack legs = ((ItemCustomArmour)CustomArmourListMFR.STANDARD_CHAIN_LEGS).construct(tier);
		ItemStack chest = ((ItemCustomArmour)CustomArmourListMFR.STANDARD_CHAIN_CHEST).construct(tier);
		ItemStack helmet = ((ItemCustomArmour)CustomArmourListMFR.STANDARD_CHAIN_HELMET).construct(tier);

		if (boots != null)
			mob.setItemStackToSlot(EntityEquipmentSlot.FEET, boots);
		if (legs != null)
			mob.setItemStackToSlot(EntityEquipmentSlot.LEGS, legs);
		if (chest != null)
			mob.setItemStackToSlot(EntityEquipmentSlot.CHEST, chest);
		if (helmet != null)
			mob.setItemStackToSlot(EntityEquipmentSlot.HEAD, helmet);
	}

	private void createZombieBrute(EntityZombie mob) {
		if (mob.isChild())
			return;
		String tier = "iron";
		int lootId = 0;
		if (mob instanceof EntityPigZombie) {
			lootId = 1;
			tier = "encrusted";
		}
		mob.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0F);
		mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
				CustomToolListMFR.STANDARD_WARAXE.construct(tier, "OakWood"));
		setArmour(mob, 0, tier);
		mob.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.35F);
		mob.getEntityData().setInteger("MF_LootDrop", lootId);
	}

	/**
	 * 1=Axe.....2=Mace.....3=dagger.....4=spear.....else sword
	 */
	private void giveEntityWeapon(EntityLivingBase mob, String tier, int weaponType) {
		if (CustomMaterial.getMaterial(tier) == null)
			return;

		ItemWeaponMFR weapon = CustomToolListMFR.STANDARD_SWORD;
		if (weaponType == 1) {
			weapon = CustomToolListMFR.STANDARD_WARAXE;
		}
		if (weaponType == 2) {
			weapon = CustomToolListMFR.STANDARD_MACE;
		}
		if (weaponType == 3) {
			weapon = CustomToolListMFR.STANDARD_DAGGER;
		}
		if (weaponType == 4) {
			weapon = CustomToolListMFR.STANDARD_SPEAR;
		}

		if (mob != null && weapon != null) {
			mob.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, weapon.construct(tier, "OakWood"));
		}
	}

	@SubscribeEvent
	public void updateLiving(LivingUpdateEvent event) {
		EntityLivingBase living = event.getEntityLiving();

		if (isEnabled() && !living.world.isRemote && !living.getEntityData().hasKey("giveMFWeapon")) {
			living.getEntityData().setBoolean("giveMFWeapon", true);
			upgradeMob(event.getEntityLiving());
		}
	}

	private boolean isEnabled() {
		return true;
	}
}
