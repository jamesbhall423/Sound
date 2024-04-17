package location;

public interface SoundLocation<T extends SoundLocation<T>> {
    public double soundTraversalTime(T other);
}
