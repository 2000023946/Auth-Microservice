package com.authservice.domain.model.valueobjects;

/**
 * A Value Object represents an immutable object in the domain
 * that encapsulates a specific value and enforces domain rules.
 * <p>
 * Value Objects are compared by their contained value, not by object identity.
 * They should be immutable and self-validating.
 *
 * @param <T> the type of the underlying value encapsulated by this Value Object
 */
public abstract class ValueObject<T> {

    /**
     * Returns the encapsulated value of this Value Object.
     *
     * @return the underlying value
     */
    public abstract T getValue();

    /**
     * Compares this ValueObject with another object for equality.
     * <p>
     * Two ValueObject instances are equal if their underlying values are equal.
     *
     * @param o the object to compare with
     * @return true if the objects represent the same value, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true; // same reference
        }
        if (o == null || getClass() != o.getClass()) {
            return false; // null or different class
        }
        ValueObject<?> that = (ValueObject<?>) o;
        return this.getValue().equals(that.getValue());
    }

    /**
     * Returns the hash code of the underlying value.
     * <p>
     * Ensures consistency with the equals method.
     *
     * @return the hash code of the value
     */
    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    /**
     * Returns the string representation of this ValueObject.
     *
     * @return the string representation of the underlying value
     */
    @Override
    public String toString() {
        return this.getValue().toString();
    }
}
