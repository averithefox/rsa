package com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.type;

import com.google.gson.JsonObject;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.Argument;
import com.ricedotwho.rsa.module.impl.dungeon.boss.p3.autop3.args.RingArgType;
import com.ricedotwho.rsm.event.impl.game.ChatEvent;
import kotlin.text.Regex;

public class ChatMatchArg extends Argument<ChatEvent.Chat> {
  private final Regex regex;
  private boolean shouldExecute = false;

  public ChatMatchArg(String pattern) {
    super(RingArgType.CHAT_MATCH);
    regex = new Regex(pattern);
  }

  @Override
  public boolean check() {
    return shouldExecute;
  }

  @Override
  public void consume(ChatEvent.Chat event) {
    if (regex.containsMatchIn(event.getMessage().getString())) {
      shouldExecute = true;
    }
  }

  @Override
  public void reset() {
    shouldExecute = false;
  }

  @Override
  public String stringValue() {
    return "message matching " + regex.getPattern();
  }

  @Override
  public void serialize(JsonObject json) {
    json.addProperty(getType().name(), regex.getPattern());
  }

  public static ChatMatchArg create(Object arg) {
    return new ChatMatchArg((String) arg);
  }
}
