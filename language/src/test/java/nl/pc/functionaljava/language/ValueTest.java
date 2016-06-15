package nl.pc.functionaljava.language;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


public class ValueTest {

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";

    private String result;

    private final Optional<String> SOME_VALUE = Optional.of("HELLO");

    @Before
    public void setUp() {
        result = FAILURE;
    }


    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void runIfEmptyShouldRun() {
        Value<String> value = Value.none();
        value.runIfEmpty(successRunnable());
        Assert.assertEquals(SUCCESS, result);
    }

    @Test
    public void runIfEmptyShouldNotRun() {
        Value<String> value = Value.ofOptional(SOME_VALUE);
        value.runIfEmpty(successRunnable());
        Assert.assertEquals(FAILURE, result);
    }

    @Test
    public void throwIfEmptyShouldThrow() {
        Value<String> value = Value.none();
        exception.expect(NullPointerException.class);
        value.throwIfEmpty(NullPointerException::new);
    }

    @Test
    public void throwIfEmptyShouldNotThrow() {
        Value<String> value = Value.ofOptional(SOME_VALUE);
        value.throwIfEmpty(NullPointerException::new);
    }

    @Test
    public void throwIfDefinedShouldThrow() {
        Value<String> value = Value.ofOptional(SOME_VALUE);
        exception.expect(NullPointerException.class);
        value.throwIfDefined(NullPointerException::new);
    }

    @Test
    public void throwIfDefinedShouldNotThrow() {
        Value<String> value = Value.none();
        value.throwIfDefined(NullPointerException::new);
    }

    @Test
    public void consumeIfDefinedShouldConsume() {
        Value<String> value = Value.ofOptional(SOME_VALUE);
        value.consumeIfDefined(successConsumer());
        Assert.assertEquals(SUCCESS, result);
    }

    @Test
    public void consumeIfDefinedShouldNotConsume() {
        Value<String> value = Value.none();
        value.consumeIfDefined(successConsumer());
        Assert.assertEquals(FAILURE, result);
    }

    @Test
    public void consumeOrElseRunShouldConsume() {
        Value<String> value = Value.ofOptional(SOME_VALUE);
        value.consumeOrElse(successConsumer(), failureRunnable());
        Assert.assertEquals(SUCCESS, result);
    }

    @Test
    public void consumeOrElseRunShouldRun() {
        Value<String> value = Value.none();
        value.consumeOrElse(failureConsumer(), successRunnable());
        Assert.assertEquals(SUCCESS, result);
    }

    @Test
    public void mapSome() {
        int length = Value.ofOptional(SOME_VALUE).map(String::length).get();
        Assert.assertEquals(5, length);
    }

    @Test
    public void mapNone() {
        Value<String> emptyValue = Value.none();
        Assert.assertTrue(emptyValue.map(x -> x).equals(Value.none()));
    }

    @Test
    public void createValueByValue() {
        String word = "functional";
        Value<String> value = Value.of(word);
        Assert.assertEquals(word, value.get());
        Assert.assertTrue(value.isDefined());
    }

    @Test
    public void createBySomeOptional() {
        Value<String> value = Value.ofOptional(Optional.of("hello"));
        Assert.assertEquals("hello", value.get());
    }

    @Test
    public void createByEmptyOptional() {
        Assert.assertEquals(Value.none(), Value.ofOptional(Optional.empty()));
    }

    @Test
    public void createByNullOptional() {
        Value<String> value = Value.ofOptional(null);
        Assert.assertEquals(Value.none(), value);
    }

    @Test
    public void getNothing() {
        Value<Void> nothing = Value.nothing();
        Assert.assertTrue(nothing.isDefined());
        Assert.assertTrue(null == nothing.get());
    }

    @Test
    public void noneOrElse() {
        Value<String> value1 = Value.of(null);
        Value<String> value2 = Value.of("test");
        Value<String> otherValue = value1.orElse(value2);
        Assert.assertEquals(value2, otherValue);
    }

    @Test
    public void someOrElse() {
        Value<String> value1 = Value.of("notEmpty");
        Value<String> value2 = Value.of("test");
        Value<String> otherValue = value1.orElse(value2);
        Assert.assertEquals(value1, otherValue);
    }

    @Test
    public void noneOrElseSupplied() {
        int number = 5;
        Assert.assertEquals(number, Value.none().orElse(()-> Value.of(number)).get());
    }

    @Test
    public void someGetOrElse() {
        String word = "hello";
        Assert.assertEquals(word, Value.of(word).getOrElse("world"));
    }

    @Test
    public void noneGetOrElse() {
        String word = "world";
        Assert.assertEquals(word, Value.none().getOrElse("world"));
    }

    @Test
    public void getNone() {
        exception.expect(NoSuchElementException.class);
        Value.of(null).get();
    }

    @Test
    public void someGetOrElseSupplied() {
        String word1 = "hello";
        String word2 = "world";
        Assert.assertEquals(word1, Value.of(word1).getOrElse(() -> word2));
    }

    @Test
    public void noneGetOrElseSupplied() {
        String word = "hello";
        Assert.assertEquals(word, Value.none().getOrElse(() -> word));
    }

    @Test
    public void someGetOrElseThrow() {
        String word = "some";
        Assert.assertEquals(word, Value.of(word).getOrElseThrow(NullPointerException::new));
    }

    @Test
    public void noneGetOrElseThrow() {
        String word = null;
        exception.expect(NullPointerException.class);
        Value.of(word).getOrElseThrow(NullPointerException::new);
    }

    @Test
    public void filterSome() {
        String word = "hello"; //length = 5
        Value<String> value = Value.of(word);
        Assert.assertTrue(value.filter(hasLength(5)).isDefined());
        Assert.assertTrue(value.filter(startsWith("a")).isEmpty());
    }

    @Test
    public void filterNone() {
        Value<String> value = Value.of(null);
        Assert.assertTrue(Value.none().equals(value.filter(hasLength(1))));
    }

    @Test
    public void flatMapSome() {
        Value<String> value = Value.of("hello");
        Assert.assertEquals(Value.of(5), value.flatMap(x -> Value.of(x.length())));
    }

    @Test
    public void flatMapNone() {
        Value<String> empty = Value.of(null);
        Assert.assertEquals(Value.none(), empty.flatMap(x -> Value.of(x.length())));
    }


    @Test
    public void testHashCodeNone() {
        Value<String> value1 = Value.of(null);
        Value<String> value2 = Value.none();
        Value<String> value3 = Value.ofOptional(null);
        List<Value<String>> values = Arrays.asList(value1, value2, value3);
        Assert.assertTrue(values.stream().allMatch( x -> x.hashCode() == 1));
    }

    @Test
    public void testHashCodeSome() {
        String word = "hello";
        Value<String> value1 = Value.of(word);
        Value<String> value2 = Value.ofOptional(Optional.of(word));
        Assert.assertEquals(value1.hashCode(), value2.hashCode());
    }

    @Test
    public void someToString() {
        Assert.assertEquals("Some(hello)", Value.of("hello").toString());
        Assert.assertEquals("Some(5)", Value.of(5).toString());
    }

    @Test
    public void noneToString() {
        Assert.assertEquals("None", Value.none().toString());
    }

    @Test
    public void transform() {
        String defined = "defined";
        String empty = "empty";
        Function<Value<String>, String> transformer = (value) -> value.isDefined() ? defined : empty;
        Value<String> emptyValue = Value.none();
        Assert.assertEquals(defined, Value.of("doesNotMatter").transform(transformer));
        Assert.assertEquals(empty, emptyValue.transform(transformer));
    }

    @Test
    public void consumeOrElseThrowSome() {
        Value.of("hello").consumeOrElseThrow(successConsumer(), NullPointerException::new);
        Assert.assertEquals(SUCCESS, result);
    }

    @Test
    public void consumeOrElseThrowNone() {
        Value<String> value = Value.of(null);
        exception.expect(NullPointerException.class);
        value.consumeOrElseThrow(successConsumer(), NullPointerException::new);
    }

    @Test
    public void testDefinedAndEmptyPredicates() {
        Value<String> value1 = Value.of("hello");
        Value<String> value2 = Value.none();
        Value<String> value3 = Value.ofOptional(Optional.of("world"));
        List<Value<String>> values = Arrays.asList(value1, value2, value3);
        Assert.assertEquals(2, values.stream().filter(Value.defined()).count());
        Assert.assertEquals(1, values.stream().filter(Value.empty()).count());
    }

    @Test
    public void valueToOptional() {
        Value<String> value1 = Value.none();
        Value<String> value2 = Value.of("value");
        Assert.assertFalse(value1.toOptional().isPresent());
        Assert.assertTrue(value2.toOptional().isPresent());
        Assert.assertEquals("value", value2.toOptional().get());
    }

    @Test
    public void iterateValue() {
        Value<String> value1 = Value.none();
        Value<String> value2 = Value.of("value");
        Assert.assertFalse(value1.iterator().hasNext());
        Assert.assertTrue(value2.iterator().hasNext());
    }

    private Predicate<String> hasLength(int length) {
        return (s) -> length == s.length();
    }

    private Predicate<String> startsWith(String prefix) {
        return (s) -> s.startsWith(prefix);
    }

    private Runnable successRunnable() {
        return () -> result = SUCCESS;
    }

    private Runnable failureRunnable() {
        return () ->  result = FAILURE;
    }

    private Consumer<String> successConsumer() {
        return (value) -> result = SUCCESS;
    }

    private Consumer<String> failureConsumer() {
        return (value) -> result = FAILURE;
    }

}
