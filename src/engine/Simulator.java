package engine;

// imports
import java.math.BigInteger;
import java.util.regex.Pattern;

import engine.constants.Function;
import engine.constants.OpCode;
import engine.constants.Registers;
import engine.instructions.Instruction;
import engine.instructions.decoded.*;
import engine.memory.DecodedMemory;

public class Simulator {

	// memory
	DecodedMemory decodedMemory;
	// parser
	Parser parser;
	// register file
	RegisterFile regFile;

	// cycle variable
	int cycle = 0;

	// pc register
	Register pc;

	// Execute phase hardware
	Alu alu = new Alu();
	Mux aluSrc = new Mux();
	Mux regDest = new Mux();

	// constructors
	public Simulator(Parser parser) {
		this.parser = parser;
		this.decodedMemory = new DecodedMemory("200");
		this.pc = new Register("pc", 0);
		regFile = new RegisterFile();
	}

	public Simulator(Parser parser, String baseAddress) {
		this.parser = parser;
		this.decodedMemory = new DecodedMemory(baseAddress);
		this.pc = new Register("pc", Integer.parseInt(baseAddress));
		regFile = new RegisterFile();
	}

	// fetch stage
	public Instruction fetch() {
		Instruction instruction = parser.instructionMemory.read(String
				.valueOf(pc.data));
		pc.incrementPC();
		return instruction;
	}

	// decode stage
	public DecodedInstruction decode(Instruction instruction) {
		String func = instruction.getInstruction()[0];
		if (Pattern.matches("(add|sub|and|jr|nor|or|sll|srl|slt|sltu)", func))
			return RDecode(instruction);
		if (Pattern.matches("(addi|andi|ori|beq|bne|blt|lw|sw)", func))
			return IDecode(instruction);
		if (Pattern.matches("(j|jal)", func))
			return JDecode(instruction);
		return null;
	}

	// decode helpers
	// decode R format
	public RFormat RDecode(Instruction instruction) {
		String opcode = OpCode.RFORMAT;
		String shamt = "00000";
		String function = "";
		boolean jump = false;
		boolean shift = false;
		switch (instruction.getInstruction()[0]) {
		case "add":
			function = Function.ADD;
			break;
		case "sub":
			function = Function.SUB;
			break;
		case "and":
			function = Function.AND;
			break;
		case "jr":
			function = Function.JR;
			jump = true;
			break;
		case "nor":
			function = Function.NOR;
			break;
		case "or":
			function = Function.OR;
			break;
		// shift
		case "sll":
			function = Function.SLL;
			shamt = binarify(instruction.getInstruction()[3], 5);
			shift = true;
			break;
		case "srl":
			function = Function.SRL;
			shamt = binarify(instruction.getInstruction()[3], 5);
			shift = true;
			break;
		// conditional
		case "slt":
			function = Function.SLT;
			break;
		case "sltu":
			function = Function.SLTU;
			break;
		default:
			break;
		}
		int i = (jump) ? 1 : 2;
		int j = (shift) ? 2 : 3;
		String rs = (shift) ? "00000" : regFile.getBinary(instruction
				.getInstruction()[i]);
		String rt = (jump) ? "00000" : regFile.getBinary(instruction
				.getInstruction()[j]);
		String rd = (jump) ? "00000" : regFile.getBinary(instruction
				.getInstruction()[1]);
		String ins = opcode + rs + rt + rd + shamt + function;
		RFormat decoded = new RFormat(ins);
		decodedMemory.write(decoded);
		return decoded;
	}

	// decode I format
	public IFormat IDecode(Instruction instruction) {
		String opcode = "";
		boolean branch = false;
		boolean load_store = false;
		switch (instruction.getInstruction()[0]) {
		case "addi":
			opcode = OpCode.ADDI;
			break;
		case "andi":
			opcode = OpCode.ANDI;
			break;
		case "ori":
			opcode = OpCode.ORI;
			break;
		// branching
		case "beq":
			opcode = OpCode.BEQ;
			branch = true;
			break;
		case "bne":
			opcode = OpCode.BNE;
			branch = true;
			break;
		case "blt":
			opcode = OpCode.BLT;
			branch = true;
			break;
		case "lw":
			opcode = OpCode.LW;
			load_store = true;
			break;
		case "sw":
			opcode = OpCode.SW;
			load_store = true;
			break;
		default:
			break;
		}
		String rs = "";
		String rt = "";
		String immediate = "";
		if (load_store) {
			String[] tem = new String[2];
			String x = instruction.getInstruction()[2].replace("(", " ");
			x = x.replace(")", "");
			tem = x.split(" ");
			rs = regFile.getBinary(tem[1]);
			rt = regFile.getBinary(instruction.getInstruction()[1]);
			immediate = binarify(tem[0], 16);
		} else {
			rs = regFile.getBinary(instruction.getInstruction()[2]);
			rt = regFile.getBinary(instruction.getInstruction()[1]);
			if (branch) {
				String branchAddress = parser.labels.get(instruction
						.getInstruction()[3]);
				immediate = binarify(branchAddress, 16);
			} else {
				// calculate branch address
				immediate = binarify(instruction.getInstruction()[3], 16);
			}
		}
		String ins = opcode + rs + rt + immediate;
		IFormat decoded = new IFormat(ins);
		decodedMemory.write(decoded);
		return decoded;
	}

	// decode J format
	public JFormat JDecode(Instruction instruction) {
		String opCode = (instruction.getInstruction()[0].equals("j")) ? OpCode.J
				: OpCode.JAL;
		// calculate the address of the label
		String branch = parser.labels.get(instruction.getInstruction()[1]);
		branch = binarify(branch, 26);
		String ins = opCode + branch;
		JFormat decoded = new JFormat(ins);
		return decoded;
	}

	// execute stage
	public void execute(DecodedInstruction decIns, Instruction ins) {
		// Initialize temporary variables
		String rd = "";
		String jAddress = "";
		Integer rs;
		Integer aluOut = 0;
		SetregDstMux(decIns);
		SetaluSrcMux(decIns);
		boolean branch = false;
		boolean mem = false;
		if (decIns instanceof IFormat) {
			aluSrc.setSelector(false);
			String opcode = ((IFormat) decIns).getOpcode();
			rd = ((IFormat) decIns).getRd();
			switch (opcode) {
			case OpCode.ADDI:
				rs = regFile.registershash.get(((IFormat) decIns).getRs())
						.getData();
				aluOut = alu.add(rs, Integer.parseInt(ins.getInstruction()[3]));
				break;
			case OpCode.LW:
				rs = regFile.registershash.get(((IFormat) decIns).getRs())
						.getData();
				aluOut = alu.address(rs,
						Integer.parseInt(((IFormat) decIns).getImmediate(), 2));
				mem = true;
				rd = rd + OpCode.LW;
				break;
			case OpCode.SW:
				rs = regFile.registershash.get(((IFormat) decIns).getRs())
						.getData();
				aluOut = alu.address(rs,
						Integer.parseInt(((IFormat) decIns).getImmediate(), 2));
				mem = true;
				rd = rd + OpCode.SW;
				break;
			case OpCode.ANDI:
				rs = regFile.registershash.get(((IFormat) decIns).getRs())
						.getData();
				aluOut = alu.and(rs, Integer.parseInt(ins.getInstruction()[3]));
				break;
			case OpCode.ORI:
				rs = regFile.registershash.get(((IFormat) decIns).getRs())
						.getData();
				aluOut = alu.or(rs, Integer.parseInt(ins.getInstruction()[3]));
				break;
			case OpCode.BEQ:
				mem = true;
				rs = regFile.registershash.get(((IFormat) decIns).getRs())
						.getData();
				rd = regFile.registershash.get(((IFormat) decIns).getRd())
						.getData().toString();
				if (rs.equals(Integer.parseInt(rd))) {
					jAddress = toInteger(extend(((IFormat) decIns)
							.getImmediate()));
					aluOut = Integer.parseInt(jAddress);
					branch = true;
				}
				break;
			case OpCode.BNE:
				mem = true;
				rs = regFile.registershash.get(((IFormat) decIns).getRs())
						.getData();
				rd = regFile.registershash.get(((IFormat) decIns).getRd())
						.getData().toString();
				if (!(rs.equals(Integer.parseInt(rd)))) {
					jAddress = toInteger(extend(((IFormat) decIns)
							.getImmediate()));
					aluOut = Integer.parseInt(jAddress);
					branch = true;
				}
				break;
			case OpCode.BLT:
				mem = true;
				rs = regFile.registershash.get(((IFormat) decIns).getRs())
						.getData();
				rd = regFile.registershash.get(((IFormat) decIns).getRd())
						.getData().toString();
				if (rs > Integer.parseInt(rd)) {
					jAddress = toInteger(extend(((IFormat) decIns)
							.getImmediate()));
					aluOut = Integer.parseInt(jAddress);
					branch = true;
				}
				break;
			default:
				break;
			}

		} else if (decIns instanceof RFormat) {
			Integer rt;
			aluSrc.setSelector(true);
			Byte shamt = Byte.parseByte(((RFormat) decIns).getShamt(), 2);
			String funct = ((RFormat) decIns).getFunct();
			rs = regFile.registershash.get(((RFormat) decIns).getRs())
					.getData();
			rt = regFile.registershash.get(((RFormat) decIns).getRt())
					.getData();
			rd = ((RFormat) decIns).getRd();
			switch (funct) {
			case Function.ADD:
				aluOut = alu.add(rs, rt);
				break;
			case Function.AND:
				aluOut = alu.and(rs, rt);
				break;
			case Function.SUB:
				aluOut = alu.sub(rs, rt);
				break;
			case Function.OR:
				aluOut = alu.or(rs, rt);
				break;
			case Function.SLL:
				aluOut = alu.sll(rt, shamt);
				break;
			case Function.SRL:
				aluOut = alu.srl(rt, shamt);
				break;
			case Function.JR:
				mem = true;
				aluOut = regFile.registershash.get(((RFormat) decIns).getRs())
						.getData();
				break;
			case Function.SLT:
				aluOut = alu.slt(rs, rt);
				break;
			case Function.SLTU:
				aluOut = alu.sltu(rs, rt);
				break;
			case Function.NOR:
				aluOut = alu.nor(rs, rt);
				break;
			default:
				break;
			}
		} else {
			// handling JFormat registers
			mem = true;
			aluSrc.setSelector(false);
			String opcode = ((JFormat) decIns).getOpcode();
			jAddress = toInteger(extend(((JFormat) decIns).getAddress()));
			aluOut = Integer.parseInt(jAddress);
			if (opcode.equals(OpCode.JAL)) {
				regFile.registershash.get(Registers.$ra).setData(
						pc.decrementPC());
			}
		}
		mem(aluOut, mem, rd, branch);
	}

	// mem stage
	public void mem(Integer aluOut, boolean mem, String rd,boolean branch) {
		if (mem) {
			if (rd.length() == 11) {
				if (rd.substring(5, 11).equals(OpCode.LW)) {
					// load work instruction
					writeBack(Integer.parseInt(parser.dataMemory.read(aluOut
							.toString())), rd.substring(0, 5));
				} else {
					// save word instruction
					String data = regFile.registershash.get(rd.substring(0, 5))
							.getData().toString();
					parser.dataMemory.write(aluOut.toString(),
							binarify(data, 32));
				}
			} else {
				// jump register or label
				if (branch)
					pc.setData(aluOut);
			}
		} else {
			// R-Format
			writeBack(aluOut, rd);
		}
	}

	// writeBack stage
	public void writeBack(Integer aluOut, String rd) {
		regFile.registershash.get(rd).setData(aluOut);
	}

	// helper methods

	// Set the aluSrc Mux
	public void SetaluSrcMux(DecodedInstruction decIns) {
		aluSrc.wire0 = regFile.registershash.get(
				decIns.getInstruction().substring(11, 16)).getData();
		aluSrc.wire1 = "0000000000000000"
				+ decIns.getInstruction().substring(0, 16);
		if (decIns instanceof RFormat) {
			aluSrc.selector = false;
		} else if (decIns instanceof IFormat) {
			String opcode = ((IFormat) decIns).getOpcode();
			if (opcode.equals(OpCode.BEQ) || opcode.equals(OpCode.BNE))
				aluSrc.selector = false;
			if (opcode.equals(OpCode.LW) || opcode.equals(OpCode.SW))
				aluSrc.selector = true;
		}
	}

	// Set the regDst Mux
	public void SetregDstMux(DecodedInstruction decIns) {
		regDest.wire0 = decIns.getInstruction().substring(11, 15);
		regDest.wire1 = decIns.getInstruction().substring(16, 20);
		if (decIns instanceof RFormat) {
			regDest.selector = true;
		} else if (decIns instanceof IFormat) {
			String opcode = ((IFormat) decIns).getOpcode();
			if (opcode.equals(OpCode.LW))
				regDest.selector = false;
		}
	}

	// get binary string of requested size
	public static String binarify(String decimal, int size) {
		String binary = Integer.toBinaryString(Integer.parseInt(decimal));
		if (binary.length() > size) {
			binary = new StringBuilder(binary).reverse().toString();
			binary = binary.substring(0, size);
			return new StringBuilder(binary).reverse().toString();
		}
		while (binary.length() < size)
			binary = "0" + binary;
		return binary;
	}

	// convert 32 bit to signed integer
	public static String toInteger(String _32_binary) {
		return String.valueOf(new BigInteger(_32_binary, 2).intValue());
	}

	// extend binary value to 32
	public static String extend(String binary) {
		while (binary.length() < 32) {
			binary = '0' + binary;
		}
		return binary;
	}

	public static void main(String[] args) {
		String[] test1 = { ".data", "varx: .word 23", "vary: .word 565",
				".text", "lop:", "addi $t0, $t0 ,-200", "beq $0 , $t0 , lop",
				"addi $t0, $t0 ,-100", "beq $0 , $0 , lop" };

		Parser p = new Parser(test1);
		Simulator s = new Simulator(p, "200");
		if (p.parse()) {
			// System.out.println(p.variables.toString());
			// System.out.println(p.instructionMemory.getMemory().toString());
			// System.out.println(s.parser.dataMemory.getMemory().toString());
			Instruction n;
			DecodedInstruction d;
			for (int i = 0; i < 9; i++) {
				n = s.fetch();
				System.out.println( i + " "+ n.toString());
				d = s.decode(n);
				s.execute(d, n);
			}
			System.out
					.println("-------------------RegFile----------------------");
			System.out.println(s.regFile.toString());
			System.out.println("-----------------------------------------");
			// print datamemory
			// System.out.println(s.parser.dataMemory.getMemory().toString());
		}

	}
}
