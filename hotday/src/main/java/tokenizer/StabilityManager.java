package tokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class StabilityManager {
	
	private final Map<String, StabilityRecord> records = new HashMap<>();
	
	public boolean isStable(String host) {
		if (host == null) {
			return true;
		}
		StabilityRecord record = null;
		synchronized (records) {
			record = records.get(host);
			if (record == null) {
				record = new StabilityRecord(host);
				records.put(host, record);
			}
		}
		return record.isStable();
	}
	
	public void remove(String host) {
		if (host == null) {
			return;
		}
		synchronized (records) {
			records.remove(host);
		}
	}
	
	public Map<String, Boolean> status() {
		Map<String, Boolean> results = new HashMap<>();
		Map<String, StabilityRecord> records = new HashMap<>();
		synchronized (this.records) {
			records.putAll(this.records);
		}
		for (Entry<String, StabilityRecord> entry : records.entrySet()) {
			String host = entry.getKey();
			StabilityRecord record = entry.getValue();
			if (System.currentTimeMillis() - record.lastModified.get() > 1000 * 60 * 5) {
				remove(host);
			} else {
				results.put(host, record.isStable());
			}
		}
		return results;
	}

	private static final class StabilityRecord {
		
		private final String host;
		private final AtomicLong lastModified = new AtomicLong(System.currentTimeMillis());
		private final AtomicBoolean isStable = new AtomicBoolean(true);
		
		public StabilityRecord(String host) {
			Objects.requireNonNull(host);
			
			this.host = host;
		}
		
		public boolean isStable() {
			return isStable.get();
		}
		
		public String host() {
			return host;
		}
		
		@Override
		public String toString() {
			return super.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((host == null) ? 0 : host.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StabilityRecord other = (StabilityRecord) obj;
			if (host == null) {
				if (other.host != null)
					return false;
			} else if (!host.equals(other.host))
				return false;
			return true;
		}
		
	}
}
