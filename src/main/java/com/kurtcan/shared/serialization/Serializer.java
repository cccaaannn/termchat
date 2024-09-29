package com.kurtcan.shared.serialization;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
public class Serializer {
    private static final String DELIMITER = "<<|>>";

    public static String serialize(Object obj) {
        var serialized = new StringBuilder();
        var annotatedFields = Arrays.stream(
                obj.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(SerializableField.class)
        ).toList();

        for (var i = 0; i < annotatedFields.size(); i++) {
            var field = annotatedFields.get(i);

            try {
                field.setAccessible(true);
                serialized.append(field.get(obj));

                if (i != annotatedFields.size() - 1) {
                    serialized.append(DELIMITER);
                }
            } catch (IllegalAccessException e) {
                log.error("Serialization error, class: {}, field: {}", obj.getClass().getName(), field.getName());
                throw new SerializationException("Serialization error", e);
            }
        }
        return serialized.toString();
    }

    public static <T> T deserialize(String object, Class<T> clazz) {
        var annotatedFields = Arrays.stream(
                clazz.getDeclaredFields()).filter(field -> field.isAnnotationPresent(SerializableField.class)
        ).toList();
        var split = object.split(Pattern.quote(DELIMITER));

        if (annotatedFields.size() != split.length) {
            log.error("Serializable field count not matching with item count, class: {}", clazz.getName());
            throw new SerializationException("Invalid object format");
        }

        try {
            var instance = clazz.getDeclaredConstructor().newInstance();

            for (var i = 0; i < annotatedFields.size(); i++) {
                var field = annotatedFields.get(i);
                var fieldType = field.getType();

                field.setAccessible(true);
                if (fieldType == String.class) {
                    field.set(instance, split[i]);
                    continue;
                }
                if (fieldType == UUID.class) {
                    field.set(instance, UUID.fromString(split[i]));
                    continue;
                }

                log.error("Unsupported field type: {}", fieldType);
                throw new SerializationException("Unsupported field type");
            }
            return instance;
        } catch (Exception e) {
            log.error("Deserialization error, class: {}", clazz);
            throw new SerializationException("Deserialization error", e);
        }
    }
}
