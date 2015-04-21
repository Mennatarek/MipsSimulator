package engine.memory;

import java.util.Hashtable;

import engine.instructions.Instruction;

public class InstructionMemory {
	// instruction container
	private Hashtable<String, Instruction> memory;
	private String pc;
	String baseAddress;

	public InstructionMemory(String baseAddress) {
		memory = new Hashtable<String, Instruction>();
		this.baseAddress = baseAddress;
		this.pc = baseAddress;
	}

	public void write(Instruction instruction) {
		memory.put(this.pc, instruction);
		pc = String.valueOf(Integer.parseInt(pc) + 4);
	}

	public Instruction read(String pc) {
		return memory.get(pc);
	}

	public Hashtable<String, Instruction> getMemory() {
		return memory;
	}

	public String toString() {
		String data = "";
		for (int i = 0; i < memory.size(); i++) {
			data += memory.get(i).toString();
		}
		return data;
	}

	public String getPc() {
		return pc;
	}
}
