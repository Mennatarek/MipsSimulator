package engine.instructions;

public class Instruction {
	String[] instruction;

	public Instruction() {
	}

	public Instruction(String[] line) {
		instruction = new String[line.length];
		create(line);
	}

	public void create(String[] line) {
		for (int i = 0; i < line.length; i++) {
			instruction[i] = line[i];
		}
	}

	public String toString() {
		String ins = "| ";
		for (int i = 0; i < instruction.length; i++) {
			ins += instruction[i];
		}
		ins += "|\n";
		return ins;
	}
	
	public String[] getInstruction(){
		return instruction;
	}
	
	
}