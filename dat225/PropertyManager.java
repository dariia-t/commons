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

public class PropertyManager {

    public void welcomePM() {
        System.out.println("\n==== WELCOME TO PROPERTY MANAGER INTERFACE! ====");
    }

    // record visit
    // default date
    // asks for first and last name, phone, and email of the person
    // dispays the sucess message with visitor id in it
    // then asks for what property they visited
    // asks what apartments they visisted 

    // i need to display vacant properties 
    // i need to display vacant apartments 
    public void recordVisit(Connection connection) {
        try {
            Scanner scanner = new Scanner(System.in);

            String fname = getNameInput(scanner, "\nEnter visitor's first name: ");
            String lname = getNameInput(scanner, "Enter visitor's last name: ");   
            String phone = getPhone(scanner, "Enter visitor's phone number (XXX-XXX-XXXX): ");
            String email = getEmail(scanner, "Enter visitor's email (user@example.com): ");
            
            PreparedStatement ps = connection.prepareStatement("insert into Visitor (first_name, last_name, phone_number, email) values (?, ?, ?, ?)" );
            ps.setString(1, fname);
            ps.setString(2, lname);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.executeQuery();

            PreparedStatement visitLast = connection.prepareStatement("SELECT visitor_id" +
                    " FROM Visitor" +
                    " WHERE visitor_id = (SELECT MAX(visitor_id) FROM Visitor)");
            ResultSet last = visitLast.executeQuery();
            int visitorId = 0;
            while (last.next()) {
                visitorId = last.getInt(1);
            }

            PreparedStatement psProperty = connection.prepareStatement(
                "select * from property where prop_id in (" + 
                " select prop_id from apartment where apart_id in (select apart_id from lease where refund_amount is not null)" +
                " union" + 
                " select prop_id from apartment where apart_id not in (select apart_id from lease))"
            );
            ResultSet rsProperty = psProperty.executeQuery();
            System.out.println("\n------------------------------------------------------------------");
            System.out.println("                PROPERTIES WITH VACANT APARTMENTS");
            System.out.println("------------------------------------------------------------------");
            System.out.printf("\n%-6s%-20s%-20s%-10s%-10s%n", "ID", "Street", "City", "Zip", "Year Built");
            System.out.println("------------------------------------------------------------------");
            while (rsProperty.next()) {
                int propId = rsProperty.getInt(1);
                String street = rsProperty.getString(2);
                String city = rsProperty.getString(3);
                int zip = rsProperty.getInt(4);
                int year = rsProperty.getInt(5);
                System.out.printf("%-6s%-20s%-20s%-10s%-10s%n", propId, street, city, zip, year);
            }


            int propId = 0;
            System.out.print("\nEnter the ID of the property they visited: ");
            while (true){
                if (scanner.hasNextInt()){
                    propId = scanner.nextInt();
                    if (propId == 1 || propId == 2 || propId == 3 || propId == 4 || propId == 5) {
                        PreparedStatement psApartment = connection.prepareStatement(
                            "select * from apartment where apart_id in (select apart_id from lease where refund_amount is not null) and property_id = ?" + 
                            " union" + //
                            " select * from apartment where apart_id not in (select apart_id from lease) and property_id = ?"
                        );
                        psApartment.setInt(1, propId);
                        psApartment.setInt(2, propId);
                        ResultSet rsApartment = psApartment.executeQuery();
                        System.out.println("\n----------------------------------------------------------------------------------------");
                        System.out.println("                          VACANT APARTMENTS IN PROPERTY "+propId);
                        System.out.println("----------------------------------------------------------------------------------------");
                        System.out.printf("\n%-6s%-10s%-12s%-12s%-10s%-15s%-15s%n", "ID", "Number", "Bathrooms", "Bedrooms", "Size", "Monthly Rent", "Security Deposit");
                        System.out.println("----------------------------------------------------------------------------------------");
                        while (rsApartment.next()) {
                            int apartId = rsApartment.getInt(1);
                            int number = rsApartment.getInt(2);
                            float bath = rsApartment.getFloat(3);
                            int bed = rsApartment.getInt(4);
                            float size = rsApartment.getFloat(5);
                            float rent = rsApartment.getFloat(6);
                            float security = rsApartment.getFloat(7);
                            System.out.printf("%-6s%-10s%-12.1f%-12s%-10.2f%-15.2f%-15.2f%n", apartId, number, bath, bed, size, rent, security);
                        }
                        break;
                    } else {
                        System.out.print("[Error]: Provided integer is not 1, 2, 3, 4 or 5. Try again: ");
                    }
                } else {
                    scanner.next();
                    System.out.print("[Error]: Invalid input. Please enter a valid integer: ");
                }
            }
            int apartId = 0;
            System.out.print("\nEnter the ID of the apartment they visited: ");
            while (true){
                if (scanner.hasNextInt()){
                    apartId = scanner.nextInt();
                     PreparedStatement psApartment = connection.prepareStatement(
                            "select * from apartment where apart_id in (select apart_id from lease where refund_amount is not null) and property_id = ? and apart_id = ?" + 
                            " union" + 
                            " select * from apartment where apart_id not in (select apart_id from lease) and property_id = ? and apart_id = ?"
                    );
                    // PreparedStatement psApartment = connection.prepareStatement("select * from apartment where apart_id = ? and property_id = ?");
                    psApartment.setInt(1, propId);
                    psApartment.setInt(2, apartId);
                    psApartment.setInt(3, propId);
                    psApartment.setInt(4, apartId);
                    ResultSet rs = psApartment.executeQuery();
                    if (rs.next()) {
                        PreparedStatement psVisits = connection.prepareStatement("insert into Visits (visitor_id, apart_id, visit_date) values (?, ?, ?)");
                        psVisits.setInt(1, visitorId);
                        psVisits.setInt(2, apartId);
                        psVisits.setDate(3, Date.valueOf(LocalDate.now()));
                        psVisits.executeQuery();
                        System.out.println("\n== A visit was successfully added with visitor ID "+visitorId+" ==");
                        break;
                    } else {
                        System.out.print("[Error]: Provided apartment ID is not found for this property or it is currently occupied and not available for visiting. Try again: ");
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
    }

    // record lease data
    // need visitor_id 
    // date now or in future
    // ask what apartment they are trying to lease
    // check if they have visited it
    // ask for ssn and dob of the now tenant 
    // ask for start and end date of the lease, end must be after start
    // also can sign the lease only for the next 5 years
    // display success message with the number of the lease added 
    public void recordLease(Connection connection) {
        int visitorId = 0;
        try {
            showVisitors(connection);
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nPlease enter the visitor ID: ");
            outerLoop: while(true){
                if (scanner.hasNextInt()) {
                    visitorId = scanner.nextInt();
                    scanner.nextLine();
                    PreparedStatement psVisitor = connection.prepareStatement("select * from Visitor where visitor_id = ?");
                    psVisitor.setInt(1, visitorId);
                    ResultSet resultSetVisitor = psVisitor.executeQuery();
                    if (resultSetVisitor.next()) {
                        int id = resultSetVisitor.getInt(1);
                        String fnameTenant = resultSetVisitor.getString(2); 
                        String lnameTenant = resultSetVisitor.getString(3); 
                        String phone = resultSetVisitor.getString(4); 
                        String email = resultSetVisitor.getString(5); 

                        showVisitedApartments(connection, visitorId);

                        // what apartment do they want to lease
                        int apartId = 0;
                        int leaseId = 0;
                        System.out.print("\nEnter the ID of the apartment they want to lease: ");
                        while (true){
                            if (scanner.hasNextInt()){
                                apartId = scanner.nextInt();
                                PreparedStatement psApartment = connection.prepareStatement("select * from apartment where apart_id = ? ");
                                psApartment.setInt(1, apartId);
                                ResultSet rs = psApartment.executeQuery();
                                if (rs.next()) {
                                    PreparedStatement psVisits = connection.prepareStatement("select * from visits where visitor_id = ? and apart_id = ?");
                                    psVisits.setInt(1, visitorId);
                                    psVisits.setInt(2, apartId);
                                    ResultSet visited = psVisits.executeQuery();
                                    if (visited.next()){
                                        System.out.println("\n== Great! The prospective tenant visited this apartment! ==");
                                        // add into tenant table
                                        PreparedStatement ps = connection.prepareStatement("insert into Tenant (tenant_id, ssn, dob) values (?, ?, ?)" ); 
                                        scanner.nextLine();
                                        Date dob = getDateInputNotMinor(scanner, "\nEnter prospective tenant's date of birth (dd-Mmm-yyyy): ");
                                        String ssn = getSsn(scanner, "Enter prospective SSN (XXX-XX-XXXX): ");
                                        ps.setInt(1, id);
                                        ps.setString(2, ssn);
                                        ps.setDate(3, dob);
                                        ps.executeQuery();
                                        // prospective tenant info 
                                        PreparedStatement psTenant = connection.prepareStatement("select * from Tenant where tenant_id = ?");
                                        psTenant.setInt(1, id);
                                        ResultSet resultSetTenant = psTenant.executeQuery();    
                                        while (resultSetTenant.next()) {
                                            String ssn1 = resultSetTenant.getString(2); 
                                            Date dobTenant = resultSetTenant.getDate(3); 
                                            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                                            String formattedDate = dateFormat.format(dobTenant);
                                            System.out.println("\nProspective Tenant Personal Information");  
                                            System.out.println("-------------------------------------------------");
                                            System.out.println("  Full name:\t\t" + fnameTenant +" "+ lnameTenant);
                                            System.out.println("  Phone:\t\t"+ phone);
                                            System.out.println("  Email:\t\t"+ email);
                                            System.out.println("  SSN:\t\t\t" + ssn1);
                                            System.out.println("  Date of birth:\t" + formattedDate );  
                                        }

                                        // i have apart id, id 
                                        // need start and end 
                                        // rent is automatically calculated by trigger
                                        PreparedStatement psAddLease = connection.prepareStatement("INSERT INTO Lease (start_date, end_date, apart_id, tenant_id) VALUES (?, ?, ?, ?)");
                                        Date start = getDateInputFuture(scanner, "\nEnter lease start date (dd-Mmm-yyyy): ");
                                        LocalDate localStartDate = start.toLocalDate();
                                        LocalDate localEndDate = localStartDate.plusYears(1);
                                        Date end = Date.valueOf(localEndDate);
                                        psAddLease.setDate(1, start);
                                        psAddLease.setDate(2, end);
                                        psAddLease.setInt(3, apartId);
                                        psAddLease.setInt(4, id);
                                        psAddLease.executeQuery();
                                        PreparedStatement leaseLast = connection.prepareStatement("SELECT lease_id" +
                                            " FROM Lease" +
                                            " WHERE lease_id = (SELECT MAX(lease_id) FROM Lease)");
                                        ResultSet last = leaseLast.executeQuery();
                                        while (last.next()) {
                                            leaseId = last.getInt(1);
                                            System.out.println("\n== A lease was successfully added with lease ID "+leaseId+" ==");
                                        }
                                        break outerLoop;
                                    } else {
                                        System.out.println("\n== Provided apartment was not visited. Please schedule a visit to view the appartment ==");
                                        break outerLoop;
                                    }
                                } else {
                                    System.out.print("[Error]: Provided apartment ID is not found. Try again: ");
                                }
                            } else {
                                scanner.next();
                                System.out.print("[Error]: Invalid input. Please enter a valid integer: ");
                            }
                        } 
                    } else {
                        System.out.print("[Error]: Provided ID was not found in the database. Enter a valid Visitor ID: ");
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
    }

    // record move out 
    // find their lease by tenant id 
    // default date 
    // display the deposit they paid 
    // deside how much refund to give 
    public void recordMoveout(Connection connection) {
        int tenantId = 0;

        try {
            System.out.println("\n------------------------------------------------------------------------------------------------------");
            System.out.println("                                             ACTIVE LEASES");
            System.out.println("------------------------------------------------------------------------------------------------------");
            seeActive(connection);
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nPlease enter the tenant ID: ");
            while(true){
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    scanner.nextLine();
                    PreparedStatement psTenant = connection.prepareStatement("select * from Tenant where tenant_id = ?");
                    psTenant.setInt(1, tenantId);
                    ResultSet resultSetTenant = psTenant.executeQuery();
                    if (resultSetTenant.next()) {
                        // find their apartment 
                        PreparedStatement psApart = connection.prepareStatement("select apart_id from lease where tenant_id = ? and start_date < current_date" );
                        psApart.setInt(1, tenantId);
                        ResultSet rsApart = psApart.executeQuery();
                        int apartId = 0;
                        if (rsApart.next()) {
                            apartId = rsApart.getInt(1);
                        } else {
                            System.out.println("\n== Unable to move-out since this lease has not started yet ==");
                            break;
                        }

                        PreparedStatement psDeposit = connection.prepareStatement("select security_deposit from apartment where apart_id = ?" );
                        psDeposit.setInt(1, apartId);
                        ResultSet rsDeposit = psDeposit.executeQuery();
                        float deposit = 0;
                        while (rsDeposit.next()) {
                            deposit = rsDeposit.getFloat(1);
                            String formattedDeposit = String.format("%.2f", deposit);
                            System.out.println("\nThe security deposit for their apartment with ID "+apartId+" was $" + formattedDeposit);
                        }         
                        float refund = getRefund(scanner,"\nEnter the amount to refund: $", deposit) ;   
                        
                        PreparedStatement psMoveOut = connection.prepareStatement("update lease set move_out_date = ? where tenant_id = ?" );
                        PreparedStatement psRefund = connection.prepareStatement("update lease set refund_amount = ? where tenant_id = ?" );

                        psMoveOut.setDate(1, Date.valueOf(LocalDate.now()));
                        psMoveOut.setInt(2, tenantId);
                        psMoveOut.executeUpdate();

                        psRefund.setFloat(1, refund);
                        psRefund.setInt(2, tenantId);
                        psRefund.executeUpdate();

                        System.out.println("\n== A move out was successfully processed ==");
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
    }

    // add dependant
    // need tenant_id
    // ask for first name, last name and dob
    public void addDependant(Connection connection) {
        int tenantId = 0;
        try {
            showTenants(connection);
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nPlease enter the tenant ID: ");
            while(true){
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    scanner.nextLine();
                    PreparedStatement psTenant = connection.prepareStatement("select * from Tenant where tenant_id = ?");
                    psTenant.setInt(1, tenantId);
                    ResultSet resultSetTenant = psTenant.executeQuery();
                    if (resultSetTenant.next()) {
                        PreparedStatement ps = connection.prepareStatement("insert into Roommate (tenant_id, first_name, last_name, dob) values (?, ?, ?, ?)" );
                        String fname = getNameInput(scanner, "\nEnter roommate's first name: ");
                        String lname = getNameInput(scanner, "Enter roommate's last name: ");
                        Date dob = getDateInput(scanner, "Enter roommate's date of birth (dd-Mmm-yyyy): ");
                        ps.setInt(1, tenantId);
                        ps.setString(2, fname);
                        ps.setString(3, lname);
                        ps.setDate(4, dob);
                        ps.executeQuery();
                        PreparedStatement roomLast = connection.prepareStatement("SELECT roommate_id" +
                            " FROM Roommate" +
                            " WHERE roommate_id = (SELECT MAX(roommate_id) FROM Roommate)");
                        ResultSet last = roomLast.executeQuery();
                        while (last.next()) {
                            int id = last.getInt(1);
                            System.out.println("\n== A roommate was successfully added with rommate id "  +id+ " ==");
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
    }

    // add pet
    // need tenant_id
    // ask for name and type
    public void addPet(Connection connection) {
        int tenantId = 0;
        try {
            showTenants(connection);
            Scanner scanner = new Scanner(System.in);
            System.out.print("\nPlease enter the tenant ID: ");
            while(true){
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    scanner.nextLine();
                    PreparedStatement psTenant = connection.prepareStatement("select * from Tenant where tenant_id = ?");
                    psTenant.setInt(1, tenantId);
                    ResultSet resultSetTenant = psTenant.executeQuery();
                    if (resultSetTenant.next()) {
                        PreparedStatement ps = connection.prepareStatement("insert into Pet (tenant_id, pet_name, pet_type) values (?, ?, ?)" );
                        String name = getNameInput(scanner, "\nEnter pet's name: ");
                        String type = getNameInput(scanner, "Enter pet's type: ");
                        ps.setInt(1, tenantId);
                        ps.setString(2, name);
                        ps.setString(3, type);
                        ps.executeQuery();
                        PreparedStatement petLast = connection.prepareStatement("SELECT pet_id" +
                            " FROM Pet" +
                            " WHERE pet_id = (SELECT MAX(pet_id) FROM Pet)");
                        ResultSet last = petLast.executeQuery();
                        while (last.next()) {
                            int id = last.getInt(1);
                            System.out.println("\n== A pet was successfully added with pet id "  +id+ " ==");
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
    }

    // see all active leases 
    // select * from lease where move_out_date is null;
    public void seeActive(Connection connection){
        try {
            PreparedStatement ps = connection.prepareStatement(
                "select lease_id, start_date, end_date, rent_amount, apart_id, tenant_id, first_name, last_name" + 
                " from lease join visitor on tenant_id = visitor_id " +
                " where move_out_date is null and start_date < current_date" +
                " order by lease_id" );
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            System.out.printf("\n%-10s%-10s%-15s%-15s%-15s%-10s%-15s%-15s%n", "Lease ID", "Tenant ID", "Start Date", "End Date", "Rent Amount", "Apart ID",  "First Name", "Last Name");
            System.out.println("------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int leaseId = rs.getInt(1);
                Date start = rs.getDate(2);
                Date end = rs.getDate(3);
                float rent = rs.getFloat(4);
                int apartId = rs.getInt(5);
                int tenantId = rs.getInt(6);
                String first = rs.getString(7);
                String last = rs.getString(8);
                String formatedStart = dateFormat.format(start);
                String formatedEnd = dateFormat.format(end);
                System.out.printf("%-10s%-10s%-15s%-15s%-15.2f%-10s%-15s%-15s%n", leaseId, tenantId, formatedStart, formatedEnd, rent, apartId, first, last);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // see all past leases
    // select * from lease where move_out_date is not null;
    public void seePast(Connection connection){
        try {
            PreparedStatement ps = connection.prepareStatement(
                "select lease_id, start_date, end_date, rent_amount, move_out_date, refund_amount, apart_id, tenant_id, first_name, last_name" + 
                " from lease join visitor on tenant_id = visitor_id " +
                " where move_out_date is not null" +
                " order by lease_id" );
            ResultSet rs = ps.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            System.out.printf("\n%-10s%-15s%-15s%-15s%-17s%-15s%-10s%-10s%-15s%-15s%n", "Lease ID", "Start Date", "End Date", "Rent Amount", "Move Out Date", "Refund Amount", "Apart ID", "Tenant ID", "First Name", "Last Name");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int leaseId = rs.getInt(1);
                Date start = rs.getDate(2);
                Date end = rs.getDate(3);
                float rent = rs.getFloat(4);
                Date out = rs.getDate(5);
                float refund = rs.getFloat(6);
                int apartId = rs.getInt(7);
                int tenantId = rs.getInt(8);
                String first = rs.getString(9);
                String last = rs.getString(10);
                String formatedStart = dateFormat.format(start);
                String formatedEnd = dateFormat.format(end);
                String formatedOut = dateFormat.format(out);
                System.out.printf("%-10s%-15s%-15s%-15.2f%-17s%-15.2f%-10s%-10s%-15s%-15s%n", leaseId, formatedStart, formatedEnd, rent, formatedOut, refund, apartId, tenantId, first, last);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }        
    }

    // add amenity
    // ask for amen and tenant id 
    // look up the lease by tenant id
    // display the info
    // insert the amenity
    // dispaly the updated lease info
    public void addAmen(Connection connection) {
        int tenantId = 0;
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nFirst, let's find the lease information");
            showTenants(connection);
            System.out.print("\nPlease enter the tenant ID: ");
            while(true){
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    scanner.nextLine();
                    PreparedStatement psTenant = connection.prepareStatement("select * from Tenant where tenant_id = ?");
                    psTenant.setInt(1, tenantId);
                    ResultSet resultSetTenant = psTenant.executeQuery();
                    if (resultSetTenant.next()) {
                        PreparedStatement psLease = connection.prepareStatement(
                            "select l.lease_id, l.start_date, l.end_date, l.rent_amount,  l.apart_id, v.first_name, v.last_name" + 
                            " from Lease l join Visitor v on v.visitor_id = l.tenant_id" + 
                            " where l.tenant_id = ?");
                        psLease.setInt(1, tenantId);
                        ResultSet resultSetLease = psLease.executeQuery();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                        int leaseId = 0;
                        int apartId = 0;
                        while(resultSetLease.next()) {
                            leaseId = resultSetLease.getInt(1); 
                            Date start = resultSetLease.getDate(2); 
                            Date end = resultSetLease.getDate(3); 
                            float rent = resultSetLease.getFloat(4); 
                            apartId = resultSetLease.getInt(5); 
                            String fnameTenant = resultSetLease.getString(6); 
                            String lnameTenant = resultSetLease.getString(7); 
                            String formatedStart = dateFormat.format(start);
                            String formatedEnd = dateFormat.format(end);
                            System.out.println("\nLease Information");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Full name:\t\t" + fnameTenant +" "+ lnameTenant);
                            System.out.println("  Apartment ID:\t\t" + apartId );
                            System.out.println("  Start date:\t\t" + formatedStart );
                            System.out.println("  End date:\t\t" + formatedEnd);
                            System.out.println("  Rent:\t\t\t$" + String.format("%.2f", rent));    
                        }
                        PreparedStatement pscheck = connection.prepareStatement("select * from prop_amenity_in_lease where lease_id = ?");
                        pscheck.setInt(1, leaseId);
                        ResultSet rscheck = pscheck.executeQuery();
                        if (rscheck.next()) {
                            showAmenInLease(connection, leaseId);
                        }

                        showAmenForProp(connection, apartId);

                        PreparedStatement ps = connection.prepareStatement("insert into Prop_Amenity_In_Lease (lease_id, amen_id) values (?, ?)" );
                        int amenId = 0;
                        while(true) {
                            amenId = getIntInput(scanner, "\nEnter the ID of amenity to add: ", "amenity ID");
                            PreparedStatement psCheckamen = connection.prepareStatement(
                                "select am.amen_id, am.name, pa.monthly_rate" +
                                " from apartment ap join prop_amenity pa on ap.property_id = pa.prop_id" +
                                " join amenity am on am.amen_id = pa.amen_id" +
                                " where apart_id = ? and am.amen_id = ?" +
                                " order by am.amen_id");
                            psCheckamen.setInt(1, apartId);
                            //ResultSet rs = ps.executeQuery();
                            //PreparedStatement psCheckamen = connection.prepareStatement("select * from amenity where amen_id = ?");
                            psCheckamen.setInt(2, amenId);
                            ResultSet rsCheck = psCheckamen.executeQuery();
                            if (rsCheck.next()) {
                                PreparedStatement psAlreadyAdded = connection.prepareStatement(
                                "select * from prop_amenity_in_lease" + 
                                " where amen_id = ? and lease_id = ?");
                                psAlreadyAdded.setInt(1, amenId);
                                psAlreadyAdded.setInt(2, leaseId);
                                ResultSet rsAlreadyAdded = psAlreadyAdded.executeQuery();
                                if (rsAlreadyAdded.next()){
                                    System.out.println("This amenity is already in the lease. Please enter an new amenity ID!");
                                } else{
                                    break;
                                }
                            } else {
                                System.out.println("The amenity ID was not found for this property. Please enter a valid one!");
                            }
                        }
                        ps.setInt(1, leaseId);
                        ps.setInt(2, amenId);
                        ps.executeQuery();
                        System.out.println("\n== The amenity was successfully added! == ");

                        ResultSet resultSetLeaseNew = psLease.executeQuery();
                        while(resultSetLeaseNew.next()) {
                            leaseId = resultSetLeaseNew.getInt(1); 
                            Date start = resultSetLeaseNew.getDate(2); 
                            Date end = resultSetLeaseNew.getDate(3); 
                            float rent = resultSetLeaseNew.getFloat(4); 
                            int apartIde = resultSetLeaseNew.getInt(5); 
                            String fnameTenant = resultSetLeaseNew.getString(6); 
                            String lnameTenant = resultSetLeaseNew.getString(7); 
                            String formatedStart = dateFormat.format(start);
                            String formatedEnd = dateFormat.format(end);
                            System.out.println("\nUpdated Lease Information");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Full name:\t\t" + fnameTenant +" "+ lnameTenant);
                            System.out.println("  Apartment ID:\t\t" + apartIde );
                            System.out.println("  Start date:\t\t" + formatedStart );
                            System.out.println("  End date:\t\t" + formatedEnd);
                            System.out.println("  Rent:\t\t\t$" + String.format("%.2f", rent));    
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
    }

    // delete amenity
    // ask for amen and tenant id 
    // look up the lease by tenant id
    // display the info
    // delete the amenity
    // dispaly the updated lease info
    public void removeAmen(Connection connection) {
        int tenantId = 0;
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("\nFirst, let's find the lease information");
            showTenants(connection);
            System.out.print("\nPlease enter the tenant ID: ");
            while(true){
                if (scanner.hasNextInt()) {
                    tenantId = scanner.nextInt();
                    scanner.nextLine();
                    PreparedStatement psTenant = connection.prepareStatement("select * from Tenant where tenant_id = ?");
                    psTenant.setInt(1, tenantId);
                    ResultSet resultSetTenant = psTenant.executeQuery();
                    if (resultSetTenant.next()) {
                        PreparedStatement psLease = connection.prepareStatement(
                            "select l.lease_id, l.start_date, l.end_date, l.rent_amount,  l.apart_id, v.first_name, v.last_name" + 
                            " from Lease l join Visitor v on v.visitor_id = l.tenant_id" + 
                            " where l.tenant_id = ?");
                        psLease.setInt(1, tenantId);
                        ResultSet resultSetLease = psLease.executeQuery();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
                        int leaseId = 0;
                        int apartId = 0;
                        while(resultSetLease.next()) {
                            leaseId = resultSetLease.getInt(1); 
                            Date start = resultSetLease.getDate(2); 
                            Date end = resultSetLease.getDate(3); 
                            float rent = resultSetLease.getFloat(4); 
                            apartId = resultSetLease.getInt(5); 
                            String fnameTenant = resultSetLease.getString(6); 
                            String lnameTenant = resultSetLease.getString(7); 
                            String formatedStart = dateFormat.format(start);
                            String formatedEnd = dateFormat.format(end);
                            System.out.println("\nLease Information");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Full name:\t\t" + fnameTenant +" "+ lnameTenant);
                            System.out.println("  Apartment ID:\t\t" + apartId );
                            System.out.println("  Start date:\t\t" + formatedStart );
                            System.out.println("  End date:\t\t" + formatedEnd);
                            System.out.println("  Rent:\t\t\t$" + String.format("%.2f", rent));    
                        }
                        PreparedStatement pscheck = connection.prepareStatement(
                            "SELECT DISTINCT pal.amen_id, am.name, pa.monthly_rate" +
                            " FROM prop_amenity_in_lease pal" +
                            " JOIN amenity am ON am.amen_id = pal.amen_id" + 
                            " JOIN prop_amenity pa ON pa.amen_id = am.amen_id" +
                            " WHERE lease_id = ?");
                        pscheck.setInt(1, leaseId);
                        ResultSet rscheck = pscheck.executeQuery();
                        if (rscheck.next()){
                            showAmenInLease(connection, leaseId);
                        } else {
                            System.out.println("\n== Provided lease has no amenities to remove! ==");
                            break;
                        }

                        PreparedStatement ps = connection.prepareStatement("delete from Prop_Amenity_In_Lease where lease_id = ? and amen_id = ?" );
                        int amenId = 0;
                        while(true) {
                            amenId = getIntInput(scanner, "\nEnter the ID of amenity to remove: ", "amenity ID");
                            PreparedStatement psCheckamen = connection.prepareStatement(
                                "select am.amen_id, am.name, pa.monthly_rate" +
                                " from apartment ap join prop_amenity pa on ap.property_id = pa.prop_id" +
                                " join amenity am on am.amen_id = pa.amen_id" +
                                " where apart_id = ? and am.amen_id = ?" +
                                " order by am.amen_id");
                            psCheckamen.setInt(1, apartId);
                            //ResultSet rs = ps.executeQuery();
                            //PreparedStatement psCheckamen = connection.prepareStatement("select * from amenity where amen_id = ?");
                            psCheckamen.setInt(2, amenId);
                            ResultSet rsCheck = psCheckamen.executeQuery();
                            //PreparedStatement psCheckamen = connection.prepareStatement("select * from amenity where amen_id = ?");
                            //psCheckamen.setInt(1, amenId);
                            //ResultSet rsCheck = psCheckamen.executeQuery();
                            if (rsCheck.next()) {
                                PreparedStatement psAlreadyAdded = connection.prepareStatement(
                                "select * from prop_amenity_in_lease" + 
                                " where amen_id = ? and lease_id = ?");
                                psAlreadyAdded.setInt(1, amenId);
                                psAlreadyAdded.setInt(2, leaseId);
                                ResultSet rsAlreadyAdded = psAlreadyAdded.executeQuery();
                                if (rsAlreadyAdded.next()){
                                    break;
                                } else{
                                    System.out.println("This amenity is not in the lease. Please try again!");
                                }
                            } else {
                                System.out.println("The amenity ID was not found for this property. Please enter a valid one!");
                            }
                        }
                        ps.setInt(1, leaseId);
                        ps.setInt(2, amenId);
                        ps.executeQuery();
                        System.out.println("\n== The amenity was successfully removed! == ");
                        ResultSet resultSetLeaseNew = psLease.executeQuery();
                        while(resultSetLeaseNew.next()) {
                            leaseId = resultSetLeaseNew.getInt(1); 
                            Date start = resultSetLeaseNew.getDate(2); 
                            Date end = resultSetLeaseNew.getDate(3); 
                            float rent = resultSetLeaseNew.getFloat(4); 
                            apartId = resultSetLeaseNew.getInt(5); 
                            String fnameTenant = resultSetLeaseNew.getString(6); 
                            String lnameTenant = resultSetLeaseNew.getString(7); 
                            String formatedStart = dateFormat.format(start);
                            String formatedEnd = dateFormat.format(end);
                            System.out.println("\nUpdated Lease Information");  
                            System.out.println("-------------------------------------------------");
                            System.out.println("  Full name:\t\t" + fnameTenant +" "+ lnameTenant);
                            System.out.println("  Apartment ID:\t\t" + apartId );
                            System.out.println("  Start date:\t\t" + formatedStart );
                            System.out.println("  End date:\t\t" + formatedEnd);
                            System.out.println("  Rent:\t\t\t$" + String.format("%.2f", rent));    
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
    }

    // helper method to get name
    public static String getNameInput(Scanner sc, String message) {
        String name = null;
        System.out.print(message);
        while (name == null || !isValidName(name)) {
            name = sc.nextLine();
            if (!isValidName(name)) {
                System.out.print("Please enter a valid name (no special characters): ");
            }
        }
        name = formatName(name);
        return name;
    }

    private static boolean isValidName(String name) {
        return name != null && name.matches("^[a-zA-Z- ]+$");
    }

    // helper method to format the name if they entered it in the wrong case 
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


    // get the date of birth for roommate, can be under 18, cant be over 120
    public static Date getDateInput(Scanner sc, String message) {
        Date userDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        System.out.print(message);
        while (userDate == null || userDate.toLocalDate().isAfter(LocalDate.now()) || getYear(userDate) < 1902) {
            try {
                String userInput = sc.nextLine();
                LocalDate localDate = LocalDate.parse(userInput, formatter);

                if (localDate.isAfter(LocalDate.now()) || getYear(Date.valueOf(localDate)) < 1902) {
                    throw new ParseException("Entered date must be in the past and not earlier than 1903", 0);
                }

                userDate = Date.valueOf(localDate);
            } catch (Exception e) {
                System.out.print("Please enter a valid past date after year 1903 (dd-Mmm-yyyy): ");
            }
        }

        return userDate;
    }
    
    // get the date of birth of tenant, must be over 18
    public static Date getDateInputNotMinor(Scanner sc, String message) {
        Date userDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        System.out.print(message);
        while (userDate == null || userDate.toLocalDate().isAfter(LocalDate.now()) || getYear(userDate) < 1902 || getYear(userDate) > 2005) {
            try {
                String userInput = sc.nextLine();
                LocalDate localDate = LocalDate.parse(userInput, formatter);

                if (localDate.isAfter(LocalDate.now()) || getYear(Date.valueOf(localDate)) < 1902 || getYear(Date.valueOf(localDate)) > 2005) {
                    throw new ParseException("Entered date must be in the past, after 1903 and before 2005", 0);
                }

                userDate = Date.valueOf(localDate);
            } catch (Exception e) {
                System.out.print("Please enter a valid past date after year 1903 and before 2005 (dd-Mmm-yyyy): ");
            }
        }

        return userDate;
    }

    private static int getYear(Date date) {
        // Convert java.util.Date to java.time.LocalDate
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        return Integer.parseInt(yearFormat.format(date));
    }

    // helper to get a valid integer
    public int getIntInput(Scanner sc, String message, String subject) {
        int userInt = 0;
        boolean isValidInput = false;
        while (!isValidInput) {
            System.out.print(message);
            try {
                userInt = Integer.parseInt(sc.nextLine());
                isValidInput = true;
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid " + subject + " (an integer). ");
            }
        }
        return userInt;
    }

    // helper to get and validate phone number
    public static String getPhone(Scanner sc, String message) {
        String phone = null;
        System.out.print(message);
        while (phone == null || !isValidPhone(phone)) {
            phone = sc.nextLine();
    
            if (!isValidPhone(phone)) {
                System.out.print("Please enter a valid phone number (XXX-XXX-XXXX): ");
            }
        }
        return phone;
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\d{3}-\\d{3}-\\d{4}");
    }

    // helper to get and validate the ssn
    public static String getSsn(Scanner sc, String message) {
        String ssn = null;
        System.out.print(message);
        while (ssn == null || !isValidSsn(ssn)) {
            ssn = sc.nextLine();
    
            if (!isValidSsn(ssn)) {
                System.out.print("Please enter a valid SSN (XXX-XX-XXXX): ");
            }
        }
        return ssn;
    }

    public static boolean isValidSsn(String ssn) {
        return ssn != null && ssn.matches("\\d{3}-\\d{2}-\\d{4}");
    }

    // helper to get and validate email
    public static String getEmail(Scanner sc, String message) {
        String email = null;
        System.out.print(message);
        while (email == null || !isValidEmail(email)) {
            email = sc.nextLine();
    
            if (!isValidEmail(email)) {
                System.out.print("Please enter a valid email (user@example.come): ");
            }
        }
        return email;
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches(".+@.+\\..+");
    }

    // get the date for the lease that must be in the future
    public static Date getDateInputFuture(Scanner sc, String message) {
        Date userDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

        System.out.print(message);
        while (userDate == null || userDate.toLocalDate().isBefore(LocalDate.now()) || getYear(userDate) > 2033) {
            try {
                String userInput = sc.nextLine();
                LocalDate localDate = LocalDate.parse(userInput, formatter);
                userDate = Date.valueOf(localDate); 

                if (userDate.toLocalDate().isBefore(LocalDate.now()) || getYear(Date.valueOf(localDate)) > 2033) {
                    throw new ParseException("Entered date must be in the future and before 2033", 0);
                }
            } catch (Exception e) {
                System.out.print("You can't create a lease for year after 2033! Please enter a valid future date (dd-Mmm-yyyy): ");
            }
        }
        return userDate;
    }

    // get the value of the refund
    // float with 2 decimal places
    public static float getRefund(Scanner sc, String message, float deposit) {
        float userFloat = 0;
        boolean isValidInput = false;
        System.out.print(message);
        while (!isValidInput) {
            try {
                userFloat = Float.parseFloat(sc.nextLine());
                if (userFloat >= 0 && userFloat <= deposit) {
                    isValidInput = true;
                } else {
                    System.out.print("Refund amount must be positive and less than or equal to the security deposit. Try again: $");
                }
            } catch (NumberFormatException e) {
                System.out.print("Please enter a valid refund number (a float): $");
            }
        }
        return userFloat;
    }

    // showing all the visitors for better navigation
    public static void showVisitors(Connection connection) {
            try {
            PreparedStatement ps = connection.prepareStatement(
                "select * from visitor order by visitor_id" );
            ResultSet rs = ps.executeQuery();
            System.out.println("\n-----------------------------------------------------------------------------------------------------");
            System.out.println("                                            ALL VISITORS");
            System.out.println("-----------------------------------------------------------------------------------------------------");
            System.out.printf("\n%-15s%-15s%-15s%-19s%-30s%n", "Visitor ID", "First Name", "Last Name", "Phone number", "Email");
            System.out.println("-----------------------------------------------------------------------------------------------------");
            while (rs.next()) {
                int visitor = rs.getInt(1);
                String first = rs.getString(2);
                String last = rs.getString(3);
                String phone = rs.getString(4);
                String email = rs.getString(5);
                System.out.printf("%-15s%-15s%-15s%-19s%-30s%n", visitor, first, last, phone, email);
            }
            PreparedStatement pst = connection.prepareStatement(
                "select * from visitor where visitor_id not in (select tenant_id from tenant) order by visitor_id" );
            ResultSet rst = pst.executeQuery();
            System.out.println("\n-----------------------------------------------------------------------------------------------------");
            System.out.println("                                   VISITORS WHO ARE NOT TENANTS");
            System.out.println("-----------------------------------------------------------------------------------------------------");
            System.out.printf("\n%-15s%-15s%-15s%-19s%-30s%n", "Visitor ID", "First Name", "Last Name", "Phone number", "Email");
            System.out.println("-----------------------------------------------------------------------------------------------------");
            while (rst.next()) {
                int visitor = rst.getInt(1);
                String first = rst.getString(2);
                String last = rst.getString(3);
                String phone = rst.getString(4);
                String email = rst.getString(5);
                System.out.printf("%-15s%-15s%-15s%-19s%-30s%n", visitor, first, last, phone, email);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // show the apartemnts visited by the visitor
    public static void showVisitedApartments(Connection connection, int visitorId) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
            PreparedStatement ps = connection.prepareStatement(
                "select apart_id, visit_date from visits where visitor_id = ?" );
            ps.setInt(1, visitorId);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n-----------------------------------------------");
            System.out.println("       APARTMENTS VISITED BY VISITOR "+visitorId);
            System.out.println("-----------------------------------------------");
            System.out.printf("\n%-17s%-17s%n", "Apartment ID", "Visit Date");
            System.out.println("-----------------------------------------------");
            while (rs.next()) {
                int apt = rs.getInt(1);
                Date date = rs.getDate(2);
                String formDate = dateFormat.format(date);
                System.out.printf("%-17s%-17s%n", apt, formDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // se all the active tenants
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

    // see amenties associated with the lease
    public static void showAmenInLease(Connection connection, int lease) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT DISTINCT pal.amen_id, am.name, pa.monthly_rate" +
                " FROM prop_amenity_in_lease pal" +
                " JOIN amenity am ON am.amen_id = pal.amen_id" + 
                " JOIN prop_amenity pa ON pa.amen_id = am.amen_id" +
                " WHERE lease_id = ?");
            ps.setInt(1, lease);
            ResultSet rs = ps.executeQuery();
            System.out.println("\n----------------------------------------------------------------------");
            System.out.println("                    AMENITIES IN LEASE "+ lease);
            System.out.println("----------------------------------------------------------------------");

            System.out.printf("\n%-15s%-40s%-15s%n", "Amenity ID", "Name", "Monthly Rate");
            System.out.println("----------------------------------------------------------------------");
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

    // see amenities
    // know the property id
    // ssn and dob cant be update
    // update first name, last name, email or phone number
    public void showAmenForProp(Connection connection, int apart_id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "select am.amen_id, am.name, pa.monthly_rate" +
                " from apartment ap join prop_amenity pa on ap.property_id = pa.prop_id" +
                " join amenity am on am.amen_id = pa.amen_id" +
                " where apart_id = ?" +
                " order by am.amen_id");
            ps.setInt(1, apart_id);
            ResultSet rs = ps.executeQuery();
    
            System.out.println("\n----------------------------------------------------------------------");
            System.out.println("                AVAILABLE AMENITIES FOR YOUR PROPERTY ");
            System.out.println("----------------------------------------------------------------------");
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

}