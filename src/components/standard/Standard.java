package components.standard;

/**
 * Minimal Standard interface used by component kernels in this project.
 *
 * @param <T> component type
 */
public interface Standard<T> {

    /**
     * Resets this to its initial value.
     */
    void clear();

    /**
     * Returns a new object with the same dynamic type as this, initialized to
     * its default value.
     *
     * @return fresh instance of this dynamic type
     */
    T newInstance();

    /**
     * Replaces the value of this with the value of source and resets source to
     * its initial value.
     *
     * @param source object whose value is transferred
     */
    void transferFrom(T source);
}
