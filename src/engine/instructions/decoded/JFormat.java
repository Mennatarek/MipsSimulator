package engine.instructions.decoded;

public class JFormat extends DecodedInstruction{
	
	String opcode;
	String address;
	
	public JFormat(String instruction){
		super(instruction);
		this.opcode=instruction.substring(0, 6);
		this.address=instruction.substring(6, 32);
	}
	public String getOpcode() {
		return opcode;
	}
	public String getAddress() {
		return address;
	}
	
	public String toString(){
		return opcode + address;
	}
}