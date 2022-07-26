package io.zkz.mc.minigameplugins.gametools.data.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TypedJSONArray<T> extends ArrayList<T> implements List<T>, JSONAware, JSONStreamAware {
    public TypedJSONArray() {
        super();
    }

    @SafeVarargs
    public TypedJSONArray(T... values) {
        super(Arrays.asList(values));
    }

    public TypedJSONArray(Collection<T> list) {
        super(list);
    }

    @SuppressWarnings("unchecked")
    public TypedJSONArray(JSONArray jsonArray, Class<T> type) throws ClassCastException {
        if (jsonArray.stream().anyMatch(el -> el != null && el.getClass().isAssignableFrom(type))) {
            throw new ClassCastException("At least one element is not the expected type.");
        }
        this.addAll(jsonArray);
    }

    @Override
    public String toJSONString() {
        return JSONArray.toJSONString(this);
    }

    @Override
    public String toString() {
        return this.toJSONString();
    }

    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONArray.writeJSONString(this, out);
    }

    public static TypedJSONArray<Integer> integers() {
        return new TypedJSONArray<>();
    }

    public static TypedJSONArray<Integer> asIntegers(JSONArray array) {
        return new TypedJSONArray<>(array, Integer.class);
    }

    public static TypedJSONArray<Double> doubles() {
        return new TypedJSONArray<>();
    }

    public static TypedJSONArray<Double> asDoubles(JSONArray array) {
        return new TypedJSONArray<>(array, Double.class);
    }

    public static TypedJSONArray<Boolean> booleans() {
        return new TypedJSONArray<>();
    }

    public static TypedJSONArray<Boolean> asBooleans(JSONArray array) {
        return new TypedJSONArray<>(array, Boolean.class);
    }

    public static TypedJSONArray<String> strings() {
        return new TypedJSONArray<>();
    }

    public static TypedJSONArray<String> asStrings(JSONArray array) {
        return new TypedJSONArray<>(array, String.class);
    }

    public static TypedJSONArray<JSONObject> objects() {
        return new TypedJSONArray<>();
    }

    public static TypedJSONArray<JSONObject> asObjects(JSONArray array) {
        return new TypedJSONArray<>(array, JSONObject.class);
    }

    public static TypedJSONArray<JSONArray> arrays() {
        return new TypedJSONArray<>();
    }

    public static TypedJSONArray<JSONArray> asArrays(JSONArray array) {
        return new TypedJSONArray<>(array, JSONArray.class);
    }
}
