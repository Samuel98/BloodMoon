package uk.co.jacekk.bukkit.bloodmoon.entities;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_5.CraftServer;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftZombie;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.Plugin;

import uk.co.jacekk.bukkit.bloodmoon.BloodMoon;
import uk.co.jacekk.bukkit.bloodmoon.Config;
import uk.co.jacekk.bukkit.bloodmoon.events.ZombieMoveEvent;
import uk.co.jacekk.bukkit.bloodmoon.pathfinders.BloodMoonNavigation;
import uk.co.jacekk.bukkit.bloodmoon.pathfinders.BloodMoonPathfinderGoalNearestAttackableTarget;

import net.minecraft.server.v1_4_5.Entity;
import net.minecraft.server.v1_4_5.EntityHuman;
import net.minecraft.server.v1_4_5.EntityLiving;
import net.minecraft.server.v1_4_5.EntityVillager;
import net.minecraft.server.v1_4_5.PathfinderGoal;
import net.minecraft.server.v1_4_5.PathfinderGoalBreakDoor;
import net.minecraft.server.v1_4_5.PathfinderGoalFloat;
import net.minecraft.server.v1_4_5.PathfinderGoalHurtByTarget;
import net.minecraft.server.v1_4_5.PathfinderGoalLookAtPlayer;
import net.minecraft.server.v1_4_5.PathfinderGoalMeleeAttack;
import net.minecraft.server.v1_4_5.PathfinderGoalMoveThroughVillage;
import net.minecraft.server.v1_4_5.PathfinderGoalMoveTowardsRestriction;
import net.minecraft.server.v1_4_5.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_4_5.PathfinderGoalRandomLookaround;
import net.minecraft.server.v1_4_5.PathfinderGoalRandomStroll;
import net.minecraft.server.v1_4_5.World;

public class BloodMoonEntityZombie extends net.minecraft.server.v1_4_5.EntityZombie {
	
	private BloodMoon plugin;
	
	@SuppressWarnings("unchecked")
	public BloodMoonEntityZombie(World world){
		super(world);
		
		Plugin plugin = Bukkit.getPluginManager().getPlugin("BloodMoon");
		
		if (plugin == null || !(plugin instanceof BloodMoon)){
			this.world.removeEntity(this);
			return;
		}
		
		this.plugin = (BloodMoon) plugin;
		
		this.bukkitEntity = new CraftZombie((CraftServer) this.plugin.server, this);
		
		if (this.plugin.config.getBoolean(Config.FEATURE_MOVEMENT_SPEED_ENABLED) && this.plugin.config.getStringList(Config.FEATURE_MOVEMENT_SPEED_MOBS).contains("ZOMBIE")){
			try{
				Field navigation = EntityLiving.class.getDeclaredField("navigation");
				navigation.setAccessible(true);
				navigation.set(this, new BloodMoonNavigation(this.plugin, this, this.world, 16.0f));
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		
		try{
			Field goala = this.goalSelector.getClass().getDeclaredField("a");
			goala.setAccessible(true);
			((List<PathfinderGoal>) goala.get(this.goalSelector)).clear();
			
			Field targeta = this.targetSelector.getClass().getDeclaredField("a");
			targeta.setAccessible(true);
			((List<PathfinderGoal>) targeta.get(this.targetSelector)).clear();
			
			this.goalSelector.a(0, new PathfinderGoalFloat(this));
			this.goalSelector.a(1, new PathfinderGoalBreakDoor(this));
			this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, EntityHuman.class, this.bG, false));
			this.goalSelector.a(3, new PathfinderGoalMeleeAttack(this, EntityVillager.class, this.bG, true));
			this.goalSelector.a(4, new PathfinderGoalMoveTowardsRestriction(this, this.bG));
			this.goalSelector.a(5, new PathfinderGoalMoveThroughVillage(this, this.bG, false));
			this.goalSelector.a(6, new PathfinderGoalRandomStroll(this, this.bG));
			this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
			this.goalSelector.a(7, new PathfinderGoalRandomLookaround(this));
			
			this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
			
			if (this.plugin.config.getBoolean(Config.FEATURE_TARGET_DISTANCE_ENABLED) && this.plugin.config.getStringList(Config.FEATURE_TARGET_DISTANCE_MOBS).contains("ZOMBIE")){
				this.targetSelector.a(2, new BloodMoonPathfinderGoalNearestAttackableTarget(this.plugin, this, EntityHuman.class, 16.0F, 0, true));
			}else{
				this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 16.0F, 0, true));
			}
			
			this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityVillager.class, 16.0F, 0, false));
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	public void j_(){
		Zombie zombie = (Zombie) this.getBukkitEntity();
		
		Location from = new Location(zombie.getWorld(), this.lastX, this.lastY, this.lastZ, this.lastYaw, this.lastPitch);
		Location to = new Location(zombie.getWorld(), this.locX, this.locY, this.locZ, this.yaw, this.pitch);
		
		ZombieMoveEvent event = new ZombieMoveEvent(zombie, from, to);
		
		this.world.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled() && !zombie.isDead()){
			return;
		}
		
		super.j_();
	}
	
	@Override
	protected Entity findTarget(){
		float distance = (plugin.isActive(this.world.worldData.getName()) && plugin.config.getStringList(Config.FEATURE_TARGET_DISTANCE_MOBS).contains("ZOMBIE")) ? plugin.config.getInt(Config.FEATURE_TARGET_DISTANCE_MULTIPLIER) * 16.0f : 16.0f;
		
		EntityHuman entityhuman = this.world.findNearbyVulnerablePlayer(this, distance);
		
		return entityhuman != null && this.n(entityhuman) ? entityhuman : null;
	}
	
	public void setEquipmentDropChance(int slot, float chance){
		this.dropChances[slot] = chance;
	}
	
}
