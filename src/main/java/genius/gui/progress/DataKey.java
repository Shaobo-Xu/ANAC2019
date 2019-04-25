package genius.gui.progress;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import genius.core.events.SessionEndedNormallyEvent;

/**
 * These keys are datatypes for data , eg in {@link HashMap}s. See e.g.
 * {@link SessionEndedNormallyEvent#getValues()}.
 * 
 * The keys also have a human-readable text version. This can be used as basis
 * for the column text strings for data display and logging.
 * 
 * The keys {@link DataKey#DISCOUNTED_UTILS}, {@link DataKey#UTILS},
 * {@link DataKey#FILES} and {@link DataKey#AGENTS} contain lists. The function
 * {@link SessionEndedNormallyEvent#getValues()} will convert these to strings
 * and extend these keys with the agent number, and we then have eg 3 columns
 * "Utility 1", "Utility 2" and "Utility 3" in the table. In the map, the UTILS
 * field will be an {@link List} with 3 values in that case. see also
 * {@link SessionEndedNormallyEvent#getValues()}.
 * 
 * The {@link DataKeyTableModel} can handle such data directly.
 * 
 * Note that the order of the enum is irrelevant; the order in which the keys
 * are provided to the {@link DataKeyTableModel} determines the column order.
 * 
 * @author W.Pasman 16jul15
 *
 */
public enum DataKey implements Comparable<DataKey> {
	RUNTIME("Run time (s)"), ROUND("Round"), EXCEPTION("Exception"), DEADLINE("deadline"), IS_AGREEMENT(
			"Agreement"), IS_DISCOUNT("Discounted"), NUM_AGREE("#agreeing"), MINUTIL(
					"min.util."), MAXUTIL("max.util."), DIST_PARETO(
							"Dist. to Pareto"), DIST_NASH("Dist. to Nash"), SOCIAL_WELFARE("Social Welfare"),
	/**
	 * List of the agent class names
	 */
	AGENTS("Agent"),
	/**
	 * List of Undiscounted utilities for all agents (in proper order)
	 */
	UTILS("Utility"),
	/**
	 * List with the discounted utilities for all agents (in proper order)
	 */
	DISCOUNTED_UTILS("Disc. Util."),
	/**
	 * List with the perceived utilities in case of uncertainty (in proper order)
	 */
	PERCEIVED_UTILS("Perceived. Util."),

	FILES("Profile");

	String name;

	DataKey(String n) {
		name = n;
	}

	/**
	 * 
	 * @return human-readable short description of this column
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return n human-readable short descriptions of this column. The
	 *         descriptions are just the name, but with an added number 1..n
	 */
	public List<String> getNames(int num) {
		List<String> names = new ArrayList<String>();
		for (int n = 1; n <= num; n++) {
			names.add(name + " " + n);
		}
		return names;
	}

};
