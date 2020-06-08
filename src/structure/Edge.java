package structure;

public class Edge {
	public int source;//source
	private int target;//target
	private int weight;//weight of this edge
	
	public Edge(int source, int target) {
		this.source = source;
		this.target = target;
	}
	public int getSource(){
		return source;
	}
	
	public int getTarget(){
		return target;
	}
	
	public void setWeight(int weight){
		this.weight = weight;
	}
	
	public int getWeight(){
		return weight;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO �Զ����ɵķ������
		Edge edge = (Edge)obj;
		if(edge.source==this.source&&edge.target==this.target){
			return true;
		}
		else
			return false;
		//return super.equals(obj);
	}
}
	
