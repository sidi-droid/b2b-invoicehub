package dao;

import java.util.List;

// generic CRUD interface for all DAOs
public interface DAOInterface<T> {

    boolean insert(T entity) throws Exception;

    T getById(int id) throws Exception;

    List<T> getAll() throws Exception;

    boolean update(T entity) throws Exception;

    boolean delete(int id) throws Exception;
}
