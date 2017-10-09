import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Postgres {


        public static void main( String args[] ) {

            //parseInsert();


	   /*
	   Scanner reader = new Scanner(System.in);  // Reading from System.in
	   System.out.println("What would you like to do? (dbOpen | dbCreateTable | dbInsert | dbSelect | dbUpdate | dbDelete): ");
	   String input = reader.nextLine(); // Scans the next token of the input as an int.
	   System.out.println("Executing...");



	   switch (input){
		   case "dbInsert" 	: response = dbInsert();
		   case "dbOpen" 	: response = dbOpen();
		   case "dbUpdate" 	: response = dbUpdate();
		   case "dbDelete" 	: response = dbDelete();
		   case "dbSelect" 	: response = dbSelect();
		   default: response = "Not a recognized command";
	   }


	   */

            //System.out.println("Response : " + attachProperties());

        }

        public static String dbOpen (){
            Connection c = null;
            try {
                Class.forName("org.postgresql.Driver");
                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/testdb","postgres", "123");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println(e.getClass().getName()+": "+e.getMessage());
                System.exit(0);
            }
            System.out.println("Opened database successfully");
            return "Opened database successfully";
        }

        public static String dbCreateTable() {
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.postgresql.Driver");
                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");

                System.out.println("Opened database successfully");

                stmt = c.createStatement();
                String sql = "CREATE TABLE COMPANY " +
                        "(ID INT PRIMARY KEY     NOT NULL," +
                        " NAME           TEXT    NOT NULL, " +
                        " AGE            INT     NOT NULL, " +
                        " ADDRESS        CHAR(50), " +
                        " SALARY         REAL)";
                stmt.executeUpdate(sql);
                stmt.close();
                c.close();
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                System.exit(0);
            }
            return "Table created successfully";
        }

        public static String dbInsert() {
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.postgresql.Driver");
                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");

                stmt = c.createStatement();
                String sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
                        + "VALUES (1, 'Paul', 32, 'California', 20000.00 );";
                stmt.executeUpdate(sql);

                sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
                        + "VALUES (2, 'Allen', 25, 'Texas', 15000.00 );";
                stmt.executeUpdate(sql);

                sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
                        + "VALUES (3, 'Teddy', 23, 'Norway', 20000.00 );";
                stmt.executeUpdate(sql);

                sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) "
                        + "VALUES (4, 'Mark', 25, 'Rich-Mond ', 65000.00 );";
                stmt.executeUpdate(sql);

                stmt.close();
                c.commit();
                c.close();
            } catch (Exception e) {
                System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                System.exit(0);
            }
            return "Records created successfully";
        }

        public static String parseInsert() {
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.postgresql.Driver");
                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");

	         /*
	          SHOW ALL TABLES
	         DatabaseMetaData md = c.getMetaData();
	         ResultSet rs = md.getTables(null, null, "%", null);
	         while (rs.next()) {
	           System.out.println(rs.getString(3));
	         }
	         */
                stmt = c.createStatement();

                String sql = "INSERT INTO PARSETESTONE (SHIPTO, CUSTOMERNAME, ADDRESS, ORDERCONTACT, SKU, MODEL, QUANTITY) "
                        + "VALUES ('Test Ship To 2', 'James Inc.', '123 E. James Street, Boulder, CO 98342', 'James Smith', '934BO', '2014 Model', 10);";
                stmt.executeUpdate(sql);

                stmt.close();
                c.commit();
                c.close();
            } catch (Exception e) {
                System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                System.exit(0);
            }
            return "Records created successfully";
        }

        public static String dbSelect() {
            Connection c = null;
            Statement stmt = null;
            try {
                Class.forName("org.postgresql.Driver");
                c = DriverManager
                        .getConnection("jdbc:postgresql://localhost:5432/postgres","postgres", "123");
                c.setAutoCommit(false);
                System.out.println("Opened database successfully");

                stmt = c.createStatement();
                ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
                while ( rs.next() ) {
                    int id = rs.getInt("id");
                    String  name = rs.getString("name");
                    int age  = rs.getInt("age");
                    String  address = rs.getString("address");
                    float salary = rs.getFloat("salary");
                    System.out.println( "ID = " + id );
                    System.out.println( "NAME = " + name );
                    System.out.println( "AGE = " + age );
                    System.out.println( "ADDRESS = " + address );
                    System.out.println( "SALARY = " + salary );
                    System.out.println();
                }
                rs.close();
                stmt.close();
                c.close();
            } catch ( Exception e ) {
                System.err.println( e.getClass().getName()+": "+ e.getMessage() );
                System.exit(0);
            }
            return "Operation done successfully";
        }


}
