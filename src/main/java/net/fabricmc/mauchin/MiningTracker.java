package net.fabricmc.mauchin;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.util.Objects;


public class MiningTracker implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("mining_tracker");
    private int start_dia_count = 0;
    private boolean show_count = true;
    private boolean ticking = false;
    private int time_passed = 0;
    private boolean has_been_reset = true;
    private int current_dia_count = 0;
    private int time_no_display = 0;
    private int dia_per_minute_modded = 0;
    private double dia_per_minute = 0.0;
    private int time_sec = 0;
    private int time_min = 0;
    private int time_hour = 0;
    private String marker_color = "\247c";
    private int timer = 0;
    private String speed_color = "\247c";
    private int money_per_hour = 0;
    private int time_no_click_detect = 0;
    private int efficiency_level = 0;
    private boolean breaker_enabled = false;
    private final int breaker_length = 440;
    private final int breaker_cd = 2400;
    private int breaker_time = 0;
    private String breaker_color = "\247b";


    @Override
    public void onInitializeClient() {
        LOGGER.info("Mining Tracker Loaded!");
        KeyBinding key_reset = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mining_tracker.reset", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "category.mining_tracker.tracker"));
        KeyBinding key_toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mining_tracker.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "category.mining_tracker.tracker"));
        KeyBinding key_start = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.mining_tracker.start", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "category.mining_tracker.tracker"));
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            while (key_toggle.wasPressed()) {
                show_count = !show_count;
                if (!show_count) {
                    if (client.player != null) {
                        client.player.sendMessage(new LiteralText("Mining Tracker: \247cHidden"), true);
                    }
                }
            }
            while (key_reset.wasPressed()) {
                has_been_reset = true;
                start_dia_count = 0;
                for (int i = 0; i < 36; i++) {
                    if (client.player != null && (client.player.getInventory().getStack(i).getItem() == Items.DIAMOND_ORE || client.player.getInventory().getStack(i).getItem() == Items.DEEPSLATE_DIAMOND_ORE)) {
                        start_dia_count += client.player.getInventory().getStack(i).getCount();
                    }
                }
                time_passed = 0;
                time_no_display = 20;
                if (client.player != null) {
                    client.player.sendMessage(new LiteralText("Mining Tracker: \247eReset"), true);
                }
                dia_per_minute = 0;
                dia_per_minute_modded = 0;
                ticking = false;

            }
            while (key_start.wasPressed()) {

                if (!ticking && has_been_reset) {
                    if (client.player != null) {
                        client.player.sendMessage(new LiteralText("Mining Tracker: \247aStarted"), true);
                    }
                    start_dia_count = 0;
                    for (int i = 0; i < 36; i++) {
                        if (client.player != null && (client.player.getInventory().getStack(i).getItem() == Items.DIAMOND_ORE || client.player.getInventory().getStack(i).getItem() == Items.DEEPSLATE_DIAMOND_ORE)) {
                            start_dia_count += client.player.getInventory().getStack(i).getCount();
                        }
                    }
                    time_passed = 0;
                    time_no_display = 20;
                    ticking = true;
                    has_been_reset = false;
                } else if (!ticking) {
                    if (client.player != null) {
                        client.player.sendMessage(new LiteralText("Mining Tracker: \247aResumed"), true);
                    }
                    ticking = true;
                    time_no_display = 20;
                } else {
                    if (client.player != null) {
                        client.player.sendMessage(new LiteralText("Mining Tracker: \247cStopped"), true);
                    }
                    ticking = false;
                    time_no_display = 20;
                }
            }

            if (ticking) {
                time_passed += 1;
            }
            if (time_no_display > 0) {
                time_no_display -= 1;
            }
            if (time_no_click_detect > 0) {
                time_no_click_detect -= 1;
            }
            current_dia_count = 0;
            if (ticking) {
                for (int i = 0; i < 36; i++) {
                    if (client.player != null && (client.player.getInventory().getStack(i).getItem() == Items.DIAMOND_ORE || client.player.getInventory().getStack(i).getItem() == Items.DEEPSLATE_DIAMOND_ORE)) {
                        current_dia_count += client.player.getInventory().getStack(i).getCount();
                    }
                }
            }

            if (client.player != null && client.mouse.wasRightButtonClicked() && time_no_click_detect <= 0) {
                time_no_display = 20;
                time_no_click_detect =40;

            }
            efficiency_level = 0;
            for (int j = 0; j < 10; j++) {
                if (client.player != null && efficiency_level == 0 && Objects.equals(client.player.getMainHandStack().getEnchantments().getCompound(j).getString("id"), "minecraft:efficiency")
                    && (client.player.getMainHandStack().getItem() == Items.NETHERITE_PICKAXE||
                        client.player.getMainHandStack().getItem() == Items.DIAMOND_PICKAXE||
                        client.player.getMainHandStack().getItem() == Items.IRON_PICKAXE||
                        client.player.getMainHandStack().getItem() == Items.STONE_PICKAXE||
                        client.player.getMainHandStack().getItem() == Items.GOLDEN_PICKAXE||
                        client.player.getMainHandStack().getItem() == Items.WOODEN_PICKAXE)) {
                    efficiency_level = client.player.getMainHandStack().getEnchantments().getCompound(j).getInt("lvl");
                }
            }
            if (efficiency_level >= 10){
                if (!breaker_enabled){
                    breaker_time = breaker_length;
                }
                breaker_enabled = true;
                breaker_color = "\247b\247l";

            }
            else if (breaker_enabled && breaker_time <= 0){
                breaker_enabled = false;
                breaker_time = breaker_cd;
            }
            if (!breaker_enabled){
                breaker_color = "\247f";
            }
            if (breaker_time > 0){
                breaker_time -= 1;
            }

            dia_per_minute_modded = 0;
            DecimalFormat df_time = new DecimalFormat("00");
            DecimalFormat df_speed = new DecimalFormat("0.00");
            if (ticking) {
                dia_per_minute_modded = (current_dia_count - start_dia_count) * 120000 / time_passed;
                dia_per_minute = dia_per_minute_modded / 100.0;
                money_per_hour = dia_per_minute_modded * 252 / 1000;
                if (money_per_hour>=400){speed_color ="\2479";}
                else if (money_per_hour >= 375){speed_color = "\247b";}
                else if (money_per_hour >= 350){speed_color = "\2472";}
                else if (money_per_hour >= 325){speed_color = "\247a";}
                else if (money_per_hour >= 300){speed_color = "\247e";}
                else if (money_per_hour >= 275){speed_color = "\2476";}
                else {speed_color = "\247c";}
            }
            time_hour = time_passed / 72000;
            time_min = (time_passed - time_hour * 72000) / 1200;
            time_sec = (time_passed - time_hour * 72000 - time_min * 1200) / 20;
            if (ticking) {
                marker_color = "e";
            } else {
                marker_color = "c";
            }
            timer += 1;
            if (current_dia_count < start_dia_count) {
                current_dia_count = start_dia_count;
            }
            if (client.player != null && show_count && time_no_display <= 0 && timer % 10 == 0) {
                if (breaker_time == 0 && !breaker_enabled){
                    client.player.sendMessage(new LiteralText("\247f" + df_time.format(time_hour) + ":" + df_time.format(time_min) + ":" + df_time.format(time_sec) + " ⌚ \247" + marker_color + "│ "+speed_color + df_speed.format(dia_per_minute) + "\247f /min ("+speed_color+money_per_hour+"\247fk/hour) \247" + marker_color + "│\247e ⚡ READY \247"+marker_color+"│ \247b⛏ " + (current_dia_count - start_dia_count)), true);
                }
                else{
                client.player.sendMessage(new LiteralText("\247f" + df_time.format(time_hour) + ":" + df_time.format(time_min) + ":" + df_time.format(time_sec) + " ⌚ \247" + marker_color + "│ "+speed_color + df_speed.format(dia_per_minute) + "\247f /min ("+speed_color+money_per_hour+"\247fk/hour) \247" + marker_color + "│ "+breaker_color+"⚡ "+(breaker_time/20 + 1)+"s \247"+marker_color+"│ \247b⛏ " + (current_dia_count - start_dia_count)), true);
                }
            }


        });


    }


}


