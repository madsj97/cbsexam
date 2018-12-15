package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import cache.UserCache;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

    private static DatabaseController dbCon;

    public UserController() {
        dbCon = new DatabaseController();
    }

    public static User getUser(int id) {

        // Check for connection
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        // Build the query for DB
        String sql = "SELECT * FROM user where id=" + id;

        // Actually do the query
        ResultSet rs = dbCon.query(sql);
        User user = null;

        try {
            // Get first object, since we only have one
            if (rs.next()) {
                user =
                        new User(
                                rs.getInt("id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("password"),
                                rs.getString("email"),
                                rs.getLong("created_at"));

                // return the create object
                return user;
            } else {
                System.out.println("No user found");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        // Return null
        return user;
    }

    /**
     * Get all users in database
     *
     * @return
     */
    public static ArrayList<User> getUsers() {

        // Check for DB connection
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        // Build SQL
        String sql = "SELECT * FROM user";

        // Do the query and initialyze an empty list for use if we don't get results
        ResultSet rs = dbCon.query(sql);
        ArrayList<User> users = new ArrayList<User>();

        try {
            // Loop through DB Data
            while (rs.next()) {
                User user =
                        new User(
                                rs.getInt("id"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("password"),
                                rs.getString("email"),
                                rs.getLong("created_at"));

                // Add element to list
                users.add(user);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }

        // Return the list of users
        return users;
    }

    public static User createUser(User user) {

        // Write in log that we've reach this step
        Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

        // Set creation time for user.
        user.setCreatedTime(System.currentTimeMillis() / 1000L);

        // Check for DB Connection
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        // Insert the user in the DB
        // TODO: Hash the user password before saving it. :FIX
        int userID = dbCon.insert(
                "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('" + user.getFirstname()
                        + "', '"
                        + user.getLastname()
                        + "', '"
                        //Hashing the user password with sha hashing before saving it when creating a user (Gemme saltet i databasen eller lave et nyt salt)
                        + Hashing.shaWithSalt(user.getPassword())
                        + "', '"
                        + user.getEmail()
                        + "', "
                        + user.getCreatedTime()
                        + ")");

        if (userID != 0) {
            //Update the userid of the user before returning
            user.setId(userID);
        } else {
            // Return null if user has not been inserted into database
            return null;
        }

        // Return user
        return user;
    }


    public static boolean delete(int id) {

        // Writing in the log what we are doing
        Log.writeLog(UserController.class.getName(), id, "Deleting a user in DB", 0);

        // Checking for the DB connection
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        User user = UserController.getUser(id);

        // Checking if there's a user object with any information in it
        if (user != null) {
            //Running the method and statement to delete a user on an id
            dbCon.deleteUpdate("DELETE FROM user WHERE id =" + id);
            return true;
        } else {
            // Returns false if user equals to null
            return false;
        }
    }

    public static boolean update(User user, int userId) {

        // Writing in the log what we are doing
        Log.writeLog(UserController.class.getName(), user, "Updating a user in DB", 0);

        // Checking for the DB connection
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        // Checking if there's a user object with any information in it
        if (user != null) {
            //Running the method and statement to delete a user on an id
            dbCon.deleteUpdate("UPDATE user SET first_name = '" + user.getFirstname()
                    + "', last_name = '" + user.getLastname()
                    + "', password = '" + Hashing.shaWithSalt(user.getPassword())
                    + "', email = '" + user.getEmail()
                    + "' WHERE id = " + userId);
            return true;
        } else {
            // Returns false if user equals to null
            return false;
        }
    }

    public static String login(User loginUser) {

        // Writing in the log what we are doing
        Log.writeLog(UserController.class.getName(), loginUser, "Logging in a user", 0);

        // Checking for the DB connection
        if (dbCon == null) {
            dbCon = new DatabaseController();
        }

        UserCache userCache = new UserCache();
        ArrayList<User> users = userCache.getUsers(false);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        for (User user : users) {
            if (user.getEmail().equals(loginUser.getEmail())
                    && user.getPassword().equals(Hashing.shaWithSalt(loginUser.getPassword()))) {

                try {
                    Algorithm algorithm = Algorithm.HMAC256("secret_tokenkey");
                    // Using .withClaim and generate a token from the key and a timestamp
                    String token = JWT.create().withIssuer("auth0").withClaim("test_tokenkey", timestamp).
                            withClaim("test", user.getId()).sign(algorithm);

                    user.setToken(token);
                    return token;
                } catch (JWTCreationException exception) {
                    //Invalid Signing configuration / Couldn't convert Claims.
                    exception.getMessage();
                }
            }

        }
        return null;
    }

    public static DecodedJWT verifier (String user) {

        Log.writeLog(UserController.class.getName(), user, "Verifying a token", 0);

        String token = user;

        try {
            Algorithm algorithm = Algorithm.HMAC256("secret_tokenkey");
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);

            return jwt;
        } catch (JWTVerificationException exception){
            //Invalid signature/claims
            exception.getMessage();
        }

        return null;
    }
}
