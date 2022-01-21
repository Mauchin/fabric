package net.mauchin.mining_tracker;

import me.shedaniel.autoconfig.ConfigData;

@me.shedaniel.autoconfig.annotation.Config(name = "mining_notifier")
public class Config implements ConfigData {
    int pricePerDiamondOre = 560;
    int optimalMoneyPerHour = 500;
    int moneyPerHourStep = 25;
    int breakerCooldownTicks = 2380;
    int breakerLengthTicks = 440;
    String subhomes = "d,d1,d2";
}
