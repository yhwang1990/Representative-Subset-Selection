package bean;

import java.util.Comparator;

public class BufferedSetAscending implements Comparator<BufferedSet> {
	
	public int compare(BufferedSet s1, BufferedSet s2) {
		if (s1.cost_effectiveness - s2.cost_effectiveness < -1.0e-7)
			return -1;
		else if (s1.cost_effectiveness - s2.cost_effectiveness > 1.0e-7)
			return 1;
		else
			return (s1.set.id - s2.set.id);
	}

}
