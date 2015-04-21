package engine.instructions.decoded;

public class RFormat extends DecodedInstruction {
	String opcode;
	String rs;
	String rd;
	String rt;
	String shamt;
	String funct;
	
	public RFormat(){
		
	}
	
	public RFormat(String instruction) {
		super(instruction);
		this.opcode = instruction.substring(0, 6);
		this.rs = instruction.substring(6, 11);
		this.rt = instruction.substring(11, 16);
		this.rd = instruction.substring(16, 21);
		this.shamt = instruction.substring(21, 26);
		this.funct = instruction.substring(26, 32);
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

	public String getRt() {
		return rt;
	}

	public String getShamt() {
		return shamt;
	}

	public String getFunct() {
		return funct;
	}
	
	public String toString(){
		return opcode + rs + rt + rd + shamt + funct;
	}
}