package engine.memory;

import java.util.LinkedList;

import engine.instructions.decoded.DecodedInstruction;

public class DecodedMemory {
	
	LinkedList<DecodedInstruction> memory;
	String baseAddress;
	
	public DecodedMemory(String baseString){
		memory = new LinkedList<DecodedInstruction>();
		this.baseAddress = baseString;
	}
	
	public void write(DecodedInstruction instruction){
		memory.add(instruction);
	}
	
	public DecodedInstruction read(String address){
		return memory.get(eqAddress(address));
	}
	
	public int eqAddress(String address){
		int eqAddress = Integer.parseInt(address);
		eqAddress = eqAddress - Integer.parseInt(baseAddress);
		return eqAddress/4;
	}
	
	public String toString(){
		String decodedData = "";
		for (int i = 0; i < memory.size(); i++) {
			decodedData +=" | " + i + " | "+ memory.get(i).toString() +" |\n";
		}
		return decodedData;
	}
}
