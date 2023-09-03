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

package com.alpsbte.plotsystem.core.system.tutorial.stage;

import com.alpsbte.alpslib.hologram.HolographicDisplay;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.holograms.TutorialTipHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.*;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.ChatEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.InteractNPCEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.TeleportPointEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.events.commands.ContinueCmdEventTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.PlaceHologramTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.RemoveHologramTask;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message.ChatMessageTask;
import com.sk89q.worldedit.math.BlockVector3;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.GRAY;

public class StageTimeline implements TutorialTimeLine {
    private static final List<TutorialTimeLine> activeTimelines = new ArrayList<>();

    protected final Player player;

    protected final List<AbstractTask> tasks = new ArrayList<>();
    private AbstractTask currentTask;
    private int currentTaskId = -1;
    private BukkitTask assignmentProgressTask;
    private final List<TutorialTipHologram> tipHolograms = new ArrayList<>();
    private final TutorialNPC tutorialNPC;

    public StageTimeline(Player player) {
        this.player = player;
        tutorialNPC = AbstractTutorial.getActiveTutorial(player.getUniqueId()).getNPC();
    }

    /**
     * This method starts the timeline.
     * It will start with the first task and add the timeline to the list of active timelines.
     */
    public void StartTimeline() {
        activeTimelines.add(this);
        nextTask();
    }

    /**
     * This method performs the next task in the timeline.
     * In addition, it keeps track of the task's assignment and updates the action bar if the task has progress.
     */
    private void nextTask() {
        currentTask = tasks.get(currentTaskId + 1);
        Bukkit.getLogger().log(Level.INFO, "Starting task " + currentTask.toString() + " [" + (currentTaskId + 2) + " of " + tasks.size() + "] for player " + player.getName());
        currentTaskId = currentTaskId + 1;

        // Check if a task has progress and if yes, update action bar
        if (currentTask.hasProgress()) {
            AbstractTask.sendAssignmentMessage(player, currentTask.getAssignmentMessage());
            if (assignmentProgressTask != null) assignmentProgressTask.cancel();
            assignmentProgressTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (currentTask.getProgress() == currentTask.getTotalProgress()) {
                        this.cancel();
                    } else updatePlayerActionBar();
                }
            }.runTaskTimerAsynchronously(PlotSystem.getPlugin(), 0, 20);
        }

        // If the task has npc interaction show the hologram click info, otherwise hide it
        if (currentTask instanceof InteractNPCEventTask || currentTask instanceof ContinueCmdEventTask ||
                (currentTask instanceof ChatMessageTask && ((ChatMessageTask) currentTask).isWaitToContinue())) {
            tutorialNPC.setHologramFooterVisibility(true, currentTask instanceof InteractNPCEventTask);
        } else if (tutorialNPC.getNpcHologram().isFooterVisible()) tutorialNPC.setHologramFooterVisibility(false, false);

        currentTask.performTask();
    }

    @Override
    public void onTaskDone(UUID playerUUID, AbstractTask task) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString()) && task != currentTask) return;
        if (!activeTimelines.contains(this)) return;

        if (currentTaskId >= tasks.size() - 1) {
            onStopTimeLine(playerUUID);
            for (int i = 0; i < AbstractTutorial.getActiveTutorials().size(); i++)
                AbstractTutorial.getActiveTutorials().get(i).onStageComplete(player.getUniqueId());
        } else nextTask();
    }

    @Override
    public void onAssignmentUpdate(UUID playerUUID, AbstractTask task) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString()) && task != currentTask) return;
        updatePlayerActionBar();
    }

    @Override
    public void onStopTimeLine(UUID playerUUID) {
        if (!player.getUniqueId().toString().equals(playerUUID.toString())) return;

        activeTimelines.remove(this);
        if (assignmentProgressTask != null) assignmentProgressTask.cancel();
        if (currentTask != null) currentTask.setTaskDone();
        tipHolograms.forEach(HolographicDisplay::remove);
        tutorialNPC.setHologramFooterVisibility(false, false);
        tasks.clear();
    }

    /**
     * This method updates the player's action bar with the current task's assignment message.
     */
    private void updatePlayerActionBar() {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(GRAY + " [" + currentTask.getProgress() + "/" + currentTask.getTotalProgress() + "] " + currentTask.getAssignmentMessage()));
    }

    /**
     * Adds a task to the timeline. If there is no function for a specific task available, use this method.
     * @param task the task to be added
     */
    public StageTimeline addTask(AbstractTask task) {
        tasks.add(task);
        return this;
    }

    /**
     * Adds a ChatMessageTask to the timeline.
     * @param message the message to be sent
     * @param soundEffect the sound effect to be played, can be null
     * @param waitToContinue whether the timeline should wait for the player to continue
     */
    public StageTimeline sendChatMessage(String message, Sound soundEffect, boolean waitToContinue) {
        return sendChatMessage(new Object[] {message} , soundEffect, waitToContinue);
    }

    /**
     * Adds a ChatMessageTask to the timeline.
     * @param message the clickable message to be sent
     * @param soundEffect the sound effect to be played, can be null
     * @param waitToContinue whether the timeline should wait for the player to continue
     */
    public StageTimeline sendChatMessage(ChatMessageTask.ClickableTaskMessage message, Sound soundEffect, boolean waitToContinue) {
        return sendChatMessage(new Object[] {message} , soundEffect, waitToContinue);
    }

    /**
     * Adds a ChatMessageTask to the timeline. Use this method if you want to send multiple messages at once.
     * @param messages the messages to be sent, can be a String or a ClickableTaskMessage
     * @param soundEffect the sound effect to be played, can be null
     * @param waitToContinue whether the timeline should wait for the player to continue
     */
    public StageTimeline sendChatMessage(Object[] messages, Sound soundEffect, boolean waitToContinue) {
        tasks.add(new ChatMessageTask(player, messages, soundEffect, waitToContinue));
        if (waitToContinue) tasks.add(new ContinueCmdEventTask(player, tutorialNPC.getVillager().getUniqueId())); // Add a task to wait for player to continue
        return this;
    }

    /**
     * Adds a TeleportPointEventTask to the timeline.
     * @param assignmentMessage the task message to show in the action bar to the player
     * @param teleportPoint a list of teleport points the player needs to teleport to
     * @param offsetRange the range in which the player can teleport to the teleport point
     * @param onTeleportAction the action to be performed when the player teleports to a teleport point
     */
    public StageTimeline addTeleportEvent(String assignmentMessage, List<BlockVector3> teleportPoint, int offsetRange, AbstractTask.TaskAction<BlockVector3> onTeleportAction) {
        tasks.add(new TeleportPointEventTask(player, assignmentMessage, teleportPoint, offsetRange, onTeleportAction));
        return this;
    }

    /**
     * Adds a ChatEventTask to the timeline.
     * @param assignmentMessage the task message to show in the action bar to the player
     * @param expectedValue the expected value the player needs to chat
     * @param offset the offset in which the player can chat the expected value
     * @param maxAttempts the maximum amount of attempts the player has to chat the expected value
     * @param onChatAction the action to be performed when the player chats the expected value
     */
    public StageTimeline addPlayerChatEvent(String assignmentMessage, int expectedValue, int offset, int maxAttempts, AbstractTask.BiTaskAction<Boolean, Integer> onChatAction) {
        tasks.add(new ChatEventTask(player, assignmentMessage, expectedValue, offset, maxAttempts, onChatAction));
        return this;
    }

    /**
     * Adds a TeleportTask to the timeline.
     * @param tutorialWorldIndex the index of the tutorial world the player teleports to
     */
    public StageTimeline teleport(int tutorialWorldIndex) {
        tasks.add(new TeleportTask(player, tutorialWorldIndex));
        return this;
    }

    /**
     * Adds a InteractNPCEventTask to the timeline.
     * @param assignmentMessage the task message to show in the action bar to the player
     */
    public StageTimeline interactNPC(String assignmentMessage) {
        tasks.add(new InteractNPCEventTask(player, tutorialNPC.getVillager().getUniqueId(), assignmentMessage));
        return this;
    }

    /**
     * Adds a PlaceHologramTask to the timeline.
     * @param tipId the id of the tip, read from the tutorial config
     * @param content the content of the hologram
     */
    public StageTimeline placeTipHologram(int tipId, String content) {
        return placeTipHologram(tipId, content, -1);
    }

    /**
     * Adds a PlaceHologramTask to the timeline.
     * The read more link is a clickable message that shows link to a documentation page.
     * @param tipId the id of the tip, read from the tutorial config
     * @param content the content of the hologram
     * @param readMoreLinkId the id of the read more link, read from the tutorial config
     */
    public StageTimeline placeTipHologram(int tipId, String content, int readMoreLinkId) {
        PlaceHologramTask task = new PlaceHologramTask(player, tipId, content, readMoreLinkId);
        tipHolograms.add(task.getHologram());
        tasks.add(task);
        return this;
    }

    /**
     * Adds a RemoveHologramTask to the timeline.
     * Removes a specific tip hologram from the tutorial world.
     * @param tipId the id of the tip, read from the tutorial config
     */
    public StageTimeline removeTipHologram(int tipId) {
        tasks.add(new RemoveHologramTask(player, tipId, tipHolograms));
        return this;
    }

    /**
     * Adds a RemoveHologramTask to the timeline.
     * Removes all tip holograms from the tutorial world.
     */
    public StageTimeline removeTipHolograms() {
        for (int i = 0; i < tipHolograms.size(); i++) tasks.add(new RemoveHologramTask(player, Integer.parseInt(tipHolograms.get(i).getId()), tipHolograms));
        return this;
    }

    /**
     * Adds a WaitTask to the timeline.
     * @param seconds the number of seconds to wait
     */
    public StageTimeline delay(long seconds) {
        tasks.add(new WaitTask(player, seconds));
        return this;
    }


    /**
     * Gets the active tutorial timelines.
     * @return timelines
     */
    public static List<TutorialTimeLine> getActiveTimelines() {
        return activeTimelines;
    }
}