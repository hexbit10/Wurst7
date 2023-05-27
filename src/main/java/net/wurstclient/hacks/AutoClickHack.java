package net.wurstclient.hacks;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.PostMotionListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.mixinterface.IKeyBinding;
import net.wurstclient.settings.AttackSpeedSliderSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.PauseAttackOnContainersSetting;
import net.wurstclient.util.BlockBreaker;

@SearchTags({"auto click"})
public final class AutoClickHack extends Hack implements UpdateListener, PostMotionListener {
    private final EnumSetting<MouseButton> mouseButton = new EnumSetting<>("Mouse Button",
            """
                    The actions will be similar to this Mouse Button
                    §lLeft§r - Attack and punch blocks. Auto = Depends on Item
                    §lRight§r - Activate/Place blocks and use items. Auto = HOLD DOWN
                    """,
            MouseButton.values(), MouseButton.LEFT);
    private final AttackSpeedSliderSetting speed =
            new AttackSpeedSliderSetting();

    private final PauseAttackOnContainersSetting pauseOnContainers =
            new PauseAttackOnContainersSetting(true);

    private final CheckboxSetting punchBlocks =
            new CheckboxSetting("Punch Blocks",
                    "Destroy Blocks.\n\n Destroys single hit blocks on cross hair.",
                    true);

    Entity entity = null;
    // left vs right-click
    // hold vs timed

    public AutoClickHack() {
        super("AutoClick");
        setCategory(Category.COMBAT);

        addSetting(mouseButton);
        addSetting(speed);
        addSetting(punchBlocks);
        addSetting(pauseOnContainers);
    }

    @Override
    protected void onEnable() {
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PostMotionListener.class, this);

        speed.resetTimer();
    }

    @Override
    protected void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.add(PostMotionListener.class, this);
        ((IKeyBinding) MC.options.attackKey).resetPressedState();
        ((IKeyBinding) MC.options.useKey).resetPressedState();
    }

    @Override
    public void onUpdate() {
        if (mouseButton.getSelected() == MouseButton.RIGHT) {
            handleRightMouseButton();
            return;
        }
        // LEFT: ↓

        entity = null;

        if(pauseOnContainers.shouldPause()){
            return;
        }

        speed.updateTimer(); // Nur außerhalb von screens speed-timer hochzählen

        if (MC.crosshairTarget == null || MC.crosshairTarget.getPos() == null) {
            return;
        }

        if (mouseButton.getSelected() == MouseButton.LEFT) {

            // Invoke Attack
            if (MC.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                entity = ((EntityHitResult) MC.crosshairTarget).getEntity();
                return;
            }

            // Hit Block
            if (punchBlocks.isChecked() && MC.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                if (speed.getValue() != speed.getDefaultValue()) { // Speed ist auf konkretem Wert -> nur hitten wenn ready
                    if (speed.isTimeToAttack()) { // Zeit erreicht -> Timer neustarten
                        speed.resetTimer();
                    } else { // Zeit noch nicht erreicht -> abbrechen
                        return;
                    }
                }
                BlockBreaker.BlockBreakingParams b = BlockBreaker.getBlockBreakingParams(((BlockHitResult) MC.crosshairTarget).getBlockPos());
                if (b != null){
                    MC.interactionManager.attackBlock(((BlockHitResult) MC.crosshairTarget).getBlockPos(), b.side());
                    MC.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
                else{
                    return;
                }
            }
        }
    }

    @Override
    public void onPostMotion() { // LEFT: Attacking
        if (mouseButton.getSelected() == MouseButton.RIGHT) {
            return;
        }

        if (!speed.isTimeToAttack() || entity == null) {
            return;
        }

        MC.interactionManager.attackEntity(MC.player, ((EntityHitResult) MC.crosshairTarget).getEntity());
        MC.player.swingHand(Hand.MAIN_HAND);

        entity = null;
        speed.resetTimer();
    }

    private void handleRightMouseButton() {
        if(pauseOnContainers.shouldPause()){
            ((IKeyBinding) MC.options.useKey).resetPressedState();
            return;
        }

        if (speed.getValue() == speed.getDefaultValue()) { // Speed auf Auto -> gedrückt halten
            MC.options.useKey.setPressed(true);
            return;
        }
        speed.updateTimer();

        // Speed ist auf konkretem Wert -> nur handeln wenn ready
        MC.options.useKey.setPressed(false);

        if (speed.isTimeToAttack()) { // Zeit erreicht -> Timer neustarten
            speed.resetTimer();
        } else { // Zeit noch nicht erreicht -> abbrechen

            return;
        }

        MC.options.useKey.setPressed(true);
    }

    private enum MouseButton {
        LEFT("Left"),
        RIGHT("Right");

        private final String name;

        MouseButton(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
