package com.ricedotwho.rsa.utils.fakeban;

import lombok.experimental.UtilityClass;
import net.minecraft.network.chat.Component;

@UtilityClass
public class DisconnectReason {
    public Component lastDisconnectReason = null;
}
