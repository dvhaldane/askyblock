package com.wasteofplastic.askyblock;

import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;

public class AsyncBackup {

	/**
	 * Class to save the register and name database. This is done in an async
	 * way.
	 * 
	 * @param plugin
	 */
	public AsyncBackup(final ASkyBlock plugin) {

		Sponge.getScheduler().createTaskBuilder().execute(() -> {
			plugin.getGrid().saveGrid();
			plugin.getTinyDB().asyncSaveDB();
		}).async().interval(Settings.backupDuration, TimeUnit.MINUTES)
				.name("Save the grid every " + Settings.backupDuration + " minutes").submit(plugin);

	}

}
