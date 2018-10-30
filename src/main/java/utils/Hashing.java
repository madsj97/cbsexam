package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.bouncycastle.util.encoders.Hex;

public final class Hashing {

//  public static byte[] getSalt(){
//
//    try {
//
//      // Using a SecureRandom to generate a secure random value
//      SecureRandom sRandom = SecureRandom.getInstance("MD5", "SUN");
//
//      // Creating an array for the salt
//      byte[] salt = new byte[20];
//
//      // We generate the random salt
//      sRandom.nextBytes(salt);
//
//      // We return the created salt
//      return salt;
//    }
//    catch (Exception e){
//
//    }
//    return null;
//  }

  // TODO: You should add a salt and make this secure :FIX
  public static String md5WithSalt(String string) {
    String salt = "G27dPfdWP8OX66VS9zK0";
    String hashedPassword = string + salt;
    return md5(hashedPassword);
  }

  public static String md5(String rawString) {
    try {

      // We load the hashing algoritm we wish to use.
      MessageDigest md = MessageDigest.getInstance("MD5");

      // We convert to byte array
      byte[] byteArray = md.digest(rawString.getBytes());

      // Initialize a string buffer
      StringBuffer sb = new StringBuffer();

      // Run through byteArray one element at a time and append the value to our stringBuffer
      for (int i = 0; i < byteArray.length; ++i) {
        sb.append(Integer.toHexString((byteArray[i] & 0xFF) | 0x100).substring(1, 3));
      }

      //Convert back to a single string and return
      return sb.toString();

    } catch (java.security.NoSuchAlgorithmException e) {

      //If somethings breaks
      System.out.println("Could not hash string");
    }

    return null;
  }

  // TODO: You should add a salt and make this secure :FIX
  public static String shaWithSalt(String string) {
    String salt = "G27dPfdWP8OX66VS9zK0";
    String hashedPassword = string + salt;
    return sha(hashedPassword);
  }

  public static String sha(String rawString) {
    try {
      // We load the hashing algoritm we wish to use.
      MessageDigest digest = MessageDigest.getInstance("SHA-256");



      // We convert to byte array
      byte[] hash = digest.digest(rawString.getBytes(StandardCharsets.UTF_8));

      // We create the hashed string
      String sha256hex = new String(Hex.encode(hash));

      // And return the string
      return sha256hex;

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }

    return rawString;
  }
}