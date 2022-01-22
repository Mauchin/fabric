package net.mauchin.mining_tracker;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@me.shedaniel.autoconfig.annotation.Config(name = "MiningTracker")
public class Config implements ConfigData {
    int pricePerDiamondOre = 560;
    int optimalMoneyPerHour = 500;
    int moneyPerHourStep = 25;
    int breakerCooldownTicks = 2380;
    int breakerLengthTicks = 440;
    String mainhome = "w";
    @ConfigEntry.Gui.Tooltip
    String subhomes = "d,d1,d2";
}
