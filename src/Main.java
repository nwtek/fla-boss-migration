import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;
import com.sforce.ws.bind.XmlObject;

import javax.xml.crypto.dsig.XMLObject;
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

            //updateFacilityLeaseAgreement("'a7V1800000059BqEAI'");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static void queryFLAs(){
            try {
                QueryResult queryResults = connection.query("SELECT Id, Name, Status__c, Lease_Agreement_Type__c, OwnerId, Requestor__c, Account_Name__c, Master_Customer_Num__c, Contact_Name__c, Brewer_Installation_Method__c, Special_Instructions__c, Project_Manager__c, Service_Provider__c, Number_of_Installation_Locations__c, Lease_Term__c, Hanging_Allowance__c FROM Facility_Lease_Agreement__c Where CreatedDate >= 2014-01-01T00:00:00Z AND CreatedDate <= 2014-02-01T00:00:00Z And Boss_Implementation__c = Null limit 1 ");

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

                //UPDATE FLA'S
                updateFacilityLeaseAgreement(processResults(saveResults));


            }catch(Exception e){
                System.out.println("ERROR: CREATE IMPLEMENTATIONS");
                e.printStackTrace();
            }
        }

        //NEED BOTH THE FLA ID AND IMPLEMENTATION ID
        public static void updateFacilityLeaseAgreement(String implementationIds) {
            try {
                //TODO: RESOLVE LIST QUERY
            QueryResult queryResults = connection.query("SELECT Id, Facility_Lease_Agreement__c, Facility_Lease_Agreement__r.Name FROM Boss_Implementation__c Where ID IN (" + implementationIds + ")");

            if (queryResults.getSize() > 0) {
                SObject[] records = queryResults.getRecords();

                SObject[] flasToUpdate = new SObject[records.length];

                for (int i = 0; i < records.length; i++) {
                    SObject implementation = records[i];

                    //GET THE RELATED FACILITY LEASE AGREEMENT RECORD
                    XmlObject flaRelatedObject = implementation.getChild("Facility_Lease_Agreement__r");

                    Object facilityLeaseAgreementId     = implementation.getField("Facility_Lease_Agreement__c");
                    Object facilityLeaseAgreementName   = flaRelatedObject.getField("Name");

                    //System.out.println("FLA: " + facilityLeaseAgreementId + " Name: " + facilityLeaseAgreementName);

                    Object implementationId             = implementation.getId();


                    //OPERATION: UPDATE FLA
                    SObject facilityLeaseAgreement = new SObject();
                    facilityLeaseAgreement.setType("Facility_Lease_Agreement__c");
                    facilityLeaseAgreement.setId(facilityLeaseAgreementId.toString());
                    facilityLeaseAgreement.setField("Name", "(MIGRATED): " + facilityLeaseAgreementName);
                    facilityLeaseAgreement.setField("BOSS_Implementation__c", implementationId);
                    flasToUpdate[i] = facilityLeaseAgreement;

                    //OPERATION: QUERY MIGRATED FACILITY LEASE AGREEMENTS

                    //OPERATION: QUERY APOLLO

                    //OPERATION: CREATE SITE

                    //OPERATION: CREATE JUNCION AND BIND TO SITE AND IMPLEMENTATION

                    //OPERATION: CREATE ASSETS AND BIND TO SITE WITH REFERENCE TO JUNCTION AND IMPLEMENTATION


                }

                SaveResult[] saveResults = connection.update(flasToUpdate);
                String successIds = processResults(saveResults);
            }


            } catch (Exception e) {
                System.out.println("ERROR: CREATE IMPLEMENTATIONS");
                e.printStackTrace();
            }
        }

        public static SObject createSite(){

            SObject site = new SObject();
            site.setType("Boss_Site__c");
            site.setField("Name", "");
            site.setField("Account__c", "");
            site.setField("Street__c", "");
            site.setField("Suite__c", "");
            site.setField("City__c", "");
            site.setField("State__c", "");
            site.setField("Zip_Code__c", "");
            site.setField("Ship_To__c", "");

            return site;

        }

        public static void createAsset(){
            SObject junction = new SObject();
            junction.setType("Boss_PS_Junction__c");
            junction.setField("Boss_Site__c", "");
            junction.setField("Serial_Number__c", "");
            junction.setField("Status__c", "");
            junction.setField("Type__c", ""); //FACILITIES OR BREAKROOM
            junction.setField("Location__c", "");
            junction.setField("Order__c", "");
            junction.setField("Install_Date__c", "");
            junction.setField("Asset_Cost__c", "");

            junction.setField("Product__c", ""); //REFERENCE TO PRODUCT

            /*
            junction.setField("Custom_Asset_Name__c", "");
            junction.setField("Custom_Asset_SKU__c", "");
            junction.setField("Custom_Asset_Vendor__c", "");
            junction.setField("Custom_Asset_List_Price__c", "");
            junction.setField("Custom_Asset_Installation_Cost__c", "");
            junction.setField("Custom_Asset_Description__c", "");
            */
        }

        public static void createJunction(){
            SObject junction = new SObject();
            junction.setType("Boss_PS_Junction__c");
            junction.setField("Boss_Site__c", "");
            junction.setField("Boss_Implementation__c", "");
            junction.setField("Contact_Name__c", "");
            junction.setField("Contact_Phone__c", "");
            junction.setField("Contact_Email__c", "");
            junction.setField("Contact_Title__c", "");

        }


        public static String processResults(SaveResult[] saveResults){
            String successIds = "";

            for(SaveResult result : saveResults){
                if (result.isSuccess()) {
                    String successId = "'"+ result.getId() +"'";
                    successIds = (successIds.isEmpty()) ? successId : successIds + "," + successIds ;
                }else{
                    for (int e = 0; e < result.getErrors().length; e++) {
                        Error err = result.getErrors()[e];
                        System.out.println("Errors were found on item " + e);
                        System.out.println("Error code: " + err.getStatusCode().toString());
                        System.out.println("Error message: " + err.getMessage());
                    }
                }
            }

            System.out.println("SUCCESS IDS: " + successIds);

            return successIds;
        }
}

