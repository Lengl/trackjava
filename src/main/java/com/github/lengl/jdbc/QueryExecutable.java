package com.github.lengl.jdbc;


import java.util.Map;

public interface QueryExecutable {
  void initialize() throws Exception;
  <T> T execQuery(String query, ResultHandler<T> handler) throws Exception;
  <T> T execQuery(String query, Map<Integer, Object> args, ResultHandler<T> handler) throws Exception;
  <T> T updateQuery(String query, Map<Integer, Object> args, ResultHandler<T> handler) throws Exception;
  void exit();
}
