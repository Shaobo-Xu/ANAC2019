package genius.gui.tree;

import javax.swing.JLabel;

import genius.gui.panels.CheckboxPanel;
import genius.gui.panels.SliderPanel;
import genius.gui.panels.VflowPanelWithBorder;

@SuppressWarnings("serial")
public class UncertaintySettingsPanel extends VflowPanelWithBorder {
	public UncertaintySettingsPanel(UncertaintySettingsModel model) {
		super("Uncertainty settings");
		add(new CheckboxPanel("Enable preference uncertainty", model.getIsEnabled()));
		add(new JLabel(
				"(Maximum number of rankings = " + model.getTotalBids() + ")"));
		add(new SliderPanel("Nr. of rankings", model.getComparisons()));
		add(new SliderPanel("Nr. of errors    ", model.getErrors()));
		add(new CheckboxPanel(
				"Fixed seed (for reproducible results)",
				model.getIsFixedSeed()));
		add(new CheckboxPanel(
				"Grant parties access to real utility functions (experimental setup)",
				model.getIsExperimental()));
	}
}
