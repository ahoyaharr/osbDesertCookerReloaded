import org.osbot.rs07.script.MethodProvider;

public final class aHopper {

    private int[] rsbFree = {
            1, 8,
            16,
            26,
            35,
            81, 82, 83, 84, 85,
            93, 94};
    private int[] f2pWorlds = rsbFree;

    private int baseWorldIndex, currWorldIndex;
    private int totalJumps = 0;

    protected MethodProvider methods;

    public aHopper(MethodProvider methods) {
        this.methods = methods;
        for (int i = 0; i < rsbFree.length - 1; i++) {
            f2pWorlds[i] += 300;
        }
        baseWorldIndex = currWorldIndex = getWorldIndex(methods.getWorlds().getCurrentWorld());
    }

    // Private methods
    private int getWorldIndex(int world) {
        if (world > 300) {
            for (int i = 0; i < f2pWorlds.length; i++) {
                if (world == f2pWorlds[i]) {
                    return i;
                }
            }
        } else if (world > 0) {
            for (int i = 0; i < f2pWorlds.length; i++) {
                if (world == rsbFree[i]) {
                    return i;
                }
            }
        }
        return -1;
    }

    private int getNextWorldIndex() {
        currWorldIndex = getWorldIndex(methods.getWorlds().getCurrentWorld());
        if (++currWorldIndex - baseWorldIndex == rsbFree.length - 1)
            currWorldIndex = 0;
        return currWorldIndex;
    }

    private int getPrevWorldIndex() {
        currWorldIndex = getWorldIndex(methods.getWorlds().getCurrentWorld());
        if (currWorldIndex == 0) {
            currWorldIndex = rsbFree[rsbFree.length - 1];
        } else {
            currWorldIndex--;
        }
        return currWorldIndex;
    }

    // Accessors
    public int getCurrWorld() {
        return f2pWorlds[currWorldIndex];
    }
    public int getTotalJumps() {
        return totalJumps;
    }

    // Status
    boolean isNextHopMultipleOf(int n) {
        if ((totalJumps + 1) % n == 0) {
            return true;
        }
        return false;
    }

    // World Hopping
    void jumpToNext() {
        methods.getWorlds().hop(rsbFree[getNextWorldIndex()]);
        totalJumps++;
        methods.log("Attempting jump to next world...");
    }
    void jumpToPrev() {
        methods.getWorlds().hop(rsbFree[getPrevWorldIndex()]);
        totalJumps++;
        methods.log("Attempting to jump to previous world...");
    }
    boolean jumpTo(int n) {
        if (getWorldIndex(n) != -1) {
            methods.getWorlds().hop(n);
            currWorldIndex = getWorldIndex(n);
            return true;
        }
        return false;
    }
}