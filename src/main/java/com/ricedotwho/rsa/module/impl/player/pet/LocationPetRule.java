package com.ricedotwho.rsa.module.impl.player.pet;

import com.ricedotwho.rsm.component.impl.location.Island;
import com.ricedotwho.rsm.event.api.SubscribeEvent;
import com.ricedotwho.rsm.event.impl.game.LocationEvent;

import java.util.function.Consumer;

public class LocationPetRule extends PetRule {
    private final Island location;

    public LocationPetRule(String id, Consumer<String> callback, Island location) {
        super(id, callback);
        this.location = location;
    }

    @SubscribeEvent
    public void onLocationChange(LocationEvent.Changed event) {
        if (!event.getNewIsland().is(location)) return;
        this.callback.accept(getId());
    }

}
