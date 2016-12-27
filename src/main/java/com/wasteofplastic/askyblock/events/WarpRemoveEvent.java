package com.wasteofplastic.askyblock.events;

import java.util.UUID;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.wasteofplastic.askyblock.ASkyBlock;

/**
 * This event is fired when a Warp is removed (when a warp sign is broken)
 * A Listener to this event can use it only to get informations. e.g: broadcast something
 * 
 * @author Poslovitch
 *
 */
public class WarpRemoveEvent implements Event {
	
	private Location<World> warpLoc;
	private UUID remover;
	
	/**
	 * @param plugin
	 * @param warpLoc
	 * @param remover
	 */
	public WarpRemoveEvent(ASkyBlock plugin, Location<World> warpLoc, UUID remover){
		this.warpLoc = warpLoc;
		this.remover = remover;
	}
	
	/**
	 * Get the location of the removed Warp
	 * @return removed warp's location
	 */
	public Location<World> getWarpLocation(){return this.warpLoc;}
	
	/**
	 * Get who has removed the warp
	 * @return the warp's remover
	 */
	public UUID getRemover(){return this.remover;}

	@Override
	public Cause getCause() {
		// TODO Auto-generated method stub
		return null;
	}
}