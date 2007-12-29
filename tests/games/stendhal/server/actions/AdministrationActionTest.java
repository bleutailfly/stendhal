package games.stendhal.server.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import games.stendhal.common.Direction;
import games.stendhal.server.actions.admin.AdministrationAction;
import games.stendhal.server.actions.admin.AlterAction;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.defaultruleset.DefaultEntityManager;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.RaidCreature;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import utilities.PlayerTestHelper;
import utilities.SpeakerNPCTestHelper;
import utilities.TestPlayer;

public class AdministrationActionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		DefaultEntityManager.getInstance();	// load item classes including "dagger" from XML
		AdministrationAction.register();
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get().clearPlayers();
	}

	@After
	public void tearDown() throws Exception {
		MockStendhalRPRuleProcessor.get().clearPlayers();
	}

	@Test
	public final void testRegisterCommandLevel() {
	}

	@Test
	public final void testGetLevelForCommand() {
		assertEquals(-1, AdministrationAction.getLevelForCommand("unkown")
				.intValue());
		assertEquals(0, AdministrationAction.getLevelForCommand("adminlevel")
				.intValue());
		assertEquals(100, AdministrationAction.getLevelForCommand("support")
				.intValue());
		assertEquals(50, AdministrationAction.getLevelForCommand(
				"supportanswer").intValue());
		assertEquals(200, AdministrationAction.getLevelForCommand("tellall")
				.intValue());
		assertEquals(300, AdministrationAction.getLevelForCommand("teleportto")
				.intValue());
		assertEquals(400, AdministrationAction.getLevelForCommand("teleport")
				.intValue());
		assertEquals(400, AdministrationAction.getLevelForCommand("jail")
				.intValue());
		assertEquals(400, AdministrationAction.getLevelForCommand("gag")
				.intValue());
		assertEquals(500, AdministrationAction.getLevelForCommand("invisible")
				.intValue());
		assertEquals(500, AdministrationAction.getLevelForCommand("ghostmode")
				.intValue());
		assertEquals(500, AdministrationAction.getLevelForCommand(
				"teleclickmode").intValue());
		assertEquals(600, AdministrationAction.getLevelForCommand("inspect")
				.intValue());
		assertEquals(700, AdministrationAction.getLevelForCommand("destroy")
				.intValue());
		assertEquals(800, AdministrationAction.getLevelForCommand("summon")
				.intValue());
		assertEquals(800, AdministrationAction.getLevelForCommand("summonat")
				.intValue());
		assertEquals(900, AdministrationAction.getLevelForCommand("alter")
				.intValue());
		assertEquals(900, AdministrationAction.getLevelForCommand(
				"altercreature").intValue());
		assertEquals(5000, AdministrationAction.getLevelForCommand("super")
				.intValue());
	}

	@Test
	public final void testIsPlayerAllowedToExecuteAdminCommand() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer();
		assertFalse(AdministrationAction.isPlayerAllowedToExecuteAdminCommand(
				pl, "", true));
		// assertEquals("Sorry, command \"\" is unknown.", pl.getPrivateText());
		assertTrue(AdministrationAction.isPlayerAllowedToExecuteAdminCommand(
				pl, "adminlevel", true));
		pl.resetPrivateText();

		assertEquals(false, AdministrationAction
				.isPlayerAllowedToExecuteAdminCommand(pl, "support", true));
		assertEquals("Sorry, you need to be an admin to run \"support\".", pl
				.getPrivateText());

		pl.put("adminlevel", 50);
		pl.resetPrivateText();
		assertEquals(true, AdministrationAction
				.isPlayerAllowedToExecuteAdminCommand(pl, "adminlevel", true));
		assertEquals(false, AdministrationAction
				.isPlayerAllowedToExecuteAdminCommand(pl, "support", true));
		assertEquals(
				"Your admin level is only 50, but a level of 100 is required to run \"support\".",
				pl.getPrivateText());
		assertEquals(true,
				AdministrationAction.isPlayerAllowedToExecuteAdminCommand(pl,
						"supportanswer", true));
	}

	@Test
	public final void testTellAllAction() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("dummy");
		// bad bad
		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		CommandCenter.execute(pl, new RPAction());
		assertEquals("Unknown command null", pl.getPrivateText());

		pl.resetPrivateText();
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "tellall");
		action.put("text", "huhu");
		CommandCenter.execute(pl, action);
		assertEquals("Administrator SHOUTS: huhu", pl.getPrivateText());
	}

	@Test
	public final void testSupportAnswerAction() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer();
		TestPlayer bob = PlayerTestHelper.createTestPlayer("bob");
		Player anptherAdmin = PlayerTestHelper.createPlayer("anotheradmin");
		anptherAdmin.setAdminLevel(5000);
		// bad bad
		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		MockStendhalRPRuleProcessor.get().addPlayer(bob);
		MockStendhalRPRuleProcessor.get().addPlayer(anptherAdmin);

		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "supportanswer");
		action.put("text", "huhu");
		action.put("target", "bob");
		CommandCenter.execute(pl, action);
		assertEquals("Support (player) tells you: huhu", bob.getPrivateText());
		assertEquals("player answers bob's support question: huhu", anptherAdmin.getPrivateText());
		
		bob.resetPrivateText();
		pl.resetPrivateText();
		pl.put("adminlevel", 0);
		assertEquals("0", pl.get("adminlevel"));
		CommandCenter.execute(pl, action);
		assertEquals(
				"Sorry, you need to be an admin to run \"supportanswer\".", pl
						.getPrivateText());
	}

	@Test
	public final void testTeleportActionToInvalidZone() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer();
		Player bob = PlayerTestHelper.createPlayer("bob");
		// bad bad
		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		MockStendhalRPRuleProcessor.get().addPlayer(bob);

		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "teleport");
		action.put("text", "huhu");
		action.put("target", "bob");
		action.put("zone", "non-existing-zone");
		action.put("x", "0");
		action.put("y", "0");

		assertTrue(action.has("target") && action.has("zone")
				&& action.has("x"));

		CommandCenter.execute(pl, action);
		// The list of existing zones depends on other tests, so we simply
		// ignore it here.
		assertTrue(pl
				.getPrivateText()
				.startsWith(
						"Zone \"IRPZone.ID [id=non-existing-zone]\" not found. Valid zones: ["));
	}

	@Test
	public final void testTeleportActionToValidZone() {

		StendhalRPZone zoneTo = new StendhalRPZone("zoneTo");
		TestPlayer pl = PlayerTestHelper.createTestPlayer();
		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		PlayerTestHelper.generatePlayerRPClasses();
		Player bob = new Player(new RPObject()) {
			@Override
			public boolean teleport(StendhalRPZone zone, int x, int y,
					Direction dir, Player teleporter) {
				assertEquals("zoneTo", zone.getName());
				// added hack to have something to verify
				setName("hugo");
				return true;

			}
		};
		bob.setName("bob");
		PlayerTestHelper.addEmptySlots(bob);

		MockStendhalRPRuleProcessor.get().addPlayer(bob);

		MockStendlRPWorld.get().addRPZone(zoneTo);
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "teleport");
		action.put("text", "huhu");
		action.put("target", "bob");
		action.put("zone", "zoneTo");
		action.put("x", "0");
		action.put("y", "0");

		assertTrue(action.has("target") && action.has("zone")
				&& action.has("x"));

		CommandCenter.execute(pl, action);
		assertEquals("hugo", bob.getName());
	}

	@Test
	public final void testTeleportToActionPlayerNotThere() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer();
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "teleportto");
		action.put("target", "blah");
		CommandCenter.execute(pl, action);
		assertEquals("Player \"blah\" not found", pl.getPrivateText());
	}

	@Test
	public final void testTeleportToActionPlayerThere() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer("blah");

		pl.put("adminlevel", 5000);

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		StendhalRPZone zone = new StendhalRPZone("zone");
		zone.add(pl);
		RPAction action = new RPAction();
		action.put("type", "teleportto");
		action.put("target", "blah");
		CommandCenter.execute(pl, action);
		assertEquals("Position [0,0] is occupied", pl.getPrivateText());
	}

	@Test
	public final void testOnAlterActionWrongAttribute() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("bob");
		pl.put("adminlevel", 5000);

		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		RPAction action = new RPAction();
		action.put("type", "alter");
		action.put("target", "bob");
		action.put("stat", "0");
		action.put("mode", "0");
		action.put("value", 0);

		CommandCenter.execute(pl, action);
		assertEquals(
				"Attribute you are altering is not defined in RPClass(player)",
				pl.getPrivateText());
	}

	@Test
	public final void testOnAlterAction() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer("bob");
		pl.put("adminlevel", 5000);

		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		RPAction action = new RPAction();
		action.put("type", "alter");
		action.put("target", "bob");
		action.put("stat", "name");
		action.put("mode", "0");
		action.put("value", 0);

		CommandCenter.execute(pl, action);
		assertEquals("Sorry, name cannot be changed.", pl.getPrivateText());
		action.put("stat", "adminlevel");
		pl.resetPrivateText();
		CommandCenter.execute(pl, action);
		assertEquals(
				"Use #/adminlevel #<playername> #[<newlevel>] to display or change adminlevel.",
				pl.getPrivateText());
	}

	@Test
	public final void testOnAlterActionTitle() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("bob");
		pl.put("adminlevel", 5000);

		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		RPAction action = new RPAction();
		action.put("type", "alter");
		action.put("target", "bob");
		action.put("stat", "title");
		action.put("mode", "0");
		action.put("value", 0);

		CommandCenter.execute(pl, action);
		assertEquals("The title attribute may not be changed directly.", pl
				.getPrivateText());
	}

	@Test
	public final void testOnAlterActionHP() {

		AdministrationAction aa = new AlterAction();

		TestPlayer pl = PlayerTestHelper.createTestPlayer("bob");
		pl.put("adminlevel", 5000);
		pl.put("base_hp", 100);
		pl.setHP(100);
		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		RPAction action = new RPAction();
		action.put("type", "alter");
		action.put("target", "bob");
		action.put("stat", "hp");
		action.put("mode", "0");
		action.put("value", 0);
		assertEquals(100, pl.getHP());

		aa.onAction(pl, action);
		assertEquals("may not change HP to 0 ", 100, pl.getHP());

		action.put("value", 120);
		aa.onAction(pl, action);
		assertEquals("may  not change HP over base_hp", 100, pl.getHP());

		action.put("value", 90);
		aa.onAction(pl, action);
		assertEquals("may  change HP to 90 ", 90, pl.getHP());

		action.put("value", 90);
		action.put("mode", "sub");
		assertEquals("may  change HP to 90 ", 90, pl.getHP());
	}

	@Test
	public final void testOnAlterActionHPsub() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("bob");
		pl.put("adminlevel", 5000);
		pl.put("base_hp", 100);
		pl.setHP(100);
		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		RPAction action = new RPAction();
		action.put("type", "alter");
		action.put("target", "bob");
		action.put("stat", "hp");
		action.put("mode", "sub");
		action.put("value", 90);
		assertEquals(100, pl.getHP());

		CommandCenter.execute(pl, action);
		assertEquals(10, pl.getHP());
		CommandCenter.execute(pl, action);
		assertEquals(-80, pl.getHP());
	}

	@Test
	public final void testOnAlterActionHPadd() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer("bob");
		pl.put("adminlevel", 5000);
		pl.put("base_hp", 100);
		pl.setHP(10);
		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		RPAction action = new RPAction();
		action.put("type", "alter");
		action.put("target", "bob");
		action.put("stat", "hp");
		action.put("mode", "add");
		action.put("value", 80);
		assertEquals(10, pl.getHP());

		CommandCenter.execute(pl, action);
		assertEquals(90, pl.getHP());
		CommandCenter.execute(pl, action);
		assertEquals(90, pl.getHP());
	}

	@Test
	public final void testAlterCreatureEntityNotFound() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");

		MockStendhalRPRuleProcessor.get().addPlayer(pl);

		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "altercreature");
		action.put("target", "bob");
		action.put("text", "blabla");

		CommandCenter.execute(pl, action);
		assertEquals("Entity not found", pl.getPrivateText());
	}

	@Test
	public final void testSummonAlterCreature() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		StendhalRPZone zone = new StendhalRPZone("testzone") {
			@Override
			public synchronized boolean collides(Entity entity, double x,
					double y) {

				return false;
			}
		};
		zone.add(pl);
		pl.setPosition(1, 1);
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "summon");
		action.put("creature", "rat");
		action.put("x", 0);
		action.put("y", 0);
		CommandCenter.execute(pl, action);
		assertEquals(1, pl.getID().getObjectID());
		Creature rat = (Creature) zone.getEntityAt(0, 0);
		assertEquals("rat", rat.get("subclass"));

		action = new RPAction();
		action.put("type", "altercreature");
		action.put("target", "#2");
		// must be of type "name/atk/def/hp/xp",
		action.put("text", "name/5/6/7/8");

		CommandCenter.execute(pl, action);

		assertEquals("name", "name", rat.getName());
		assertEquals("atk", 5, rat.getATK());
		assertEquals("def", 6, rat.getDEF());
		assertEquals("hp", 7, rat.getHP());
		assertEquals("xp", 8, rat.getXP());
	}

	@Test
	public final void testInvisible() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "invisible");
		assertFalse(pl.isInvisible());
		CommandCenter.execute(pl, action);
		assertTrue(pl.isInvisible());
		CommandCenter.execute(pl, action);
		assertFalse(pl.isInvisible());
	}

	@Test
	public final void testTeleclickmode() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "teleclickmode");
		assertFalse(pl.isTeleclickEnabled());
		CommandCenter.execute(pl, action);
		assertTrue(pl.isTeleclickEnabled());
		CommandCenter.execute(pl, action);
		assertFalse(pl.isTeleclickEnabled());
	}

	@Test
	public final void testJail() {
		MockStendlRPWorld.get().addRPZone(new StendhalRPZone("-1_semos_jail", 100, 100));

		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		PlayerTestHelper.registerPlayer(pl, "-1_semos_jail");
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "jail");

		CommandCenter.execute(pl, action);

		assertEquals("Usage: /jail name minutes reason", pl.getPrivateText());
		pl.resetPrivateText();
		action = new RPAction();
		action.put("type", "jail");
		action.put("target", "name");
		action.put("reason", "whynot");
		action.put("minutes", 1);

		CommandCenter.execute(pl, action);
		assertEquals("You have jailed name for 1 minutes. Reason: whynot.\r\n" 
				    + "JailKeeper asks for support to ADMIN: hugo jailed name for 1 minutes. Reason: whynot.\r\n"
				    + "Player name is not online, but the arrest warrant has been recorded anyway.", pl.getPrivateText());

		pl.resetPrivateText();

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		action = new RPAction();
		action.put("type", "jail");
		action.put("target", "hugo");
		action.put("reason", "whynot");
		action.put("minutes", "noNumber");

		CommandCenter.execute(pl, action);
		assertEquals("Usage: /jail name minutes reason", pl.getPrivateText());
		pl.resetPrivateText();

		action = new RPAction();
		action.put("type", "jail");
		action.put("target", "hugo");
		action.put("reason", "whynot");
		action.put("minutes", 1);

		// We have to use a mock player object here to avoid conflicts because
		// otherwise the teleporting resets the stored message text events and
		// getPrivateText() returns null instead.
		assertTrue(CommandCenter.execute(pl, action));
		assertTrue(pl.getPrivateText().startsWith("You have jailed hugo for 1 minutes. Reason: whynot."));
	}

	@Test
	public final void testGag() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "gag");

		CommandCenter.execute(pl, action);

		assertEquals("Usage: /gag name minutes reason", pl.getPrivateText());
		pl.resetPrivateText();
		action = new RPAction();
		action.put("type", "gag");
		action.put("target", "name");
		action.put("reason", "whynot");
		action.put("minutes", 1);

		CommandCenter.execute(pl, action);
		assertEquals("Player name not found", pl.getPrivateText());

		pl.resetPrivateText();

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		action = new RPAction();
		action.put("type", "gag");
		action.put("target", "hugo");
		action.put("reason", "whynot");
		action.put("minutes", "noNumber");

		CommandCenter.execute(pl, action);
		assertEquals("Usage: /gag name minutes reason", pl.getPrivateText());
		pl.resetPrivateText();

		action = new RPAction();
		action.put("type", "gag");
		action.put("target", "hugo");
		action.put("reason", "whynot");
		action.put("minutes", 1);

		CommandCenter.execute(pl, action);
		assertTrue(pl.getPrivateText().startsWith(
				"You have gagged hugo for 1 minutes. Reason: "));
	}

	@Test
	public final void testOnDestroyEntityNotFOund() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		pl.put("adminlevel", 5000);
		RPAction action = new RPAction();
		action.put("type", "destroy");

		CommandCenter.execute(pl, action);
		assertEquals("Entity not found", pl.getPrivateText());
	}

	@Test
	public final void testOnDestroyPlayer() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		pl.put("adminlevel", 5000);
		pl.resetPrivateText();

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		RPAction action = new RPAction();
		action.put("type", "destroy");
		action.put("target", "hugo");

		CommandCenter.execute(pl, action);
		assertEquals("You can't remove players", pl.getPrivateText());
	}

	@Test
	public final void testOnDestroyNPC() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		SpeakerNPC npc = SpeakerNPCTestHelper.createSpeakerNPC("npcTest");
		StendhalRPZone testzone = new StendhalRPZone("Testzone");
		testzone.add(npc);
		testzone.add(pl);

		assertEquals(1, npc.getID().getObjectID());
		pl.put("adminlevel", 5000);
		pl.resetPrivateText();

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		RPAction action = new RPAction();
		action.put("type", "destroy");
		action.put("target", "#1");

		CommandCenter.execute(pl, action);
		assertEquals("You can't remove SpeakerNPCs", pl.getPrivateText());
	}

	@Test
	public final void testOnDestroyRat() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		Creature rat = new RaidCreature(StendhalRPWorld.get().getRuleManager()
				.getEntityManager().getCreature("rat"));
		StendhalRPZone testzone = new StendhalRPZone("Testzone");
		testzone.add(rat);
		testzone.add(pl);

		assertEquals(1, rat.getID().getObjectID());
		pl.put("adminlevel", 5000);
		pl.resetPrivateText();

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		RPAction action = new RPAction();
		action.put("type", "destroy");
		action.put("target", "#1");

		CommandCenter.execute(pl, action);
		assertEquals("Removed entity null", pl.getPrivateText());
	}

	@Test
	public final void testOnDestroyRatWithTargetID() {

		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		Creature rat = new RaidCreature(StendhalRPWorld.get().getRuleManager()
				.getEntityManager().getCreature("rat"));
		StendhalRPZone testzone = new StendhalRPZone("Testzone");
		testzone.add(rat);
		testzone.add(pl);

		assertEquals(1, rat.getID().getObjectID());
		pl.put("adminlevel", 5000);
		pl.resetPrivateText();

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		RPAction action = new RPAction();
		action.put("type", "destroy");
		action.put("targetid", 1);

		CommandCenter.execute(pl, action);
		assertEquals("Removed entity 1", pl.getPrivateText());
	}

	@Test
	public final void testOnInspectRatWithTargetID() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		Creature rat = new RaidCreature(StendhalRPWorld.get().getRuleManager()
				.getEntityManager().getCreature("rat"));
		StendhalRPZone testzone = new StendhalRPZone("Testzone");
		testzone.add(rat);
		testzone.add(pl);

		assertEquals(1, rat.getID().getObjectID());
		pl.put("adminlevel", 5000);
		pl.resetPrivateText();

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		RPAction action = new RPAction();
		action.put("type", "inspect");
		action.put("targetid", 1);

		CommandCenter.execute(pl, action);
		assertTrue(pl
				.getPrivateText()
				.startsWith(
						"Inspected creature is called \"rat\" and has the following attributes:"));
	}

	@Test
	public final void testOnSummonAt() {
		TestPlayer pl = PlayerTestHelper.createTestPlayer("hugo");
		pl.put("adminlevel", 5000);
		pl.resetPrivateText();

		MockStendhalRPRuleProcessor.get().addPlayer(pl);
		StendhalRPZone testzone = new StendhalRPZone("Testzone");
		testzone.add(pl);

		RPAction action = new RPAction();
		action.put("type", "summonat");
		action.put("target", "hugo");
		action.put("slot", "hugo");
		action.put("item", "hugo");

		CommandCenter.execute(pl, action);
		assertEquals("Player \"hugo\" does not have an RPSlot named \"hugo\".",
				pl.getPrivateText());
		pl.resetPrivateText();

		action = new RPAction();
		action.put("type", "summonat");
		action.put("target", "hugo");
		action.put("slot", "bag");
		action.put("item", "hugo");

		CommandCenter.execute(pl, action);
		assertEquals("Not an item.", pl.getPrivateText());
		pl.resetPrivateText();

		action = new RPAction();
		action.put("type", "summonat");
		action.put("target", "hugo");
		action.put("slot", "bag");
		action.put("item", "dagger");
		assertFalse(pl.isEquipped("dagger"));
		CommandCenter.execute(pl, action);
		if (pl.getPrivateText() != null) {
			// print error message in JUnit log
			assertEquals("", pl.getPrivateText());
		}
		assertTrue(pl.isEquipped("dagger"));
		pl.resetPrivateText();

		action = new RPAction();
		action.put("type", "summonat");
		action.put("target", "noone");
		action.put("slot", "bag");
		action.put("item", "dagger");

		CommandCenter.execute(pl, action);
		assertEquals("Player \"noone\" not found.", pl.getPrivateText());
		pl.resetPrivateText();
	}
}
