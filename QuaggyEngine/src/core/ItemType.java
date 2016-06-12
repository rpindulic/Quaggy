package quaggy;

/** Enum for representing the different types of items
 *  in the game. Names must match up exactly with the API
 *  terminology, allowing for spaces to be removed.
 * @author Ryan
 */
public enum ItemType {
	Armor, Back, Bag, Consumable, Container, CraftingMaterial, 
	Gizmo, Mini, Trinket, Trophy, UpgradeComponent, Weapon;

	/** Pulls spaces from an input string to generate the 
	 * correct enum type for that string. Can provide database
	 * TYPE value as input and get the appropriate enum value.
	 */
	public static ItemType fromString(String input) {
		input = input.replace(" ", "");
		return ItemType.valueOf(input);
	}
}
