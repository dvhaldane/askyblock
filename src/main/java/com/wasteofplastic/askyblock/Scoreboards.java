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

import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scoreboard.Scoreboard;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.text.serializer.TextSerializers;

/**
 * This class puts a player into a "team" and sets the island level as the suffix.
 * The team suffix variable can then be used by other plugins, such as Essentials Chat
 * {TEAMSUFFIX}
 * @author tastybento
 *
 */
public class Scoreboards {
    private static ASkyBlock plugin = ASkyBlock.getPlugin();
    private static Scoreboards instance = new Scoreboards();
    private static Scoreboard board;

    /**
     * 
     */
    private Scoreboards() {
        Sponge.getServer().getServerScoreboard().ifPresent(b -> board = b);
    }

    /**
     * @return the instance
     */
    public static Scoreboards getInstance() {
        return instance;
    }

    /**
     * Puts a player into a team of their own and sets the team suffix to be the level
     * @param playerUUID
     */
    public void setLevel(UUID playerUUID) {
        Optional<Player> player = plugin.getServer().getPlayer(playerUUID);
        if (!player.isPresent()) {
            // Player is offline...
            return;
        }
        // The default team name is their own name
        String teamName = player.get().getName();
        String level = plugin.getPlayers().getIslandLevel(playerUUID).toString();
        Optional<Team> team = board.getTeam(teamName);
        if (!team.isPresent()) {
            //Team does not exist. Register the team
            board.registerTeam(Team.builder().name(teamName).build());
        }
        // Add the suffix
        team.get().setSuffix(TextSerializers.FORMATTING_CODE.deserialize((Settings.teamSuffix.replace("[level]",String.valueOf(level)))));
        //Adding player to team
        team.get().addMember(player.get().getTeamRepresentation());
        // Assign scoreboard to player
        player.get().setScoreboard(board);
    } 

    /**
     * Sets the player's level explicitly
     * @param playerUUID
     * @param level
     */
    public void setLevel(UUID playerUUID, int level) {
        Optional<Player> player = plugin.getServer().getPlayer(playerUUID);
        if (!player.isPresent()) {
            // Player is offline...
            return;
        }
        // The default team name is their own name - must be 16 chars or less
        String teamName = player.get().getName();
        Optional<Team> team = board.getTeam(teamName);
        if (!team.isPresent()) {
            //Team does not exist. Register the team.
            board.registerTeam(Team.builder().name(teamName).build());
        }
        // Add the suffix
        team.get().setSuffix(TextSerializers.FORMATTING_CODE.deserialize(Settings.teamSuffix.replace("[level]",String.valueOf(level))));
        //Adding player to team
        team.get().addMember(player.get().getTeamRepresentation());
        // Assign scoreboard to player
        player.get().setScoreboard(board);
    }
}
