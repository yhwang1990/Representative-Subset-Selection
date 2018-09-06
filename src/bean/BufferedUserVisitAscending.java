package bean;

import java.util.Comparator;

public class BufferedUserVisitAscending implements Comparator<BufferedUserVisit> {
	
	public int compare(BufferedUserVisit v1, BufferedUserVisit v2) {
		if (v1.cost_effectiveness - v2.cost_effectiveness < -1.0e-7)
			return -1;
		else if (v1.cost_effectiveness - v2.cost_effectiveness > 1.0e-7)
			return 1;
		else
			return (v1.visit.id - v2.visit.id);
	}

}
