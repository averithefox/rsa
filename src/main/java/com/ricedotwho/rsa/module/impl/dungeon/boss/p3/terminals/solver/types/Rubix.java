package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.solver.types;

import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.terminals.solver.TerminalSolver;
import com.ricedotwho.rsm.component.impl.Terminals;
import com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.TermSol;

public class Rubix extends com.ricedotwho.rsm.module.impl.dungeon.boss.p3.terminal.types.Rubix {

    public Rubix(String title) {
        super(title);
    }

    @Override
    protected boolean canClick(int slot, int button) {
        TermSol sol = getBySlot(slot);
        if (sol == null || !solution.contains(sol) || TerminalSolver.getBlockAll().getValue()) return false;

        long now = System.currentTimeMillis();
        if (now - Terminals.getOpenedAt() < TerminalSolver.getFirstDelay().getValue().longValue() || now - Terminals.getClickedAt() < TerminalSolver.getClickDelay().getValue().longValue()) return false;
        if (TerminalSolver.getMode().is("Zero Ping")) {
            if (now - Terminals.getClickedAt() < TerminalSolver.getClickDelay().getValue().longValue()) return false;
        } else {
            if (isClicked()) return false;
        }
        return this.getHoveredSlot() == slot;
    }

    @Override
    public void clickSlot(int slot, int button) {
        if (!canClick(slot, button)) return;
        clicked = true;

        if (TerminalSolver.getMode().getIndex() != 0) {
            TermSol sol = getBySlot(slot);

            int realClicks = sol.getClicks() > 2 ? sol.getClicks() - 5 : sol.getClicks();

            if (TerminalSolver.getAnyRubix().getValue()) {
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
