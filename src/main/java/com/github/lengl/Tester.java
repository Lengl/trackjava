package com.github.lengl;


import java.util.HashMap;
import java.util.Map;

public class Tester {
  public static void main(String[] args) {
    Map<String, Long> mymap = new HashMap<>();

    mymap.put("smth", (long) 1);
    mymap.put("smth", (long) 2);
  }
}
