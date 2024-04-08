import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Tenant {
    // first i ask for the tenant id
    // based on the tenant id i print out all the relavent data
    public int[] welcomeTenant(Connection connection) {
        int leaseId = 0;
        int tenantId = 0;
        int propId = 0;
        try {
            Scanner scanner = new Scanner(System.in);
            //int tenantId;
            System.out.println("\n==== WELCOME TO TENANT INTERFACE! ====");
            System.out.println("\n== IMPORTANT: This table is here just so it is easier for you to test the tenant interface without having to know the IDs ==");
            System.out.println("== In the real program tenants are not able to see other tenant's IDs ==");
            System.out.println("== This interface would start with 'Please enter ID' prompt ==");
            showTenants(connection);
            System.out.println("\nLet's start with providing current information for your account. ");
            System.out.print("Please enter your tenant id: ");
            while(true){
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    PreparedStatement psTenant = connection.prepareStatement("select * from Tenant where tenant_id = ?");
                    psTenant.setInt(1, tenantId);
                    ResultSet resultSetTenant = psTenant.executeQuery();

                    if (resultSetTenant.next()) {
                        String ssn = resultSetTenant.getString(2); 
                        Date dobTenant = resultSetTenant.getDate(3); 
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                        String formattedDate = dateFormat.format(dobTenant);

                        PreparedStatement psVisitor = connection.prepareStatement("select * from Visitor where visitor_id = ?");
                        PreparedStatement psLease = connection.prepareStatement("select * from Lease where tenant_id = ?");
                        PreparedStatement psApart = connection.prepareStatement("select * from Apartment where apart_id = ?");
                        PreparedStatement psPet = connection.prepareStatement("select * from Pet where tenant_id = ?");
                        PreparedStatement psRoommate = connection.prepareStatement("select * from Roommate where tenant_id = ?");
                        PreparedStatement psProp = connection.prepareStatement("select * from property where prop_id = ?");
                        PreparedStatement psPropAmen = connection.prepareStatement(
                                "select am.name, pa.monthly_rate" + 
                                " from prop_amenity_in_lease pl, prop_amenity pa, amenity am" + 
                                " where pl.amen_id = pa.amen_id and am.amen_id = pa.amen_id and pl.lease_id = ?");                    
                        PreparedStatement psApartAmen = connection.prepareStatement(
                                "SELECT apart_amenity.apart_id, apart_amenity.amen_id, amenity.name" + 
                                " FROM apart_amenity" + 
                                " JOIN amenity ON apart_amenity.amen_id = amenity.amen_id" + 
                                " WHERE apart_amenity.apart_id = ?");

                        String fnameTenant = "";
                        String fnameRoommate = "";
                        String lnameTenant = "";
                        String lnameRoommate = "";
                        String phone = "";
                        String email = "";
                        Date dobRoommate = null;
                        String formatedDobRoommate = "";
                        //int leaseId = 0;
                        Date start = null;
                        String formatedStart = "";
                        Date end = null;
                        String formatedEnd = "";
                        int apartId = 0;
                        float rent = 0;
                        String petName = "";
                        String petType = "";
                        int apartNum = 0; 
                        float bathrooms = 0;
                        int bedrooms = 0;
                        float securityDeposit = 0;
                        float apartSize = 0;
                        String street = "";
                        String city = "";
                        int zip = 0;
                        int year = 0;
                    

                        // visitor query
                        psVisitor.setInt(1, tenantId);
                        ResultSet resultSetVisitor = psVisitor.executeQuery();
                        while (resultSetVisitor.next()) {
                            fnameTenant = resultSetVisitor.getString(2); 
                            lnameTenant = resultSetVisitor.getString(3); 
                            phone = resultSetVisitor.getString(4); 
                            email = resultSetVisitor.getString(5); 
                            System.out.println("\nPersonal Information");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Full name:\t\t" + fnameTenant +" "+ lnameTenant);
                            System.out.println("  Phone:\t\t"+ phone);
                            System.out.println("  Email:\t\t"+ email);
                            System.out.println("  SSN:\t\t\t" + ssn);
                            System.out.println("  Date of birth:\t" + formattedDate );  
                        }

                        // lease query
                        psLease.setInt(1, tenantId);
                        ResultSet resultSetLease = psLease.executeQuery();
                        while (resultSetLease.next()) {
                            leaseId = resultSetLease.getInt(1); 
                            start = resultSetLease.getDate(2); 
                            end = resultSetLease.getDate(3); 
                            rent = resultSetLease.getFloat(4); 
                            apartId = resultSetLease.getInt(7); 
                            formatedStart = dateFormat.format(start);
                            formatedEnd = dateFormat.format(end);
                            System.out.println("\nLease Information");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Start date:\t\t" + formatedStart );
                            System.out.println("  End date:\t\t" + formatedEnd);
                            System.out.println("  Rent:\t\t\t$" + String.format("%.2f", rent));                        
                        }
                        // apartment query 
                        psApart.setInt(1, apartId);
                        ResultSet resultSetApart = psApart.executeQuery();
                        while (resultSetApart.next()) {
                            apartNum = resultSetApart.getInt(2); 
                            bathrooms = resultSetApart.getFloat(3);   
                            bedrooms = resultSetApart.getInt(4);   
                            apartSize = resultSetApart.getFloat(5);   
                            securityDeposit = resultSetApart.getFloat(7);   
                            propId = resultSetApart.getInt(8);   
                            System.out.println("\nApartment and Amenities Information");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Apartment number:\t" + apartNum);
                            System.out.println("  Number of bedrooms:\t" + bedrooms);
                            System.out.println("  Number of bathrooms:\t" + bathrooms);
                            System.out.println("  Apartment size:\t" + apartSize+ " sq.ft.");
                            System.out.println("  Security  deposit:\t$" + String.format("%.2f", securityDeposit));  
                        }

                        // apartment amenities
                        psApartAmen.setInt(1, apartId);
                        ResultSet resultSetApartAmen = psApartAmen.executeQuery();
                        if (resultSetApartAmen.next()){
                            System.out.print("  Amenities:" ); 
                            String amen = resultSetApartAmen.getString(3); 
                            System.out.println("\t\t"+amen); 
                        }
                        while (resultSetApartAmen.next()) {
                            String amen = resultSetApartAmen.getString(3); 
                            System.out.println("\t\t\t"+amen);   
                        }
                        // property query 
                        psProp.setInt(1, propId);
                        ResultSet resultSetProp = psProp.executeQuery();
                        while (resultSetProp.next()) {
                            street = resultSetProp.getString(2); 
                            city = resultSetProp.getString(3);  
                            zip = resultSetProp.getInt(4);  
                            year = resultSetProp.getInt(5);  
                            System.out.println("\nProperty and Amenities Information");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Street:\t\t" + street);
                            System.out.println("  City:\t\t\t" + city);
                            System.out.println("  Zip:\t\t\t" + zip);
                            System.out.println("  Year built:\t\t" + year); 
                            System.out.print("  Amenities:" );
                        }
                        // property amenities
                        psPropAmen.setInt(1, leaseId);
                        ResultSet resultSetPropAmen = psPropAmen.executeQuery();
                        int j = 0;
                        while (resultSetPropAmen.next()) {
                            String amen = resultSetPropAmen.getString(1);  
                            float price = resultSetPropAmen.getFloat(2);  
                            if (j == 0){
                                System.out.println("\t\t$"+String.format("%.2f", price) +" - "+amen); 
                            } else {
                                System.out.println("\t\t\t$"+String.format("%.2f", price) +" - "+amen); 
                            }
                            j++;
                        }
                        // Pet query
                        psPet.setInt(1, tenantId);
                        ResultSet resultSetPet = psPet.executeQuery();
                        while (resultSetPet.next()) {
                            petName = resultSetPet.getString(3); 
                            petType = resultSetPet.getString(4); 
                            System.out.println("\nPets");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Name:\t\t\t"+ petName );
                            System.out.println("  Species:\t\t" + petType);
                        }
                        // rommate query
                        psRoommate.setInt(1, tenantId);
                        ResultSet resultSetRommate = psRoommate.executeQuery();
                        while (resultSetRommate.next()) {
                            fnameRoommate = resultSetRommate.getString(3); 
                            lnameRoommate = resultSetRommate.getString(4); 
                            dobRoommate = resultSetRommate.getDate(5); 
                            formatedDobRoommate = dateFormat.format(dobRoommate);
                            System.out.println("\nRoommates");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Full name:\t\t" +fnameRoommate+" "+ lnameRoommate);
                            System.out.println("  Date of birth:\t" + formatedDobRoommate );  
                        }
                        break;
                    } else {
                        System.out.print("[Error]: Provided ID was not found in the database. Enter a valid Tenant ID: ");
                    } 
                } else {
                    scanner.next();
                    System.out.print("[Error]: Invalid input. Please enter a valid integer: ");
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
            System.out.println("Sorry an error has occured");
        }
        int[] info = {leaseId, tenantId, propId};
        return info;
    }
    // check payment status
    // know the lease id
    // calculate when the next payment is due
    // see when the last payment was made
    // show the status
    public int paymentStatus(Connection connection, int lease_id) {
        int check = 0;
        try { 
            PreparedStatement psPayment = connection.prepareStatement("SELECT *" +
                    " FROM Payment" +
                    " WHERE lease_id = ?" +
                    " AND payment_id = (SELECT MAX(payment_id) FROM Payment WHERE lease_id = ?)");

            PreparedStatement psDue = connection.prepareStatement("SELECT *" +
                    " FROM Lease" +
                    " WHERE lease_id = ?");
            
            PreparedStatement pscheck = connection.prepareStatement(
                "select lease_id, start_date, end_date, rent_amount, apart_id, tenant_id, first_name, last_name" + 
                " from lease join visitor on tenant_id = visitor_id " +
                " where move_out_date is null and start_date < current_date and lease_id = ?" +
                " order by lease_id" );

            pscheck.setInt(1, lease_id);
            ResultSet rscheck = pscheck.executeQuery();
            if (!rscheck.next()){
                System.out.println("== \nCould not process payment information because this tenant's lease with ID "+lease_id+" is not active ==");
                System.out.println("== Please check payment status after the start date of your lease. Thank you! ==");
            } else{
            
                PreparedStatement numPayments = connection.prepareStatement("select count(*) from payment where lease_id = ?");

                psPayment.setInt(1, lease_id);
                psPayment.setInt(2, lease_id);
                ResultSet resultSetPayment = psPayment.executeQuery();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
                LocalDate currentDate = LocalDate.now();
                float money = 0;

                LocalDate datePaid = null;
                while (resultSetPayment.next()) {
                    datePaid = resultSetPayment.getDate(3).toLocalDate();
                    money = resultSetPayment.getFloat(4);
                    System.out.println("\nLast payment of $" + String.format("%.2f", money) + " date: " + datePaid.format(formatter));
                }

                psDue.setInt(1, lease_id);
                ResultSet resultSetDue = psDue.executeQuery();

                LocalDate dateDue = null;
                while (resultSetDue.next()) {
                    dateDue = resultSetDue.getDate(2).toLocalDate();
                    String formattedDate = dateDue.format(formatter);
                    money = resultSetDue.getInt(4);
                    formattedDate = formattedDate.replaceAll("-[a-zA-Z]{3}-", "-Dec-");
                    System.out.println("The payment of $" + String.format("%.2f", money) +" due date: " + formattedDate);
                }
                int payments = 0;
                numPayments.setInt(1, lease_id);
                ResultSet resultSetNum = numPayments.executeQuery();
                while (resultSetNum.next()) {
                    payments = resultSetNum.getInt(1);
                }

                System.out.println("\nCurrent date: " + currentDate.format(formatter));
                if (currentDate != null && dateDue != null) {
                    int comparisonResult = dateDue.compareTo(currentDate);
                    if (payments > 2) {
                        System.out.println("== You already paid for this month. Thank you! == ");
                        check = 1;
                        
                    } else if (comparisonResult > 0) {
                        System.out.println("== The payment is overdue. Please pay ASAP! ==");
                    } else if (comparisonResult == 0) {
                        System.out.println("== Payment is due today! ==");
                    } else {
                        System.out.println("== You still have time until the deadline to complete the payment :) ==");
                    }
                }
            }   
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return check;
    }
    // make rental payment 
    // show what payment method was used in the past
    // ask if they want to keep it
    // if yes proceed if the payment
    // if no ask for new info
    // enter the payment method information
    // proceed if the payment 
    public void rentalPayment(Connection connection, int lease_id, int check) {
        if (check == 1) {
            System.out.println("\n== You already paid for this month. Thank you! ==");            
        } else{
            try {
                PreparedStatement pscheck = connection.prepareStatement(
                    "select lease_id, start_date, end_date, rent_amount, apart_id, tenant_id, first_name, last_name" + 
                    " from lease join visitor on tenant_id = visitor_id " +
                    " where move_out_date is null and start_date < current_date and lease_id = ?" +
                    " order by lease_id" );

                pscheck.setInt(1, lease_id);
                ResultSet rscheck = pscheck.executeQuery();
                if (!rscheck.next()){
                    System.out.println("== \nCould not process payment information because this tenant's lease with ID "+lease_id+" is not active ==");
                    System.out.println("== Please submit your payment after the start date of your lease. Thank you! ==");
                } else{

                    PreparedStatement psRent = connection.prepareStatement("select rent_amount from lease where lease_id = ?");
                    psRent.setInt(1, lease_id);
                    ResultSet resultSetRent = psRent.executeQuery();
                    float rent = 0;
                    while (resultSetRent.next()) {
                        rent = resultSetRent.getFloat(1);
                        System.out.println("\nThe payment due: $"+String.format("%.2f", rent));
                    }
                    PreparedStatement psPaymentID = connection.prepareStatement("SELECT payment_id" +
                                " FROM Payment" +
                                " WHERE lease_id = ?" +
                                " AND payment_id = (SELECT MAX(payment_id) FROM Payment WHERE lease_id = ?)");
                    PreparedStatement psCredit = connection.prepareStatement("select * from CreditPayment where payment_id = ?");
                    PreparedStatement psDebit = connection.prepareStatement("select * from DebitPayment where payment_id = ?");
                    PreparedStatement psCrypto = connection.prepareStatement("select * from CryptoPayment where payment_id = ?");
                    psPaymentID.setInt(1, lease_id);
                    psPaymentID.setInt(2, lease_id);
                    ResultSet resultSetPayment = psPaymentID.executeQuery();
                    int payID = 0;

                    while (resultSetPayment.next()) {
                        payID = resultSetPayment.getInt(1);
                    }
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                    int type = 0;
                    String creditNum = "";
                    String debitNum = "";
                    String crypto = "";
                    String creditHolder = "";
                    String debitHolder = "";
                    Date expCredit = null;
                    Date expDebit = null;

                    psDebit.setInt(1, payID);
                    ResultSet resultSetDebit= psDebit.executeQuery();
                    while (resultSetDebit.next()) {
                        debitNum = resultSetDebit.getString(2);
                        expDebit = resultSetDebit.getDate(3);
                        debitHolder = resultSetDebit.getString(4);
                        String formattedDate = dateFormat.format(expDebit);
                        System.out.println("\nThis is the debit card used for the last payment:");
                        System.out.println("  Card number: "+debitNum);
                        System.out.println("  Expiry date: "+formattedDate);
                        System.out.println("  Cardholder name: "+debitHolder);
                        type = 1;
                    }

                    psCredit.setInt(1, payID);
                    ResultSet resultSetCredit= psCredit.executeQuery();
                    while (resultSetCredit.next()) {
                        creditNum = resultSetCredit.getString(2);
                        expCredit = resultSetCredit.getDate(3);
                        creditHolder = resultSetCredit.getString(4);
                        String formattedDate = dateFormat.format(expCredit);
                        System.out.println("\nThis is the credit card used for the last payment:");
                        System.out.println("  Card number: "+creditNum);
                        System.out.println("  Expiry date: "+formattedDate);
                        System.out.println("  Cardholder name: "+creditHolder);
                        type = 2;
                    }

                    psCrypto.setInt(1, payID);
                    ResultSet resultSetCrypto = psCrypto.executeQuery();
                    while (resultSetCrypto.next()) {
                        crypto = resultSetCrypto.getString(2);
                        System.out.println("\nThis is the crypto wallet used for the last payment:");
                        System.out.println("  Wallet address: "+crypto);
                        type = 3;
                    }

                    PreparedStatement psPaymentIDLast = connection.prepareStatement("SELECT payment_id" +
                        " FROM Payment" +
                        " WHERE lease_id = ?" +
                        " AND payment_id = (SELECT MAX(payment_id) FROM Payment WHERE lease_id = ?)");
                    PreparedStatement psInsertPay = connection.prepareStatement("insert into Payment (lease_id, payment_date, amount) values (?, ?, ?)");
                    PreparedStatement psInsertCredit = connection.prepareStatement("insert into CreditPayment (payment_id, card_number, card_expiry_date, cardholder_name) values (?, ?, ?, ?)");
                    PreparedStatement psInsertDebit = connection.prepareStatement("insert into DebitPayment (payment_id, card_number, debit_expiry_date, cardholder_name) values (?, ?, ?, ?)");
                    PreparedStatement psInsertCrypto = connection.prepareStatement("insert into CryptoPayment (payment_id, wallet_address) values (?, ?)");

                    if (type == 0){
                        System.out.print("Press 2 to proceed: ");
                    } else {
                        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                        System.out.println("Do you want to use the same payment method?");
                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                        System.out.println("\n  [1] Yes");
                        System.out.println("  [2] No");
                        System.out.print("\n[*] :> ");
                    }
                    Scanner scanner = new Scanner(System.in);
                    int payIdNew = 0;
                    int option = 0;

                    psInsertPay.setInt(1, lease_id);
                    psInsertPay.setDate(2, Date.valueOf(LocalDate.now()));
                    psInsertPay.setFloat(3, rent);
                    psInsertPay.executeQuery();

                    psPaymentIDLast.setInt(1, lease_id);
                    psPaymentIDLast.setInt(2, lease_id);
                    ResultSet resultSetPaymentLast = psPaymentIDLast.executeQuery();
                    while (resultSetPaymentLast.next()) {
                        payIdNew = resultSetPaymentLast.getInt(1);
                    }
                    outerLoop: while (true){
                        if (scanner.hasNextInt()){
                            option = scanner.nextInt();
                            if (option == 1){
                                if (type == 1){ // debit
                                    psInsertDebit.setInt(1, payIdNew);
                                    psInsertDebit.setString(2, debitNum);
                                    psInsertDebit.setDate(3, expDebit);
                                    psInsertDebit.setString(4, debitHolder);
                                    psInsertDebit.executeQuery();
                                } else if (type == 2){ // credit
                                    psInsertCredit.setInt(1, payIdNew);
                                    psInsertCredit.setString(2, creditNum);
                                    psInsertCredit.setDate(3, expCredit);
                                    psInsertCredit.setString(4, creditHolder);
                                    psInsertCredit.executeQuery();
                                } else if (type == 3) { //crypto
                                    psInsertCrypto.setInt(1, payIdNew);
                                    psInsertCrypto.setString(2, crypto);
                                    psInsertCrypto.executeQuery();
                                }
                                System.out.println("\n== Payment was processed successfully with payment ID " + payIdNew + " ==");
                                break;
                            } else if (option == 2){
                                System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                                System.out.println("Choose the payment method");
                                System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                                System.out.println("\n  [1] Credit card");
                                System.out.println("  [2] Debit card");
                                System.out.println("  [3] Crypto");
                                System.out.print("\n[*] :> ");
                                int payType = 0;
                                while (true){
                                    if (scanner.hasNextInt()){
                                        payType = scanner.nextInt();
                                        scanner.nextLine();
                                        if (payType == 1){
                                            String card = getCardNumber(scanner, "\nEnter a 16-digit credit card number: ");
                                            String name = getNameInput(scanner, "Enter the cardholder name: ");
                                            Date exp = getDateInput(scanner, "Enter the card expiry date in the format dd-Mmm-yyyy: ");
                                            psInsertCredit.setInt(1, payIdNew);
                                            psInsertCredit.setString(2, card);
                                            psInsertCredit.setDate(3, exp);
                                            psInsertCredit.setString(4, name);  
                                            psInsertCredit.executeQuery();
                                            System.out.println("\n== Payment was processed successfully with payment ID "  +payIdNew+ " ==");
                                            break outerLoop;                                  
                                        } else if (payType == 2){
                                            String card = getCardNumber(scanner, "\nEnter a 16-digit credit card number: ");
                                            String name = getNameInput(scanner, "Enter the cardholder name: ");
                                            Date exp = getDateInput(scanner, "Enter the card expiry date in the format dd-Mmm-yyyy: ");
                                            psInsertDebit.setInt(1, payIdNew);
                                            psInsertDebit.setString(2, card);
                                            psInsertDebit.setDate(3, exp);
                                            psInsertDebit.setString(4, name);
                                            psInsertDebit.executeQuery();
                                            System.out.println("\n== Payment was processed successfully with payment ID "  +payIdNew+ " ==");
                                            break outerLoop;
                                        } else if (payType == 3){
                                            String wallet = getStringInput(scanner, "\nEnter the crypto wallet: ", "crypto wallet: ");
                                            psInsertCrypto.setInt(1, payIdNew);
                                            psInsertCrypto.setString(2, wallet);
                                            psInsertCrypto.executeQuery();
                                            System.out.println("\n== Payment was processed successfully with payment ID "  +payIdNew+ " ==");
                                            break outerLoop;
                                        } else {
                                            System.out.print("[Error]: Provided integer is not 1, 2 or 3. Try again: ");
                                        }
                                    }
                                    else {
                                        scanner.next();
                                        System.out.print("[Error]: Invalid input. Please enter a valid integer: ");
                                    }
                                }
                            } else {
                                System.out.print("[Error]: Provided integer is not 1 or 2. Try again: ");
                            }
                        } else {
                            scanner.next();
                            System.out.print("[Error]: Invalid input. Please enter a valid integer: ");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // helper to get a valid string
    public String getStringInput(Scanner sc, String message, String subject) {
        String userString = null;
        System.out.print(message);
        while (userString == null || userString.isEmpty()) {
            userString = sc.nextLine();
            if (userString.isEmpty()) {
                System.out.print("Please enter a valid " + subject);
            }
        }
        return userString;
    }

    // helper to get and validate name
    public static String getNameInput(Scanner sc, String message) {
        String name = null;
        System.out.print(message);
        while (name == null || !isValidName(name)) {
            name = sc.nextLine();
            if (!isValidName(name)) {
                System.out.print("Please enter a valid name: ");
            }
        }
        name = formatName(name);
        return name;
    }

    private static boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z- ]+$");
    }

    // helper to format the name with the capitalization of first letter 
    private static String formatName(String name) {
        if (name != null && !name.isEmpty()) {
            StringBuilder formattedName = new StringBuilder();
    
            boolean capitalizeNext = true; 
    
            for (char c : name.toCharArray()) {
                if (Character.isWhitespace(c)) {
                    formattedName.append(c);
                    capitalizeNext = true; // set flag to capitalize the next character after a space
                } else if (capitalizeNext) {
                    formattedName.append(Character.toUpperCase(c));
                    capitalizeNext = false; // reset the flag
                } else {
                    formattedName.append(Character.toLowerCase(c));
                }
            }
    
            return formattedName.toString();
        }
        return name;
    }


    // get the date in the future for expiration date
    public static Date getDateInput(Scanner sc, String message) {
        Date userDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        System.out.print(message);
        while (userDate == null || userDate.toLocalDate().isBefore(LocalDate.now()) || getYear(userDate) > 2043) {
            try {
                String userInput = sc.nextLine();
                LocalDate localDate = LocalDate.parse(userInput, formatter);
                userDate = Date.valueOf(localDate); 

                if (userDate.toLocalDate().isBefore(LocalDate.now()) || getYear(userDate) > 2043) {
                    throw new ParseException("Entered date must be in the future.", 0);
                }
            } catch (Exception e) {
                System.out.print("Please enter a valid future date that is not over year 2043 (dd-Mmm-yyyy): ");
            }
        }

        return userDate;
    }


    private static int getYear(Date date) {
        // Convert java.util.Date to java.time.LocalDate
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        return Integer.parseInt(yearFormat.format(date));
    }

    // get and validate the card number
    public static String getCardNumber(Scanner sc, String message) {
        String creditCardNumber = null;
        System.out.print(message);
        while (creditCardNumber == null || !isValidCreditCardNumber(creditCardNumber)) {
            creditCardNumber = sc.nextLine();
    
            if (!isValidCreditCardNumber(creditCardNumber)) {
                System.out.print("Please enter a valid card number: ");
            }
        }
        return creditCardNumber;
    }

    public static boolean isValidCreditCardNumber(String creditCardNumber) {
        return creditCardNumber != null && creditCardNumber.matches("\\d{16}");
    }

    // het an validate the phone number
    public static String getPhone(Scanner sc, String message) {
        String phone = null;
        System.out.print(message);
        while (phone == null || !isValidPhone(phone)) {
            phone = sc.nextLine();
    
            if (!isValidPhone(phone)) {
                System.out.print("Please enter a valid phone number: ");
            }
        }
        return phone;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{3}-\\d{3}-\\d{4}");
    }

    // get and validate the email
    public static String getEmail(Scanner sc, String message) {
        String email = null;
        System.out.print(message);
        while (email == null || !isValidEmail(email)) {
            email = sc.nextLine();
    
            if (!isValidEmail(email)) {
                System.out.print("Please enter a valid email: ");
            }
        }
        return email;
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(".+@.+\\..+");
    }

    // show the uodated info for the personal information
    public void getUpdatedInfo(Connection connection, int tenant_id){
        try {
            PreparedStatement psVisitor = connection.prepareStatement("select * from Visitor where visitor_id = ?");
            // visitor query
            psVisitor.setInt(1, tenant_id);
            ResultSet resultSetVisitor = psVisitor.executeQuery();
            while (resultSetVisitor.next()) {
                String fnameTenant = resultSetVisitor.getString(2); 
                String lnameTenant = resultSetVisitor.getString(3); 
                String phone = resultSetVisitor.getString(4); 
                String email = resultSetVisitor.getString(5); 
                System.out.println("\nUpdated Personal Information");  
                System.out.println("-------------------------------------------------");
                System.out.println("  Full name:\t\t" + fnameTenant +" "+ lnameTenant);
                System.out.println("  Phone:\t\t"+ phone);
                System.out.println("  Email:\t\t"+ email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    // update personal data
    // knot the tenant id
    // ssn and dob cant be update
    // update first name, last name, email or phone number
    public void updateFirstName(Connection connection, int tenant_id) {
        Scanner scanner = new Scanner(System.in);
        try {
            PreparedStatement ps = connection.prepareStatement("update visitor set first_name = ? where visitor_id = ?" );
            String first = getNameInput(scanner, "\nEnter updated first name (only letters, dashes and spaces are allowed): ");
            ps.setString(1, first);
            ps.setInt(2, tenant_id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastName(Connection connection, int tenant_id) {
        Scanner scanner = new Scanner(System.in);
        try {
            PreparedStatement ps = connection.prepareStatement("update visitor set last_name = ? where visitor_id = ?" );
            String last = getNameInput(scanner, "\nEnter updated last name (only letters, dashes and spaces are allowed):  ");
            ps.setString(1, last);
            ps.setInt(2, tenant_id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updatePhone(Connection connection, int tenant_id) {
        Scanner scanner = new Scanner(System.in);
        try {
            PreparedStatement ps = connection.prepareStatement("update visitor set phone_number = ? where visitor_id = ?" );
            String phone = getPhone(scanner, "\nEnter updated phone number (XXX-XXX-XXXX): ");
            ps.setString(1, phone);
            ps.setInt(2, tenant_id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateEmail(Connection connection, int tenant_id) {
        Scanner scanner = new Scanner(System.in);
        try {
            PreparedStatement ps = connection.prepareStatement("update visitor set email = ? where visitor_id = ?" );
            String email = getEmail(scanner, "\nEnter updated email (user@example.com): ");
            ps.setString(1, email);
            ps.setInt(2, tenant_id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // see amenities
    // know the property id
    // ssn and dob cant be update
    // update first name, last name, email or phone number
    public void showAmen(Connection connection, int prop_id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "select am.amen_id, am.name, pa.monthly_rate" +
                " from prop_amenity pa join amenity am on am.amen_id = pa.amen_id" +
                " where prop_id = ?" +
                " order by am.amen_id");
            ps.setInt(1, prop_id);
            ResultSet rs = ps.executeQuery();
            System.out.printf("\n%-15s%-40s%-15s%n", "Amenity ID", "Name", "Monthly Rate");
            System.out.println("--------------------------------------------------------------------");
            while (rs.next()) {
                int amenId = rs.getInt(1);
                String name = rs.getString(2);
                float rate = rs.getFloat(3);
                // Check if the last value retrieved was null
                if (rs.wasNull()) {
                    System.out.printf("%-15s%-40s%-15s%n", amenId, name, "0.00");
                } else {
                    System.out.printf("%-15s%-40s%-15.2f%n", amenId, name, rate);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }

    // see all the tenants to help to navigate the program
    public static void showTenants(Connection connection) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            PreparedStatement ps = connection.prepareStatement(
                "select * from tenant join visitor on tenant_id = visitor_id where tenant_id in (" + 
                " select tenant_id from lease where refund_amount is null and start_date < current_date)" + 
                " order by tenant_id" );
            ResultSet rs = ps.executeQuery();
            System.out.println("\n-------------------------------------------------------------------------------------------------------------------------");
            System.out.println("                                                  CURRENT TENANTS");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("\n%-15s%-15s%-15s%-19s%-30s%-20s%-15s%n", "Tenant ID", "First Name", "Last Name", "Phone number", "Email", "Date of birth", "SSN");
            System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int id = rs.getInt(1);
                String ssn = rs.getString(2);
                Date dob = rs.getDate(3);
                String first = rs.getString(5);
                String last = rs.getString(6);
                String phone = rs.getString(7);
                String email = rs.getString(8);
                String formDate = dateFormat.format(dob);
                System.out.printf("%-15s%-15s%-15s%-19s%-30s%-20s%-15s%n", id, first, last, phone, email, formDate, ssn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}