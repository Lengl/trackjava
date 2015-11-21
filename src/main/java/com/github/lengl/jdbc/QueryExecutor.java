package com.github.lengl.jdbc;

import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public class QueryExecutor implements QueryExecutable {
  private static PGPoolingDataSource source;
  private static volatile long userCounter = 0;

  public synchronized void initialize() throws ClassNotFoundException {
    if (source == null) {
      Class.forName("org.postgresql.Driver");

      source = new PGPoolingDataSource();
      source.setDataSourceName("My DB");
      source.setServerName("178.62.140.149");
      source.setDatabaseName("Lengl");
      source.setUser("senthil");
      source.setPassword("ubuntu");
      source.setMaxConnections(10);
    }
    userCounter++;
  }

  // General Query
  public <T> T execQuery(String query, ResultHandler<T> handler) throws SQLException {
    Connection connection = source.getConnection();
    Statement stmt = connection.createStatement();
    stmt.execute(query);
    ResultSet result = stmt.getResultSet();
    T value = handler.handle(result);
    result.close();
    stmt.close();

    return value;
  }

  // Prepared Statement
  public <T> T execQuery(String query, Map<Integer, Object> args, ResultHandler<T> handler) throws SQLException {
    Connection connection = source.getConnection();
    PreparedStatement stmt = connection.prepareStatement(query);
    for (Map.Entry<Integer, Object> entry : args.entrySet()) {
      stmt.setObject(entry.getKey(), entry.getValue());
    }
    ResultSet rs = stmt.executeQuery();
    T value = handler.handle(rs);
    rs.close();
    stmt.close();
    connection.close();
    return value;
  }

  // Prepared Update Query

  public <T> T updateQuery(String query, Map<Integer, Object> args, ResultHandler<T> handler) throws SQLException {
    Connection connection = source.getConnection();
    PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
    for (Map.Entry<Integer, Object> entry : args.entrySet()) {
      statement.setObject(entry.getKey(), entry.getValue());
    }
    int affectedRows = statement.executeUpdate();
    if (affectedRows == 0)
      throw new SQLException("Update failed, no rows affected");

    T value = handler.handle(statement.getGeneratedKeys());
    statement.close();
    connection.close();
    return value;
  }

  public void exit() {
    userCounter--;
    if (source != null && userCounter == 0) {
      source.close();
      source = null;
    }
  }
}