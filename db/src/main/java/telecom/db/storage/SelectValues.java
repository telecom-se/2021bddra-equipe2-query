package telecom.db.storage;

import java.util.ArrayList;
import java.util.List;

public class SelectValues {
	private List<String> values;
	private List<String> timestamps;

	public SelectValues() {
		this.values = new ArrayList<String>();
		this.timestamps = new ArrayList<String>();
	}

	public List<String> getTimestamps() {
		return this.timestamps;
	}

	public List<String> getValues() {
		return this.values;
	}
}