package uk.co.jacekk.bukkit.bloodmoon.nms;

import net.minecraft.server.v1_6_R3.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftWitch;
import org.bukkit.plugin.Plugin;

import uk.co.jacekk.bukkit.bloodmoon.BloodMoon;
import uk.co.jacekk.bukkit.bloodmoon.entity.BloodMoonEntityType;
import uk.co.jacekk.bukkit.bloodmoon.entity.BloodMoonEntityWitch;

public class EntityWitch extends net.minecraft.server.v1_6_R3.EntityWitch {
	
	private BloodMoon plugin;
	private BloodMoonEntityWitch bloodMoonEntity;
	
	public EntityWitch(World world){
		super(world);
		
		Plugin plugin = Bukkit.getPluginManager().getPlugin("BloodMoon");
		
		if (plugin == null || !(plugin instanceof BloodMoon)){
			this.world.removeEntity(this);
			return;
		}
		
		this.plugin = (BloodMoon) plugin;
		
		this.bukkitEntity = new CraftWitch((CraftServer) this.plugin.server, this);
		this.bloodMoonEntity = new BloodMoonEntityWitch(this.plugin, this, (CraftLivingEntity) this.bukkitEntity, BloodMoonEntityType.WITCH);
	}
	
	@Override
	public void bj(){
		try{
			this.bloodMoonEntity.onTick();
			super.bj();
		}catch (Exception e){
			plugin.log.warn("Exception caught while ticking entity");
			e.printStackTrace();
		}
	}
	
}
