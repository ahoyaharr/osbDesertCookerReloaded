public enum Jobs {
    COMBAT_NEW( // Gears up a brand new account
            "basic combat supplies",
            new String[]{"Iron scimitar", "Steel scimitar", "Mithril scimitar", "Adamant scimitar", "Amulet of power",
                    "Mithril full helm", "Mithril platebody", "Mithril platelegs", "Mithril kiteshield",
                    "Air rune", "Water rune", "Earth rune", "Mind rune", "Staff of Fire",
                    "Trout"},
            new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 6000, 200, 200, 2500, 1, 308},
            true
    ),
    COMBAT_RUNESCIM( // Gives a rune scimitar
            "rune scimitar supplies",
            new String[]{"Rune scimitar"},
            new int[]{1},
            true
    ),
    COMBAT_ARMOUR(
            "rune armour supplies",
            new String[]{"Adamant platebody, Rune full helm", "Rune kiteshield", "Rune platelegs"},
            new int[]{1, 1, 1, 1},
            true
    ),
    COMBAT_FREERANGE(
            "f2p range supplies",
            new String[]{"Shortbow", "Oak shortbow", "Willow shortbow", "Maple shortbow", "Coif", "Bronze arrow"},
            new int[]{1, 1, 1, 1, 1, 12000},
            true
    ),
    COMBAT_FREEARROW(
            "f2p arrow reload",
            new String[]{"Bronze arrow"},
            new int[]{5000},
            true
    ),
    COOK_START(
            "range chef supplies",
            new String[]{"Raw sardine", "Raw trout"},
            new int[]{140, 2016},
            true
    ),
    COOK_MID(
            "station chef supplies",
            new String[]{"Raw salmon"},
            new int[]{1500},
            true
    ),
    COOK_ANCHOVYPIZZA(
            "anchovy pizza supplies",
            new String[]{"Plain pizza", "Anchovies"},
            new int[]{3800, 3800},
            false
    ),
    COOK_PLAINPIZZA(
            "plain pizza supplies",
            new String[]{"Pizza base", "Tomato", "Cheese"},
            new int[]{1064, 1064, 1064},
            false
    ),
    TEST(
            "testing supplies",
            new String[]{"Raw sardine", "Fire rune", "Water rune", "Air rune", "Earth rune", "Mind rune"},
            new int[]{1, 1, 1, 1, 1, 1},
            true
    );

    private String title;
    private String[] items;
    private int[] quantities;
    private boolean whitelisted;

    Jobs(String title, String[] items, int[] quantities, boolean whitelisted) {
        this.title = title;
        this.items = items;
        this.quantities = quantities;
        this.whitelisted = whitelisted;
    }

    public String getTitle() {
        return title;
    }

    public String[] getItems() {
        return items;
    }

    public int[] getQuantities() {
        return quantities;
    }

    public boolean isWhitelisted() {
        return whitelisted;
    }
}