package com.cbsexam;

import cache.UserCache;
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
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down? :FIX
    if(user != null) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not get user by id").build();
    }
  }

  /** @return Responses */
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

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
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
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }

  // TODO: Make the system able to delete users :FIX
  @DELETE
  @Path("/delete/{userId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response deleteUser(@PathParam("userId") int id) {

    Boolean delete = UserController.delete(id);

    userCache.getUsers(true);

    // Return a response with status 200 and JSON as type
    if (delete) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Deleted the user with the id:" + id).build();
    } else {
      return Response.status(400).entity("The user was not found, and therefore not deleted").build();
    }
  }

  // TODO: Make the system able to update users :FIX
  @POST
  @Path("/update/{userId}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response updateUser(@PathParam("userId") int userId, String body) {

    User user = new Gson().fromJson(body, User.class);

    Boolean update = UserController.update(user, userId);

    userCache.getUsers(true);

    // Return a response with status 200 and JSON as type
    if (update) {
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Updated the user with the id:" + userId).build();
    } else {
      return Response.status(400).entity("The user was not found, and therefore not updated").build();
    }
  }
}
