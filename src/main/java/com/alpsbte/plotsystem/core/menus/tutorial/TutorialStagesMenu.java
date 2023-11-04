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

package com.alpsbte.plotsystem.core.menus.tutorial;

import com.alpsbte.alpslib.utils.item.ItemBuilder;
import com.alpsbte.alpslib.utils.item.LoreBuilder;
import com.alpsbte.plotsystem.core.menus.AbstractMenu;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.tutorial.AbstractTutorial;
import com.alpsbte.plotsystem.core.system.tutorial.Tutorial;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialCategory;
import com.alpsbte.plotsystem.core.system.tutorial.TutorialUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.ConfigUtil;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import com.alpsbte.plotsystem.utils.items.MenuItems;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.logging.Level;

import static net.md_5.bungee.api.ChatColor.*;

public class TutorialStagesMenu extends AbstractMenu {
    private static final int totalStagesRows = 2;
    private static final int totalStagesSlots = totalStagesRows * (9 - 2);
    private static final int stagesPerRow = totalStagesSlots / totalStagesRows;

    private final int tutorialId;
    private final String tutorialItemName;

    private final int stagesInFirstRow;
    private final int stagesInSecondRow;
    private final int startSlotFirstRow;
    private final int startSlotSecondRow;

    private Tutorial tutorial;
    private TutorialPlot plot;

    private int playerHighestStage = -1;
    private int playerCurrentStage = -1;
    private boolean isTutorialCompleted;

    public TutorialStagesMenu(Player menuPlayer, int tutorialId) {
        super(6, LangUtil.getInstance().getString(menuPlayer, LangPaths.MenuTitle.TUTORIAL_STAGES), menuPlayer, false);
        this.tutorialId = tutorialId;
        int totalStages = ConfigUtil.getTutorialInstance().configs[tutorialId].getInt(TutorialUtils.Path.TUTORIAL_STAGES);

        // Calculate the number of stages to place in the first row
        stagesInFirstRow = Math.min(stagesPerRow, totalStages);

        // Calculate the number of stages to place in the second row
        stagesInSecondRow = Math.min(stagesPerRow, totalStages - stagesInFirstRow);

        // Calculate the starting slot for the first row
        startSlotFirstRow = 9 * 2 + ((stagesPerRow - stagesInFirstRow) / 2) + 1; // 9 = slots, 2 = rows

        // Calculate the starting slot for the second row
        startSlotSecondRow = 9 * 3 + ((stagesPerRow - stagesInSecondRow) / 2) + 1;

        // Get tutorial item name
        tutorialItemName = ConfigUtil.getTutorialInstance().configs[tutorialId].getString(TutorialUtils.Path.TUTORIAL_ITEM_NAME);

        reloadMenuAsync();
    }

    @Override
    protected void setPreviewItems() {
        // Set loading item for tutorial item
        getMenu().getSlot(4).setItem(MenuItems.loadingItem(Material.valueOf(tutorialItemName), getMenuPlayer()));

        // Place stages in the first row
        for (int i = 0; i < stagesInFirstRow; i++) {
            getMenu().getSlot(startSlotFirstRow + i).setItem(MenuItems.loadingItem(Material.GRAY_STAINED_GLASS_PANE, getMenuPlayer()));
        }

        // Place stages in the second row
        for (int i = 0; i < stagesInSecondRow; i++) {
            getMenu().getSlot(startSlotSecondRow + i).setItem(MenuItems.loadingItem(Material.GRAY_STAINED_GLASS_PANE, getMenuPlayer()));
        }

        // Get tutorial
        tutorial = AbstractTutorial.getActiveTutorial(getMenuPlayer().getUniqueId());
        if (tutorial != null) playerCurrentStage = tutorial.getCurrentStage();

        // Set end tutorial item if the player is in a tutorial, otherwise set back item
        if (playerCurrentStage != -1) {
            getMenu().getSlot(49).setItem(new ItemBuilder(Material.BARRIER)
                    .setName(RED + BOLD.toString() + LangUtil.getInstance().getString(getMenuPlayer(), LangPaths.MenuTitle.TUTORIAL_END))
                    .setLore(new LoreBuilder().addLine(LangUtil.getInstance().getString(getMenuPlayer(), LangPaths.MenuDescription.TUTORIAL_END)).build())
                    .build());
        } else getMenu().getSlot(49).setItem(MenuItems.backMenuItem(getMenuPlayer()));

        super.setPreviewItems();
    }

    @Override
    protected void setMenuItemsAsync() {
        try {
            plot = TutorialPlot.getPlot(getMenuPlayer().getUniqueId().toString(), tutorialId);
            if (plot != null) {
                playerHighestStage = plot.getStageID();
                isTutorialCompleted = plot.isCompleted();
            }
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.INFO, "A SQL error occurred!", ex);
        }

        // Set tutorial stats item
        ItemBuilder tutorialItem = new ItemBuilder(Material.valueOf(tutorialItemName));
        tutorialItem.setName(AQUA + BOLD.toString() + LangUtil.getInstance().getString(getMenuPlayer(), LangPaths.MenuTitle.TUTORIAL_BEGINNER));
        if (plot != null) {
            tutorialItem.setLore(
                    new LoreBuilder().addLines("",
                            LangUtil.getInstance().getString(getMenuPlayer(), LangPaths.Tutorials.TUTORIALS_STAGE) + ": " + WHITE +
                            (playerHighestStage + (isTutorialCompleted ? 1 : 0)) + "/" + ConfigUtil.getTutorialInstance().configs[tutorialId].getInt(TutorialUtils.Path.TUTORIAL_STAGES))
                    .build());
        }
        getMenu().getSlot(4).setItem(tutorialItem.build());

        // Place stages in the first row
        for (int i = 0; i < stagesInFirstRow; i++) {
            getMenu().getSlot(startSlotFirstRow + i).setItem(getStageItem(tutorialId, i));
        }

        // Place stages in the second row
        for (int i = 0; i < stagesInSecondRow; i++) {
            getMenu().getSlot(startSlotSecondRow + i).setItem(getStageItem(tutorialId, stagesInFirstRow + i));
        }
    }

    @Override
    protected void setItemClickEventsAsync() {
        // Set click events for stages in the first row
        for (int i = 0; i < stagesInFirstRow; i++) {
            setStageClickEvent(startSlotFirstRow + i, i);
        }

        // Set click events for stages in the second row
        for (int i = 0; i < stagesInSecondRow; i++) {
            setStageClickEvent(startSlotSecondRow + i, stagesInFirstRow + i);
        }

        // Set click event for back item
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> {
            if (playerCurrentStage != -1) {
                clickPlayer.closeInventory();
                tutorial.onTutorialStop(clickPlayer.getUniqueId());
            } else new TutorialsMenu(clickPlayer);
        });
    }

    @Override
    protected Mask getMask() {
        return BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
    }

    /**
     * Sets the click event for a stage item and loads the tutorial stage
     * @param slot The slot of the stage item
     * @param stageId The id of the stage
     */
    private void setStageClickEvent(int slot, int stageId) {
        getMenu().getSlot(slot).setClickHandler(((player, clickInformation) -> {
            getMenuPlayer().playSound(getMenuPlayer().getLocation(), Sound.ENTITY_ITEM_FRAME_ADD_ITEM, 0.8f, 0.8f);

            if (playerCurrentStage != stageId && playerHighestStage >= stageId && clickInformation.getClickType().isLeftClick()) {
                getMenuPlayer().closeInventory();

                // Load the tutorial stage by id
                if (!AbstractTutorial.loadTutorial(getMenuPlayer(), tutorialId, stageId)) {
                    Tutorial tutorial = AbstractTutorial.getActiveTutorial(player.getUniqueId());
                    if (tutorial != null) {
                        if (tutorial.getId() == tutorialId) tutorial.setStage(stageId);
                    } else {
                        getMenuPlayer().sendMessage(Utils.ChatUtils.getErrorMessageFormat(LangUtil.getInstance().getString(getMenuPlayer(), LangPaths.Message.Error.ERROR_OCCURRED)));
                        getMenuPlayer().playSound(getMenuPlayer().getLocation(), Utils.SoundUtils.ERROR_SOUND, 1, 1);
                    }
                }
            }
        }));
    }

    /**
     * Gets the menu stage item by the given tutorial id and stage id
     * @param tutorialId The tutorial id
     * @param stageId The stage id
     * @return The menu stage item
     */
    private ItemStack getStageItem(int tutorialId, int stageId) {
        LoreBuilder lore = new LoreBuilder().addLine(getStageTitle(getMenuPlayer(), tutorialId, stageId + 1));
        boolean isInProgress = playerHighestStage == stageId && plot != null && !isTutorialCompleted;

        ChatColor titleColor = isInProgress ? ChatColor.YELLOW : (stageId < playerHighestStage || isTutorialCompleted ? ChatColor.GREEN : RED);
        ItemStack stageItem = new ItemStack(isInProgress ? Material.YELLOW_STAINED_GLASS_PANE : (stageId < playerHighestStage || isTutorialCompleted ?
                Material.LIME_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE), stageId + 1);
        boolean isClickable = stageId != playerCurrentStage && stageId <= playerHighestStage;

        if (isClickable) lore.addLines("", LangUtil.getInstance().getString(getMenuPlayer(), LangPaths.Note.Action.LEFT_CLICK) + " §8» " +
                "§e" + LangUtil.getInstance().getString(getMenuPlayer(), LangPaths.Note.Action.START));

        return new ItemBuilder(stageItem)
                .setName(titleColor + ChatColor.BOLD.toString() + LangUtil.getInstance().getString(getMenuPlayer(), LangPaths.Tutorials.TUTORIALS_STAGE) + " " + (stageId + 1))
                .setLore(lore.build())
                .setEnchanted(playerCurrentStage == stageId)
                .build();
    }

    /**
     * Gets the stage title by reading the language file manually.
     * @param player The player
     * @param tutorialId The tutorial id
     * @param stageId The stage id
     * @return The stage title
     */
    private static String getStageTitle(Player player, int tutorialId, int stageId) {
        String stageString = "stage-" + stageId;
        return LangUtil.getInstance().getString(player, LangPaths.Tutorials.TUTORIALS +
                TutorialCategory.values()[tutorialId].name().toLowerCase() + "." +
                stageString + "." + stageString + "-title");
    }
}
