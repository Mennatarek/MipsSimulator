package engine.constants;

public interface OpCode {
	
	public static final String RFORMAT = "000000";
	public static final String ADDI = "001000";
	public static final String LW = "100011";
	public static final String SW = "101011";
	public static final String ANDI = "001100";
	public static final String ORI = "001101";
	public static final String BEQ = "000100";
	public static final String BNE = "000101";
	public static final String BLT = "000111";
	public static final String J = "000010";
	public static final String JAL = "000011";
}
