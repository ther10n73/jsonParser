package ru.vdovin.jsonParser;

import ru.nojs.json.*;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ImplementedJsonParser implements StreamingJsonParser {
    private static final Set<Character> INSIGNIFICANT_SYMBOLS = new HashSet<>(Arrays.asList('\n', '\r', '\t', ' '));
    private JSONElement jsonElement;

    public JSONElement parse(Reader r) {
        JsonParseReader jpr = new JsonParseReader(r);
        return chooseJson(jpr);
    }

    public JSONElement chooseJson(JsonParseReader jpr) {
        jpr.nextElement();
        checkIsRemove(jpr);
        switch (jpr.getElement()) {
            case '{':
                return parseObject(jpr);
            case '[':
                return parseArray(jpr);
            case '"':
                return parseString(jpr);
            default:
                return getPrimitive(jpr);
        }
    }

    public JSONElement parseString(JsonParseReader jpr) {
        StringBuffer string = new StringBuffer();
        while (!isEndMarker(jpr.getElement())) {
            string.append((char) jpr.getElement());
            jpr.nextElement();
            checkIsRemove(jpr);
        }
        String s = string.toString();
        s = s.replace("\\", "");
        if (enclosedInQuotes(s)) {
            return new JSONPrimitiveImpl(s.substring(1, s.length() - 1));
        }
        throw new IllegalArgumentException("Parse Error!!!");
    }

    public JSONElement parseObject(JsonParseReader jpr) {
        JSONObjectImpl jsonObject = new JSONObjectImpl();
        String key = "";
        do {
            jpr.nextElement();
            checkIsRemove(jpr);
            if (jpr.getElement() == '"') {
                jpr.nextElement();
            } else {
                throw new IllegalArgumentException("Error syntax in object");
            }

            while (jpr.getElement() != '"') {
                key += (char) jpr.getElement();
                jpr.nextElement();
            }

            jpr.nextElement();
            checkIsRemove(jpr);
            JSONElement jsonElement = chooseJson(jpr);

            jsonObject.add(key, jsonElement);
            key = "";
            if (jpr.getElement() == '"') {
                jpr.nextElement();
            }
            checkIsRemove(jpr);
        } while (jpr.getElement() == ',');
        jpr.nextElement();
        return jsonObject;
    }

    public JSONElement parseArray(JsonParseReader jpr) {
        JSONArrayImpl JSONArrayImpl = new JSONArrayImpl();
        do {
            JSONArrayImpl.add(chooseJson(jpr));
        } while (jpr.getElement() == ',');
        jpr.nextElement();
        return JSONArrayImpl;
    }

    public JSONElement getPrimitive(JsonParseReader jpr) {
        StringBuffer string = new StringBuffer();
        while (!isEndMarker(jpr.getElement())) {
            string.append((char) jpr.getElement());
            jpr.nextElement();
            checkIsRemove(jpr);
        }
        String el = string.toString();
        if (jpr.isDouble(el)) {
            jsonElement = new JSONPrimitiveImpl(jpr.doubleValue(el));
        } else if (jpr.isLong(el)) {
            jsonElement = new JSONPrimitiveImpl(jpr.longValue(el));
        } else if (jpr.isInteger(el)) {
            jsonElement = new JSONPrimitiveImpl(jpr.integerValue(el));
        } else if (enclosedInQuotes(el)) {
            jsonElement = new JSONPrimitiveImpl(el);
        } else if (isNull(el)) {
            jsonElement = new JSONNullImpl();
        } else if (isBoolean(el)) {
            jsonElement = new JSONPrimitiveImpl(Boolean.parseBoolean(el));
        } else {
            throw new IllegalArgumentException("Bad syntax!!");
        }
        return jsonElement;
    }

    private boolean isEndMarker(int rd) {
        return rd == '}' || rd == ']' || rd == -1 || rd == ',';
    }

    private boolean enclosedInQuotes(String value) {
        return value.startsWith("\"") && value.endsWith("\"");
    }

    private boolean isNull(String value) {
        return value.equals("null");
    }

    private boolean isBoolean(String value) {
        if (value.equals("true") || value.equals("false")) {
            return true;
        } else {
            return false;
        }
    }

    public void checkIsRemove(JsonParseReader jpr) {
        while (INSIGNIFICANT_SYMBOLS.contains((char) (jpr.getElement()))) {
            jpr.nextElement();
        }
    }
}
