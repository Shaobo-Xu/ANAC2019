package group1;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiasedSelectionSet<T> {
	private List<T> elements;
	private List<Double> probs;
	private List<Double> cumProbs;
	
	public BiasedSelectionSet(List<T> elements, List<Double> probs) {
		this.elements = elements;
		this.probs = probs;
		
		normaliseProbs();
		
		cumProbs = new ArrayList<>();
		double sum = 0;
		for (int i = 0; i < probs.size(); i++) {
			sum += probs.get(i);
			cumProbs.add(sum);
		}
	}
	
	public T randomSelect() {
		Random random = new Random();
		double r = random.nextDouble();
		for (int i = 0; i < cumProbs.size(); i++) {
			if (r < cumProbs.get(i)) {
				return elements.get(i);
			}
		}
		
		return elements.get(0); // Shouldn't occur!
	}
	
	private void normaliseProbs() {
		double sum = 0;
		for (Double prob : probs) { 
			sum += prob;
		}
		
		if (sum == 1.0) {
			return;
		} else if (sum == 0.0) {
			for (int i = 0; i < probs.size(); i++) {
				probs.set(i, 1.0/probs.size());
			}
			sum = 1.0;
		}
		
		for (int i = 0; i < probs.size(); i++) {
			probs.set(i, probs.get(i)/sum);
		}
	}
}
