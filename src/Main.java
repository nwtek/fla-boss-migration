import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;

import java.util.ArrayList;

public class Main{
    public static ConnectorConfig config = new ConnectorConfig();
    public static PartnerConnection connection;
    public static void main(String[] args) {

        config.setUsername("");
        config.setPassword("");

        }

        public static void queryFLAs(){
            try {
                QueryResult queryResults = connection.query("SELECT Id, Opportunity__c, Account_Name__c, Contact_Name__c, Status__c, CreatedDate FROM Facility_Lease_Agreement__c limit 10 ");

                if (queryResults.getSize() > 0) {
                    SObject[] records = queryResults.getRecords();

                    for (int i = 0; i < records.length; i++) {

                        SObject facility_lease_agreement = records[i];
                        Object flaStatus = facility_lease_agreement.getField("Status__c");
                        //Object flaStatus = facility_lease_agreement.getField("Status__c");


                        System.out.println("FLA ID: " + facility_lease_agreement.getId() + " Status: " + flaStatus);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void createImplementations(){
            try{
                connection = Connector.newConnection(config);
                SObject account = new SObject();
                account.setType("Account");
                account.setField("Name", "Test Account 2");
                connection.create(new SObject[]{account});

            }catch(ConnectionException ce){
                ce.printStackTrace();
            }
        }

}

