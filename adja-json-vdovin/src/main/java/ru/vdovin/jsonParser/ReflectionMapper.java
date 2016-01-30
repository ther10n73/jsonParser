package ru.vdovin.jsonParser;

import org.apache.commons.lang.WordUtils;
import ru.nojs.json.JSONElement;
import ru.nojs.json.JSONObject;
import ru.nojs.json.JSONPrimitiveImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ReflectionMapper {
    public <T> T createObject(JSONElement je, Class<T> targetType) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        T obj;
        JSONObject jo = je.getAsJsonObject();
            obj = createConstructor(jo, targetType);
        return obj;
    }

    public <T> T createConstructor(JSONObject jo, Class<T> targetType) throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException {
        T obj;
            Constructor<T> constructor = targetType.getDeclaredConstructor();
            obj = constructor.newInstance();

        jo
                .entrySet()
                .forEach(je -> {

                    String setMethod = setterNameOf(je.getKey());
                    Method method = Stream.of(targetType.getMethods())
                            .filter(method1 -> method1.getName().equals(setMethod) && method1.getParameterCount() == 1)
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No field " + je.getKey()));
                    methodInvoke(obj, method, je);

                });
        return obj;
    }

    public <T> T methodInvoke(T obj, Method method, Map.Entry<String, JSONElement> je) {
        try {
            Class typeClass = method.getParameterTypes()[0];
            if (typeClass == BigDecimal.class) {
                BigDecimal bd = je.getValue().getAsBigDecimal();
                method.invoke(obj, bd);
            } else if (typeClass == Map.class) {
                Map<String, String> map = new HashMap<>();
                JSONObject jso = je.getValue().getAsJsonObject();
                for (Map.Entry<String, JSONElement> jse : jso.entrySet()) {
                    JSONPrimitiveImpl jpi = (JSONPrimitiveImpl) jse.getValue().getAsJsonPrimitive();
                    map.put(jse.getKey(), String.valueOf(jpi.getAsObject()));
                }
                method.invoke(obj, map);
            } else if (typeClass.isEnum()) {
                Enum enums = (Enum) Stream.of(typeClass.getEnumConstants())
                        .filter(e -> e.toString().equals(je.getValue().getAsString()))
                        .findFirst().orElseThrow(() -> new IllegalArgumentException("Don't have enum"));
                method.invoke(obj, enums);
            } else {
                JSONPrimitiveImpl jpi = (JSONPrimitiveImpl) je.getValue().getAsJsonPrimitive();
                method.invoke(obj, jpi.getAsObject());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return obj;
    }

    /**
     * According to common sense JSON originated from JavaScript, the appropriate naming convention of keys
     * for JSON is and should be in camelCase regardless of what programming language the JSON was formed. (c) so
     * But nevertheless, if we encounter underscores in field_name, we try to convert it to fieldName
     */
    String fieldNameOf(String jsonFieldName) {
        String name = WordUtils.capitalize(jsonFieldName, new char[]{'_'}).replaceAll("_", "");
        StringBuilder s = new StringBuilder();
        s.append(name.toLowerCase().charAt(0)).append(name.substring(1));
        return s.toString();
    }

    /**
     * @see ReflectionMapper#fieldNameOf(String)
     * but this method will also append get as prefix
     */
    String getterNameOf(String jsonFieldName) {
        return fieldNameOf("get_" + jsonFieldName);
    }

    String setterNameOf(String jsonFieldName) {
        return fieldNameOf("set_" + jsonFieldName);
    }
}
