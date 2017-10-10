import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;

import java.util.List;
import java.util.ArrayList;

public class Main{
    public static ConnectorConfig config = new ConnectorConfig();
    public static PartnerConnection connection;
    public static void main(String[] args) {

        config.setUsername("");
        config.setPassword("");

        queryFLAs();

        }

        public static void queryFLAs(){
            try {
                QueryResult queryResults = connection.query("SELECT Id, Name, Status__c, Lease_Agreement_Type__c, OwnerId, Requestor__c, Account_Name__c, Master_Customer_Num__c, Contact_Name__c, Brewer_Installation_Method__c, Special_Instructions__c, Project_Manager__c, Service_Provider__c, Number_of_Installation_Locations__c, Lease_Term__c, Hanging_Allowance__c FROM Facility_Lease_Agreement__c Where CreatedDate >= 2014-01-01T00:00:00Z AND CreatedDate <= 2014-02-01T00:00:00Z limit 10 ");

                if (queryResults.getSize() > 0) {
                    SObject[] records = queryResults.getRecords();


                    List<SObject> listImplementations = new ArrayList<SObject>();

                    for (int i = 0; i < records.length; i++) {

                        SObject facility_lease_agreement = records[i];




                        listImplementations.add(facility_lease_agreement);
/*
                        dbSelect(flaId);
*/

                        System.out.println("FLA ID: " + facility_lease_agreement.getId());
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void createImplementations(SObject facility_lease_agreement){
            try{
                connection = Connector.newConnection(config);

                SObject implementation = new SObject();
                implementation.setType("Boss_Implemenation__c");

                implementation.setField("Name", facility_lease_agreement.getField("Name"));
                implementation.setField("Account__c", facility_lease_agreement.getField("Account_Name__c"));
                implementation.setField("Status__c", facility_lease_agreement.getField("Status__c"));
                implementation.setField("Type__c", facility_lease_agreement.getField("Lease_Agreement_Type__c"));

                /*
                Object flaStatus                = facility_lease_agreement.getField("Status__c");
                Object flaName                  = facility_lease_agreement.getField("Name");
                Object flaType                  = facility_lease_agreement.getField("Lease_Agreement_Type__c");
                Object flaRequestor             = facility_lease_agreement.getField("Requestor__c");
                Object flaBrewerInstallMethod   = facility_lease_agreement.getField("Brewer_Installation_Method__c");
                Object flaBrewerAccountId       = facility_lease_agreement.getField("Account_Name__c");
                Object flaBrewerContactId       = facility_lease_agreement.getField("Contact_Name__c");
                Object flaMasterCustomerNumber  = facility_lease_agreement.getField("Master_Customer_Num__c");
                Object flaMasterOwnerId         = facility_lease_agreement.getField("OwnerId");
                Object flaBrewerSpecialInstructions   = facility_lease_agreement.getField("Special_Instructions__c");
                Object flaProjectManager        = facility_lease_agreement.getField("Project_Manager__c");
                Object flaServiceProvider       = facility_lease_agreement.getField("Service_Provider__c");
                Object flaNumInstallLocations   = facility_lease_agreement.getField("Number_of_Installation_Locations__c");
                Object flaLeaseTerm             = facility_lease_agreement.getField("Lease_Term__c");
                Object flaHangingAllowance      = facility_lease_agreement.getField("Hanging_Allowance__c");
                */

                connection.create(new SObject[]{implementation});

            }catch(ConnectionException ce){
                ce.printStackTrace();
            }
        }



}

