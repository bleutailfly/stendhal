package games.stendhal.client.entity;

public class CarrotGrower extends GrainField {
	/**
	 * Create a carrot/vegatable grower.
	 */
	public CarrotGrower()  {
		super(1, 1);
	}

	@Override
	public ActionType defaultAction() {
		return ActionType.PICK;
	}
}
