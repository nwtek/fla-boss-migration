import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;
import com.sforce.ws.bind.XmlObject;

import javax.xml.crypto.dsig.XMLObject;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Main{
    public static ConnectorConfig config = new ConnectorConfig();
    public static PartnerConnection connection;
    public static Boolean commitData = false;

    public static void main(String[] args) {

        try {
            config.setUsername("");
            config.setPassword("");
            connection = Connector.newConnection(config);

            processImplementations();
            processSites();

            //updateFacilityLeaseAgreement("''");

            //commitData = true;



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

        public static void processImplementations(){
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

                if(commitData){
                    SaveResult[] saveResults = connection.create(newImplementations);

                    //UPDATE FLA'S
                    updateFacilityLeaseAgreement(processResults("Boss Implementation", saveResults));
                }


            }catch(Exception e){
                System.out.println("ERROR: CREATE IMPLEMENTATIONS");
                e.printStackTrace();
            }
        }



        public static void processSites(){
            ResultSet apolloResult = null;
            Map<String, Map<String, Map<String, String>>> mapAssets = new HashMap<>();

            try {
                QueryResult queryResults = connection.query("SELECT Id, Boss_Implementation__c, Name, Status__c, Lease_Agreement_Type__c, OwnerId, Requestor__c, Account_Name__c, Master_Customer_Num__c, Contact_Name__c, Contact_Name__r.Name, Contact_Name__r.Phone, Contact_Name__r.Email, Brewer_Installation_Method__c, Special_Instructions__c, Project_Manager__c, Service_Provider__c, Number_of_Installation_Locations__c, Lease_Term__c, Hanging_Allowance__c FROM Facility_Lease_Agreement__c Where Boss_Implementation__c != Null");

                List<SObject> facilityLeaseAgreements = new ArrayList<SObject>();

                if (queryResults.getSize() > 0) {
                    SObject[] records = queryResults.getRecords();

                    for (int i = 0; i < records.length; i++) {
                        SObject facility_lease_agreement = records[i];
                        facilityLeaseAgreements.add(facility_lease_agreement);

                        System.out.println("FLA ID: " + facility_lease_agreement.getId());
                    }
                }

                Map<String, Map<String, Map<String, String>>> flaMap = new HashMap<>();

                //USED BOTH IN NESTED MAP AND FOR CREATING JUNCTIONS AND ASSETS
                Map<String, Map<String, String>> siteMap = new HashMap();

                for (SObject facilityLeaseAgreement : facilityLeaseAgreements) {

                    String flaId = facilityLeaseAgreement.getId();
                    Object impId = facilityLeaseAgreement.getField("Boss_Implementation__c");

                    //GET THE RELATED FACILITY LEASE AGREEMENT RECORD
                    XmlObject contactRelatedObject = facilityLeaseAgreement.getChild("Contact_Name__r");

                    //QUERY APOLLO DATA
                    apolloResult = Postgres.dbSelect(flaId);

                    siteMap = new HashMap();

                    while (apolloResult.next()) {

                        Map<String, String> fieldMap = new HashMap<>();
                        String orderNumber  = apolloResult.getString("invision_order_number__c");
                        String accountId    = apolloResult.getString("account_name__c");
                        String shipToId     = apolloResult.getString("ship_id");
                        String siteKey      = accountId  + "_" + shipToId;



                        //THIS IS UGLY
                        Map<String, String> mapAsset = new HashMap<>();

                        mapAsset.put("OrderNumber", orderNumber);
                        mapAsset.put("SKU", apolloResult.getString("staples_sku"));
                        mapAsset.put("QTY", apolloResult.getString("qty"));

                        if(mapAssets.containsKey(siteKey)){
                            mapAssets.get(siteKey).put(orderNumber, mapAsset);
                        }else{
                            Map<String, Map<String, String>> mapOrder = new HashMap();
                            mapOrder.put(orderNumber, mapAsset);

                            mapAssets.put(siteKey, mapOrder);
                        }

                        if(!siteMap.containsKey(siteKey)){
                            /*
                            String accountId            = rs.getString("account_name__c");
                            String contactId            = rs.getString("contact_name__c");
                            String flaID                = rs.getString("id");
                            String leaseType            = rs.getString("lease_agreement_type__c");
                            String specialInstructions  = rs.getString("special_instructions__c");
                            String status               = rs.getString("status__c");
                            String customerNumber       = rs.getString("customer");

                            //ASSET
                            String orderNumber          = apolloResult.getString("invision_order_number__c");
                            String staplesSku           = apolloResult.getString("staples_sku");
                            Integer quantity            = apolloResult.getInt("qty");
                            */

                            //SITE
                            fieldMap.put("AccountId", apolloResult.getString("account_name__c"));
                            fieldMap.put("Street", apolloResult.getString("address_1"));
                            fieldMap.put("Suite", apolloResult.getString("address_3"));
                            fieldMap.put("City", apolloResult.getString("city"));
                            fieldMap.put("State", apolloResult.getString("state"));
                            fieldMap.put("ZipCode", apolloResult.getString("zip_code_p1") + apolloResult.getString("zip_code_p2"));
                            fieldMap.put("Ship_To__c", apolloResult.getString("ship_id"));

                            fieldMap.put("ContactName", contactRelatedObject.getField("Name").toString());
                            fieldMap.put("ContactPhone", contactRelatedObject.getField("Phone").toString());
                            fieldMap.put("ContactEmail", contactRelatedObject.getField("Email").toString());
                            fieldMap.put("impId", impId.toString());

                            siteMap.put(siteKey, fieldMap);
                        }
                    }

                    flaMap.put(flaId, siteMap);
                }

                if (commitData){
                    SaveResult[] saveResults = connection.create(createSites(flaMap));

                    String siteIds = processResults("Boss Site", saveResults);

                    processJunctions(siteIds, mapAssets, siteMap);
                }

            } catch (Exception e) {
                System.out.println("ERROR: QUERY FLAs");
                e.printStackTrace();
            }


            //OPERATION: QUERY MIGRATED FACILITY LEASE AGREEMENTS AND TRAVERSE (CONTACT)

            //OPERATION: QUERY APOLLO

            //OPERATION: CREATE SITE

            //OPERATION: CREATE JUNCTION AND BIND TO SITE AND IMPLEMENTATION

            //OPERATION: CREATE ASSETS AND BIND TO SITE WITH REFERENCE TO JUNCTION AND IMPLEMENTATION

            //<FLA> <SITENAME> <FIELD, VALUE>
        }

        public static void processJunctions(String siteIds,  Map<String, Map<String, Map<String, String>>> mapAssets, Map<String, Map<String, String>> mapSites){
            try {
                QueryResult queryResults = connection.query("SELECT Id, Ship_To__c, Account__c, Name, Street__c, Suite__c, City__c, State__c, Zip_Code__c From Boss_Site__c Where ID IN (" + siteIds + ")");

                if (queryResults.getSize() > 0) {
                    SObject[] records = queryResults.getRecords();

                    SObject[] junctions = new SObject[mapSites.size()];

                    for (int i = 0; i < records.length; i++) {
                        SObject site = records[i];

                        Object siteId = site.getId();

                        String accountId    = site.getField("Account__c").toString();
                        String shipToId     = site.getField("Ship_To__c").toString();
                        String siteKey      = accountId  + "_" + shipToId;

                        Map<String, String> mapSite = mapSites.get(siteKey);

                        //CREATE JUNCTION
                        SObject junction = new SObject();
                        junction.setType("Boss_PS_Junction__c");
                        junction.setField("Boss_Site__c", siteId);
                        junction.setField("Boss_Implementation__c", mapSite.get("impId"));
                        junction.setField("Contact_Name__c", mapSite.get("ContactName"));
                        junction.setField("Contact_Phone__c", mapSite.get("ContactPhone"));
                        junction.setField("Contact_Email__c", mapSite.get("ContactEmail"));

                        junctions[i] = junction;

                    }

                    if (commitData) {
                        SaveResult[] saveResults = connection.create(junctions);

                        String junctionIds = processResults("Boss Junction", saveResults);

                        processAssets(junctionIds, mapAssets);
                    }

                }

            } catch (Exception e) {
                System.out.println("ERROR: QUERY FLAs");
                e.printStackTrace();
            }
        }

    public static void processAssets(String junctionIds,  Map<String, Map<String, Map<String, String>>> mapAssets){
        try {
            QueryResult queryResults = connection.query("SELECT Id, Boss_Implementation__c, Boss_Site__c, Boss_Site__r.Account__c, Boss_Site__r.Ship_To__c FROM Boss_PS_Junction__c Where ID IN (" + junctionIds + ")");

            if (queryResults.getSize() > 0) {
                SObject[] records = queryResults.getRecords();

                SObject[] assets = new SObject[mapAssets.size()];

                for (int i = 0; i < records.length; i++) {
                    SObject junction = records[i];

                    Object junctionId = junction.getId();

                    //GET THE RELATED FACILITY LEASE AGREEMENT RECORD
                    XmlObject siteRelatedObject = junction.getChild("Boss_Site__r");

                    String accountId    = siteRelatedObject.getField("Account__c").toString();
                    String shipToId     = siteRelatedObject.getField("Ship_To__c").toString();
                    String siteKey      = accountId  + "_" + shipToId;

                    Map<String, Map<String, String>> mapOrder = mapAssets.get(siteKey);

                    for(String orderNumber : mapOrder.keySet()){

                        Map<String, String> mapAsset = mapOrder.get(orderNumber);

                        //CREATE ASSET
                        SObject asset = new SObject();
                        asset.setType("Boss_Asset__c");
                        asset.setField("Boss_Site__c", junction.getField("Boss_Site__c"));
                        asset.setField("BOSS_Implementation__c", junction.getField("Boss_Implementation__c"));
                        asset.setField("BOSS_PS_Junction__c", junctionId);

                        asset.setField("Quantity__c", mapAsset.get("QTY"));
                        asset.setField("Order__c", mapAsset.get("OrderNumber"));
                        asset.setField("Custom_Asset_SKU__c", mapAsset.get("SKU"));

                        /*
                        asset.setField("Serial_Number__c", "SerialNumber");
                        asset.setField("Status__c", "Status");
                        asset.setField("Type__c", "Type"); //FACILITIES OR BREAKROOM
                        asset.setField("Location__c", "Location");
                        asset.setField("Install_Date__c", "InstallDate");
                        asset.setField("Asset_Cost__c", "Cost");
                        */

                        assets[i] = asset;
                    }

                }

                if (commitData) {
                    SaveResult[] saveResults = connection.create(assets);
                    String assetIds = processResults("Boss Asset", saveResults);
                }

            }

        } catch (Exception e) {
            System.out.println("ERROR: QUERY FLAs");
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

                        Object implementationId             = implementation.getId();

                        //OPERATION: UPDATE FLA
                        SObject facilityLeaseAgreement = new SObject();
                        facilityLeaseAgreement.setType("Facility_Lease_Agreement__c");
                        facilityLeaseAgreement.setId(facilityLeaseAgreementId.toString());
                        facilityLeaseAgreement.setField("Name", "(MIGRATED): " + facilityLeaseAgreementName);
                        facilityLeaseAgreement.setField("BOSS_Implementation__c", implementationId);
                        flasToUpdate[i] = facilityLeaseAgreement;
                    }

                    if (commitData) {
                        SaveResult[] saveResults = connection.update(flasToUpdate);
                        String successIds = processResults("Facility Lease Agreement", saveResults);
                    }
                }

            } catch (Exception e) {
                System.out.println("ERROR: CREATE IMPLEMENTATIONS");
                e.printStackTrace();
            }
        }

        public static SObject[] createSites(Map<String,  Map<String, Map<String, String>>> mapFLA){
            SObject[] newSites = null;
            for(String flaId : mapFLA.keySet()){
                Map<String, Map<String, String>> mapSite = mapFLA.get(flaId);
                newSites = new SObject[mapSite.size()];

                Integer i = 0;
                for(String siteId : mapSite.keySet()){
                    SObject site = new SObject();
                    site.setType("Boss_Site__c");
                    site.setField("Name", mapFLA.get("Name"));
                    site.setField("Account__c", mapFLA.get("AccountId"));
                    site.setField("Street__c", mapFLA.get("Street"));
                    site.setField("Suite__c", mapFLA.get("Suite"));
                    site.setField("City__c", mapFLA.get("City"));
                    site.setField("State__c", mapFLA.get("State"));
                    site.setField("Zip_Code__c", mapFLA.get("ZipCode"));
                    site.setField("Ship_To__c", mapFLA.get("ShipTo"));
                    newSites[i] = site;
                    i++;
                }
            }

            return newSites;
        }

        public static SObject createJunction(String key, Map<String, Map<String, String>> mapJunctions){

            Map<String, String> mapJunction = mapJunctions.get(key);

            SObject junction = new SObject();
            junction.setType("Boss_PS_Junction__c");
            junction.setField("Boss_Site__c", mapJunction.get("SiteId"));
            junction.setField("Boss_Implementation__c", mapJunction.get("ImpId"));
            junction.setField("Contact_Name__c", mapJunction.get("ContactName"));
            junction.setField("Contact_Phone__c", mapJunction.get("ContactPhone"));
            junction.setField("Contact_Email__c", mapJunction.get("ContactEmail"));
            junction.setField("Contact_Title__c", mapJunction.get("ContactTitle"));

            return junction;
        }

        public static void createAsset(){
            SObject junction = new SObject();
            junction.setType("Boss_PS_Junction__c");
            junction.setField("Boss_Site__c", "SiteId");
            junction.setField("Serial_Number__c", "SerialNumber");
            junction.setField("Status__c", "Status");
            junction.setField("Type__c", "Type"); //FACILITIES OR BREAKROOM
            junction.setField("Location__c", "Location");
            junction.setField("Order__c", "OrderNumber");
            junction.setField("Install_Date__c", "InstallDate");
            junction.setField("Asset_Cost__c", "Cost");

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

        public static String processResults(String objectName, SaveResult[] saveResults){
            //String successIds = "";
            StringBuilder successIds = new StringBuilder();

            for(SaveResult result : saveResults){
                if (result.isSuccess()) {
                    String successId = (successIds.length() == 0) ? "'" + result.getId() + "'" : ",'" + result.getId() + "'";
                    //successIds = (successIds.isEmpty()) ? successId : successIds + "," + successIds ;
                    successIds.append(successId);

                }else{
                    for (int e = 0; e < result.getErrors().length; e++) {
                        Error err = result.getErrors()[e];
                        System.out.println(objectName + " Errors were found on item " + e);
                        System.out.println("Error code: " + err.getStatusCode().toString());
                        System.out.println("Error message: " + err.getMessage());
                    }
                }
            }

            System.out.println(objectName + " SUCCESS IDS: " + successIds.toString());

            return successIds.toString();
        }

        class NestedMap{
            public Map<String, Map<String, String>> nestedMap = new HashMap<>();
        }
}

