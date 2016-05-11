package shared;

/**
 * Represents a key-value pair
 *
 * @author Huw Jones
 * @since 11/05/2016
 */
public class Pair<K, V> {

    private final K key;
    private final V value;

    public Pair(K key, V value){
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.key + "+" + this.value;
    }

    @Override
    public int hashCode() {
        return this.key.hashCode() * 13 + (this.value == null ? 0 : this.value.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Pair) {
            Pair pair = (Pair) o;
            if (key != null ? !key.equals(pair.key) : pair.key != null) return false;
            if (value != null ? !value.equals(pair.value) : pair.value != null) return false;
            return true;
        }
        return false;
    }
}
