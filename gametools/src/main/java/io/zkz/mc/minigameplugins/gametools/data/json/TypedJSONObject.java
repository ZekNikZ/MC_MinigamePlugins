package io.zkz.mc.minigameplugins.gametools.data.json;

import org.json.simple.*;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SuppressWarnings("unchecked")
public class TypedJSONObject<T> extends HashMap<String, T> implements Map<String, T>, JSONAware, JSONStreamAware {
    public TypedJSONObject() {
        super();
    }

    public TypedJSONObject(Map<String, T> map) {
        super(map);
    }

    public TypedJSONObject(JSONObject jsonObject, Class<T> type) {
        if (jsonObject.values().stream().anyMatch(el -> el != null && el.getClass().isAssignableFrom(type))) {
            throw new ClassCastException("At least one element is not the expected type.");
        }
        this.putAll(jsonObject);
    }

    public TypedJSONObject(TypedJSONObject<Object> jsonObject, Class<T> type) {
        if (jsonObject.values().stream().anyMatch(el -> el != null && el.getClass().isAssignableFrom(type))) {
            throw new ClassCastException("At least one element is not the expected type.");
        }
        this.putAll((Map<? extends String, ? extends T>) jsonObject);
    }

    @Override
    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }

    @Override
    public String toString() {
        return this.toJSONString();
    }

    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONObject.writeJSONString(this, out);
    }

    public static TypedJSONObject<Integer> integers() {
        return new TypedJSONObject<>();
    }

    public static TypedJSONObject<Integer> asIntegers(JSONObject object) {
        return new TypedJSONObject<>(object, Integer.class);
    }

    public static TypedJSONObject<Double> doubles() {
        return new TypedJSONObject<>();
    }

    public static TypedJSONObject<Double> asDoubles(JSONObject object) {
        return new TypedJSONObject<>(object, Double.class);
    }

    public static TypedJSONObject<Boolean> booleans() {
        return new TypedJSONObject<>();
    }

    public static TypedJSONObject<Boolean> asBooleans(JSONObject object) {
        return new TypedJSONObject<>(object, Boolean.class);
    }

    public static TypedJSONObject<String> strings() {
        return new TypedJSONObject<>();
    }

    public static TypedJSONObject<String> asStrings(JSONObject object) {
        return new TypedJSONObject<>(object, String.class);
    }

    public static TypedJSONObject<JSONObject> objects() {
        return new TypedJSONObject<>();
    }

    public static TypedJSONObject<JSONObject> asObjects(JSONObject object) {
        return new TypedJSONObject<>(object, JSONObject.class);
    }

    public static TypedJSONObject<JSONObject> arrays() {
        return new TypedJSONObject<>();
    }

    public static TypedJSONObject<JSONArray> asArrays(JSONObject object) {
        return new TypedJSONObject<>(object, JSONArray.class);
    }

    public String getString(String key) {
        return (String) this.get(key);
    }

    public int getInteger(String key) {
        return (int) this.getLong(key);
    }

    public long getLong(String key) {
        return (long) this.get(key);
    }

    public double getDouble(String key) {
        return (double) this.get(key);
    }

    public boolean getBoolean(String key) {
        return (boolean) this.get(key);
    }

    public JSONArray getArray(String key) {
        return (JSONArray) this.get(key);
    }

    public <R> TypedJSONArray<R> getArray(String key, Class<R> clazz) {
        return new TypedJSONArray<>((JSONArray) this.get(key), clazz);
    }

    public List<Object> getList(String key) {
        return (List<Object>) this.get(key);
    }

    public <R> List<R> getList(String key, Class<R> clazz) {
        return (List<R>) this.get(key);
    }

    public JSONObject getObject(String key) {
        return (JSONObject) this.get(key);
    }
}
