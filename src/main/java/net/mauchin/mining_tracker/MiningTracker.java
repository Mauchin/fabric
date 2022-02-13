package net.mauchin.mining_tracker;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.mixin.client.rendering.MixinInGameHud;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Matrix4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;



import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
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
    private int breaker_time = 0;
    private String breaker_color = "\247b";
    private int pickaxe_durability = 100;
    private String breaker_string = "\247e⚡ READY";
    private String pickaxe_string = "\247b⛏";
    private int time_pickaxe_display = 0;
    private String home = "";
    @SuppressWarnings("FieldMayBeFinal")
    private List<String> homes_used = new ArrayList<>();
    private String displayHome = "";
    private boolean home_set = false;
    private BlockPos lastRunSethomeLocation = new BlockPos(0,0,0);
    private String lastSethomeName = "";
    private int sethomeMainCooldown = 0;
    @Override
    public void onInitializeClient() {
        LOGGER.info("Mining Tracker Loaded!");
        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("homemanager").then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(context -> {
                home = StringArgumentType.getString(context,"name");
                return 1;
            })));
        AutoConfig.register(Config.class, GsonConfigSerializer::new);
        KeyBinding key_reset = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.reset", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "mining_tracker.category.tracker"));
        KeyBinding key_toggle = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "mining_tracker.category.tracker"));
        KeyBinding key_start = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.start", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, "mining_tracker.category.tracker"));
        KeyBinding key_mainhome = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.mainhome",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_B,"mining_tracker.category.tracker"));
        KeyBinding key_home = KeyBindingHelper.registerKeyBinding(new KeyBinding("mining_tracker.key.home",InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_H,"mining_tracker.category.tracker"));
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            Config config = AutoConfig.getConfigHolder(Config.class).getConfig();
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
            while (key_mainhome.wasPressed() && client.player != null){
                if (!homes_used.isEmpty()){

                    client.player.sendChatMessage("/home "+homes_used.get(0).strip());
                    client.player.sendMessage(new LiteralText("\2476Teleporting to subhome \247c"+homes_used.get(0).strip()+"\2476..."),false);
                    sethomeMainCooldown = 1;
                    homes_used.remove(0);


                }
                else{
                    client.player.sendMessage(new LiteralText("\2476You don't have any subhomes set!"),false);
                }
            }
            if (sethomeMainCooldown >= config.setMainHomeCooldown && client.player != null){
                client.player.sendChatMessage("/homemanager " + config.mainhome.strip());
                client.player.sendChatMessage("/sethome " + config.mainhome.strip());
                client.player.sendMessage(new LiteralText("\2476Setting mainhome \247c" + config.mainhome.strip()+"\2476..."), false);
                sethomeMainCooldown = 0;
            }
            if (sethomeMainCooldown != 0){
                sethomeMainCooldown += 1;
            }

            while (key_home.wasPressed() && client.player != null){
                if (breaker_enabled || breaker_time <= 0){
                    if (!homes_used.isEmpty()){
                        client.player.sendChatMessage("/home "+homes_used.get(0).strip());
                        client.player.sendMessage(new LiteralText("\2476Teleporting to subhome \247c"+homes_used.get(0).strip()+"\2476..."),false);
                        homes_used.remove(0);
                    }
                    else{
                        client.player.sendChatMessage("/home "+config.mainhome.strip());
                        client.player.sendMessage(new LiteralText("\2476You don't have any more subhomes which has diamonds."),false);
                        client.player.sendMessage(new LiteralText("\2476Teleporting to mainhome \247c"+config.mainhome+"\2476..."),false);
                    }
                }
                else{
                    home_set = false;
                    if (client.player.getBlockPos().isWithinDistance(lastRunSethomeLocation,3.0)){
                        client.player.sendChatMessage("/homemanager "+lastSethomeName.strip());
                        client.player.sendChatMessage("/sethome "+lastSethomeName.strip());
                        client.player.sendMessage(new LiteralText("\2476Rerun of sethome detected. Overriding latest sethome..."),false);
                        client.player.sendMessage(new LiteralText("\2476Setting subhome \247c"+lastSethomeName.strip()+"\2476..."),false);
                        home_set = true;
                        lastRunSethomeLocation = client.player.getBlockPos();
                    }
                    else {
                        for (String subhome : config.subhomes.split(",")) {
                            if (!homes_used.contains(subhome.strip())) {
                                client.player.sendChatMessage("/homemanager " + subhome.strip());
                                client.player.sendChatMessage("/sethome " + subhome.strip());
                                client.player.sendMessage(new LiteralText("\2476Setting subhome \247c" + subhome.strip()+"\2476..."), false);
                                home_set = true;
                                lastRunSethomeLocation = client.player.getBlockPos();
                                lastSethomeName = subhome.strip();
                                break;
                            }
                        }
                    }
                    if (!home_set){
                        client.player.sendMessage(new LiteralText("\2476You don't have any more subhomes reserved."),false);
                        client.player.sendMessage(new LiteralText("\2476Setting mainhome \247c"+config.mainhome+"\2476... \247aPrepare your super breaker!"),false);
                    }
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
            for (int i = 0; i < 9; i++){
                for (int j = 0; j < 10; j++) {
                    if (client.player != null && efficiency_level == 0 && Objects.equals(client.player.getInventory().getStack(i).getEnchantments().getCompound(j).getString("id"), "minecraft:efficiency")
                        && (client.player.getInventory().getStack(i).getItem() == Items.NETHERITE_PICKAXE||
                            client.player.getInventory().getStack(i).getItem() == Items.DIAMOND_PICKAXE||
                            client.player.getInventory().getStack(i).getItem() == Items.IRON_PICKAXE||
                            client.player.getInventory().getStack(i).getItem() == Items.STONE_PICKAXE||
                            client.player.getInventory().getStack(i).getItem() == Items.GOLDEN_PICKAXE||
                            client.player.getInventory().getStack(i).getItem() == Items.WOODEN_PICKAXE)) {
                        efficiency_level = client.player.getInventory().getStack(i).getEnchantments().getCompound(j).getInt("lvl");
                    }
                }
            }
            if (efficiency_level >= 10) {
                if (!breaker_enabled) {
                    breaker_time = config.breakerLengthTicks;
                }
                breaker_enabled = true;
                breaker_color = "\247b\247l";

            }
            else if (breaker_enabled && breaker_time <= 0 && efficiency_level != 0){
                breaker_enabled = false;
                breaker_time = config.breakerCooldownTicks;
            }
            if (!breaker_enabled && breaker_time > 0){
                breaker_color = "\247f";
            }
            else if (!breaker_enabled){
                breaker_color = "\247e";
            }
            if (breaker_time > 0){
                breaker_time -= 1;
            }
            if (client.player != null && (client.player.getMainHandStack().getItem() == Items.NETHERITE_PICKAXE ||
                    client.player.getMainHandStack().getItem() == Items.DIAMOND_PICKAXE ||
                    client.player.getMainHandStack().getItem() == Items.IRON_PICKAXE ||
                    client.player.getMainHandStack().getItem() == Items.STONE_PICKAXE ||
                    client.player.getMainHandStack().getItem() == Items.GOLDEN_PICKAXE ||
                    client.player.getMainHandStack().getItem() == Items.WOODEN_PICKAXE)) {
                pickaxe_durability = (client.player.getMainHandStack().getMaxDamage() - client.player.getMainHandStack().getDamage()) * 100 / client.player.getMainHandStack().getMaxDamage();
                if (pickaxe_durability<=5) {
                    time_pickaxe_display = 40;
                }
            }
            if (time_pickaxe_display > 0) {
                time_pickaxe_display -= 1;
            }

            dia_per_minute_modded = 0;
            DecimalFormat df_time = new DecimalFormat("00");
            DecimalFormat df_speed = new DecimalFormat("0.00");
            if (ticking) {
                dia_per_minute_modded = (current_dia_count - start_dia_count) * 120000 / time_passed;
                dia_per_minute = dia_per_minute_modded / 100.0;
                money_per_hour = dia_per_minute_modded * config.pricePerDiamondOre * 6 / 10000;
                if (money_per_hour>=config.optimalMoneyPerHour){speed_color ="\2479";}
                else if (money_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep){speed_color = "\247b";}
                else if (money_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep*2){speed_color = "\2472";}
                else if (money_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep*3){speed_color = "\247a";}
                else if (money_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep*4){speed_color = "\247e";}
                else if (money_per_hour >= config.optimalMoneyPerHour - config.moneyPerHourStep*5){speed_color = "\2476";}
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
            if (!breaker_enabled && breaker_time <= 0){
                breaker_string = breaker_color +"⚡ READY";
            }
            else{
                breaker_string = breaker_color + "⚡ "+(breaker_time/20 + 1)+"s";
            }
            //Pickaxe Durability Display
            if (pickaxe_durability <= 10){
                pickaxe_string = "\247c\247l[⛏]";
            }
            else if (pickaxe_durability <= 20) {
                pickaxe_string = "\247c⛏";
            }
            else if (pickaxe_durability <= 35){
                pickaxe_string = "\247e⛏";
            }
            else if (pickaxe_durability <= 50){
                pickaxe_string = "\247a⛏";
            }
            else{
                pickaxe_string = "\247b⛏";
            }
            //home manager
            displayHome = "";

            for (String subhome : config.subhomes.split(",")){
                if (Objects.equals(subhome.strip(), home) && !homes_used.contains(home)){
                    homes_used.add(home);
                }
                if (homes_used.contains(subhome) && !breaker_enabled){
                    displayHome += " \2477" + subhome.strip();
                }
                else if (homes_used.contains(subhome) && breaker_enabled){
                    displayHome += " \247b" + subhome.strip();
                }
                else if (!homes_used.contains(subhome) && breaker_enabled){
                    displayHome += " \2477" + subhome.strip();
                }
                else{displayHome += " \247a" + subhome.strip();}
            }
            home = "";

            //show message
            if (client.player != null && show_count && time_no_display <= 0 && timer % 10 == 0 && time_pickaxe_display <= 0) {
                client.player.sendMessage(new LiteralText("\247f" + df_time.format(time_hour) + ":" + df_time.format(time_min)
                        + ":" + df_time.format(time_sec) + " ⌚ \247" + marker_color + "│ "+speed_color + df_speed.format(dia_per_minute)
                        + "\247f /min ("+speed_color+money_per_hour+"\247fk/hour) \247" + marker_color + "│ "+breaker_string+"\247"+marker_color+"│ "
                        +pickaxe_string+"\247b " + (current_dia_count - start_dia_count)+"\247" + marker_color + "│"+displayHome) , true);
            }
            else if (client.player != null &&show_count && time_no_display <= 0 && timer % 10 == 0 ) {
                    client.player.sendMessage(new LiteralText("\247c\247l REPAIR YOUR PICKAXE!"),true);
            }

        });


    }


}


