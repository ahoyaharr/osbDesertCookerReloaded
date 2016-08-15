import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.util.ExperienceTracker;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.awt.*;
import java.util.Hashtable;
import java.util.Set;
import java.util.List;

@ScriptManifest(name = "aleksandrDesertChef", author = "Solzhenitsyn", version = 1.2, info = "AIO Cooker + Pizza maker", logo = "")
public class aleksandrCookerRewrite extends Script {

    private Timer timer;
    private Cursor m;
    private String status = "Getting ready for work...";

    //Paint
    Hashtable<String, Integer> progress = new Hashtable<>();
    private String[] cookedFood = {"Shrimps", "Beef", "Chicken", "Sardine", "Anchovies", "Trout", "Salmon", "Tuna", "Plain pizza", "Lobster", "Swordfish", "Monkfish",
            "Shark", "Sea turtle", "Dark crab", "Manta ray"};
    private String[] combinedIngredients = {"Incomplete pizza", "Uncooked pizza", "Plain pizza", "Anchovy Pizza"};

    // Areas
    private Area validRegion = new Area(
            new int[][]{
                    {3269, 3174}, {3269, 3160}, {3273, 3160},
                    {3273, 3166}, {3280, 3179}, {3279, 3184},
                    {3271, 3183}, {3271, 3179}, {3275, 3179},
                    {3273, 3174}
            }
    );

    private Area grandExchange = new Area(3172, 3487, 3169, 3491);

    // Used for obstacle handling
    private Position insideRange = new Position(3272, 3180, 0);
    private Position insideBank = new Position(3269, 3170, 0);

    private NPC banker = null;

    // Flags
    private int combineIngredients = -2;
    private ExperienceTracker xpTracker;


    // Index corresponds
    private String[] pizzaIngredients = {"Pizza base", "Tomato", "Incomplete pizza", "Cheese", "Anchovies", "Plain pizza"};
    private String[] rawFoods = {"Raw shrimps", "Raw beef", "Raw chicken", "Raw sardine", "Raw trout", "Raw anchovies", "Raw salmon", "Raw tuna", "Uncooked pizza", "Raw lobster", "Raw swordfish",
            "Raw monkfish", "Raw shark", "Raw sea turtle", "Raw dark crab", "Raw manta ray"};
    private int[] rawFoodLevels = {1, 1, 1, 1, 15, 34, 50, 60, 68, 74, 86, 90, 99, 99, 99, 99};

    private enum State {
        Lost,   // returns player character to valid region
        Bank,   // opens bank interface, stores all items, withdraws appropriate items, closes bank interface
        Pizza,  // combines pizza ingredients
        Cook    // walks to the range, cooks food
    }

    // Ingredient Methods
    private int getBankPizzaIngredientsIndex() {
        for (int i = 1; i < pizzaIngredients.length; i += 2) {
            if (getBank().contains(pizzaIngredients[i])
                    && getBank().contains(pizzaIngredients[i - 1])
                    && getSkills().getStatic(Skill.COOKING) >= 55) {
                return i;
            }
        }
        log("returning " + (-1) + " for pizza status");
        return -1;
    }

    private int getInventoryRawFoodIndex() {
        for (int i = 0; i < rawFoods.length - 1; i++) {
            if (getInventory().contains(rawFoods[i])
                    && getSkills().getStatic(Skill.COOKING) >= rawFoodLevels[i]) {
                return i;
            }
        }
        return -1;
    }

    private int getBankRawFoodIndex() {
        if (!getBank().isOpen()) {
            log("Tried checking for items in the bank, but the bank interface was not open. Returning -1.");
            return -1;
        }
        for (int i = 0; i < rawFoods.length - 1; i++) {
            if (getBank().contains(rawFoods[i])
                    && getSkills().getStatic(Skill.COOKING) >= rawFoodLevels[i]) {
                return i;
            }
        }
        return -1;
    }

    // Get NPCs
    // Gets identity of closest banker
    private NPC getBanker() {
        List<NPC> npcs = getNpcs().get(3267, 3167);
        for (NPC a : npcs) {
            if (a.getName().equals("Banker")) {
                return a;
            }
        }
        return null;
    }

    // Set camera
    private void setCamera(int pitchLower, int pitchUpper, int yawLower, int yawUpper) {
        getCamera().movePitch(random(pitchLower, pitchUpper));
        getCamera().moveYaw(random(yawLower, yawUpper));
    }

    // Convert time in ms to hours:minutes:seconds
    private String parse(long millis) {
        String time = "n/a";
        if (millis > 0) {
            int seconds = (int) (millis / 1000) % 60;
            int minutes = (int) ((millis / (1000 * 60)) % 60);
            int hours = (int) ((millis / (1000 * 60 * 60)) % 24);
            time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        return time;
    }

    private State getState() {
        // (LOST)
        // We are not in the region.
        if (!validRegion.contains(myPosition())) {
            log("STATE: Returning to the bank tiles because we are lost.");
            return State.Lost;
        }
        // (PIZZA)
        // We are combining ingredients.
        else if (combineIngredients > -1) {
            log("STATE: Combining pizza ingredients. DEBUG");
            return State.Pizza;
        }
        // (COOK)
        // We have raw ingredients (AND) we are not combining ingredients.
        else if (getInventoryRawFoodIndex() != -1 && combineIngredients != -2) {
            log("STATE: Going to cook food, because we have valid raw food in our inventory.");
            return State.Cook;
        }
        // (BANK)
        // We don't have any raw food (OR) we finished combining ingredients.
        return State.Bank;
    }

    public void onStart() {
        m = new Cursor(this);
        timer = new Timer(System.currentTimeMillis());
        xpTracker = getExperienceTracker();
        xpTracker.start(Skill.COOKING);
    }

    //@paint
    public void onPaint(Graphics2D g) {
        int msHour = 3600000;
        int fishCooked = 0;
        int ingrCombined = 0;
        String prog = "Work Completed:\n";

        for (String s : progress.keySet()) {
            for (String combine : combinedIngredients) {
                if (s.equals(combine)) {
                    ingrCombined += progress.get(s);
                    if (progress.get(s) > 0) {
                        prog += s + " - " + progress.get(s) + "\n";
                    }
                }
            }
            for (String cooked : cookedFood) {
                if (s.equals(cooked)) {
                    fishCooked += progress.get(s);
                    if (progress.get(s) > 0) {
                        prog += s + " - " + progress.get(s) + "\n";
                    }
                }
            }
        }
        m.draw(g);
        g.setFont(new Font("Tahoma", Font.PLAIN, 14));
        g.drawString("aleksandrDesertChefReloaded", 12, 30);
        g.drawString("Currently " + status, 12, 49);

        g.drawString("Experience Tracker:", 12, 68);
        g.drawString("Current Level: " + getSkills().getDynamic(Skill.COOKING), 30, 87);
        g.drawString("Experience Gained: " + xpTracker.getGainedXP(Skill.COOKING)
                + " (" + xpTracker.getGainedXPPerHour(Skill.COOKING) + ")", 30, 106);
        g.drawString("Levels Gained: " + xpTracker.getGainedLevels(Skill.COOKING)
                + " (TTL: " + parse(xpTracker.getTimeToLevel(Skill.COOKING)) + ")", 30, 125);
        if (fishCooked > 0) {
            g.drawString("Food Cooked: "
                    + fishCooked
                    + " ("
                    + (int) ((float) fishCooked / ((float) timer.getElapsed() / (float) msHour))
                    + ")", 12, 144);
        }
        if (ingrCombined > 0) {
            g.drawString("Ingredients Combined: "
                    + ingrCombined
                    + " ("
                    + (int)((float)ingrCombined/((float)timer.getElapsed()/(float)msHour))
                    + ")", 12, 163);
        }
        g.drawString("Timer: " + timer.parse(timer.getElapsed()), 12, 182);
        if (!prog.equals("Work completed: \n")) {
            g.drawString(prog, 12, 201);
        }
    }

    public int onLoop() throws InterruptedException {
        switch (getState()) {
            case Lost:
                if (grandExchange.contains(myPosition())
                        && getNpcs().closest("Banker") != null) {
                    getNpcs().closest("Banker").interact("Bank");
                    new cSleep(() -> getBank().isOpen(), 6500).sleep();
                    sleep(550);
                    if (!inventory.isEmpty()) {
                        getBank().depositAll();
                        new cSleep(() -> getInventory().isEmpty(), 1500);
                        sleep(250);
                        getBank().close();
                        sleep(250);
                        int i = getWorlds().getCurrentWorld();
                        getWorlds().hopToF2PWorld();
                        new cSleep(() -> getWorlds().getCurrentWorld() != i, 5500);
                        sleep(750);
                    }
                }
                status = "walking back to the cooking area...";
                getWalking().webWalk(new Position(3271, 3167, 0));
                new cSleep(() -> validRegion.contains(myPosition()), 5000).sleep();
                sleep(random(1500, 2000));
                break;
            case Bank:
                if (getSettings().getRunEnergy() < 25 && getSettings().isRunning()) {
                    status = "disabling run toggle...";
                    getSettings().setRunning(false);
                }
                // If there is an obstacle en route to the bank, then handle it.
                    status = "validating that a path to the bank exists...";
                    if (getDoorHandler().handleNextObstacle(insideBank)) {
                        status = "opening a closed door...";
                        sleep(2500);
                    }

                if (banker == null) {
                    if (getBanker() != null) {
                        banker = getBanker();
                    }
                } else {
                    status = "opening the bank interface...";
                    getNpcs().closest("Banker").interact("Bank");
                    status = "adjusting the camera so that the range is easier to click...";
                    setCamera(22, 36, 0, 30);
                    status = "waiting for bank interface to exist...";
                    new cSleep(() -> getBank().isOpen(), 10000).sleep();
                    sleep(random(350, 500));
                    if (getBank().isOpen()) {
                        for (Item s : getInventory().getItems()) {
                            if (s != null) {
                                int i = 0;
                                if (progress.containsKey(s.getName())) {
                                    i = progress.get(s.getName());
                                }
                                progress.put(s.getName(), i + s.getAmount());
                            }
                        }
                        // Deposit all items
                        if (!getInventory().isEmpty()) {
                            status = "depositing all items...";
                            getBank().depositAll();
                        }
                        new cSleep(() -> getInventory().isEmpty(), 1500).sleep();
                        sleep(250);
                        // If we have ingredients for a pizza, and we have a high enough cooking level, then we will withdraw the ingredients to combine.
                        combineIngredients = getBankPizzaIngredientsIndex();
                        if (combineIngredients > -1) {
                            status = "withdrawing " + pizzaIngredients[combineIngredients - 1] + "...";
                            getBank().withdraw(pizzaIngredients[combineIngredients - 1], 14);
                            new cSleep(() -> getInventory().contains(pizzaIngredients[combineIngredients - 1]), 2500).sleep();
                            sleep(150);
                            status = "withdrawing " + pizzaIngredients[combineIngredients] + "...";
                            getBank().withdraw(pizzaIngredients[combineIngredients], 14);
                            new cSleep(() -> getInventory().contains(pizzaIngredients[combineIngredients]), 2500).sleep();
                            sleep(150);
                        }
                        // Otherwise, if we have raw food, and we are a high enough cooking level to attempt cooking them, we will withdraw those raw foods to cook.
                        else if (getBankRawFoodIndex() > -1) {
                            status = "withdrawing " + rawFoods[getBankRawFoodIndex()] + "...";
                            getBank().withdrawAll(rawFoods[getBankRawFoodIndex()]);
                            new cSleep(() -> getInventory().contains(rawFoods[getBankRawFoodIndex()]), 2500).sleep();
                            sleep(150);
                        }
                        // If we don't have pizza ingredients or raw food, then we will walk to the Grand Exchange. If our mule is online, we will trade our mule.
                        else {
                            if (getBank().isOpen()) { // Redundant check
                                status = "walking to the Grand Exchange, because we have finished working...";
                                getWalking().webWalk(grandExchange);
                                new cSleep(() -> grandExchange.contains(myPosition()), 180000).sleep();
                                // Check to see if our mule exists, if it does then trade it.
                                stop();
                            }
                        }
                        sleep(250);
                        getBank().close();
                    }
                }
                break;
            case Pizza:
                // Use the inedible ingredient on the edible ingredient
                status = "selecting the first ingredient...";
                getInventory().interact("Use", pizzaIngredients[combineIngredients - 1]);
                new cSleep(() -> getInventory().isItemSelected(), 1000).sleep();
                status = "using it on the second ingredient...";
                getInventory().getItem(pizzaIngredients[combineIngredients]).interact();
                // Wait for the combine interface to exist
                status = "waiting for the combine interface is exist...";
                new cSleep(() -> getWidgets().get(309, 2) != null, 4000).sleep();
                sleep(500);
                // Make all
                if (getWidgets().get(309, 2) != null) {
                    status = "using the combine interface...";
                    getWidgets().get(309, 2).interact("Make all");
                    status = "combining ingredients...";
                }
                // Wait until none of the original ingredients are left
                new cSleep(() -> !getInventory().contains(pizzaIngredients[combineIngredients]), 30000).sleep();
                sleep(250);
                combineIngredients = -2;
                break;
            case Cook:
                if (getSettings().getRunEnergy() >= 50 && !getSettings().isRunning()) {
                    status = "enabling run toggle...";
                    getSettings().setRunning(true);
                }
                if (getObjects().closest("Range") != null && !myPlayer().isAnimating()) {
                    // If there is an obstacle to the range, then handle it.
                    status = "validating that a path to the bank exists...";
                    if (getDoorHandler().handleNextObstacle(insideRange)) {
                        status = "opening a closed door...";
                        new cSleep(() -> new Area(3276, 3178, 3278,3181).contains(myPosition()), 2500).sleep();
                        sleep(3500);
                    }
                   //status = "walking to the range...";
                   //getWalking().webWalk(insideRange);
                   // new cSleep(() -> insideRange.distance(myPosition()) < 3, 10000);
                    // Select the food and use it on the range.
                    status = "selecting the raw food...";
                    getInventory().interact("Use", rawFoods[getInventoryRawFoodIndex()]);
                    new cSleep(() -> getInventory().isItemSelected(), 1000).sleep();
                    sleep(250);
                    status = "using the raw food on the range...";
                    getObjects().closest("Range").interact("Use");
                    //range.interact("Use");
                    status = "waiting for the cooking interface to exist...";
                    new cSleep(() -> getWidgets().get(307, 2) != null, 17500).sleep();
                    sleep(1000);
                    if (getWidgets().get(307, 2) != null) {
                        status = "using the cooking interface...";
                        getWidgets().get(307, 2).interact("Cook All");
                        status = "cooking food...";
                        sleep(2500);
                        status = "adjusting the camera so that the banker is easier to click...";
                        setCamera(22, 40, 116, 200);
                        status = "cooking food...";
                        new cSleep(() -> !myPlayer().isAnimating(), 70000).sleep();
                    }
                }
                break;
        }
        return 300;
    }
}