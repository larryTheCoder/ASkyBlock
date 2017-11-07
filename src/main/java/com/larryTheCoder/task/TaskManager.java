package com.larryTheCoder.task;

import cn.nukkit.scheduler.TaskHandler;
import com.larryTheCoder.ASkyBlock;
import com.larryTheCoder.utils.object.RunnableVal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager {

    public static final HashSet<String> TELEPORT_QUEUE = new HashSet<>();
    public static final HashMap<Integer, Integer> TASK = new HashMap<>();
    public static TaskManager IMP;
    public static AtomicInteger index = new AtomicInteger(0);

    public static int runTaskRepeat(Runnable runnable, int interval) {
        if (runnable != null) {
            if (IMP == null) {
                throw new IllegalArgumentException("disabled");
            }
            return IMP.taskRepeat(runnable, interval);
        }
        return -1;
    }

    public static int runTaskRepeatAsync(Runnable runnable, int interval) {
        if (runnable != null) {
            if (IMP == null) {
                throw new IllegalArgumentException("disabled");
            }
            return IMP.taskRepeat(runnable, interval);
        }
        return -1;
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
     * @param delay    The delay in ticks
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

    public static void runTaskLaterAsync(Runnable runnable, int delay) {
        if (runnable != null) {
            if (IMP == null) {
                runnable.run();
                return;
            }
            IMP.taskLaterAsync(runnable, delay);
        }
    }

    /**
     * Break up a series of tasks so that they can run without lagging the
     * server.
     *
     * @param objects
     * @param task
     * @param whenDone
     */
    public static <T> void objectTask(Collection<T> objects, final RunnableVal<T> task, final Runnable whenDone) {
        final Iterator<T> iterator = objects.iterator();
        TaskManager.runTask(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                boolean hasNext;
                while ((hasNext = iterator.hasNext()) && System.currentTimeMillis() - start < 5) {
                    task.value = iterator.next();
                    task.run();
                }
                if (!hasNext) {
                    TaskManager.runTaskLater(whenDone, 1);
                } else {
                    TaskManager.runTaskLater(this, 1);
                }
            }
        });
    }

    public <T> T sync(final RunnableVal<T> function) {
        return sync(function, Integer.MAX_VALUE);
    }

    public <T> T sync(final RunnableVal<T> function, int timeout) {
        final AtomicBoolean running = new AtomicBoolean(true);
        RunnableVal<RuntimeException> run = new RunnableVal<RuntimeException>() {
            @Override
            public void run(RuntimeException value) {
                try {
                    function.run();
                } catch (RuntimeException e) {
                    this.value = e;
                } catch (Throwable neverHappens) {
                    if (ASkyBlock.get().isDebug()) {
                        neverHappens.printStackTrace();
                    }
                } finally {
                    running.set(false);
                }
                synchronized (function) {
                    function.notifyAll();
                }
            }
        };
        TaskManager.IMP.task(run);
        try {
            synchronized (function) {
                while (running.get()) {
                    function.wait(timeout);
                }
            }
        } catch (InterruptedException e) {
            if (ASkyBlock.get().isDebug()) {
                e.printStackTrace();
            }
        }
        if (run.value != null) {
            throw run.value;
        }
        return function.value;
    }

    public int taskRepeat(Runnable r, int interval) {
        TaskHandler task = ASkyBlock.get().getServer().getScheduler().scheduleRepeatingTask(ASkyBlock.get(), r, interval, false);
        return task.getTaskId();
    }

    @SuppressWarnings("deprecation")
    public int taskRepeatAsync(Runnable r, int interval) {
        TaskHandler task = ASkyBlock.get().getServer().getScheduler().scheduleRepeatingTask(ASkyBlock.get(), r, interval, true);
        return task.getTaskId();
    }

    public void taskAsync(Runnable r) {
        if (r == null) {
            return;
        }
        ASkyBlock.get().getServer().getScheduler().scheduleTask(ASkyBlock.get(), r, true);
    }

    public void task(Runnable r) {
        if (r == null) {
            return;
        }
        ASkyBlock.get().getServer().getScheduler().scheduleTask(ASkyBlock.get(), r, false);
    }

    public void taskLater(Runnable r, int delay) {
        if (r == null) {
            return;
        }
        ASkyBlock.get().getServer().getScheduler().scheduleDelayedTask(ASkyBlock.get(), r, delay);
    }

    public void taskLaterAsync(Runnable r, int delay) {
        ASkyBlock.get().getServer().getScheduler().scheduleDelayedTask(ASkyBlock.get(), r, delay, true);
    }

    public void cancelTask(int task) {
        if (task != -1) {
            ASkyBlock.get().getServer().getScheduler().cancelTask(task);
        }
    }
}
