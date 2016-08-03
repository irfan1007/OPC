package core;

public class Key<T> {

	private T value;
	public static final String SEPARATOR = "-";
	public static final String CHKSUM = "checksum";

	public Key(T v) {
		if (v == null)
			throw new IllegalArgumentException("Key cannot be null");
		if (v instanceof String && ((String) v).contains(SEPARATOR)) {
			throw new IllegalArgumentException("Key cannot contain '-' (minus) character");
		}
		this.value = v;
	}

	@Override
	public String toString() {
		return value.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Key<?> other = (Key<?>) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

}
