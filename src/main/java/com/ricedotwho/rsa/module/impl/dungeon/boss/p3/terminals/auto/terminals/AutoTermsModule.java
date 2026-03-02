//package com.ricedotwho.rsa.module.impl.dungeon.terminals;
//
//import cloud.meowclient.meowforge.event.MeowEvents;
//import cloud.meowclient.meowforge.utils.Trigger;
//import cloud.meowclient.meowforge.event.events.PacketEvent;
//import cloud.meowclient.meowforge.event.events.RenderOverlayEvent;
//import cloud.meowclient.meowforge.mixin.AccessorMinecraft;
//import cloud.meowclient.meowforge.module.Module;
//import cloud.meowclient.meowforge.property.properties.PropertyBoolean;
//import cloud.meowclient.meowforge.property.properties.PropertyEnum;
//import cloud.meowclient.meowforge.property.properties.PropertyInteger;
//import cloud.meowclient.meowforge.utils.ChatUtils;
//import cloud.meowclient.meowforge.utils.NetworkUtils;
//import com.google.common.collect.ImmutableMap;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.FontRenderer;
//import net.minecraft.client.gui.ScaledResolution;
//import net.minecraft.client.gui.inventory.GuiContainer;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.renderer.RenderHelper;
//import net.minecraft.client.renderer.entity.RenderItem;
//import net.minecraft.init.Blocks;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.nbt.NBTTagList;
//import net.minecraft.network.Packet;
//import net.minecraft.network.play.client.C0DPacketCloseWindow;
//import net.minecraft.network.play.client.C0EPacketClickWindow;
//import net.minecraft.network.play.server.S2DPacketOpenWindow;
//import net.minecraft.network.play.server.S2EPacketCloseWindow;
//import net.minecraft.network.play.server.S2FPacketSetSlot;
//import net.minecraft.util.Timer;
//
//import java.util.*;
//import java.util.concurrent.Executors;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.TimeUnit;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//import java.util.stream.IntStream;
//
//public class AutoTermsModule extends Module {
//	private static final Minecraft mc = Minecraft.getMinecraft();
//	private static final RenderItem renderItem = mc.getRenderItem();
//	// final 이 아님
////	private static final FontRenderer fontRenderer = mc.fontRendererObj;
//	private static final Timer timer = ((AccessorMinecraft) mc).getTimer();
//	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
//
//
//	private final PropertyInteger firstClickDelay = new PropertyInteger("first-click-delay", 400, 0, Integer.MAX_VALUE);
//	private final PropertyInteger delay = new PropertyInteger("delay", 150, 0, Integer.MAX_VALUE);
//	private final PropertyInteger timeout = new PropertyInteger("timeout", 500, 0, Integer.MAX_VALUE);
//	private final PropertyBoolean colorsEnabled =  new PropertyBoolean("colors-terminal", true);
//	private final PropertyBoolean melodyEnabled = new PropertyBoolean("melody-terminal", true);
//	private final PropertyBoolean numbersEnabled = new PropertyBoolean("numbers-terminal", true);
//	private final PropertyBoolean redgreenEnabled = new PropertyBoolean("redgreen-terminal", true);
//	private final PropertyBoolean rubixEnabled = new PropertyBoolean("rubix-terminal", true);
//	private final PropertyBoolean startswithEnabled = new PropertyBoolean("startswith-terminal", true);
//	private final PropertyInteger melodyFirstClickDelay = new PropertyInteger("melody-first-click-delay", 50, 0, Integer.MAX_VALUE);
//	private final PropertyEnum melodySkipOn = new PropertyEnum("melody-skip-on", 1, new String[]{"NONE", "EDGES", "ALL"});
//	private final PropertyInteger melodySkipDelay = new PropertyInteger("melody-skip-delay", 50, 0, Integer.MAX_VALUE);
//	private final PropertyBoolean melodyPreventInsta = new PropertyBoolean("melody-prevent-insta", false);
//	private final PropertyBoolean invwalkEnabled = new PropertyBoolean("invwalk", false);
//	private final PropertyInteger invwalkMelodyMoveDelay = new PropertyInteger("melody-move-delay", 300, 0, Integer.MAX_VALUE);
//	private final PropertyBoolean invwalkRenderer = new PropertyBoolean("terminal-renderer", true);
//
//	private static class Click {
//		final int slot;
//		final int button;
//
//		Click(int slot, int button) {
//			this.slot = slot;
//			this.button = button;
//		}
//
//		Click(int slot) {
//			this.slot = slot;
//			this.button = 0;
//		}
//	}
//
//	private abstract class Terminal {
//		final PropertyBoolean toggle;
//		final int size;
//		final ItemStack[] itemStacks;
//		final String name;
//		int windowId = 0;
//		short actionNumber = 0;
//		long lastClick = System.currentTimeMillis();
//		int pCurrent = 0;
//		int pMax = 0;
//		boolean overlayTrigger = false;
//		boolean closeTrigger = false;
//		boolean setSlotTrigger = false;
//
//		Terminal(PropertyBoolean toggle, String name, int size) {
//			this.toggle = toggle;
//			this.name = name;
//			this.size = size;
//			this.itemStacks = new ItemStack[size];
//		}
//
//		void onPacketReceive(PacketEvent.ChannelRead event) {
//			if (!toggled) return;
//			Packet<?> packet = event.getPacket();
//			if (packet instanceof S2EPacketCloseWindow) {
//				if (!closeTrigger) return;
//				closeWindow();
//			} else if (packet instanceof S2DPacketOpenWindow) {
//				S2DPacketOpenWindow packetOpenWindow = (S2DPacketOpenWindow) packet;
//				resetWindow();
//				windowId = packetOpenWindow.getWindowId();
//				// TODO: P3 체크
//				if (!toggle.getValue()) return;
//				if (windowId < 1 || windowId > 100) return;
//				if (!"minecraft:chest".equals(packetOpenWindow.getGuiId())) return;
//				if (packetOpenWindow.getSlotCount() != size) return;
//				if (!checkTerminalTitle(ChatUtils.removeFormatting(packetOpenWindow.getWindowTitle().getUnformattedText()))) return;
//				setSlotTrigger = true;
//				closeTrigger = true;
//				if (invwalkEnabled.getValue()) {
//					if (mc.currentScreen instanceof GuiContainer) mc.currentScreen = null; // 마우스 grab 안되긴 하는데 어차피 failsafe라 상관없음
//					overlayTrigger = true;
//					event.setCanceled(true);
//				}
//			} else if (packet instanceof S2FPacketSetSlot) {
//				if (!setSlotTrigger) return;
//				onSetSlot((S2FPacketSetSlot) packet);
//			}
//		}
//
//		void onPacketSend(PacketEvent.ChannelWrite event) {
//			if (!toggled) return;
//			Packet<?> packet = event.getPacket();
//			if (packet instanceof C0DPacketCloseWindow) {
//				if (!closeTrigger) return;
//				closeWindow();
//			}
//		}
//
//		void onRenderOverlay(RenderOverlayEvent event) {
//			if (!toggled) return;
//			if (overlayTrigger) draw();
//		}
//
//		abstract void onSetSlot(S2FPacketSetSlot packet);
//
//		abstract boolean checkTerminalTitle(String title);
//
//		void resetWindow() {
//			setSlotTrigger = false;
//			closeTrigger = false;
//			overlayTrigger = false;
//			windowId = 0;
//			for (int i = 0; i < size; ++i) itemStacks[i] = null;
//			actionNumber = 0;
//		}
//
//		void closeWindow() {
//			resetWindow();
//		}
//
//		void draw() {
//			drawText();
//			if (invwalkRenderer.getValue()) drawItems();
//		}
//
//		void drawText() {
//			FontRenderer fontRenderer = mc.fontRendererObj;
//			ScaledResolution sr = new ScaledResolution(mc);
//			String text = getTextOverlay();
//
//			float scale = 2;
//			float x = ((sr.getScaledWidth() / scale - fontRenderer.getStringWidth(text)) / 2);
//			float y = (sr.getScaledHeight() / scale / 2 + 16);
//
//			GlStateManager.pushMatrix();
//			GlStateManager.scale(scale, scale, 1.0F);
//			fontRenderer.drawStringWithShadow(text, x, y, 0xFFFFFF);
//			GlStateManager.popMatrix();
//		}
//
//		void drawItems() {
//			FontRenderer fontRenderer = mc.fontRendererObj;
//			ScaledResolution sr = new ScaledResolution(mc);
//			int originX = sr.getScaledWidth() / 2 - 72;
//			int originY = sr.getScaledHeight() / 2 + 64;
//			RenderHelper.enableGUIStandardItemLighting();
//			for (int i = 0; i < size; ++i) {
//				ItemStack itemStack = itemStacks[i];
//				if (itemStack == null) continue;
//				int offsetX = i % 9 * 16;
//				int offsetY = i / 9 * 16;
//				int x = originX + offsetX;
//				int y = originY + offsetY;
//				renderItem.renderItemAndEffectIntoGUI(itemStack, x, y);
//				renderItem.renderItemOverlayIntoGUI(fontRenderer, itemStack, x, y, null);
//			}
//			RenderHelper.disableStandardItemLighting();
//		}
//
//		String getTextOverlay() {
//			return "§2" + name + " §9" + pCurrent + "/" + pMax;
//		}
//
//		void click(Click click) {
//			if (click == null) return;
//			if (windowId < 1 || windowId > 100) return;
//			++actionNumber; // short라 32767 다음 -32768로 가는 바닐라 behaviour와 같음
//			lastClick = System.currentTimeMillis();
//			NetworkUtils.sendPacket(new C0EPacketClickWindow(windowId, click.slot, click.button, 0, itemStacks[click.slot], actionNumber));
//		}
//	}
//
//	private abstract class RegularTerminal extends Terminal {
//		final ItemStack[] preItemStacks;
//		int preWindowId = 0;
//
//		RegularTerminal(PropertyBoolean toggle, String name, int size) {
//			super(toggle, name, size);
//			preItemStacks = new ItemStack[size];
//		}
//
//		@Override
//		void onSetSlot(S2FPacketSetSlot packet) {
//			if (packet.func_149175_c() != windowId) return;
//			int slot = packet.func_149173_d();
//			if (slot < 0 || slot >= size) return;
//			itemStacks[slot] = packet.func_149174_e();
//			if (!Arrays.stream(itemStacks).allMatch(Objects::nonNull)) return;
//			setSlotTrigger = false;
//			int initialWindowId = windowId;
//			boolean isNewTerminal = windowId != preWindowId || IntStream.range(0, size).anyMatch(i -> !ItemStack.areItemStacksEqual(itemStacks[i], preItemStacks[i]));
//			resetPreWindow();
//			if (isNewTerminal) {
//				scheduler.schedule(() -> clickSolution(initialWindowId), firstClickDelay.getValue(), TimeUnit.MILLISECONDS);
//				pCurrent = 0;
//				pMax = getSolution().length;
//			} else {
//				long calculatedDelay = Math.max(delay.getValue() - (System.currentTimeMillis() - lastClick), 0);
//				if (calculatedDelay > 0) scheduler.schedule(() -> clickSolution(initialWindowId), calculatedDelay, TimeUnit.MILLISECONDS);
//				else clickSolution(initialWindowId);
//				pCurrent = pMax - getSolution().length;
//			}
//		}
//
//		abstract Click[] getSolution();
//
//		@Override
//		void closeWindow() {
//			super.closeWindow();
//			resetPreWindow();
//		}
//
//		void resetPreWindow() {
//			preWindowId = 0;
//			for (int i = 0; i < size; ++i) preItemStacks[i] = null;
//		}
//
//		void clickSolution(int initialWindowId) {
//			if (windowId != initialWindowId) return;
//			Click[] solution = getSolution();
//			if (solution.length == 0) return;
//			resetPreWindow();
//			preWindowId = windowId % 100 + 1; // +1 %100 뒤에 하는거 버그 아님
//			ItemStack[] prediction = getPrediction(solution[0]);
//			System.arraycopy(prediction, 0, preItemStacks, 0, size);
//			click(solution[0]);
//			if (timeout.getValue() > 0) scheduler.schedule(() -> clickSolution(initialWindowId), timeout.getValue(), TimeUnit.MILLISECONDS);
//		}
//
//		abstract ItemStack[] getPrediction(Click click);
//	}
//
//	private class ColorsTerminal extends RegularTerminal {
//		final int[] allowedSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
//        final Map<String, String> replacements = ImmutableMap.<String, String>builder()
//                .put("light gray", "silver")
//                .put("wool", "white")
//                .put("bone", "white")
//                .put("ink", "black")
//                .put("lapis", "blue")
//                .put("cocoa", "brown")
//                .put("dandelion", "yellow")
//                .put("rose", "red")
//                .put("cactus", "green")
//                .build();
//
//		final Pattern pattern = Pattern.compile("^Select all the ([\\w ]+) items!$");
//		String color = "";
//
//		ColorsTerminal() {
//			super(colorsEnabled, "Colors", 54);
//		}
//
//		String fixName(String name) {
//			for (Map.Entry<String, String> replacement : replacements.entrySet()) {
//				if (!name.startsWith(replacement.getKey())) continue;
//				name = replacement.getValue();
//				break;
//			}
//			return name;
//		}
//
//		@Override
//		Click[] getSolution() {
//			return Arrays.stream(allowedSlots).filter(i -> itemStacks[i] != null && !itemStacks[i].getTagCompound().hasKey("ench") && fixName(ChatUtils.removeFormatting(itemStacks[i].getTagCompound().getCompoundTag("display").getString("Name")).toLowerCase()).startsWith(color)).boxed().map(i -> new Click(i, 0)).toArray(Click[]::new);
//		}
//
//		@Override
//		ItemStack[] getPrediction(Click click) {
//			ItemStack[] prediction = Arrays.stream(itemStacks).map(itemStack -> itemStack == null ? null : itemStack.copy()).toArray(ItemStack[]::new);
//			ItemStack itemStack = prediction[click.slot];
//			if (itemStack != null) itemStack.getTagCompound().setTag("ench", new NBTTagList());
//			return prediction;
//		}
//
//		@Override
//		boolean checkTerminalTitle(String title) {
//			Matcher matcher = pattern.matcher(title);
//			if (matcher.find()) {
//				color = matcher.group(1).toLowerCase();
//				return true;
//			}
//			return false;
//		}
//	}
//
//	private class NumbersTerminal extends RegularTerminal {
//		final int[] allowedSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25};
//
//		NumbersTerminal() {
//			super(numbersEnabled, "Numbers", 36);
//		}
//
//		@Override
//		Click[] getSolution() {
//			return Arrays.stream(allowedSlots).filter(i -> itemStacks[i] != null && itemStacks[i].getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) && itemStacks[i].getItemDamage() == 14).boxed().sorted((a, b) -> itemStacks[a].stackSize - itemStacks[b].stackSize).map(i -> new Click(i, 0)).toArray(Click[]::new);
//		}
//
//		@Override
//		ItemStack[] getPrediction(Click click) {
//			ItemStack[] prediction = Arrays.stream(itemStacks).map(itemStack -> itemStack == null ? null : itemStack.copy()).toArray(ItemStack[]::new);
//			ItemStack itemStack = prediction[click.slot];
//			if (itemStack != null) itemStack.setItemDamage(5);
//			return prediction;
//		}
//
//		@Override
//		boolean checkTerminalTitle(String title) {
//			return "Click in order!".equals(title);
//		}
//	}
//
//	private class RedgreenTerminal extends RegularTerminal {
//		final int[] allowedSlots = {11, 12, 13, 14, 15, 20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
//
//		RedgreenTerminal() {
//			super(redgreenEnabled, "Red Green", 45);
//		}
//
//		@Override
//		Click[] getSolution() {
//			return Arrays.stream(allowedSlots).filter(i -> itemStacks[i] != null && itemStacks[i].getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) && itemStacks[i].getItemDamage() == 14).boxed().map(i -> new Click(i, 0)).toArray(Click[]::new);
//		}
//
//		@Override
//		ItemStack[] getPrediction(Click click) {
//			ItemStack[] prediction = Arrays.stream(itemStacks).map(itemStack -> itemStack == null ? null : itemStack.copy()).toArray(ItemStack[]::new);
//			ItemStack itemStack = prediction[click.slot];
//			if (itemStack != null) {
//				itemStack.setItemDamage(5);
//				itemStack.getTagCompound().getCompoundTag("display").setString("Name", "§aOn");
//			}
//			return prediction;
//		}
//
//		@Override
//		boolean checkTerminalTitle(String title) {
//			return "Correct all the panes!".equals(title);
//		}
//	}
//
//	private class RubixTerminal extends RegularTerminal {
//		final int[] allowedSlots = {12, 13, 14, 21, 22, 23, 30, 31, 32};
//		final int[] damageOrder = {14, 1, 4, 13, 11};
//		final String[] stringOrder = {"§aRed", "§aOrange", "§aYellow", "§aGreen", "§aBlue"};
//
//		RubixTerminal() {
//			super(rubixEnabled, "Rubix", 45);
//		}
//
//		int calcIndex(int index) {
//			return (index + damageOrder.length) % damageOrder.length;
//		}
//
//		@Override
//		Click[] getSolution() {
//			List<Click> solution = new ArrayList<>();
//			int[] clicks = {0, 0, 0, 0, 0};
//			for (int i = 0; i < clicks.length; ++i) {
//				int finalI = i;
//				for (ItemStack itemStack : Arrays.stream(allowedSlots).filter(i2 -> itemStacks[i2] != null && itemStacks[i2].getItemDamage() != damageOrder[calcIndex(finalI)]).boxed().map(i2 -> itemStacks[i2]).toArray(ItemStack[]::new)) {
//					int damage = itemStack.getItemDamage();
//					if (damage == damageOrder[calcIndex(i - 1)] || damage == damageOrder[calcIndex(i + 1)]) {
//						++clicks[i];
//					} else if (damage == damageOrder[calcIndex(i - 2)] || damage == damageOrder[calcIndex(i + 2)]) {
//						clicks[i] += 2;
//					}
//				}
//			}
//			int origin = IntStream.range(0, clicks.length).reduce((i, j) -> clicks[i] < clicks[j] ? i : j).orElse(-1);
//			for (int i : Arrays.stream(allowedSlots).filter(i2 -> itemStacks[i2] != null && itemStacks[i2].getItemDamage() != damageOrder[calcIndex(origin)]).toArray()) {
//				int damage = itemStacks[i].getItemDamage();
//				if (damage == damageOrder[calcIndex(origin - 2)]) {
//					solution.add(new Click(i, 0));
//					solution.add(new Click(i, 0));
//				} else if (damage == damageOrder[calcIndex(origin - 1)]) {
//					solution.add(new Click(i, 0));
//				} else if (damage == damageOrder[calcIndex(origin + 1)]) {
//					solution.add(new Click(i, 1));
//				} else if (damage == damageOrder[calcIndex(origin + 2)]) {
//					solution.add(new Click(i, 1));
//					solution.add(new Click(i, 1));
//				}
//			}
//			return solution.toArray(new Click[0]);
//		}
//
//		@Override
//		ItemStack[] getPrediction(Click click) {
//			ItemStack[] prediction = Arrays.stream(itemStacks).map(itemStack -> itemStack == null ? null : itemStack.copy()).toArray(ItemStack[]::new);
//			ItemStack itemStack = prediction[click.slot];
//			int offset = click.button == 0 ? 1 : -1;
//			if (itemStack != null) {
//				int index = IntStream.range(0, damageOrder.length).filter(i -> damageOrder[i] == itemStack.getItemDamage()).findFirst().orElse(-1);
//				int newIndex = calcIndex(index + offset);
//				itemStack.setItemDamage(damageOrder[newIndex]);
//				itemStack.getTagCompound().getCompoundTag("display").setString("Name", stringOrder[newIndex]);
//			}
//			return prediction;
//		}
//
//		@Override
//		boolean checkTerminalTitle(String title) {
//			return "Change all to same color!".equals(title);
//		}
//	}
//
//	private class StartswithTerminal extends RegularTerminal {
//		final int[] allowedSlots = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
//		final Pattern pattern = Pattern.compile("^What starts with: '(\\w)'\\?$");
//		String letter = "";
//
//		StartswithTerminal() {
//			super(startswithEnabled, "Starts With", 45);
//		}
//
//		@Override
//		Click[] getSolution() {
//			return Arrays.stream(allowedSlots).filter(i -> itemStacks[i] != null && !itemStacks[i].getTagCompound().hasKey("ench") && ChatUtils.removeFormatting(itemStacks[i].getTagCompound().getCompoundTag("display").getString("Name")).toLowerCase().startsWith(letter)).boxed().map(i -> new Click(i, 0)).toArray(Click[]::new);
//		}
//
//		@Override
//		ItemStack[] getPrediction(Click click) {
//			ItemStack[] prediction = Arrays.stream(itemStacks).map(itemStack -> itemStack == null ? null : itemStack.copy()).toArray(ItemStack[]::new);
//			ItemStack itemStack = prediction[click.slot];
//			if (itemStack != null) itemStack.getTagCompound().setTag("ench", new NBTTagList());
//			return prediction;
//		}
//
//		@Override
//		boolean checkTerminalTitle(String title) {
//			Matcher matcher = pattern.matcher(title);
//			if (matcher.find()) {
//				letter = matcher.group(1).toLowerCase();
//				return true;
//			}
//			return false;
//		}
//	}
//
//	private class MelodyTerminal extends Terminal {
//		long lastReset = System.currentTimeMillis();
//		int ticks = 0;
//
//		MelodyTerminal() {
//			super(melodyEnabled, "Melody", 54);
//		}
//
//		@Override
//		void onSetSlot(S2FPacketSetSlot packet) {
//			if (packet.func_149175_c() != windowId) return;
//			int slot = packet.func_149173_d();
//			if (slot < 0 || slot >= size) return;
//			itemStacks[slot] = packet.func_149174_e();
//			if (itemStacks[slot].getItem() != Item.getItemFromBlock(Blocks.stained_glass_pane) || itemStacks[slot].getItemDamage() != 5) return;
//			++ticks;
//			int initialWindowId = windowId;
//			int correct = IntStream.range(0, size).filter(i -> itemStacks[i] != null && itemStacks[i].getItem() == Item.getItemFromBlock(Blocks.stained_glass_pane) && itemStacks[i].getItemDamage() == 2).findFirst().orElse(0) - 1;
//			if (correct == -1) return;
//			int button = slot / 9 - 1;
//			int current = slot % 9 - 1;
//			pCurrent = button;
//			pMax = 4;
//			if (current != correct) return;
//			int buttonSlot = button * 9 + 16;
//			int fcd = melodyFirstClickDelay.getValue();
//			int skd = melodySkipDelay.getValue();
//			long calculatedDelay = Math.max(fcd - (System.currentTimeMillis() - lastReset), 0);
//			if (calculatedDelay > 0) scheduler.schedule(() -> click(initialWindowId, new Click(buttonSlot, 0)), calculatedDelay, TimeUnit.MILLISECONDS);
//			else click(initialWindowId, new Click(buttonSlot, 0));
//			if (melodySkipOn.getValue() == 1 && (current == 0 || current == 4) || melodySkipOn.getValue() == 2) {
//				if (button <= 3) scheduler.schedule(() -> click(initialWindowId, new Click(buttonSlot + 9, 0)), skd + calculatedDelay, TimeUnit.MILLISECONDS);
//				if (button <= 2) scheduler.schedule(() -> click(initialWindowId, new Click(buttonSlot + 18, 0)), skd * 2L + calculatedDelay, TimeUnit.MILLISECONDS);
//				if (button <= 1 && (!melodyPreventInsta.getValue() || ticks > 1)) scheduler.schedule(() -> click(initialWindowId, new Click(buttonSlot + 27, 0)), skd * 3L + calculatedDelay, TimeUnit.MILLISECONDS);
//			}
//		}
//
//		@Override
//		void closeWindow() {
//			super.closeWindow();
//			if (invwalkEnabled.getValue()) timer.timerSpeed = 1;
//		}
//
//		@Override
//		void resetWindow() {
//			super.resetWindow();
//			lastReset = System.currentTimeMillis();
//			ticks = 0;
//		}
//
//		@Override
//		boolean checkTerminalTitle(String title) {
//			return "Click the button on time!".equals(title);
//		}
//
//		String getTextOverlay() {
//			String text = "";
//			long time = System.currentTimeMillis();
//			int moveDelay = invwalkMelodyMoveDelay.getValue();
//			if (lastClick + moveDelay > time) text += "§c" + name + " " + (lastClick - time + moveDelay) + "ms";
//			else text += "§2" + name;
//			text += " §9" + pCurrent + "/" + pMax;
//			return text;
//		}
//
//		void click(int initialWindowId, Click click) {
//			if (windowId != initialWindowId) return;
//			click(click);
//			if (!invwalkEnabled.getValue()) return;
//			int moveDelay = invwalkMelodyMoveDelay.getValue();
//			timer.timerSpeed = 0;
//			scheduler.schedule(() -> {
//				if (lastClick + moveDelay <= System.currentTimeMillis()) timer.timerSpeed = 1;
//			}, moveDelay, TimeUnit.MILLISECONDS);
//		}
//	}
//
//	public AutoTermsModule() {
//		super("AutoTerms", false, 0, false);
//
//		registerProperty(firstClickDelay);
//		registerProperty(delay);
//		registerProperty(timeout);
//		registerProperty(colorsEnabled);
//		registerProperty(melodyEnabled);
//		registerProperty(numbersEnabled);
//		registerProperty(redgreenEnabled);
//		registerProperty(rubixEnabled);
//		registerProperty(startswithEnabled);
//		registerProperty(melodyFirstClickDelay);
//		registerProperty(melodySkipOn);
//		registerProperty(melodySkipDelay);
//		registerProperty(melodyPreventInsta);
//		registerProperty(invwalkEnabled);
//		registerProperty(invwalkMelodyMoveDelay);
//		registerProperty(invwalkRenderer);
//
//		Terminal[] terminals = {new ColorsTerminal(), new NumbersTerminal(), new RedgreenTerminal(), new RubixTerminal(), new StartswithTerminal(), new MelodyTerminal()};
//		for (Terminal terminal : terminals) {
//			MeowEvents.PACKET_CHANNEL_READ.register(terminal::onPacketReceive);
//			MeowEvents.PACKET_CHANNEL_WRITE.register(terminal::onPacketSend);
//			MeowEvents.RENDER_OVERLAY.register(terminal::onRenderOverlay);
//		}
//	}
//}
