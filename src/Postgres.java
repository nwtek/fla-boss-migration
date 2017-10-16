import com.sforce.soap.partner.QueryResult;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Postgres {

    static Connection postgresConnection;

        public static void main( String args[] ) {

            //dbOpen();
                //queryFLAs();
            //dbClose();

        }



    public static List<ArrayList<String[]>> dbSelect(String flaId) {
        List<ArrayList<String[]>> resultsArray = new ArrayList<>();

        dbOpen();
        ResultSet rs = null;
        try {
            postgresConnection.setAutoCommit(false);

            PreparedStatement pstmt = postgresConnection.prepareStatement("SELECT * FROM apollo_fla Where id = ?");
            pstmt.setString(1, flaId);
            System.out.println("prepared statment: " + pstmt.toString());


            rs = pstmt.executeQuery();



            while ( rs.next() ) {
                ArrayList<String[]> fieldArray = new ArrayList<String[]>();
                String[] result = new String[18];

                //int id = rs.getInt("id");
                //ASSET
                result[0] = Utilities.fieldCleanseUtil(rs.getString("staples_sku"));
                result[1] = Utilities.fieldCleanseUtil(rs.getString("qty"));

                //ORDER
                result[2] = Utilities.fieldCleanseUtil(rs.getString("invision_order_number__c"));
                result[3] = Utilities.fieldCleanseUtil(rs.getString("create_date"));
                result[9] = Utilities.fieldCleanseUtil(rs.getString("status__c"));

                //IMPLEMENTATION
                result[4] = Utilities.fieldCleanseUtil(rs.getString("account_name__c"));

                //JUNCTION
                result[5] = Utilities.fieldCleanseUtil(rs.getString("contact_name__c"));
                result[6] = Utilities.fieldCleanseUtil(rs.getString("id"));
                result[7] = Utilities.fieldCleanseUtil(rs.getString("lease_agreement_type__c"));
                result[8] = Utilities.fieldCleanseUtil(rs.getString("special_instructions__c"));
                result[10] = Utilities.fieldCleanseUtil(rs.getString("customer"));
                result[11] = Utilities.fieldCleanseUtil(rs.getString("ship_id"));

                //SITE
                result[12] = Utilities.fieldCleanseUtil(rs.getString("address_1"));
                result[13] = Utilities.fieldCleanseUtil(rs.getString("address_3"));
                result[14] = Utilities.fieldCleanseUtil(rs.getString("city"));
                result[15] = Utilities.fieldCleanseUtil(rs.getString("state"));
                result[16] = Utilities.fieldCleanseUtil(rs.getString("zip_code_p1"));
                result[17] = Utilities.fieldCleanseUtil(rs.getString("zip_code_p2"));


                fieldArray.add(result);

                resultsArray.add(fieldArray);

                System.out.println( "invision order number: " + Utilities.fieldCleanseUtil(rs.getString("invision_order_number__c")) );
                System.out.println( "street: " + rs.getString("address_1") );
                System.out.println("---");
            }
            rs.close();
            pstmt.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName()+": "+ e.getMessage() );
            System.exit(0);
        }

        /*
        List<ArrayList<String[]>> apolloResult = Postgres.dbSelect("a1738000002LwZTAA0");
        for (Integer i = 0; i < apolloResult.size(); i++) {
            System.out.println(" ORDER: " + apolloResult.get(i));
            for (String[] field : apolloResult.get(i)) {
                for(Integer j = 0; j < field.length; j++){
                    System.out.println("FIELD: " + field[j]);
                }
            }
        }
        */


        dbClose();
        return resultsArray;
    }


    public static String dbOpen (){
        postgresConnection = null;
        try {
            Class.forName("org.postgresql.Driver");
            postgresConnection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/apollo_fla","", "");
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
