package org.vesta.db.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.vesta.db.Dao;
import org.vesta.db.HibernateConnection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

public class H2DaoImpl<T> implements Dao<T> {
    private final HibernateConnection connection;
    private final Class<T> typeParameterClass;

    public H2DaoImpl(HibernateConnection connection, Class<T> typeParameterClass) {
        this.connection = connection;
        this.typeParameterClass = typeParameterClass;
    }

    @Override
    public void insert(Object t) {
        Transaction transaction;
        try (Session session = connection.startSession()) {
            transaction = session.beginTransaction();
            session.persist(t);
            transaction.commit();
        } catch (Exception e) {
            throw new RuntimeException(String.format("Exception when inserting object %s", t.toString()), e);
        }
    }

    @Override
    public T updateField(String id, String field, Object value) {
        Transaction transaction;
        try (Session session = connection.startSession()) {
            transaction = session.beginTransaction();

            Optional<T> current = runGetQuery(session, id);
            if (current.isEmpty()) {
                throw new RuntimeException("No existing object found with id " + id);
            }

            T updated = current.get();
            Field declaredField = updated.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            declaredField.set(updated, value);
            session.merge(updated);

            transaction.commit();

            return updated;
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format(
                            "Exception when updating object with id %s for field %s for value %s",
                            id, field, value.toString()), e);
        }
    }

    @Override
    public Optional<T> get(String id) {
        Transaction transaction;
        try (Session session = connection.startSession()) {
            transaction = session.beginTransaction();
            Optional<T> obj = runGetQuery(session, id);
            transaction.commit();
            return obj;
        } catch (Exception e) {
            throw new RuntimeException("Exception when getting object with id "+ id, e);
        }
    }

    @Override
    public List<T> getByField(String field, String value) {
        Transaction transaction;
        try (Session session = connection.startSession()) {
            transaction = session.beginTransaction();
            Query<T> q = session.createQuery(
                    String.format(
                            " from %s where %s = :%s", typeParameterClass.getSimpleName(), field, field),
                    typeParameterClass);
            q.setParameter(field, value);
            transaction.commit();
            return q.list();
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Exception when getting %s objects with field %s equal to %s",
                            typeParameterClass.getSimpleName(), field, value), e);
        }
    }

    @Override
    public List<T> getAll() {
        Transaction transaction;
        try (Session session = connection.startSession()) {
            transaction = session.beginTransaction();
            Query<T> q = session.createQuery(
                    String.format(
                            " from %s", typeParameterClass.getName()),
                    typeParameterClass);
            transaction.commit();
            return q.list();
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format("Exception when getting all %s objects",
                            typeParameterClass.getSimpleName()), e);
        }
    }

    private Optional<T> runGetQuery(Session session, String id) {
        Query<T> q = session.createQuery(
                "from " + typeParameterClass.getName() + " where id = :id", typeParameterClass);
        q.setParameter("id", id);
        return q.list().isEmpty() ? Optional.empty() : Optional.of(q.list().get(0));
    }
}
