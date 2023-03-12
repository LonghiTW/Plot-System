/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.core.system.tutorial.tasks.events;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.tasks.AbstractTask;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.logging.Level;

public class TeleportPointEventTask extends AbstractTask implements Listener {
    private List<String> messages;
    private List<double[]> teleportPoints;
    private int offsetRange;

    public TeleportPointEventTask(Player player) {
        super(player);
    }

    public TeleportPointEventTask(Player player, List<String> messages, List<double[]> teleportPoints, int offsetRange) {
        this(player);
        this.messages = messages;
        this.teleportPoints = teleportPoints;
        this.offsetRange = offsetRange;
    }

    @Override
    public void performTask() {
        PlotSystem.getPlugin().getServer().getPluginManager().registerEvents(this, PlotSystem.getPlugin());
    }

    @EventHandler
    private void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (!player.getUniqueId().equals(event.getPlayer().getUniqueId()) || teleportPoints == null || event.getTo().getWorld() != player.getWorld()) return;
        if (teleportPoints.size() == 0) return;

        int blockX = event.getTo().getBlockX();
        int blockZ = event.getTo().getBlockZ();

        for (double[] teleportPoint : teleportPoints) {
            Bukkit.getLogger().log(Level.INFO, "BlockX: " + blockX + " | TeleportPointX: " + teleportPoint[0]);
            Bukkit.getLogger().log(Level.INFO, "BlockZ: " + blockZ + " | TeleportPointZ: " + teleportPoint[1]);
            if (blockX < ((int) teleportPoint[0] - offsetRange) || blockX > ((int) teleportPoint[0] + offsetRange)) continue;
            if (blockZ < ((int) teleportPoint[1] - offsetRange) || blockZ > ((int) teleportPoint[1] + offsetRange)) continue;
            removePoint(teleportPoint);
            break;
        }
    }

    private void removePoint(double[] teleportPoint) {
        teleportPoints.remove(teleportPoint);
        AbstractTutorial.ChatHandler.printInfo(player, AbstractTutorial.ChatHandler.getTaskMessage(messages.get(2), ChatColor.GREEN));
        player.playSound(player.getLocation(), Utils.Done, 1f, 1f);

        if (teleportPoints.size() == 0) {
            setTaskDone();
            HandlerList.unregisterAll(this);
        }
    }
}
