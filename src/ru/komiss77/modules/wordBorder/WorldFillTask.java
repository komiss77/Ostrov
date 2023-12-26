package ru.komiss77.modules.wordBorder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.Server;
import org.bukkit.World;
import ru.komiss77.ApiOstrov;
import ru.komiss77.modules.world.WorldManager;
import ru.komiss77.Ostrov;

//https://github.com/Brettflan/WorldBorder/tree/master/src/main/java/com/wimbli/WorldBorder
public class WorldFillTask implements Runnable {

    private transient Server server = null;
    private transient World world = null;
    private transient WorldFileData worldData = null;
    private transient boolean readyToGo = false;
    private transient boolean paused = false;
    private transient boolean pausedForMemory = false;
    private transient int taskID = -1;
    private transient int chunksPerRun = 1;
    private transient boolean continueNotice = false;
    private transient boolean forceLoad = false;

    private transient int fillDistance = 208;
    private transient int tickFrequency = 1;
    private transient int refX = 0, lastLegX = 0;
    private transient int refZ = 0, lastLegZ = 0;
    private transient int refLength = -1;
    private transient int refTotal = 0, lastLegTotal = 0;

    // values for the spiral pattern check which fills out the map to the border
    private transient int cX = 0;
    private transient int cZ = 0;
    private transient boolean isZLeg = false;
    private transient boolean isNeg = false;
    private transient int length = -1;
    private transient int current = 0;
    private transient boolean insideBorder = true;
    private List<CoordXZ> storedChunks = new LinkedList<>();
    private Set<CoordXZ> originalChunks = new HashSet<>();
    private transient CoordXZ lastChunk = new CoordXZ(0, 0);

    // for reporting progress back to user occasionally
    private transient long lastReport = System.currentTimeMillis();
    private transient long lastAutosave = System.currentTimeMillis();
    private transient int totalWorldChunk = 0;
    private transient int reportTotal = 0;
    private transient int reportNum = 0;

    private int cXmax;
    private int cXmin;
    private int cZmax;
    private int cZmin;

    public WorldFillTask(final String worldName) {
        this.server = Bukkit.getServer();
//System.out.println("fillDistance="+fillDistance+" tickFrequency="+tickFrequency+" chunksPerRun="+chunksPerRun+" force="+forceLoad);

        this.world = server.getWorld(worldName);
        if (this.world == null) {
            if (worldName.isEmpty()) {
                sendMessage("You must specify a world!");
            } else {
                sendMessage("World \"" + worldName + "\" not found!");
            }
            this.stop();
            return;
        }

        // load up a new WorldFileData for the world in question, used to scan region files for which chunks are already fully generated and such
        worldData = WorldFileData.create(world);
        if (worldData == null) {
            this.stop();
            return;
        }

        //берём стартовый чанк в центре
        this.cX = CoordXZ.blockToChunk(world.getWorldBorder().getCenter().getBlockX());//CoordXZ.blockToChunk((int)border.getX());
        this.cZ = CoordXZ.blockToChunk(world.getWorldBorder().getCenter().getBlockZ());//CoordXZ.blockToChunk((int)border.getZ());

        int worldDiameter = (int) world.getWorldBorder().getSize() < Bukkit.getServer().getMaxWorldSize() ? ((int) world.getWorldBorder().getSize()) : Bukkit.getServer().getMaxWorldSize();//VM.getNmsServer().getMaxWorldSize(p.getWorld());//propertyManager.getInt("max-world-size", 500);
        int chunkGenDiameter = (worldDiameter >> 4); //(int) Math.ceil((double)((worldRadius + 160) * 2) / 16); //160-прогенерить за границей, чтобы не было обрыва

        totalWorldChunk = (chunkGenDiameter * chunkGenDiameter) + chunkGenDiameter + 1;
//System.out.println(" worldRadius="+worldDiameter+" chunkGenRadius="+chunkGenDiameter+" totalWorldChunk="+totalWorldChunk);
        cXmax = cX + chunkGenDiameter / 2 + Bukkit.getServer().getViewDistance();
        cXmin = cX - (chunkGenDiameter / 2 + Bukkit.getServer().getViewDistance());
        cZmax = cZ + chunkGenDiameter / 2 + Bukkit.getServer().getViewDistance();
        cZmin = cZ - (chunkGenDiameter / 2 + Bukkit.getServer().getViewDistance());

        // keep track of the chunks which are already loaded when the task starts, to not unload them
        Chunk[] originals = world.getLoadedChunks();
        for (Chunk original : originals) {
            originalChunks.add(new CoordXZ(original.getX(), original.getZ()));
        }

        this.readyToGo = true;
    }

    public void setTaskID(int ID) {
        if (ID == -1) {
            this.stop();
        }
        this.taskID = ID;
    }

    @Override
    public void run() {
        if (continueNotice) {	// notify user that task has continued automatically
            continueNotice = false;
            sendMessage("Предгенерация автоматически продолжена.");
            //sendMessage("wb fill cancel - отмена,  wb fill pause - пауза");
        }

        if (pausedForMemory) {	// if available memory gets too low, we automatically pause, so handle that
            if (WorldManager.AvailableMemoryTooLow()) {
                return;
            }
            pausedForMemory = false;
            readyToGo = true;
            sendMessage("Available memory is sufficient, automatically continuing.");
        }

        if (server == null || !readyToGo || paused) {
            return;
        }

        // this is set so it only does one iteration at a time, no matter how frequently the timer fires
        readyToGo = false;
        // and this is tracked to keep one iteration from dragging on too long and possibly choking the system if the user specified a really high frequency
        long loopStartTime = System.currentTimeMillis();//System.currentTimeMillis();

        for (int loop = 0; loop < chunksPerRun; loop++) {
            // in case the task has been paused while we're repeating...
            if (paused || pausedForMemory) {
                return;
            }

            long now = System.currentTimeMillis();

            // every 5 seconds or so, give basic progress report to let user know how it's going
            if (now > lastReport + 5000) {
                reportProgress();
            }

            // if this iteration has been running for 45ms (almost 1 tick) or more, stop to take a breather
            if (now > loopStartTime + 45) {
                readyToGo = true;
                return;
            }

            // if we've made it at least partly outside the border, skip past any such chunks
            while (!insideBorder(cX, cZ)) {
                if (!moveToNext()) {
                    return;
                }
            }
            insideBorder = true;

            if (!forceLoad) {
                // skip past any chunks which are confirmed as fully generated using our super-special isChunkFullyGenerated routine
                while (worldData.isChunkFullyGenerated(cX, cZ)) {
                    insideBorder = true;
                    if (!moveToNext()) {
                        return;
                    }
                }
            }

            // load the target chunk and generate it if necessary
            world.loadChunk(cX, cZ, true);
//world.getChunkAt(cX, cX).getBlock(7, 150, 7).setType(Material.EMERALD_BLOCK);
            worldData.chunkExistsNow(cX, cZ);

            // There need to be enough nearby chunks loaded to make the server populate a chunk with trees, snow, etc.
            // So, we keep the last few chunks loaded, and need to also temporarily load an extra inside chunk (neighbor closest to center of map)
            int popX = !isZLeg ? cX : (cX + (isNeg ? -1 : 1));
            int popZ = isZLeg ? cZ : (cZ + (!isNeg ? -1 : 1));
            world.loadChunk(popX, popZ, false);
//world.getChunkAt(popX, popZ).getBlock(7, 150, 7).setType(Material.EMERALD_BLOCK);

            // make sure the previous chunk in our spiral is loaded as well (might have already existed and been skipped over)
            if (!storedChunks.contains(lastChunk) && !originalChunks.contains(lastChunk)) {
                world.loadChunk(lastChunk.x, lastChunk.z, false);
//world.getChunkAt(lastChunk.x,  lastChunk.z).getBlock(7, 150, 7).setType(Material.EMERALD_BLOCK);
                storedChunks.add(new CoordXZ(lastChunk.x, lastChunk.z));
            }

            // Store the coordinates of these latest 2 chunks we just loaded, so we can unload them after a bit...
            storedChunks.add(new CoordXZ(popX, popZ));
            storedChunks.add(new CoordXZ(cX, cZ));

            // If enough stored chunks are buffered in, go ahead and unload the oldest to free up memory
            while (storedChunks.size() > 8) {
                CoordXZ coord = storedChunks.remove(0);
                if (!originalChunks.contains(coord)) {
                    world.unloadChunkRequest(coord.x, coord.z);
                }
            }

            // move on to next chunk
            if (!moveToNext()) {
                return;
            }
        }

        // ready for the next iteration to run
        readyToGo = true;
    }

    // step through chunks in spiral pattern from center; returns false if we're done, otherwise returns true
    public boolean moveToNext() {
        if (paused || pausedForMemory) {
//System.out.println("paused="+paused+"  pausedForMemory="+pausedForMemory);            
            return false;
        }

        reportNum++;
//System.out.println("isNeg="+isNeg+"  current="+current+" length="+length);            

        // keep track of progress in case we need to save to config for restoring progress after server restart
        if (!isNeg && current == 0 && length > 3) {
            if (!isZLeg) {
                lastLegX = cX;
                lastLegZ = cZ;
                lastLegTotal = reportTotal + reportNum;
            } else {
                refX = lastLegX;
                refZ = lastLegZ;
                refTotal = lastLegTotal;
                refLength = length - 1;
            }
        }

        // make sure of the direction we're moving (X or Z? negative or positive?)
        if (current < length) {
            current++;
        } else { // one leg/side of the spiral down...
            current = 0;
            isZLeg ^= true;
            if (isZLeg) {	// every second leg (between X and Z legs, negative or positive), length increases
                isNeg ^= true;
                length++;
            }
        }

        // keep track of the last chunk we were at
        lastChunk.x = cX;
        lastChunk.z = cZ;

        // move one chunk further in the appropriate direction
        if (isZLeg) {
            cZ += (isNeg) ? -1 : 1;
        } else {
            cX += (isNeg) ? -1 : 1;
        }

        // if we've been around one full loop (4 legs)...
        if (isZLeg && isNeg && current == 0) {	// see if we've been outside the border for the whole loop
            if (!insideBorder) {	// and finish if so
                finish();
                return false;
            } else {	// otherwise, reset the "inside border" flag
                insideBorder = false;
            }
        }
        return true;

        /* reference diagram used, should move in this pattern:
	 *  8 [>][>][>][>][>] etc.
	 * [^][6][>][>][>][>][>][6]
	 * [^][^][4][>][>][>][4][v]
	 * [^][^][^][2][>][2][v][v]
	 * [^][^][^][^][0][v][v][v]
	 * [^][^][^][1][1][v][v][v]
	 * [^][^][3][<][<][3][v][v]
	 * [^][5][<][<][<][<][5][v]
	 * [7][<][<][<][<][<][<][7]
         */
    }

    // for successful completion
    public void finish() {
        this.paused = true;
        reportProgress();
        world.save();
        sendMessage("предгенерация чанков для мира " + worldName()+ " выполнена!");
        this.stop();
    }

    // for cancelling prematurely
    public void cancel() {
        this.stop();
    }

    // we're done, whether finished or cancelled
    private void stop() {
        if (server == null) {
            return;
        }

        readyToGo = false;
        if (taskID != -1) {
            server.getScheduler().cancelTask(taskID);
        }
        server = null;

        // go ahead and unload any chunks we still have loaded
        while (!storedChunks.isEmpty()) {
            CoordXZ coord = storedChunks.remove(0);
            if (!originalChunks.contains(coord)) {
                world.unloadChunkRequest(coord.x, coord.z);
            }
        }
        WorldManager.fillTask = null;
    }

    // is this task still valid/workable?
    public boolean valid() {
        return this.server != null;
    }

    // handle pausing/unpausing the task
    public void pause() {
        if (this.pausedForMemory) {
            pause(false);
        } else {
            pause(!this.paused);
        }
    }

    public void pause(boolean pause) {
        if (this.pausedForMemory && !pause) {
            this.pausedForMemory = false;
        } else {
            this.paused = pause;
        }
        if (this.paused) {
            WorldManager.StoreFillTask();
            reportProgress();
        } else {
            WorldManager.UnStoreFillTask();
        }
    }

    public boolean isPaused() {
        return this.paused || this.pausedForMemory;
    }

    // let the user know how things are coming along
    private void reportProgress() {
        lastReport = System.currentTimeMillis();
        sendMessage("+" + reportNum + " чанков (" + (reportTotal + reportNum) + " всего), ~" + getPercentageCompleted() + "% от мира");
        reportTotal += reportNum;
        reportNum = 0;

        //WorldManager.save(true);
        // go ahead and save world to disk every 30 seconds or so by default, just in case; can take a couple of seconds or more, so we don't want to run it too often
        //if (WbConfig.FillAutosaveFrequency() > 0 && lastAutosave + (WbConfig.FillAutosaveFrequency() * 1000) < lastReport) {
        if (WorldManager.fillAutosaveFrequency > 0 && lastAutosave + (WorldManager.fillAutosaveFrequency * 1000) < lastReport) {
            lastAutosave = lastReport;
            sendMessage("Сохранения чанков на диск.");
            world.save();
        }
    }

    // send a message to the server console/log and possibly to an in-game player
    private void sendMessage(String text) {
        // Due to chunk generation eating up memory and Java being too slow about GC, we need to track memory availability
        int availMem = WorldManager.AvailableMemory();
        text = "§5[Fill] " + text + " (доступно RAM: " + availMem + " MB)";
        Ostrov.log_ok(text);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (ApiOstrov.canBeBuilder(p)) {
                p.sendMessage(text);
            }
        }

        if (availMem < 200) {	// running low on memory, auto-pause
            pausedForMemory = true;
            WorldManager.StoreFillTask();
            //text = "Недостаточно свободной памяти. Сейчас будет вызван GC. Если это не поможет, перезагрузите сервер";
            //text = "Available memory is very low, task is pausing. A cleanup will be attempted now, and the task will automatically continue if/when sufficient memory is freed up.\n Alternatively, if you restart the server, this task will automatically continue once the server is back up.";
            Ostrov.log_warn("§6[Fill] Недостаточно свободной памяти. Сейчас будет вызван GC. Если это не поможет, перезагрузите сервер");
            //if (notifyPlayer != null)
            //notifyPlayer.sendMessage("[Fill] " + text);
            // prod Java with a request to go ahead and do GC to clean unloaded chunks from memory; this seems to work wonders almost immediately
            // yes, explicit calls to System.gc() are normally bad, but in this case it otherwise can take a long long long time for Java to recover memory
            System.gc();
        }
    }

    // stuff for saving / restoring progress
    public void continueProgress(int x, int z, int length, int totalDone) {
        Ostrov.log_warn("===========continueProgress ");
        this.cX = x;
        this.cZ = z;
        this.length = length;
        this.reportTotal = totalDone;
        this.continueNotice = true;
    }

    public int refX() {
        return refX;
    }

    public int refZ() {
        return refZ;
    }

    public int refLength() {
        return refLength;
    }

    public int refTotal() {
        return refTotal;
    }

    public int refFillDistance() {
        return fillDistance;
    }

    public int refTickFrequency() {
        return tickFrequency;
    }

    public int refChunksPerRun() {
        return chunksPerRun;
    }

    public String worldName() {
        return world.getName();
    }

    public boolean refForceLoad() {
        return forceLoad;
    }

    /**
     * Get the percentage completed for the fill task.
     *
     * @return Percentage
     */
    public int getPercentageCompleted() {
        int percent = (reportTotal + reportNum) * 100 / totalWorldChunk;
        return percent > 99 ? 99 : percent;
    }

    /**
     * Amount of chunks completed for the fill task.
     *
     * @return Number of chunks processed.
     */
    public int getChunksCompleted() {
        return reportTotal;
    }

    /**
     * Total amount of chunks that need to be generated for the fill task.
     *
     * @return Number of chunks that need to be processed.
     */
    public int getChunksTotal() {
        return totalWorldChunk;
    }

    private boolean insideBorder(final int cX, final int cZ) {
        return cX <= cXmax && cX >= cXmin && cZ <= cZmax && cZ >= cZmin;
    }
}
