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

        String objectAPIName = "LocationBacklog__c";

        try{
            DescribeSObjectResult desc = new DescribeSObjectResult();

            desc = connection.describeSObject(objectAPIName);

            Field[] fields = desc.getFields();

            createTableSQL(objectAPIName, fields);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static Map<String, String> convertDataTypes(){
        Map<String, String> convertType = new HashMap<>();

        convertType.put("id", "character"); //or varchar2 for oracle
        convertType.put("reference", "character");
        convertType.put("string", "character");
        convertType.put("boolean", "character");
        convertType.put("picklist", "character");
        convertType.put("textarea", "character");
        convertType.put("datetime", "timestamp");
        convertType.put("number", "number");

        return convertType;
    }

    private static String createTableSQL(String object, Field[] fields){
        Map<String, String> convertType = convertDataTypes();

        String createTableStatement = "CREATE TABLE " + object + "(";

        for (int i = 0; i < fields.length; i++) {
            Field   field           = fields[i];
            String  fieldType       = field.getType().toString();
            Integer fieldLength     = 0;

            switch (fieldType) {
                case "datetime": fieldLength = 9;
                    break;
                case "boolean":  fieldLength = 5;
                    break;
                default: fieldLength = field.getLength();
                    break;
            }

            createTableStatement += field.getName() + " " + convertType.get(fieldType) + "(" + fieldLength + ")";

            if(i < (fields.length-1))
            createTableStatement += ", ";
        }

        createTableStatement += ");";

        System.out.println(createTableStatement);

        return createTableStatement;
    }
}
