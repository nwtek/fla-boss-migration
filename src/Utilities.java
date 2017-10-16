import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Utilities {

    public static String fieldCleanseUtil(String field){
        String cleanField = "";

        if(field != null)
            cleanField = field.trim();

        return cleanField;
    }

    public static Date dateFormatUtil(String dateString) {
        Calendar cal = Calendar.getInstance();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd");
            Date date = format.parse(dateString);
            cal.setTime(date);
        }catch(Exception e){
            System.out.println("Date exception : " + e);
        }

        return cal.getTime();
    }
}
