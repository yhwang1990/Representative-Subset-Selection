package bean;

public class BufferedUserVisit {
	public UserVisit visit;
	public double cost_effectiveness;
	
	public BufferedUserVisit(UserVisit visit, double cost_effectiveness) {
		this.visit = visit;
		this.cost_effectiveness = cost_effectiveness;
	}

}
