package engine;

public class Alu {
	public Integer add(Integer op1, Integer op2) {
		return op1 + op2;
	}

	public Integer sub(Integer op1, Integer op2) {
		return op1 - op2;
	}

	public Integer and(Integer op1, Integer op2) {
		return op1 & op2;
	}

	public Integer or(Integer op1, Integer op2) {
		return op1 | op2;
	}

	public Integer sll(Integer op1, Byte shamt) {
		return op1 << shamt;
	}

	public Integer srl(Integer op1, Byte shamt) {
		return op1 >>> shamt;
	}

	public Integer sll(Integer op1, Integer op2) {
		return op1 << op2;
	}

	public Integer slt(Integer op1, Integer op2) {
		return (op1 < op2) ? 1 : 0;
	}

	public Integer sltu(Integer op1, Integer op2) {
		for (int i = 0;i<32;i++){
		Integer	op1B = (op1 >>> 31);
		Integer op2B = (op2 >>> 31);
		op1 = op1 << 1;
		op2 = op2 << 1;
		if (op1B != op2B){
			if (op1==0)
				return 1;
		}else 
			return 0;
		}
		return (op1 < op2) ? 1 : 0;
	}

	public Integer nor(Integer op1, Integer op2) {
		return ~(op1 | op2);
	}

	public Integer address(Integer op1, Integer op2) {
		return op1 + op2;
	}
}
