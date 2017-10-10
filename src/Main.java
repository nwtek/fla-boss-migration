import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;

public class Main{
    public static ConnectorConfig config = new ConnectorConfig();
    public static PartnerConnection connection;
    public static void main(String[] args) {

        config.setUsername("");
        config.setPassword("");

        }

        public static void queryFLAs(){
            try {
                QueryResult queryResults = connection.query("SELECT Id, Name, Status__c, Lease_Agreement_Type__c, OwnerId, Requestor__c, Account_Name__c, Master_Customer_Num__c, Contact_Name__c, Brewer_Installation_Method__c, Special_Instructions__c, Project_Manager__c, Service_Provider__c, Number_of_Installation_Locations__c, Lease_Term__c, Hanging_Allowance__c FROM Facility_Lease_Agreement__c Where CreatedDate >= 2014-01-01T00:00:00Z AND CreatedDate <= 2014-02-01T00:00:00Z limit 10 ");

                if (queryResults.getSize() > 0) {
                    SObject[] records = queryResults.getRecords();

                    for (int i = 0; i < records.length; i++) {

                        SObject facility_lease_agreement = records[i];
                        Object flaStatus        = facility_lease_agreement.getField("Status__c");
                        Object flaName          = facility_lease_agreement.getField("Name");
                        Object flaType          = facility_lease_agreement.getField("Lease_Agreement_Type__c");
                        Object flaRequestor     = facility_lease_agreement.getField("Requestor__c");



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

