package com.wasteofplastic.askyblock.events;

import java.util.UUID;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.wasteofplastic.askyblock.ASkyBlock;

/**
 * This event is fired when a Warp is created
 * A Listener to this event can use it only to get informations. e.g: broadcast something
 * 
 * @author Poslovitch
 *
 */
public class WarpCreateEvent implements Event {
	
	private Location<World> warpLoc;
	private UUID creator;
	
	/**
	 * @param plugin
	 * @param warpLoc
	 * @param creator
	 */
	public WarpCreateEvent(ASkyBlock plugin, Location<World> warpLoc, UUID creator){
		this.warpLoc = warpLoc;
		this.creator = creator;
	}
	
	/**
	 * Get the location of the created Warp
	 * @return created warp's location
	 */
	public Location<World> getWarpLocation(){return this.warpLoc;}
	
	/**
	 * Get who has created the warp
	 * @return the warp's creator
	 */
	public UUID getCreator(){return this.creator;}

	@Override
	public Cause getCause() {
		// TODO Auto-generated method stub
		return null;
	}
}
