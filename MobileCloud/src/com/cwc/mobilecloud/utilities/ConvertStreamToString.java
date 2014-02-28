package com.cwc.mobilecloud.utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConvertStreamToString {

  public static void main(String[] args) throws Exception {
    InputStream is = ConvertStreamToString.class.getResourceAsStream("/data.txt");
    System.out.println(convertStreamToString(is));
  }

  public static String convertStreamToString(InputStream is) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line = null;
    while ((line = reader.readLine()) != null) {
      sb.append(line + "\n");
    }
    is.close();
    return sb.toString();
  }
}
