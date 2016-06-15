package nl.pc.functionaljava.language;


import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Replacement for {@link Optional}
 *
 * @param <T> the type of the value
 */
public interface Value<T> extends Iterable<T>, Serializable {

    /**
     *
     * @param optional a optional
     * @param <T> type of the value
     * @return {@code Some(value)} if optional is not null and not empty, else {@code None}.
     */
    static <T> Value<T> ofOptional(Optional<T> optional) {
        return optional != null && optional.isPresent() ? new Some<>(optional.get()) : none();
    }

    /**
     *
     * @param value a value
     * @param <T> the type of value
     * @return {@code Some(value)} if value is not {@code null}, else {@code None}.
     */
    static <T> Value<T> of(T value) {
        return (value == null) ? None.none() : new Some<>(value);
    }

    /**
     * Return the singleton instance of {@code Some<Void>}.
     * @return {@code Some<Void>}
     */
    static Value<Void> nothing() {
        return Some.NOTHING;
    }

    /**
     * Return the singleton instance of {@code None}
     * @param <T> type of the value
     * @return the singleton instance of {@code None}
     */
    static <T> Value<T> none() {
        return None.none();
    }

    /**
     * Predicate for filtering defined Values
     * @param <T> the type of the value
     * @return  a {@link Predicate} that returns true only for defined Values
     */
    static <T> Predicate<Value<T>> defined() {
        return Value::isDefined;
    }

    /**
     * Predicate for filtering empty Values
     * @param <T> the type of the value
     * @return  a {@link Predicate} that returns true only for empty Values
     */
    static <T> Predicate<Value<T>> empty() {
        return Value::isEmpty;
    }

    /**
     * Gets the underlying value or throws if no value is present.
     * @return the underlying value
     * @throws NoSuchElementException if no value is defined
     */
    T get();

    /**
     * Returns true, if this is {@code None}, otherwise false, if this is {@code Some}.
     *
     * @return true, if this {@code Value} is empty, false otherwise
     */
    boolean isEmpty();

    /**
     * Returns true, if this is {@code Some}, otherwise false, if this is {@code None}.
     *
     * @return true, if this {@code Option} is defined, false otherwise
     */
    default boolean isDefined() {
        return !isEmpty();
    }

    /**
     * Convert this to an {@link Optional}.
     * @return a new {@link Optional}.
     */
    default Optional<T> toOptional() {
        return isEmpty() ? Optional.empty() : Optional.ofNullable(get());
    }

    /**
     * Returns this {@code Value} if it is defined, otherwise return other.
     *
     * @param other An other {@code Value}
     * @return this {@code Value} if it is defined, otherwise return other.
     */
    @SuppressWarnings("unchecked")
    default Value<T> orElse(Value<? extends T> other) {
        Objects.requireNonNull(other, "Other is null");
        return isEmpty() ? (Value<T>) other : this;
    }

    /**
     * Returns this {@code Value} if it is defined, otherwise return other.
     *
     * @param supplier A {@code Value} supplier
     * @return this {@code Value} if it is defined, otherwise return result of evaluating supplier.
     */
    @SuppressWarnings("unchecked")
    default Value<T> orElse(Supplier<? extends Value<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "Supplier is null");
        return isEmpty() ? (Value<T>) supplier.get() : this;
    }


    /**
     * Returns the value if this is a {@code Some} or the {@code other} value if this is a {@code None}.
     *
     * @param other An other value
     * @return This value, if this Value is defined or the {@code other} value, if this Value is empty.
     */
    default T getOrElse(T other) {
        return isEmpty() ? other : get();
    }

    /**
     * Returns the value if this is a {@code Some} or the {@code other} value if this is a {@code None}.
     *
     * @param supplier A supplier of value of type T
     * @return This value, if this Value is defined or result of evaluating supplier if this Value is empty.
     */
    default T getOrElse(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "Supplier is null");
        return isEmpty() ? supplier.get() : get();
    }

    /**
     * Return this value if defined, else throws exception by evaluating exception supplier
     * @param exceptionSupplier the supplier of the exception to be throw if Value is empty
     * @param <X> the type of the exception
     * @return this value if defined
     * @throws X
     */
    default <X extends Throwable> T getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier, "Exception Supplier is null");
        if (isEmpty()) {
            throw exceptionSupplier.get();
        } else {
            return get();
        }
    }

    /**
     * Return {@code Some(value) if this is {@code Some} and predicate evaluates true, otherwise {@code None}
     * @param predicate a predicate to test this value against
     * @return {@code Some(value)} or {@code None} as specified
     */
    default Value<T> filter(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "Predicate is null");
        return isEmpty() || predicate.test(get()) ? this : None.none();
    }

    /**
     * Maps value if present and wraps it in new {@code Some}, otherwise returns {@code None}
     * @param mapper a mapper which maps value of type T to type U
     * @param <U> a type
     * @return a {@code Value} where value of type T is transformed to type U
     */
    @SuppressWarnings("unchecked")
    default <U> Value<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "Mapper is null");
        return isEmpty() ? None.none() : new Some(mapper.apply(get()));
    }

    /**
     * Maps the value to a new {@code Value} if this is a {@code Some}, otherwise returns {@code None}.
     * @param mapper a mapper
     * @param <U> type of resulting Value
     * @return a new Value
     */
    @SuppressWarnings("unchecked")
    default <U> Value<U> flatMap(Function<? super T, ? extends Value<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? None.none() : (Value<U>) mapper.apply(get());
    }

    /**
     * Transforms this Value
     * @param f a transforming function
     * @param <U> the type of the result
     * @return an instance of type {@code U}
     */
    default <U> U transform(Function<? super Value<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    /**
     * Throws exception from evaluating exception supplier if this {@code Value} is defined
     * @param exceptionSupplier the supplier of the exception
     * @param <X> the type of the exception
     * @throws X
     */
    default <X extends Throwable> void throwIfDefined(Supplier<? extends X> exceptionSupplier) throws X {
        if (isDefined()) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Throws exception from evaluating exception supplier if this {@code Value}  is empty
     * @param exceptionSupplier the supplier of the exception
     * @param <X> the type of the exception
     * @throws X
     */
    default <X extends Throwable> void throwIfEmpty(Supplier<? extends X> exceptionSupplier) throws X {
        if (isEmpty()) {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Calls consumer if this {@code Value} is defined, else executes orElse
     * @param consumer the consumer of this value
     * @param orElse the code to execute if this {@code Value} is empty
     */
    default void consumeOrElse(Consumer<T> consumer, Runnable orElse) {
        if (isDefined()) {
            consumer.accept(get());
        } else {
            orElse.run();
        }
    }

    /**
     * Calls consumer if this {@code Value} is defined, else throws exception from evaluating exception supplier
     * @param consumer the consumer of this value
     * @param exceptionSupplier the supplier of the exception
     * @param <X> the type of the exception
     * @throws X
     */
    default <X extends Throwable> void consumeOrElseThrow(Consumer<T> consumer, Supplier<? extends X> exceptionSupplier) throws  X {
        if (isDefined()) {
            consumer.accept(get());
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Calls consumer if this value is defined
     * @param consumer the consumer of this value
     */
    default void consumeIfDefined(Consumer<T> consumer) {
        if (isDefined()) {
            consumer.accept(get());
        }
    }

    /**
     * Executes runnable if this {@code Value} is empty
     * @param runnable the code to execute
     */
    default void runIfEmpty(Runnable runnable) {
        if (isEmpty()) {
            runnable.run();
        }
    }


    final class None<T> implements Value<T>, Serializable {
        private static final long serialVersionUID = 1L;

        private static final None<?> INSTANCE = new None<>();

        private None() {
            //hidden constructor
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public T get() {
            throw new NoSuchElementException("Value is empty");
        }

        @SuppressWarnings("unchecked")
        public static <T> Value<T> none() {
            return (None<T>) None.INSTANCE;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Iterator<T> iterator() {
            return Collections.EMPTY_LIST.iterator();
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return 1;
        }


        @Override
        public String toString() {
            return "None";
        }

    }


    final class Some<T> implements Value<T>, Serializable {
        private static final long serialVersionUID = 1L;
        private static final Some<Void> NOTHING = new Some<>(null);

        private final T value;

        private Some(T value) {
            this.value = value;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Iterator<T> iterator() {
            return Collections.singletonList(value).iterator();
        }

        @Override
        public boolean equals(Object obj) {
            return (obj == this) || (obj instanceof Some && Objects.equals(value, ((Some<?>) obj).value));
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return "Some(" + value + ")";
        }

    }
}
