package com.github.lengl.jdbc;

import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class QueryExecutor implements QueryExecutable {
  private static PGPoolingDataSource source;
  private static Map<String, PreparedStatement> preparedStatements;
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
    if (preparedStatements == null) {
      preparedStatements = new HashMap<>();
    }
    userCounter++;
  }

  // General Query
  public <T> T execQuery(String query, ResultHandler<T> handler) throws SQLException {
    Connection connection = source.getConnection();
    PreparedStatement stmt = getPrepared(connection, query);
    ResultSet result = stmt.executeQuery();
    T value = handler.handle(result);

    result.close();
    connection.close();
    return value;
  }

  // Prepared Statement
  public <T> T execQuery(String query, Map<Integer, Object> args, ResultHandler<T> handler) throws SQLException {
    Connection connection = source.getConnection();
    PreparedStatement stmt = getPrepared(connection, query);
    for (Map.Entry<Integer, Object> entry : args.entrySet()) {
      stmt.setObject(entry.getKey(), entry.getValue());
    }
    ResultSet result = stmt.executeQuery();
    T value = handler.handle(result);

    result.close();
    connection.close();
    return value;
  }

  // Prepared Update Query

  public <T> T updateQuery(String query, Map<Integer, Object> args, ResultHandler<T> handler) throws SQLException {
    Connection connection = source.getConnection();
    PreparedStatement statement = getPrepared(connection, query, Statement.RETURN_GENERATED_KEYS);
    for (Map.Entry<Integer, Object> entry : args.entrySet()) {
      statement.setObject(entry.getKey(), entry.getValue());
    }
    int affectedRows = statement.executeUpdate();
    if (affectedRows == 0)
      throw new SQLException("Update failed, no rows affected");

    T value = handler.handle(statement.getGeneratedKeys());
    connection.close();
    return value;
  }

  private PreparedStatement getPrepared(Connection connection, String query) throws SQLException {
    if (preparedStatements.containsKey(query)) {
      return preparedStatements.get(query);
    } else {
      PreparedStatement stmt = connection.prepareStatement(query);
      preparedStatements.put(query, stmt);
      return stmt;
    }
  }

  private PreparedStatement getPrepared(Connection connection, String query, int params) throws SQLException {
    if (preparedStatements.containsKey(query)) {
      return preparedStatements.get(query);
    } else {
      PreparedStatement stmt = connection.prepareStatement(query, params);
      preparedStatements.put(query, stmt);
      return stmt;
    }
  }

  public void exit() {
    userCounter--;
    if (userCounter == 0) {
      if (source != null) {
        source.close();
        source = null;
      }
      if (preparedStatements != null) {
        preparedStatements.values().stream().forEach(stmt -> {
              try {
                stmt.close();
              } catch (SQLException ex) {
                //TODO: Log it?
              }
            }
        );
        preparedStatements = null;
      }
    }
  }
}