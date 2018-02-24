import com.sforce.soap.partner.*;
import com.sforce.soap.partner.Error;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;
import com.sforce.ws.bind.XmlObject;

import java.util.*;

public class describe {
    public static ConnectorConfig config = new ConnectorConfig();
    public static PartnerConnection connection;
    public static Boolean commitData = false;

    public static void main(String[] args) {
        try {
            config.setUsername("");
            config.setPassword("");
            connection = Connector.newConnection(config);

            commitData = true;

            describeObject();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void describeObject(){

    try{
        DescribeSObjectResult desc = new DescribeSObjectResult();

        desc = connection.describeSObject("LocationBacklog__c");

        Field[] fields = desc.getFields();

        System.out.println("OBJECT FIELDS: " + fields);

        // Iterate through each field and gets its properties
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            System.out.println("Field name: " + field.getName());
            System.out.println("Field label: " + field.getLabel());

            // If this is a picklist field, show the picklist values
            if (field.getType().equals(FieldType.picklist)) {
                PicklistEntry[] picklistValues = field.getPicklistValues();
                if (picklistValues != null) {
                    System.out.println("Picklist values: ");
                    for (int j = 0; j < picklistValues.length; j++) {
                        if (picklistValues[j].getLabel() != null) {
                            System.out.println("\tItem: " + picklistValues[j].getLabel()
                            );
                        }
                    }
                }
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }

    }
}
