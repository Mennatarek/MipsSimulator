package engine.instructions.decoded;

public class IFormat extends DecodedInstruction{
	String opcode;
	String rs;
	String rd;
	String immediate;
	
	public IFormat(String instruction){
		super(instruction);
		this.opcode=instruction.substring(0, 6);
		this.rs=instruction.substring(6,11);
		this.rd=instruction.substring(11, 16);
		this.immediate=instruction.substring(16,32);
	}

	public String getOpcode() {
		return opcode;
	}

	public String getRs() {
		return rs;
	}

	public String getRd() {
		return rd;
	}

	public String getImmediate() {
		return immediate;
	}
	
	public String toString(){
		return opcode + rs + rd + immediate;
	}
}