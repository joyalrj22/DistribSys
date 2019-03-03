import java.util.ArrayList;
public class TimeStamp {
	
	private ArrayList<Integer> vals;
	
	public TimeStamp(int...values) {
		vals = new ArrayList<Integer>();
		for (int v: values) {
			vals.add(Integer.valueOf(v));
		}
		
	}
	
	public TimeStamp(ArrayList<Integer> values) {
		vals = values;
	}
	
	public TimeStamp(String values) {
		String[] strValArray = values.split(",");
		for (String v: strValArray) {
			vals.add(Integer.valueOf(v));
		}
	}
	
	public TimeStamp() {
		vals = new ArrayList<Integer>();
	}

	public int getVal(int index) {
		return vals.get(index).intValue();
	}
	
	public void setVal(int index, int value) {
		vals.set(index, Integer.valueOf(value));
	}
	
	private void addVal(int value) {
		vals.add(value);
	}
	
	public void incrementVal(int index) {
		vals.set(index, Integer.valueOf(getVal(index)+1));
	}
	
	public int length() {
		return vals.size();
	}
	
	public boolean isSmallerThan(TimeStamp ts) {
		if (ts == null) {
			return false;
		}
		for (int i=0; i<ts.length(); i++) {
			if (ts.getVal(i)>=getVal(i)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isGreaterThan(TimeStamp ts) {
		if (ts == null) {
			return false;
		}
		for (int i=0; i<ts.length(); i++) {
			if (ts.getVal(i)<=getVal(i)) {
				return false;
			}
		}
		return true;
	}
	
	public TimeStamp getClone() {
		TimeStamp clone = new TimeStamp();
		for (int i=0; i<vals.size(); i++) {
			clone.addVal(getVal(i));
		}
		
		return clone;
	}
	
	public String toString() {
		String strRep = "";
		for (Integer n: vals) {
			strRep += n.toString() + ",";
		}
		return strRep;
	}
	
	

}
