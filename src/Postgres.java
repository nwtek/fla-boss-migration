import com.sforce.soap.partner.QueryResult;

import java.sql.*;
import java.util.List;

public class Postgres {

    static Connection postgresConnection;

        public static void main( String args[] ) {

            dbOpen();
                //queryFLAs();
            dbClose();

        }



    public static String dbSelect(String flaId) {

        try {
            postgresConnection.setAutoCommit(false);

            PreparedStatement pstmt = postgresConnection.prepareStatement("SELECT * FROM apollo_fla Where id = ?");
            pstmt.setString(1, flaId);
            System.out.println("prepared statment: " + pstmt.toString());

            ResultSet rs = pstmt.executeQuery();
            while ( rs.next() ) {
                //int id = rs.getInt("id");
                String accountId            = rs.getString("account_name__c");
                String contactId            = rs.getString("contact_name__c");
                String flaID                = rs.getString("id");
                String leaseType            = rs.getString("lease_agreement_type__c");
                String specialInstructions  = rs.getString("special_instructions__c");
                String status               = rs.getString("status__c");
                String customerNumber       = rs.getString("customer");
                String orderNumber          = rs.getString("invision_order_number__c");

                //SITE
                String street               = rs.getString("address_1");
                String suite                = rs.getString("address_3");
                String city                 = rs.getString("city");
                String state                = rs.getString("state");
                String zip                  = rs.getString("zip_code_p1");


                //ASSET
                String staplesSku           = rs.getString("staples_sku");
                Integer quantity            = rs.getInt("qty");


                System.out.println( "invision order number: " + orderNumber );
                System.out.println( "street: " + street );
                System.out.println("---");
            }
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }
        return "Operation done successfully";
    }


    public static String dbOpen (){
        postgresConnection = null;
        try {
            Class.forName("org.postgresql.Driver");
            postgresConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/apollo_fla","heran004", "");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        return "Opened database successfully";
    }

    public static void dbClose() {
        try{
            postgresConnection.close();
        }catch(Exception e){
            System.err.println(e.getClass().getName()+": "+e.getMessage());
        }

    }
}
