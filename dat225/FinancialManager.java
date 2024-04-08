import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class FinancialManager {

    public void welcomeFM(Connection connection) {
        System.out.println("\n==== WELCOME TO FINANCIAL MANAGER INTERFACE! ====");
    }

    // make use of the rollup by year month day to get the full picture
    public void reportYear(Connection connection) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT" +
                " NVL(TO_CHAR(payment_date, 'YYYY'), 'All Years') AS year," +
                " NVL(TO_CHAR(payment_date, 'Month'), 'All Months') AS month," +
                " NVL(TO_CHAR(payment_date, 'DD'), 'All Days') AS day," +
                " SUM(CASE WHEN lease_id IS NOT NULL THEN amount ELSE 0 END) AS total_amount" +
                " FROM Payment" +
                " GROUP BY ROLLUP ( TO_CHAR(payment_date, 'YYYY'), TO_CHAR(payment_date, 'Month'), TO_CHAR(payment_date, 'DD'))");
            ResultSet rs = ps.executeQuery();
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("                     FINANCIAL REPORT");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("\n%-15s%-15s%-15s%-15s%n", "Year", "Month", "Day", "Total Amount");
            System.out.println("-------------------------------------------------------------");
            while (rs.next()) {
                String year = rs.getString(1);
                String month = rs.getString(2);
                String day = rs.getString(3);
                String amount = rs.getString(4);
                System.out.printf("%-15s%-15s%-15s%-15s%n",  year, month, day, amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // financial manager can see all the payments
    public void reportPayments(Connection connection) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            PreparedStatement ps = connection.prepareStatement(
                "SELECT p.payment_id, p.lease_id, p.payment_date, p.amount," +
                " COALESCE(c.payment_method, d.payment_method, cr.payment_method) as PAYMENT_METHOD" +
                " FROM Payment P" +
                " LEFT JOIN (SELECT payment_id, 'Credit Card' AS payment_method FROM CreditPayment) C ON P.payment_id = C.payment_id" +
                " LEFT JOIN (SELECT payment_id, 'Debit Card' AS payment_method FROM DebitPayment) D ON P.payment_id = D.payment_id" +
                " LEFT JOIN (SELECT payment_id, 'Crypto' AS payment_method FROM CryptoPayment) CR ON P.payment_id = CR.payment_id" +
                " order by p.payment_id" );
            ResultSet rs = ps.executeQuery();
            System.out.println("\n-----------------------------------------------------------------");
            System.out.println("                         ALL PAYMENTS");
            System.out.println("-----------------------------------------------------------------");
            System.out.printf("\n%-12s%-12s%-15s%-10s%-20s%n", "Payment ID", "Lease ID", "Payment Date", "Amount", "Payment Method");
            System.out.println("-----------------------------------------------------------------");
            while (rs.next()) {
                int pay = rs.getInt(1);
                int lease = rs.getInt(2);
                Date date = rs.getDate(3);
                float total = rs.getFloat(4);
                String method = rs.getString(5);
                String formated = dateFormat.format(date);
                System.out.printf("%-12s%-12s%-15s%-10.2f%-20s%n",  pay, lease, formated, total, method);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // see stats about the use of different payment options
    public void reportPayMethod(Connection connection){
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT COALESCE(C.payment_method, D.payment_method, CR.payment_method, 'Unknown') AS payment_method," +
                " SUM(P.amount) AS total_amount," +
                " ROUND(AVG(P.amount), 2) AS average_amount" +
                " FROM Payment P" +
                " LEFT JOIN (SELECT payment_id, 'Credit Card' AS payment_method FROM CreditPayment" +
                " ) C ON P.payment_id = C.payment_id" +
                " LEFT JOIN (SELECT payment_id, 'Debit Card' AS payment_method FROM DebitPayment" +
                " ) D ON P.payment_id = D.payment_id" +
                " LEFT JOIN (SELECT payment_id, 'Crypto' AS payment_method FROM CryptoPayment" +
                " ) CR ON P.payment_id = CR.payment_id" +
                " GROUP BY COALESCE(C.payment_method, D.payment_method, CR.payment_method, 'Unknown')" );
            ResultSet rs = ps.executeQuery();
            System.out.println("\n-------------------------------------------------------------");
            System.out.println("                  AMOUNT BY PAYMENT METHOD");
            System.out.println("-------------------------------------------------------------");
            System.out.printf("\n%-20s%-20s%-20s%n", "Payment Method", "Total Amount", "Average Payment");
            System.out.println("-------------------------------------------------------------");
            while (rs.next()) {
                String method = rs.getString(1);
                float total = rs.getFloat(2);
                float avergae = rs.getFloat(3);
                System.out.printf("%-20s%-20.2f%-20.2f%n", method, total, avergae);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // see how much revenue is made by every property 
    public void reportProperty(Connection connection) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "select pr.prop_id, pr.street, pr.city, pr.zip, NVL(SUM(p.amount), 0) AS total_profit" +
                " from payment p" +
                " join lease l on p.lease_id = l.lease_id" +
                " join apartment ap on ap.apart_id = l.apart_id" +
                " join property pr on ap.property_id = pr.prop_id" +
                " GROUP BY pr.prop_id, pr.street, pr.city, pr.zip" +
                " ORDER BY total_profit DESC" );
            ResultSet rs = ps.executeQuery();
            System.out.println("\n------------------------------------------------------------------------------");
            System.out.println("                         REVENUE BY PROPERTY");
            System.out.println("------------------------------------------------------------------------------");
            System.out.printf("\n%-12s%-20s%-20s%-10s%-15s%n", "Property ID", "Street", "City", "ZIP", "Total Revenue");
            System.out.println("------------------------------------------------------------------------------");
            while (rs.next()) {
                int propId = rs.getInt(1);
                String street = rs.getString(2);
                String city = rs.getString(3);
                int zip = rs.getInt(4);
                float profit = rs.getFloat(5);
                System.out.printf("%-12s%-20s%-20s%-10s%-15.2f%n", propId, street, city, zip, profit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // see what amenity is the most profitable
    public void reportAmenities(Connection connection) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT pal.amen_id, am.name, NVL(SUM(pa.monthly_rate), 0) AS total_profit" + 
                " FROM prop_amenity_in_lease pal" + //
                " JOIN prop_amenity pa ON pa.amen_id = pal.amen_id" + //
                " JOIN amenity am ON am.amen_id = pa.amen_id" + //
                " GROUP BY pal.amen_id, am.name" + //
                " ORDER BY total_profit DESC" );
            ResultSet rs = ps.executeQuery();
            System.out.println("\n-----------------------------------------------------------------------");
            System.out.println("                         REVENUE BY AMENITY");
            System.out.println("-----------------------------------------------------------------------");
            System.out.printf("\n%-15s%-40s%-15s%n", "Amenity ID", "Name", "Total Revenue");
            System.out.println("-----------------------------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);
                float profit = rs.getFloat(3);
                System.out.printf("%-15s%-40s%-15.2f%n", id, name, profit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
