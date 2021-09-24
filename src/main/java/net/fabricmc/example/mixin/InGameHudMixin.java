package net.fabricmc.example.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	private final MinecraftClient client = MinecraftClient.getInstance();

	@Inject(method = "render", at = @At("RETURN"), cancellable = true)

	public void onRender (MatrixStack matrices, float tickDelta, CallbackInfo info) {

		int x = 5;
		assert this.client.player != null;
		int test = client.player.getStatHandler().getStat(Stats.MINED.getOrCreateStat(Blocks.DIAMOND_ORE));
		int y = client.getWindow().getScaledHeight() / 2;
		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, "Test" + test , x, y, -1);

	}
}
