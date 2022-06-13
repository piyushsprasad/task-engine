package org.vesta.db;

import java.util.List;
import java.util.Optional;

public interface Dao<T> {
    void insert(T t);

    T updateField(String id, String field, Object value);

    Optional<T> get(String id);

    List<T> getByField(String field, String value);

    List<T> getAll();
}
