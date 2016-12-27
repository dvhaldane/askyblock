/*******************************************************************************
 * This file is part of ASkyBlock.
 *
 *     ASkyBlock is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ASkyBlock is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ASkyBlock.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package com.wasteofplastic.askyblock;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * This class runs when the config file is not set up enough, or is unsafe
 * It provides useful information to the admin on what is wrong.
 * 
 * @author tastybento
 * 
 */
public class NotSetup implements CommandExecutor {

    public enum Reason {
        DISTANCE, GENERATOR, WORLD_NAME
    };

    private Reason reason;

    /**
     * Handles plugin operation if a critical setup parameter is missing
     * 
     * @param reason
     */
    public NotSetup(Reason reason) {
        this.reason = reason;
    }

	@Override
	public CommandResult execute(CommandSource sender, CommandContext args) throws CommandException {
		sender.sendMessage(Text.of(TextColors.RED + "More set up is required before the plugin can start..."));
        switch (reason) {
        case DISTANCE:
            sender.sendMessage(Text.of(TextColors.RED + "Edit config.yml. Then restart server."));
            sender.sendMessage(Text.of(TextColors.RED + "Make sure you set island distance. If upgrading, set it to what it was before."));
            break;
        case GENERATOR:
            sender.sendMessage(Text.of(TextColors.RED + "The world generator for the island world is not registered."));
            sender.sendMessage(Text.of(TextColors.RED + "Potential reasons are:"));
            sender.sendMessage(Text.of(TextColors.RED + "  1. If you are configuring the island world as the only server world"));
            sender.sendMessage(Text.of(TextColors.RED + "     Make sure you have added the world to bukkit.yml"));
            break;
        case WORLD_NAME:
            sender.sendMessage(Text.of(TextColors.RED + "The world name in config.yml is different to the world name in islands.yml."));
            sender.sendMessage(Text.of(TextColors.RED + "If this is intentional, I assume you are doing a full reset. If so,"));
            sender.sendMessage(Text.of(TextColors.RED + "delete islands.yml and the previous world. If not, correct the world name in"));
            sender.sendMessage(Text.of(TextColors.RED + "config.yml and restart. This is probably the case if you are upgrading."));
        default:
            break;
        }
        return CommandResult.success();
	}

}