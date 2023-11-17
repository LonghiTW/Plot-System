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

package com.alpsbte.plotsystem.utils.chat;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.LangPaths;
import com.alpsbte.plotsystem.utils.io.LangUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.alpsbte.plotsystem.core.system.tutorial.TutorialUtils.TEXT_HIGHLIGHT_END;
import static com.alpsbte.plotsystem.core.system.tutorial.TutorialUtils.TEXT_HIGHLIGHT_START;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public class PlayerInviteeChatInput implements ChatInput {
    private final LocalDateTime dateTime;
    private final Plot plot;

    public PlayerInviteeChatInput(UUID playerUUID, Plot plot) {
        this.dateTime = LocalDateTime.now();
        this.plot = plot;
        awaitChatInput.put(playerUUID, this);
    }

    @Override
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public Plot getPlot() {
        return plot;
    }

    public static void sendChatInputMessage(Player player) {
        player.sendMessage(Component.empty());
        player.sendMessage(Utils.ChatUtils.getInfoFormat(text(LangUtil.getInstance().get(player, LangPaths.Message.Info.CHAT_ENTER_PLAYER), GRAY)));
        Utils.ChatUtils.sendChatInputExpiryComponent(player);
        player.sendMessage(Component.empty());
        player.playSound(player.getLocation(), Utils.SoundUtils.NOTIFICATION_SOUND, 1f, 1f);
    }
}
