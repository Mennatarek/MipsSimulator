package engine;

import java.util.Hashtable;
import java.util.Map;

import engine.constants.Registers;

public class RegisterFile {

	private static final Integer init = new Integer(0);
	Hashtable<String, Register> registershash;

	public RegisterFile() {
		registershash = new Hashtable<String, Register>();
		registershash.put(Registers.$0, new Register("$0", init));
		registershash.put(Registers.$at, new Register("$at", init));
		// V
		registershash.put(Registers.$v0, new Register("$v0", init));
		registershash.put(Registers.$v1, new Register("$v1", init));
		// A
		registershash.put(Registers.$a0, new Register("$a0", init));
		registershash.put(Registers.$a1, new Register("$a1", init));
		registershash.put(Registers.$a2, new Register("$a2", init));
		registershash.put(Registers.$a3, new Register("$a3", init));
		// T
		registershash.put(Registers.$t0, new Register("$t0", init));
		registershash.put(Registers.$t1, new Register("$t1", init));
		registershash.put(Registers.$t2, new Register("$t2", init));
		registershash.put(Registers.$t3, new Register("$t3", init));
		registershash.put(Registers.$t4, new Register("$t4", init));
		registershash.put(Registers.$t5, new Register("$t5", init));
		registershash.put(Registers.$t6, new Register("$t6", init));
		registershash.put(Registers.$t7, new Register("$t7", init));
		registershash.put(Registers.$t8, new Register("$t8", init));
		registershash.put(Registers.$t9, new Register("$t9", init));
		// S
		registershash.put(Registers.$s0, new Register("$s0", init));
		registershash.put(Registers.$s1, new Register("$s1", init));
		registershash.put(Registers.$s2, new Register("$s2", init));
		registershash.put(Registers.$s3, new Register("$s3", init));
		registershash.put(Registers.$s4, new Register("$s4", init));
		registershash.put(Registers.$s5, new Register("$s5", init));
		registershash.put(Registers.$s6, new Register("$s6", init));
		registershash.put(Registers.$s7, new Register("$s7", init));
		// K
		registershash.put(Registers.$k0, new Register("$k0", init));
		registershash.put(Registers.$k1, new Register("$k1", init));
		// Other
		registershash.put(Registers.$gp, new Register("$gp", init));
		registershash.put(Registers.$sp, new Register("$sp", init));
		registershash.put(Registers.$fp, new Register("$fp", init));
		registershash.put(Registers.$ra, new Register("$ra", init));
	}

	public String getBinary(String value) {
		for (Map.Entry<String, Register> entry : registershash.entrySet()) {
			if (((Register) entry.getValue()).compareTo(value) == 1) {
				return (String) entry.getKey();
			}
		}
		return null;
	}
	
	public Integer getData(String regName){
		return registershash.get(getBinary(regName)).getData();
	}
	
	public String toString(){
		String res = "";
		for (Map.Entry<String, Register> entry : registershash.entrySet()) {
			res += ((Register)entry.getValue()).name + " = " + ((Register)entry.getValue()).data + "\n";	
		}
		return res;
	}

}

class Register implements Comparable<String> {
	String name;
	Integer data;
	
	public Register(){
		
	}
	public Register(String name, Integer data) {
		this.name = name;
		this.data = data;
	}

	@Override
	public int compareTo(String name) {
		if (this.name.equalsIgnoreCase(name)) {
			return 1;
		}
		return 0;
	}
	
	public void incrementPC(){
		this.data += 4;
	}
	
	public Integer decrementPC(){
		return data - 4;
	}
	
	public Integer getData() {
		return data;
	}

	public void setData(Integer data) {
		if (name.equals("$0"))
			return;
		this.data = data;
	}
}