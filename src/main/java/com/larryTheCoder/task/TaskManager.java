/*
 * Adapted from the Wizardry License
 *
 * Copyright (c) 2016-2020 larryTheCoder and contributors
 *
 * Permission is hereby granted to any persons and/or organizations
 * using this software to copy, modify, merge, publish, and distribute it.
 * Said persons and/or organizations are not allowed to use the software or
 * any derivatives of the work for commercial use or any other means to generate
 * income, nor are they allowed to claim this software as their own.
 *
 * The persons and/or organizations are also disallowed from sub-licensing
 * and/or trademarking this software without explicit permission from larryTheCoder.
 *
 * Any persons and/or organizations using this software must disclose their
 * source code and have it publicly available, include this license,
 * provide sufficient credit to the original authors of the project (IE: larryTheCoder),
 * as well as provide a link to the original project.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,FITNESS FOR A PARTICULAR
 * PURPOSE AND NON INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.larryTheCoder.task;

import com.larryTheCoder.ASkyBlock;

public class TaskManager {

    public static TaskManager IMP;

    public static void runTaskRepeatAsync(Runnable runnable, int interval) {
        if (runnable != null) {
            if (IMP == null) {
                throw new IllegalArgumentException("disabled");
            }
            IMP.taskRepeatAsync(runnable, interval);
        }
    }

    public static void runTaskAsync(Runnable runnable) {
        if (runnable != null) {
            if (IMP == null) {
                runnable.run();
                return;
            }
            IMP.taskAsync(runnable);
        }
    }

    public static void runTask(Runnable runnable) {
        if (runnable != null) {
            if (IMP == null) {
                runnable.run();
                return;
            }
            IMP.task(runnable);
        }
    }

    /**
     * Run task later.
     *
     * @param runnable The task
     * @param delay    The delay in ticks (milliseconds)
     */
    public static void runTaskLater(Runnable runnable, int delay) {
        if (runnable != null) {
            if (IMP == null) {
                runnable.run();
                return;
            }
            IMP.taskLater(runnable, delay);
        }
    }

    private void taskRepeatAsync(Runnable r, int interval) {
        ASkyBlock.get().getServer().getScheduler().scheduleRepeatingTask(ASkyBlock.get(), r, interval, true);
    }

    private void taskAsync(Runnable r) {
        if (r == null) {
            return;
        }
        ASkyBlock.get().getServer().getScheduler().scheduleTask(ASkyBlock.get(), r, true);
    }

    private void task(Runnable r) {
        if (r == null) {
            return;
        }
        ASkyBlock.get().getServer().getScheduler().scheduleTask(ASkyBlock.get(), r, false);
    }

    private void taskLater(Runnable r, int delay) {
        if (r == null) {
            return;
        }
        ASkyBlock.get().getServer().getScheduler().scheduleDelayedTask(ASkyBlock.get(), r, delay);
    }

}
