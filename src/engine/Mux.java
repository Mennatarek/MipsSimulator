package engine;

public class Mux {
	
	// instance variables
	Object wire0;
	Object wire1;
	
	// false --> wire 0 || true --> wire1
	boolean selector;
	
	//constructors
	
	public Mux(){
		
	}
	
	public Mux(Object wire0,Object wire1,boolean selector) {
		this.wire0 = wire0;
		this.wire1 = wire1;
		this.selector = selector;
	}
	
	public Object output(){
		return (selector)?wire0:wire1;
	}
	
	public void setSelector(boolean selector) {
		this.selector = selector;
	}
}
