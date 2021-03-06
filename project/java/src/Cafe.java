/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
// import java.io.File;
// import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Cafe {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
         new InputStreamReader(System.in));

   /**
    * Creates a new instance of Cafe
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Cafe(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try {
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      } catch (Exception e) {
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      } // end catch
   }// end Cafe

   /**
    * Method to execute an update SQL statement. Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate(String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the update instruction
      stmt.executeUpdate(sql);

      // close the instruction
      stmt.close();
   }// end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()) {
         if (outputHeader) {
            for (int i = 1; i <= numCol; i++) {
               System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i = 1; i <= numCol; ++i) {
            System.out.print(rs.getString(i) + "\t");
         }
         System.out.println();
         ++rowCount;
      } // end while
      stmt.close();
      return rowCount;
   }// end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      /*
       ** obtains the metadata object for the returned result set. The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData();
      int numCol = rsmd.getColumnCount();
      // int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      // boolean outputHeader = false;
      List<List<String>> result = new ArrayList<List<String>>();
      while (rs.next()) {
         List<String> record = new ArrayList<String>();
         for (int i = 1; i <= numCol; ++i)
            record.add(rs.getString(i));
         result.add(record);
      } // end while
      stmt.close();
      return result;
   }// end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT). This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery(String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery(query);

      int rowCount = 0;

      // iterates through the result set and count nuber of results.
      if (rs.next()) {
         rowCount++;
      } // end while
      stmt.close();
      return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement();

      ResultSet rs = stmt.executeQuery(String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup() {
      try {
         if (this._connection != null) {
            this._connection.close();
         } // end if
      } catch (SQLException e) {
         // ignored.
      } // end try
   }// end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login
    *             file>
    */
   public static void main(String[] args) {
      if (args.length != 3) {
         System.err.println(
               "Usage: " +
                     "java [-classpath <classpath>] " +
                     Cafe.class.getName() +
                     " <dbname> <port> <user>");
         return;
      } // end if

      Greeting();
      Cafe esql = null;
      try {
         // use postgres JDBC driver.
         Class.forName("org.postgresql.Driver").newInstance();
         // instantiate the Cafe object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Cafe(dbname, dbport, user, "");

         boolean keepon = true;
         while (keepon) {
            // These are sample SQL statements
            System.out.println("\nMAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorizedUser = null;
            switch (readChoice()) {
               case 1:
                  CreateUser(esql);
                  break;
               case 2:
                  authorizedUser = LogIn(esql);
                  break;
               case 9:
                  keepon = false;
                  break;
               default:
                  System.out.println("Unrecognized choice, try again.");
                  break;
            }// end switch
            if (authorizedUser != null) {
               boolean usermenu = true;
               while (usermenu) {
                  System.out.println("\nMAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. Go to Menu");
                  System.out.println("2. Update Profile");
                  System.out.println("3. Place an Order");
                  System.out.println("4. Update an Order");
                  System.out.println(".........................");
                  System.out.println("9. Log Out");
                  switch (readChoice()) {
                     case 1:
                        Menu(esql, authorizedUser);
                        break;
                     case 2:
                        UpdateProfile(esql);
                        break;
                     case 3:
                        PlaceOrder(esql);
                        break;
                     case 4:
                        UpdateOrder(esql);
                        break;
                     case 9:
                        System.out.println("\nSuccessfully logged out.");
                        usermenu = false;
                        break;
                     default:
                        System.out.println("Unrecognized choice!");
                        break;
                  }
               }
            }
            // FIX: ELSE STATEMENT for authorizedUser == null (Print wrong user or password)
         } // end while
      } catch (Exception e) {
         System.err.println(e.getMessage());
      } finally {
         // make sure to cleanup the created table and close the connection.
         try {
            if (esql != null) {
               System.out.print("\nDisconnecting from the database... ");
               esql.cleanup();
               System.out.println("Done!\n\nBye!");
            } // end if
         } catch (Exception e) {
            // ignored.
         } // end try
      } // end try
   }// end main

   public static void Greeting() {
      System.out.println(
            "\n\n*******************************************************\n" +
                  "              User Interface      	               \n" +
                  "*******************************************************\n");
   }// end Greeting

   /*`
    * Reads the users choice given from the keyboard
    * 
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("\nPlease make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         } catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         } // end try
      } while (true);
      return input;
   }// end readChoice

   /*
    * Creates a new user with provided login, passowrd and phoneNum
    **/
   public static void CreateUser(Cafe esql) {
      try {
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user phone: ");
         String phone = in.readLine();

         String type = "Customer";
         String favItems = "";

         // FIX: (EC) Add check if the login is already taken
         // FIX: (EC) Add a check if the password does not contain certain characters
         String query = String.format(
               "INSERT INTO USERS (phoneNum, login, password, favItems, type) VALUES ('%s','%s','%s','%s','%s')", phone,
               login, password, favItems, type);

         esql.executeUpdate(query);
         System.out.println("User successfully created!");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }// end CreateUser

   /*
    * Check log in credentials for an existing user
    * 
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Cafe esql) {
      try {
         System.out.print("\nEnter user login: ");
         String login = in.readLine();
         System.out.print("Enter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USERS WHERE login = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0) {
            String welcome = String.format("\nLogin successful. Welcome, %s!", login);
            System.out.println(welcome);
            return login;
         } else {
            System.out.println("\nLogin not found! Please try again.");
            return null;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
         return null;
      }
   }// end

   // FIX: Print Menu
   // FIX: Search by itemName
   // FIX: Search by type
   // FIX: If manager, add option to add/delete/update items
   // FIX: Create an option to go back to the Main Menu
   public static void Menu(Cafe esql, String authorizedUser) {
      try {
         // Check if the user is a Manager
         boolean isManager = false;
         String query = String.format("SELECT * FROM Users U WHERE U.login = '%s' AND U.type = 'Manager,,,'", authorizedUser);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            isManager = true;

         System.out.println("\nMENU OPTIONS");
         System.out.println("---------");
         System.out.println("1. Browse Menu");
         System.out.println("2. Search By Name");
         System.out.println("3. Search By Category");
         if (isManager) System.out.println("4. Update Menu");
         System.out.println(".........................");
         System.out.println("9. Return to Main Menu");
         int rowNum = 0;

         switch (readChoice()) {

            case 1:
               System.out.println("\nDrinks:\n-------------------------");
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Drinks'");
               rowNum = esql.executeQueryAndPrintResult(query);
               System.out.println(String.format("(%d items)", rowNum));

               System.out.println("\nSweets:\n-------------------------");
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Sweets'");
               rowNum = esql.executeQueryAndPrintResult(query);
               System.out.println(String.format("(%d items)", rowNum));

               System.out.println("\nSoup:\n-------------------------");
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = 'Soup'");
               rowNum = esql.executeQueryAndPrintResult(query);
               System.out.println(String.format("(%d items)", rowNum));
               break;

            case 2:
               System.out.print("\nEnter item name: ");
               String itemName = in.readLine();
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.itemName = '%s'", itemName);
               rowNum = esql.executeQueryAndPrintResult(query);
               if (rowNum > 0) {
                  System.out.println(String.format("(%d items)", rowNum));
               } else {
                  System.out.println("Item not found, please try again.");
               }
               break;

            case 3:
               System.out.print("\nEnter 'Drinks', 'Sweets', or 'Soup': ");
               String type = in.readLine();
               query = String.format("SELECT M.itemName AS Name, M.price AS Price, M.description AS Types FROM Menu M WHERE M.type = '%s'", type);
               rowNum = esql.executeQueryAndPrintResult(query);
               if (rowNum > 0) {
                  System.out.println(String.format("(%d items)", rowNum));
               } else {
                  System.out.println("Invalid input, please try again.");
               } 
               break;

            case 4:
               if (!isManager) 
                  System.out.println("Unrecognized choice!");
               break;
            case 9:
               break;
            default:
               System.out.println("Unrecognized choice!");
               break;
         }
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }
   
   /* FIX: Can only update your own information (phonenumber, favItems, password)
   // FIX: Managers can choose which user to modify (maybe don't include changing logins)
   // FIX: Create an option to go back to the Main Menu */
   public static void UpdateProfile(Cafe esql) {
   }

   /* ORDER
    * orderid serial UNIQUE NOT NULL,
    * login char(50), 
    * paid boolean,
    * timeStampRecieved timestamp NOT NULL,
    * total real NOT NULL,
   
    * ITEMSTATUS
    * orderid integer,
	 * itemName char(50), 
	 * lastUpdated timestamp NOT NULL,
	 * status char(20), 
	 * comments char(130), 

   // FIX: Create a unique orderid using getCurrSeqVal
   // FIX: Automatically show menu items and price
   // FIX: Output the current order items every time they add a new item
   // FIX: Menu Option: Done with order
   // FIX: Ask for what items, create the item's status and keep a running total?
   // FIX: Ask for any comments between each item
   // FIX: Set the last updated status to when they create the order (www.javapoint.com/java-timestamp) */
   public static void PlaceOrder(Cafe esql) {
   }

   /* FIX: View 5 most recent orders (Order history)
   // FIX: If customer
   //        Menu Option: Type in orderID and modify it if its not paid
   // FIX: If employee or manager
   //        Menu Option: Type in orderID and modify it regardless
   //        Menu Option: View all orders within the last 24 hours */
   public static void UpdateOrder(Cafe esql) {
   }

}// end Cafe

// For debugging
// System.out.println(String.format("itemName = %s, rowNum = %d", itemName, rowNum));