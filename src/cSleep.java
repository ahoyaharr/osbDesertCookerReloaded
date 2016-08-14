import org.osbot.rs07.utility.ConditionalSleep;

import java.util.function.BooleanSupplier;

public final class cSleep extends ConditionalSleep {

    private final BooleanSupplier condition;

    public cSleep(final BooleanSupplier condition, final int timeout) {
        super(timeout);
        this.condition = condition;
    }

    @Override
    public final boolean condition() throws InterruptedException {
        return condition.getAsBoolean();
    }
}

/* Usage:
Author: https://github.com/explv
new cSleep(() -> myPlayer().isAnimating(), 5000).sleep();

is equivalent to

new ConditionalSleep(5000) {
    @ Override
    public boolean condition() throws InterruptedException {
        return myPlayer().isAnimating();
    }
}.sleep();
 */