package engine;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.regex.Pattern;

import engine.instructions.Instruction;
import engine.memory.*;

public class Parser {
	String[] code;

	// memory for instruction
	InstructionMemory instructionMemory;

	// memory for data
	DataMemory dataMemory;

	// <labels, address of located instruction>
	Hashtable<String, String> labels;

	// Arraylist of all labels that want to jump to
	ArrayList<String> jump;

	// boolean for data or text
	private boolean data = false;

	// RAM <variable, content>
	Hashtable<String, String> variables;

	// Regular expressions
	// Separator
	private static final String separator = "[\\s]*,[\\s]*";
	// label
	private static final String label = "[\\s]*[a-zA-Z0-9]*:[\\s]*";
	// register formats
	private static final String regformat0 = "[$]((a[0-3])|(t[0-9])|(s[0-7])|(v[0-1])|(k[0-1]))";
	private static final String regformat1 = "[$](sp|gp|fp|ra|at)";
	private static final String regformat2 = "[$][0]";
	private static final String immediate = "[-]{0,1}?[0-9]*";
	private static final String word = "[a-zA-Z0-9]*";
	private static final String regformat = "(" + regformat0 + "|" + regformat1
			+ "|" + regformat2 + ")";
	private static final String memAccess = "([0-9])*\\([\\s]*" + regformat
			+ "[\\s]*\\)";
	// functions
	private static final String arithmeticR = "[\\s]*(add|sub|slt|sltu|and|or|nor)[\\s]+";
	private static final String arithmeticI = "[\\s]*(addi|andi|ori|sll|srl)[\\s]+";
	private static final String loadStore = "[\\s]*(lw|sw)[\\s]+";
	private static final String branch = "[\\s]*(beq|bne|blt)[\\s]+";
	private static final String jumpLabel = "[\\s]*(j|jal)[\\s]+";
	private static final String jumpReg = "[\\s]*(jr)[\\s]+";
	private static final String move = "[\\s]*(move)[\\s]*";
	// body
	private static final String bodyI = regformat + separator + regformat
			+ separator + immediate + "[\\s]*";
	private static final String bodyR = regformat + separator + regformat
			+ separator + regformat + "[\\s]*";
	private static final String bodyLoadStore = regformat + separator
			+ memAccess + "[\\s]*";
	private static final String bodyBranch = regformat + separator + regformat
			+ separator + word + "[\\s]*";
	private static final String bodyJumpLabel = "[\\s]*" + word + "[\\s]*";
	private static final String bodyJumpReg = "[\\s]*" + regformat + "[\\s]*";
	private static final String ins = "(" + arithmeticR + bodyR + "|"
			+ arithmeticI + bodyI + "|" + loadStore + bodyLoadStore + "|"
			+ branch + bodyBranch + "|" + jumpReg + bodyJumpReg + "|"
			+ jumpLabel + bodyJumpLabel + ")";
	private static final String DATA = "[\\s]*(.data)[\\s]*";
	private static final String WORD = label + "(.word)[\\s]*" + immediate
			+ "[\\s]*";
	private static final String TEXT = "[\\s]*(.text)[\\s]*";
	private static final String bodyMov = regformat + separator + regformat
			+ "[\\s]*";

	// constructors
	public Parser(String[] code) {
		this.code = code;
		this.instructionMemory = new InstructionMemory("200");
		this.labels = new Hashtable<String, String>();
		this.jump = new ArrayList<String>();
		this.dataMemory = new DataMemory("200");
		this.variables = new Hashtable<String, String>();
	}

	public Parser(String[] code, String baseAddress) {
		this.code = code;
		this.instructionMemory = new InstructionMemory(baseAddress);
		this.labels = new Hashtable<String, String>();
		this.jump = new ArrayList<String>();
		this.dataMemory = new DataMemory(baseAddress);
		this.variables = new Hashtable<String, String>();
	}

	/*
	 * Parse the code and check it for syntax returns true if assembled
	 * successfully returns false
	 */
	public boolean parse() {
		// iterate over the code lines
		for (int i = 0; i < code.length; i++) {
			String line = code[i];

			// Check syntax of line
			if (!syntax(line)) {
				System.out
						.println("Assemble: operation completed with errors :: in line ( \""
								+ line + " \") :: " + (int) (i + 1));
				return false;
			}

		}

		// check labels existence
		for (int i = 0; i < jump.size(); i++) {
			if (labels.get(jump.get(i)) == null) {
				System.out.println("no label " + jump.get(i)
						+ " to jump/branch to");
				return false;
			}
		}

		System.out.println("Assemble: operation completed successfully.");
		return true;
	}

	// validate syntax of instruction
	public boolean syntax(String line) {
		if (!data) {
			line = line.toLowerCase();
			// arithmetic R
			if (Pattern.matches(arithmeticR + bodyR, line)) {
				return process(line);
			}

			// arithmetic I
			if (Pattern.matches(arithmeticI + bodyI, line)) {
				return process(line);
			}

			// load store
			if (Pattern.matches(loadStore + bodyLoadStore, line)) {
				return process(line);
			}

			// branch
			if (Pattern.matches(branch + bodyBranch, line)) {
				String[] tem = splitter(line);
				jump.add(tem[tem.length - 1]);
				instructionMemory.write(new Instruction(tem));
				return true;
			}

			// jump label
			if (Pattern.matches(jumpLabel + bodyJumpLabel, line)) {
				String[] tem = splitter(line);
				jump.add(tem[tem.length - 1]);
				instructionMemory.write(new Instruction(tem));
				return true;
			}

			// jump reg
			if (Pattern.matches(jumpReg + bodyJumpReg, line)) {
				instructionMemory.write(new Instruction(splitter(line)));
				return true;
			}

			// label only
			if (Pattern.matches(label, line)) {
				String[] tem = splitter(line);
				labels.put(tem[0], instructionMemory.getPc());
				return true;
			}

			// label and instruction
			if (Pattern.matches(label + ins, line)) {
				String[] x = line.split(":");
				return syntax(x[0] + ":") && syntax(x[1]);
			}

			// move
			if (Pattern.matches(move + bodyMov, line)) {
				return process(line);
			}
		} else {
			// data instruction
			if (Pattern.matches(WORD, line)) {
				return process(line);
			}
		}

		// .data
		if (Pattern.matches(DATA, line)) {
			data = true;
			return true;
		}

		// .text
		if (Pattern.matches(TEXT, line)) {
			data = false;
			return true;
		}
		return false;
	}

	// check validity and write to instruction memory
	public boolean process(String line) {
		if (!data) {
			String[] tem = splitter(line);
			if (!tem[0].equals("sw") && Pattern.matches(regformat2, tem[1])) {
				System.out.println("Cannot Write to Reg zero !!");
				return false;
			}
			if (Pattern.matches("(addi|andi|ori)", tem[0])
					&& !checkImmediateValidity(tem[3])) {
				System.out.println(tem[3] + " is not 32 bit integer");
				return false;
			}

			if (Pattern.matches("(lw|sw)", tem[0])) {
				String[] temp = new String[2];
				String x = tem[2].replace("(", " ");
				x = x.replace(")", "");
				temp = x.split(" ");
				if (Integer.parseInt(temp[0]) % 4 != 0) {
					System.out
							.println("Fetch address is not aligned on word boundry");
					return false;
				}
			}

			if (Pattern.matches("(sll|srl)", tem[0])) {
				int shamt = Integer.parseInt(tem[3]);
				if (shamt > 31 || shamt < 0) {
					System.out.println(shamt + " : operand is out of range");
					return false;
				}
			}

			instructionMemory.write(new Instruction(tem));
		} else {
			// handling data section
			String[] tem = splitter(line);
			variables.put(tem[0], tem[2]);
		}
		return true;
	}

	// split the instruction into pieces
	public String[] splitter(String line) {
		String[] tem = line.split("(\\s)|(,)|(:)");
		ArrayList<String> temp = new ArrayList<String>();
		for (int i = 0; i < tem.length; i++) {
			if (!tem[i].isEmpty())
				temp.add(tem[i]);
		}
		return temp.toArray(new String[temp.size()]);
	}

	// check the validity of the immediate value
	public static boolean checkImmediateValidity(String decimal) {
		try {
			Integer.toBinaryString(Integer.parseInt(decimal));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
