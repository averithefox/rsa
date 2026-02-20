package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autoterms;

import com.ricedotwho.rsa.component.impl.TickFreeze;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autoterms.terminals.TerminalRenderer;
import com.ricedotwho.rsm.data.Colour;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.client.InputPollEvent;
import com.ricedotwho.rsm.event.impl.render.Render2DEvent;
import com.ricedotwho.rsm.module.SubModule;
import com.ricedotwho.rsm.module.api.SubModuleInfo;
import com.ricedotwho.rsm.ui.clickgui.settings.impl.*;
import com.ricedotwho.rsm.utils.ChatUtils;
import com.ricedotwho.rsm.utils.Utils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Input;
import org.joml.Vector2d;

import java.util.Arrays;
import java.util.List;

@Getter
@SubModuleInfo(name = "InvWalk", alwaysDisabled = false)
public class InvWalk extends SubModule<AutoTerms> {

    private final ModeSetting style = new ModeSetting("Style", "Items", Arrays.asList("Solver", "Items"));
    private final BooleanSetting renderTitles = new BooleanSetting("Render title thing", true);
    private final BooleanSetting renderClicksLeft = new BooleanSetting("Render clicks left", true);
    private final ColourSetting titleColour = new ColourSetting("Title Colour", new Colour(96,31,158));
    private final ColourSetting remainingColour = new ColourSetting("Remaining Colour", new Colour(96,31,158));
    private final ColourSetting clicksColour = new ColourSetting("Clicks Colour", new Colour(0, 191, 0));
    @Getter private static final ColourSetting solutionColour = new ColourSetting("Solution Colour", new Colour(0, 150, 0));
    @Getter private static final ColourSetting oppositeColour = new ColourSetting("Opposite Colour", new Colour(0, 0, 150));
    @Getter private static final ColourSetting orderColour1 = new ColourSetting("Order Colour 1", new Colour(0, 150, 0));
    @Getter private static final ColourSetting orderColour2 = new ColourSetting("Order Colour 2", new Colour(150, 150, 0));
    @Getter private static final ColourSetting orderColour3 = new ColourSetting("Order Colour 3", new Colour(150, 0, 0));
    private final NumberSetting gap = new NumberSetting("Gap", 0, 3, 1.5, 0.01);
    private final BooleanSetting textShadow = new BooleanSetting("Text Shadow", false);

    private final ModeSetting moveDelayMode = new ModeSetting("Mode Delay", "Stop Inputs", List.of("Stop Inputs", "Freeze"));
    private final NumberSetting melodyMoveDelay = new NumberSetting("Melody Move Delay", 0, 500, 300, 50);

    private final DragSetting termTitle = new DragSetting("Term Title", new Vector2d(10, 10), new Vector2d(150, 15));
    private final DragSetting clicksText = new DragSetting("Clicks Text", new Vector2d(10, 10), new Vector2d(150, 15));
    private final DragSetting gui = new DragSetting("Visualiser Gui", new Vector2d(551, 330), new Vector2d(144, 80));

    private final TerminalRenderer terminalRenderer;
    public int melodyMoveCounter = 0;

    public InvWalk(AutoTerms module) {
        super(module);
        this.registerProperty(
                style,
                renderTitles,
                renderClicksLeft,
                titleColour,
                remainingColour,
                clicksColour,
                solutionColour,
                oppositeColour,
                orderColour1,
                orderColour2,
                orderColour3,
                gap,
                textShadow,
                moveDelayMode,
                melodyMoveDelay,
                termTitle,
                clicksText,
                gui
        );
        this.terminalRenderer = new TerminalRenderer();
    }

    @Override
    public void reset() {
        melodyMoveCounter = 0;
    }

    @SubscribeEvent
    public void onRenderGui(Render2DEvent event) {
        try {
            if (!module.isInTerm()) return;

            float width = 9 * 16f;
            int slots = Utils.getGuiSlotCount(module.getTerminalContainer().getType());
            float height = (float) (Math.floor(slots / 9f) * 16);

            if (this.style.is("Items")) {
                gui.renderScaledGFX(event.getGfx(), () -> this.terminalRenderer.renderItems(event.getGfx(), module.getTerminal()), width, height);
            } else {
                gui.renderScaled(event.getGfx(), () -> this.terminalRenderer.renderSolver(this.gap.getValue().floatValue(), module.getTerminal()), width, height);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // This works for strafe but not for forwards and backwards for some reason
    @SubscribeEvent
    public void onPollInput(InputPollEvent event) {
        if (this.melodyMoveCounter < 1) return;

        if (Minecraft.getInstance().screen == null && !module.isInTerm()) {
            this.melodyMoveCounter = 0;
            return;
        }

        Input oldInputs = event.getClientInput();
        Input newInputs = new Input(false, false, false, false, false, oldInputs.shift(), false);
        event.getInputConsumer().accept(newInputs);

        this.melodyMoveCounter--;
    }

    public void onMelodyClick() {
        if (this.moveDelayMode.is("Freeze")) {
            TickFreeze.freeze(this.melodyMoveDelay.getValue().longValue(), true);
        } else {
            this.melodyMoveCounter = (this.melodyMoveDelay.getValue().intValue() / 50);
        }
    }
}
