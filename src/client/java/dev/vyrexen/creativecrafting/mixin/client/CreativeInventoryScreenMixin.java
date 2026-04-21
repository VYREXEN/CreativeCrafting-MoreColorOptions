package dev.vyrexen.creativecrafting.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import dev.vyrexen.creativecrafting.SlotColorConfig;

@Mixin(CreativeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends HandledScreen<ScreenHandler> {
    private static final int[] SLOT_X = { 172, 133, 151, 133, 151 };
    private static final int[] SLOT_Y = {  20,  10,  10,  28,  28 };

    @Shadow private static ItemGroup selectedTab;

    public CreativeInventoryScreenMixin(ScreenHandler handler) {
        super(handler, null, null);
    }

    @Inject(method = "setSelectedTab", at = @At("TAIL"))
    private void onSetSelectedTab(ItemGroup tab, CallbackInfo ci) {
        if (!tab.getType().equals(ItemGroup.Type.INVENTORY)) return;

        for (int i = 0; i < 5; i++) {
            Slot slot = handler.slots.get(i);
            slot.x = SLOT_X[i];
            slot.y = SLOT_Y[i];
        }
    }

    @Inject(method = "drawBackground", at = @At("TAIL"))
    private void onDrawBackground(DrawContext context, float delta, int mouseX, int mouseY, CallbackInfo ci) {
        if (!selectedTab.getType().equals(ItemGroup.Type.INVENTORY)) return;

        int[] colors = SlotColorConfig.getInstance().getColors();
        int shadow  = colors[0];
        int hilight = colors[1];
        int fill    = colors[2];

        for (int i = 0; i < 5; i++) {
            Slot slot = handler.slots.get(i);
            int sx = x + slot.x - 1;
            int sy = y + slot.y - 1;

            context.fill(sx,      sy,      sx + 18, sy + 1,  shadow);
            context.fill(sx,      sy + 1,  sx + 1,  sy + 17, shadow);
            context.fill(sx + 17, sy + 1,  sx + 18, sy + 17, hilight);
            context.fill(sx,      sy + 17, sx + 18, sy + 18, hilight);
            context.fill(sx + 1,  sy + 1,  sx + 17, sy + 17, fill);
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(KeyInput input, CallbackInfoReturnable<Boolean> cir) {
        if (!selectedTab.getType().equals(ItemGroup.Type.INVENTORY)) return;
        if (input.getKeycode() != InputUtil.GLFW_KEY_DELETE) return;

        int slotIdx = handler.slots.indexOf(focusedSlot);
        if (slotIdx < 1 || slotIdx > 4 || focusedSlot.getStack().isEmpty()) return;

        focusedSlot.setStack(ItemStack.EMPTY);
        client.player.networkHandler.sendPacket(new CreativeInventoryActionC2SPacket(slotIdx, ItemStack.EMPTY));

        cir.setReturnValue(true);
    }
}
