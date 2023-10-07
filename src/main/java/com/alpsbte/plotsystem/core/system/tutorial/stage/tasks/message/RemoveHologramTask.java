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

package com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.message;

import com.alpsbte.plotsystem.core.holograms.TutorialTipHologram;
import com.alpsbte.plotsystem.core.system.tutorial.stage.tasks.AbstractTask;
import org.bukkit.entity.Player;

import java.util.List;

public class RemoveHologramTask extends AbstractTask {
    private final int tipId;
    private final List<TutorialTipHologram> holograms;

    public RemoveHologramTask(Player player, int tipId, List<TutorialTipHologram> holograms) {
        super(player);
        this.tipId = tipId;
        this.holograms = holograms;
    }

    @Override
    public void performTask() {
        TutorialTipHologram hologram = holograms.stream().filter(holo -> holo.getId().equals(String.valueOf(tipId))).findFirst().orElse(null);
        if (hologram != null && hologram.getHologram(player.getUniqueId()) != null) {
            hologram.remove(player.getUniqueId());
            holograms.remove(hologram);
        }

        setTaskDone();
    }
}
