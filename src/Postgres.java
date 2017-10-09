import com.sforce.soap.partner.QueryResult;

import java.sql.*;
import java.util.List;

public class Postgres {

    static Connection postgresConnection;
        public static void main( String args[] ) {

            dbOpen();
                queryFLAs();
            dbClose();

        }

    private static void queryFLAs(){

        try {
            QueryResult queryResults = sfdcConnection.query("SELECT Id, Name, Status__c, Lease_Agreement_Type__c, OwnerId, Requestor__c, Account_Name__c, Master_Customer_Num__c, Contact_Name__c, Brewer_Installation_Method__c, Special_Instructions__c, Project_Manager__c, Service_Provider__c, Number_of_Installation_Locations__c, Lease_Term__c, Hanging_Allowance__c FROM Facility_Lease_Agreement__c Where CreatedDate >= 2014-01-01T00:00:00Z AND CreatedDate <= 2014-02-01T00:00:00Z limit 10 ");

            List<Boss_Implementation__c> implementations = new ArrayList<Boss_Implementation__c>();

            if (queryResults.getSize() > 0) {
                for (int i = 0; i < queryResults.getRecords().length; i++) {
                    Facility_Lease_Agreement__c fla = (Facility_Lease_Agreement__c) queryResults.getRecords()[i];

                    String flaId = fla.getId();
                    System.out.println("FACILITY LEASE AGREEMENT: " + flaId);

                    Boss_Implementation__c implementation = new Boss_Implementation__c();

                    SObject imp = new SObject();
                    imp.setField('test', 'test');

                    //implementation.setFacility_Lease_Agreement__c(flaId);
                    implementation.setField
                    implementation.setName(fla.getName());
                    implementation.setAccount__c(fla.getAccount_Name__c());
                    implementation.setStatus__c(fla.getStatus__c());
                    implementation.setType__c(fla.getLease_Agreement_Type__c());

                    //FLA HAS PICKLIST OF PM'S, WILL NEED TO TRANSLATE
                    //implementation.setStaples_Project_Manager__c();

                    implementations.add(implementation);

/*
                        Id => FLA Lookup Field
                        Name => Implementation Name
                        Status__c   =>
                        Lease_Agreement_Type__c
                        OwnerId
                        Requestor__c
                        Account_Name__c
                        Master_Customer_Num__c
                        Contact_Name__c
                        Brewer_Installation_Method__c
                        Special_Instructions__c
                        Project_Manager__c
                        Service_Provider__c
                        Number_of_Installation_Locations__c
                        Lease_Term__c
                        Hanging_Allowance__c
*/
                    dbSelect(flaId);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
