package engine.instructions.decoded;

public class DecodedInstruction {
	int address;
	String instruction;
	
	public DecodedInstruction(String instruction){
		address = 0;
		this.instruction = instruction;
	}
	
	public DecodedInstruction(int address){
		this.address = address;
	}
	
	public DecodedInstruction(){
	}
	
	public String getInstruction(){
		return instruction;
	}
	
}