package com.bulkheadengineering;

import com.bulkheadengineering.legacy.block.entity.doors.DoorDecl;
import com.bulkheadengineering.legacy.block.entity.doors.DoorBlockEntity;
import com.bulkheadengineering.legacy.interfaces.IDoorAnimator;
import com.bulkheadengineering.legacy.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Vertical modular blast door from HBM 1.12; geometry and timing credited in UPSTREAM-112.txt. */
public final class ModularBlastDoorDecl extends DoorDecl {
    public ModularBlastDoorDecl() {
        defineStructure(DoorStructureDefinition.create().parseVertical(
                new String[]{"#", "#", "#", "#", "#", "#", "C"},
                new String[]{"#", " ", " ", " ", " ", " ", "#"}, 'C'));
    }
    @Override public ResourceLocation getBlockId() { return new ResourceLocation(Doors.ID, "modular_blast_door"); }
    @Override public int getOpenTime() { return 100; }
    @Override public String[] getPartNames() { return new String[]{"base", "block", "tooth", "slider1", "slider2", "slider3", "slider4"}; }
    @Override public void doOffsetTransform(IDoorAnimator a) { a.rotate(180, 0, 1, 0); }
    @Override public SoundEvent getOpenSoundStart() { return ModSounds.MODULAR_START.get(); }
    @Override public SoundEvent getOpenSoundEnd() { return ModSounds.MODULAR_STOP.get(); }
    @Override public float getSoundVolume() { return .5f; }

    /** Extension is five blocks closed, zero open. No wall-clock animation jumps on reload. */
    public static float extension(float ticks) { return 5f * (1f - Math.max(0, Math.min(100, ticks)) / 100f); }
    public static boolean visible(String part, float ticks) {
        return !part.startsWith("slider") || extension(ticks) > Integer.parseInt(part.substring(6));
    }
    @Override public void getTranslation(String part, float ticks, boolean child, float[] out) {
        float y = switch (part) {
            case "block" -> 3;
            case "tooth", "slider1" -> 5 - extension(ticks);
            case "slider2" -> 6 - extension(ticks);
            case "slider3" -> 7 - extension(ticks);
            case "slider4" -> 8 - extension(ticks);
            default -> 0;
        };
        set(out, 0, y, 0);
    }

    /** Keep virtual parts in place while their collision clears in the original 20-tick stages. */
    public static VoxelShape shape(DoorBlockEntity door, BlockPos local) {
        int y = local.getY();
        if (y == 0 || y == 6) return Shapes.block();
        if (y < 1 || y > 5) return Shapes.empty();
        boolean clear = switch (door.state) {
            case 1 -> true;
            case 3 -> door.getOpenTicks() >= Math.max(1, (y - 1) * 20);
            case 2 -> door.getOpenTicks() > (y - 1) * 20;
            default -> false;
        };
        return clear ? Shapes.empty() : Shapes.block();
    }
}
