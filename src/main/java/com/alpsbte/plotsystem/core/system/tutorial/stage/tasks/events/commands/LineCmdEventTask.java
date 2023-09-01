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

package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.patterns.SingleBlockPattern;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.logging.Level;

public class LineCmdEventTask extends AbstractCmdEventTask {
    private final Map<Vector, Vector> linePoints;
    private final BiTaskAction<Vector, Vector> lineCmdAction;

    private Vector minPoint;
    private Vector maxPoint;

    public LineCmdEventTask(Player player, String assignmentMessage, String blockName, int blockId, Map<Vector, Vector> linePoints, BiTaskAction<Vector, Vector> lineCmdAction) {
        super(player, "//line", new String[] { blockName, String.valueOf(blockId) },  assignmentMessage, linePoints.size(),true);
        this.linePoints = linePoints;
        this.lineCmdAction = lineCmdAction;
    }

    @Override
    protected void onCommand(String[] args) {
        // Check if the player has selected valid points
        if (minPoint == null || maxPoint == null) return;

        // Check if the points are valid and draw line
        if (linePoints.containsKey(minPoint) && linePoints.get(minPoint).equals(maxPoint) && drawLine()) {
            lineCmdAction.performAction(minPoint, maxPoint);
        } else if (linePoints.containsKey(maxPoint) && linePoints.get(maxPoint).equals(minPoint) && drawLine()) {
            lineCmdAction.performAction(maxPoint, minPoint);
        } else return;

        updateProgress();
        minPoint = null; maxPoint = null;
        if (linePoints.size() == 0) setTaskDone();
    }

    @Override
    public void performEvent(Event event) {
        if (event instanceof PlayerInteractEvent) {
            onPlayerInteractEvent((PlayerInteractEvent) event);
        } else super.performEvent(event);
    }

    private void onPlayerInteractEvent(PlayerInteractEvent event) {
        // Check if player has a wooden axe in his hand
        if (event.getClickedBlock() == null || event.getItem() == null || !event.getItem().equals(new ItemStack(Material.WOOD_AXE))) return;

        // Get clicked block and update min/max point
        Vector point = new Vector(event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ());
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && isPointInLine(point)) {
            minPoint = point;
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isPointInLine(point)) {
            maxPoint = point;
        } else return;

        player.playSound(player.getLocation(), Sound.ENTITY_ITEMFRAME_ADD_ITEM, 0.5f, 1f);
    }

    private boolean isPointInLine(Vector point) {
        return linePoints.containsKey(point) || linePoints.containsValue(point);
    }

    private boolean drawLine() {
        try {
            EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(player.getWorld()), -1);
            editSession.drawLine(new SingleBlockPattern(new BaseBlock(Integer.parseInt(args1[1]))), minPoint, maxPoint, 0, false);
            editSession.flushQueue();
        } catch (MaxChangedBlocksException ex) {
           Bukkit.getLogger().log(Level.SEVERE, "An error occurred while drawing line!", ex);
           return false;
        }
        return true;
    }
}
