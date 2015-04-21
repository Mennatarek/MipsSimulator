package engine.memory;

import java.util.Hashtable;

public class DataMemory {
	Hashtable<String, String> memory;
	String baseAddress;
	String pc;

	public DataMemory(String baseAddress) {
		memory = new Hashtable<String, String>();
		this.baseAddress = baseAddress;
		pc = baseAddress;
	}

	public void write(String address, String data) {
		if (address == null) {
			memory.put(String.valueOf(pc), data);
			pc = String.valueOf(Integer.parseInt(pc)+4);
			return;
		}
		memory.put(address, data);
	}

	public String read(String address) {
		if (memory.get(address) == null){
			return "00000000000000000000000000000000";
		}
		return String.valueOf(Integer.parseInt(memory.get(address),2));
	}

	public Hashtable<String, String> getMemory() {
		return memory;
	}

	public String getCurrentAddress() {
		return pc;
	}
}
