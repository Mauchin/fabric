package net.fabricmc.example.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.stat.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;


@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

	private final MinecraftClient client = MinecraftClient.getInstance();

	@Inject(method = "render", at = @At("TAIL"), cancellable = true)

	public void counterRenderer(MatrixStack matrices, float tickDelta, CallbackInfo info) {

		int x = 5;
		Objects.requireNonNull(client.getNetworkHandler()).sendPacket(new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
		assert client.player != null;
		int test = client.player.getStatHandler().getStat(Stats.MINED.getOrCreateStat(Blocks.DIRT));
		int y = client.getWindow().getScaledHeight() / 2;
		MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, "Test:" + test , x, y, -1);

	}
}
