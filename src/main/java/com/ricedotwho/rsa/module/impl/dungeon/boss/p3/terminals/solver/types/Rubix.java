package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.solver.types;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.solver.Terminals;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;

public class Rubix extends com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Rubix {

    public Rubix(String title) {
        super(title);
    }

    @Override
    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || !solution.contains(sol) || Terminals.getBlockAll().getValue()) return false;

        long now = System.currentTimeMillis();
        if (now - Terminals.getOpenedAt() < Terminals.getFirstDelay().getValue().longValue() || now - Terminals.getClickedAt() < Terminals.getClickDelay().getValue().longValue()) return false;
        if (Terminals.getMode().is("Zero Ping")) {
            if (now - Terminals.getClickedAt() < Terminals.getClickDelay().getValue().longValue()) return false;
        } else {
            if (isClicked()) return false;
        }
        return this.getHoveredSlot() == slot;
    }

    @Override
    public void clickSlot(int slot, int button) {
        if (!canClick(slot, button)) return;
        clicked = true;

        if (Terminals.getMode().getIndex() != 0) {
            TermSol sol = getBySlot(slot);

            int realClicks = sol.getClicks() > 2 ? sol.getClicks() - 5 : sol.getClicks();

            if (Terminals.getAnyRubix().getValue()) {
                if (realClicks < 0) {
                    sol.setClicks(sol.getClicks() + 1);
                    button = 1;
                } else {
                    sol.setClicks(sol.getClicks() - 1);
                    button = 2;
                }
            } else {
                if (button == 1) {
                    if (realClicks > 0) return;
                    sol.setClicks(sol.getClicks() + 1);
                } else {
                    if (realClicks < 0) return;
                    sol.setClicks(sol.getClicks() - 1);
                }
            }

            onZeroPingClick(slot, button, sol);
        }

        this.click(slot, button);
    }

}
