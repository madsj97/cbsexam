package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import controllers.UserController;

import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

    private static UserCache userCache = new UserCache();

    /**
     * @param idUser
     * @return Responses
     */
    @GET
    @Path("/{idUser}")
    public Response getUser(@PathParam("idUser") int idUser) {

        // Use the ID to get the user from the controller.
        User user = UserController.getUser(idUser);

        // TODO: Add Encryption to JSON :FIX
        // Convert the user object to json in order to return the object
        String json = new Gson().toJson(user);
        //Added the encryption to the json
        json = Encryption.encryptDecryptXOR(json);

        // TODO: What should happen if something breaks down? :FIX
        if (user != null) {
            // Return the user with the status code 200
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } else {
            // Returns status code 400
            return Response.status(400).entity("Could not get user by id").build();
        }
    }

    /**
     * @return Responses
     */
    @GET
    @Path("/")
    public Response getUsers() {

        // Write to log that we are here
        Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

        // Get a list of users -- Added caching
        //ArrayList<User> users = UserController.getUsers();
        ArrayList<User> users = userCache.getUsers(false);

        // TODO: Add Encryption to JSON :FIX
        // Transfer users to json in order to return it to the user
        String json = new Gson().toJson(users);
        json = Encryption.encryptDecryptXOR(json);

        if (users != null) {
            // Return the users with the status code 200
            return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
        } else {
            // Returns status code 400
            return Response.status(400).entity("Could not get users").build();
        }
    }

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createUser(String body) {

        // Read the json from body and transfer it to a user class
        User newUser = new Gson().fromJson(body, User.class);

        // Use the controller to add the user
        User createUser = UserController.createUser(newUser);

        // Get the user back with the added ID and return it to the user
        String json = new Gson().toJson(createUser);

        // Return the data to the user
        if (createUser != null) {
            // Return a response with status 200 and JSON as type
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
        } else {
            // Return status code 400
            return Response.status(400).entity("Could not create user").build();
        }
    }

    // TODO: Make the system able to login users and assign them a token to use throughout the system. :FIX
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginUser(String body) {

        // Read the json from loginUser and transfer it to a user class
        User loginUser = new Gson().fromJson(body, User.class);

        //Getting the token
        String token = UserController.login(loginUser);

        if (token != null) {
            // Returns status code 200 and a message with the users token
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("You're now logged in - your token is: \n" + token).build();
        } else {
            // Returns status code 400
            return Response.status(400).entity("Incorrect email or password - Try again!").build();
        }
    }

    // TODO: Make the system able to delete users :FIX
    @DELETE
    @Path("/delete/{userId}")
    public Response deleteUser(@PathParam("userId") int id, String body) {

        //Verifying the information in the body (where the token is intented to be)
        DecodedJWT token = UserController.verifier(body);

        //Checking if the id the user wants to delete matches the user itselfs id, since you cant delete any users other than yourself
        if (token.getClaim("test").asInt() == id) {

            Boolean delete = UserController.delete(token.getClaim("test").asInt());

            //Checking if the boolean is true
            if (delete) {
                userCache.getUsers(true);
                // Return a response with status 200 and JSON as type
                return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("You deleted your user").build();
            } else {
                // Returns a response with status code 400
                return Response.status(400).entity("The user was not found, and therefore not deleted").build();
            }
        } else {
            // Returns a response with status code 400
            return Response.status(400).entity("You cant delete any other users than your own").build();
        }
    }

    // TODO: Make the system able to update users :FIX
    @POST
    @Path("/update/{userId}/{token}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("userId") int id, @PathParam("token") String token, String body) {

        // Read the json from user and transfer it to a user class
        User user = new Gson().fromJson(body, User.class);

        //Verifying the token from the url
        DecodedJWT jwt = UserController.verifier(token);

        Boolean update = UserController.update(user, jwt.getClaim("test").asInt());

        //Update the cache
        userCache.getUsers(true);

        //Checking if the boolean is true
        if (update) {
            // Return a response with status 200 and JSON as type and the user that's been updated
            return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Updated the user with the id: " + id).build();
        } else {
            // Returns a response with status code 400
            return Response.status(400).entity("The user was not found, and therefore not updated").build();
        }
    }
}
