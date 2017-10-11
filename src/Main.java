import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;

import java.util.List;
import java.util.ArrayList;

public class Main{
    public static ConnectorConfig config = new ConnectorConfig();
    public static PartnerConnection connection;
    public static void main(String[] args) {
        try {
            config.setUsername("");
            config.setPassword("");
            connection = Connector.newConnection(config);

            queryFLAs();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static void queryFLAs(){
            try {
                QueryResult queryResults = connection.query("SELECT Id, Name, Status__c, Lease_Agreement_Type__c, OwnerId, Requestor__c, Account_Name__c, Master_Customer_Num__c, Contact_Name__c, Brewer_Installation_Method__c, Special_Instructions__c, Project_Manager__c, Service_Provider__c, Number_of_Installation_Locations__c, Lease_Term__c, Hanging_Allowance__c FROM Facility_Lease_Agreement__c Where CreatedDate >= 2014-01-01T00:00:00Z AND CreatedDate <= 2014-02-01T00:00:00Z limit 1 ");

                if (queryResults.getSize() > 0) {
                    SObject[] records = queryResults.getRecords();

                    List<SObject> facilityLeaseAgreements = new ArrayList<SObject>();

                    for (int i = 0; i < records.length; i++) {
                        SObject facility_lease_agreement = records[i];
                        facilityLeaseAgreements.add(facility_lease_agreement);
                        //dbSelect(flaId);
                        System.out.println("FLA ID: " + facility_lease_agreement.getId());
                    }

                    createImplementations(facilityLeaseAgreements);
                }

            } catch (Exception e) {
                System.out.println("ERROR: QUERY FLAs");
                e.printStackTrace();
            }
        }

        public static void createImplementations(List<SObject> facility_lease_agreements){
            try{

                SObject[] newImplementations = new SObject[facility_lease_agreements.size()];

                Integer i = 0;
                for(SObject facility_lease_agreement : facility_lease_agreements){

                    System.out.println("CREATING IMPLEMENTATION FOR: " + facility_lease_agreement.getId());
                    SObject implementation = new SObject();
                    implementation.setType("Boss_Implementation__c");

                    implementation.setField("Name", "(MIGRATED): " + facility_lease_agreement.getField("Name"));

                    implementation.setField("Account__c", facility_lease_agreement.getField("Account_Name__c"));
                    implementation.setField("Status__c", facility_lease_agreement.getField("Status__c"));
                    implementation.setField("Type__c", facility_lease_agreement.getField("Lease_Agreement_Type__c"));
                    implementation.setField("Requestor__c", facility_lease_agreement.getField("Requestor__c"));
                    implementation.setField("Install_Method__c", facility_lease_agreement.getField("Brewer_Installation_Method__c"));
                    implementation.setField("Primary_Customer_Contact__c", facility_lease_agreement.getField("Contact_Name__c"));
                    implementation.setField("Special_Instructions__c", facility_lease_agreement.getField("Special_Instructions__c"));
                    implementation.setField("Vendor__c", facility_lease_agreement.getField("Service_Provider__c"));
                    implementation.setField("Lease_Term__c", facility_lease_agreement.getField("Lease_Term__c"));

                    implementation.setField("OwnerId", facility_lease_agreement.getField("OwnerId"));
                    implementation.setField("Facility_Lease_Agreement__c", facility_lease_agreement.getId());

                    /*
                        NO CUSTOMER NUMBER ON IMPLEMENTATION
                        implementation.setField("", facility_lease_agreement.getField("Master_Customer_Num__c"));
                        PICKLIST ON FLA AND USER ON IMPLEMENTATION
                        implementation.setField("", facility_lease_agreement.getField("Project_Manager__c"));
                        TEXT ON FLA ROLLUP ON IMPLEMENTATION
                        implementation.setField("", facility_lease_agreement.getField("Number_of_Installation_Locations__c"));
                        CHECKBOX ON FLA BUT NUMBER FIELD ON IMPLEMENTATION

                    */

                    newImplementations[i] = implementation;
                    i++;
                }

                System.out.println("FLA SIZE: " + facility_lease_agreements.size() + " IMPLEMENTATION SIZE: " + newImplementations.length);

                SaveResult[] saveResults = connection.create(newImplementations);

                List<String> successIds = processResults(saveResults);

                //UPDATE FLA'S
                updateFacilityLeaseAgreement(successIds);


            }catch(Exception e){
                System.out.println("ERROR: CREATE IMPLEMENTATIONS");
                e.printStackTrace();
            }
        }

        //NEED BOTH THE FLA ID AND IMPLEMENTATION ID
        public static void updateFacilityLeaseAgreement(List<String> implementationIds) {
            try {
                //TODO: RESOLVE LIST QUERY
            QueryResult queryResults = connection.query("SELECT Id, Facility_Lease_Agreement__c FROM Boss_Implementation__c Where ID = : implementationIds ");

            if (queryResults.getSize() > 0) {
                SObject[] records = queryResults.getRecords();

                SObject[] flasToUpdate = new SObject[records.length];

                for (int i = 0; i < records.length; i++) {
                    SObject implementation = records[i];
                    Object facilityLeaseAgreementId = implementation.getField("Facility_Lease_Agreement__c");
                    Object implementationId         = implementation.getId();

                    SObject facilityLeaseAgreement = new SObject();
                    facilityLeaseAgreement.setType("Facility_Lease_Agreement__c");

                    facilityLeaseAgreement.setId(facilityLeaseAgreementId.toString());
                    facilityLeaseAgreement.setField("Name", "(MIGRATED): " + facilityLeaseAgreement.getField("Name"));
                    facilityLeaseAgreement.setField("BOSS_Implementation__c", implementationId);

                    flasToUpdate[i] = facilityLeaseAgreement;
                }

                SaveResult[] saveResults = connection.update(flasToUpdate);
                List<String> successIds = processResults(saveResults);
            }


            } catch (Exception e) {
                System.out.println("ERROR: CREATE IMPLEMENTATIONS");
                e.printStackTrace();
            }
        }

        public static List<String> processResults(SaveResult[] saveResults){
            List<String> successIds = new ArrayList<>();

            for(SaveResult result : saveResults){
                if (result.isSuccess()) {
                    successIds.add(result.getId());
                    System.out.println("RECORD MODIFIED SUCCESSFULLY: " + result.getId());
                }else{
                    for (int e = 0; e < result.getErrors().length; e++) {
                        Error err = result.getErrors()[e];
                        System.out.println("Errors were found on item " + e);
                        System.out.println("Error code: " + err.getStatusCode().toString());
                        System.out.println("Error message: " + err.getMessage());
                    }
                }
            }

            return successIds;
        }
}

