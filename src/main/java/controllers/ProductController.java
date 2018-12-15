package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import cache.ProductCache;
import model.Product;
import utils.Log;

public class ProductController {

  private static DatabaseController dbCon;

  public ProductController() {
    dbCon = new DatabaseController();
  }

  public static Product getProduct(int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the SQL query for the DB
    String sql = "SELECT * FROM product where id=" + id;

    // Run the query in the DB and make an empty object to return
    ResultSet rs = dbCon.query(sql);
    Product product = null;

    try {
      // Get first row and create the object and return it
      if (rs.next()) {
        product =
            new Product(
                rs.getInt("id"),
                rs.getString("product_name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"));

        // Return the product
        return product;
      } else {
        System.out.println("No product found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return empty object
    return product;
  }

  public static Product getProductBySku(String sku) {

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //SQL statement that we want to run
    String sql = "SELECT * FROM product where sku='" + sku + "'";

    // Run the query in the DB and make an empty object to return
    ResultSet rs = dbCon.query(sql);
    Product product = null;

    try {
      // Get first row and create the object and return it
      if (rs.next()) {
        product =
            new Product(
                rs.getInt("id"),
                rs.getString("product_name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"));

        // Return the product
        return product;
      } else {
        System.out.println("No product found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    //Return empty object
    return product;
  }

  /**
   * Get all products in database
   *
   * @return
   */
  public static ArrayList<Product> getProducts() {

    //Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // TODO: Use caching layer. :FIX
    //Our sql statement
    String sql = "SELECT * FROM product";

    // Run the query in the DB and make an arraylist for storing
    ResultSet rs = dbCon.query(sql);
    ArrayList<Product> products = new ArrayList<Product>();

    try {
      //Loops while theres still objects in the resultset
      while (rs.next()) {
        Product product =
            new Product(
                rs.getInt("id"),
                rs.getString("product_name"),
                rs.getString("sku"),
                rs.getFloat("price"),
                rs.getString("description"),
                rs.getInt("stock"));

        // Adding the products to the arraylist
        products.add(product);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }
    //Return all the products
    return products;
  }

  public static Product createProduct(Product product) {

    // Write in log that we've reach this step
    Log.writeLog(ProductController.class.getName(), product, "Actually creating a product in DB", 0);

    // Set creation time for product.
    product.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the product in the DB
    int productID = dbCon.insert(
        "INSERT INTO product(product_name, sku, price, description, stock, created_at) VALUES('"
            + product.getName()
            + "', '"
            + product.getSku()
            + "', '"
            + product.getPrice()
            + "', '"
            + product.getDescription()
            + "', '"
            + product.getStock()
            + "', '"
            + product.getCreatedTime()
            + "')");

    if (productID != 0) {
      //Update the productid of the product before returning
      product.setId(productID);
    } else{
      // Return null if product has not been inserted into database
      return null;
    }

    // Return product
    return product;
  }
}
