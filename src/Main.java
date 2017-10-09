import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.*;
import com.sforce.ws.*;

public class Main{

    public static void main(String[] args) {
        ConnectorConfig config = new ConnectorConfig();
        config.setUsername("");
        config.setPassword("");

        try{

            PartnerConnection connection = Connector.newConnection(config);
            SObject account = new SObject();
            account.setType("Account");
            account.setField("Name", "Test Account 2");
            account.setField("Division__c", "LA");
            connection.create(new SObject[]{account});

        }catch(ConnectionException ce){
            ce.printStackTrace();
        }

        }

}

