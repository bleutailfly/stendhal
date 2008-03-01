package games.stendhal.client.actions;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import games.stendhal.client.MockClientUI;
import games.stendhal.client.MockStendhalClient;
import games.stendhal.client.entity.User;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;

import org.junit.Test;

/**
 * Test the DropAction class.
 *
 * @author Martin Fuchs
 */
public class DropActionTest {
	private static final String ZONE_NAME = "Testzone";
	private static final int USER_ID = 1001;
	private static final int MONEY_ID = 1234;
	private static final int SILVER_SWORD_ID = 1235;

	/**
	 * Create and initialize a User object.
	 */
	private static RPObject createPlayer() {
		RPObject rpo = new RPObject();

		rpo.put("type", "player");
		rpo.put("name", "player");
		rpo.setID(new ID(USER_ID, ZONE_NAME));

		User pl = new User();
		pl.initialize(rpo);

		for(String slotName : DropAction.CARRYING_SLOTS) {
			rpo.addSlot(slotName);
		}

		return rpo;
	}

	private static RPObject createItem(String itemName, int id, int amount) {
		RPObject rpo = new RPObject();
		rpo.put("type", "item");
		rpo.put("name", itemName);
		rpo.put("quantity", amount);
		rpo.setID(new ID(id, ZONE_NAME));

		return rpo;
	}

	@Test
	public void testNoMoney() {
		MockClientUI clientUI = new MockClientUI();
		DropAction action = new DropAction();

		createPlayer();

		// issue "/drop money"
		assertTrue(action.execute(new String[]{"money"}, ""));
		assertEquals("You don't have any money", clientUI.getEventBuffer());
	}

	@Test
	public void testInvalidAmount() {
		MockClientUI clientUI = new MockClientUI();
		DropAction action = new DropAction();

		createPlayer();

		// issue "/drop 85x money"
		assertTrue(action.execute(new String[]{"85x"}, "money"));
		assertEquals("Invalid quantity", clientUI.getEventBuffer());
	}

	@Test
	public void testDropSingle() {
		// create client UI
		MockClientUI clientUI = new MockClientUI();

		// create client
		new MockStendhalClient("") {
			@Override
			public void send(RPAction action) {
				client = null;
				assertEquals("drop", action.get("type"));
				assertEquals(USER_ID, action.getInt("baseobject"));
				assertEquals(0, action.getInt("x"));
				assertEquals(0, action.getInt("y"));
				assertEquals("bag", action.get("baseslot"));
				assertEquals(1, action.getInt("quantity"));
				assertEquals(MONEY_ID, action.getInt("baseitem"));
			}
		};

		// create a player and give him some money
		RPObject player = createPlayer();
		player.getSlot("bag").addPreservingId(createItem("money", MONEY_ID, 100));

		// issue "/drop money"
		DropAction action = new DropAction();
		assertTrue(action.execute(new String[]{"money"}, ""));
		assertEquals("", clientUI.getEventBuffer());
	}

	@Test
	public void testDropMultiple() {
		// create client UI
		MockClientUI clientUI = new MockClientUI();

		// create client
		new MockStendhalClient("") {
			@Override
			public void send(RPAction action) {
				client = null;
				assertEquals("drop", action.get("type"));
				assertEquals(USER_ID, action.getInt("baseobject"));
				assertEquals(0, action.getInt("x"));
				assertEquals(0, action.getInt("y"));
				assertEquals("bag", action.get("baseslot"));
				assertEquals(50, action.getInt("quantity"));
				assertEquals(MONEY_ID, action.getInt("baseitem"));
			}
		};

		// create a player and give him some money
		RPObject player = createPlayer();
		player.getSlot("bag").addPreservingId(createItem("money", MONEY_ID, 100));

		// issue "/drop 50 money"
		DropAction action = new DropAction();
		assertTrue(action.execute(new String[]{"50"}, "money"));
		assertEquals("", clientUI.getEventBuffer());
	}

	@Test
	public void testSpaceHandling() {
		// create client UI
		MockClientUI clientUI = new MockClientUI();

		// create client
		new MockStendhalClient("") {
			@Override
			public void send(RPAction action) {
				client = null;
				assertEquals("drop", action.get("type"));
				assertEquals(USER_ID, action.getInt("baseobject"));
				assertEquals(0, action.getInt("x"));
				assertEquals(0, action.getInt("y"));
				assertEquals("bag", action.get("baseslot"));
				assertEquals(1, action.getInt("quantity"));
				assertEquals(SILVER_SWORD_ID, action.getInt("baseitem"));
			}
		};

		// create a player and give him some money
		RPObject player = createPlayer();
		player.getSlot("bag").addPreservingId(createItem("silver sword", SILVER_SWORD_ID, 1));

		// issue "/drop money"
		DropAction action = new DropAction();
		assertTrue(action.execute(new String[]{"silver"}, "sword"));
		assertEquals("", clientUI.getEventBuffer());
	}

	@Test
	public void testGetMaximumParameters() {
		DropAction action = new DropAction();
		assertThat(action.getMaximumParameters(), is(1));
	}

	@Test
	public void testGetMinimumParameters() {
		DropAction action = new DropAction();
		assertThat(action.getMinimumParameters(), is(1));
	}

}
