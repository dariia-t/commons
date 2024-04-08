import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
//import java.util.*;
import java.util.Scanner;

public class EU{
    static final String DB_URL = "jdbc:oracle:thin:@edgar1.cse.lehigh.edu:1521:cse241";
    public static void main(String[] args) {
        Connection conn = null;
        Scanner scan = new Scanner(System.in);
        do {
            try {
                System.out.print("\nEnter Oracle user id: ");
                String user = scan.nextLine();
                System.out.print("Enter Oracle user password: ");
                String pass = scan.nextLine();
                conn = DriverManager.getConnection(DB_URL, user, pass);
                System.out.println("Super! You are connected.");
            } catch(SQLException se) {
                //se.printStackTrace();
                System.out.println("[Error]: Connect error. Re-enter login data: ");
            }
        } while (conn == null);

        try{

            Tenant tenantManager = new Tenant();
            PropertyManager propertyManager = new PropertyManager();
            FinancialManager financialManager = new FinancialManager();
            
            while (true) {
                mainMenu();
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                String mainMenu = "PpTtFfQq";
                char action = prompt(in, mainMenu);

                if (action == 'Q' || action == 'q' ) {
                    try {
                        conn.close();
                        System.out.println("\n== Thank you for using the program! ==\n");
                    } catch(SQLException se) {
                        se.printStackTrace();
                        System.out.println("Error closing connection");
                    }
                    return;

                } else if (action == 'P' || action == 'p') {
                    propertyManager.welcomePM();
                    while(true){
                        propertyManagerMenu();
                        String pmMenu = "123456789BbQq";
                        action = prompt(in, pmMenu);
                        if (action == 'Q' || action == 'q' ) {
                            try {
                                conn.close();
                                System.out.println("\n== Thank you for using the program! ==\n");
                            } catch(SQLException se) {
                                se.printStackTrace();
                                System.out.println("Error closing connection");
                            }
                            return;
                        } else if (action == 'B' || action == 'b' ) {
                            break;
                        } else if (action == '1') {
                            propertyManager.recordVisit(conn);
                        } else if (action == '2') {
                            propertyManager.recordLease(conn);
                        } else if (action == '3') {
                            propertyManager.recordMoveout(conn);
                        } else if (action == '4') {
                            propertyManager.addDependant(conn);
                        } else if (action == '5') {
                            propertyManager.addPet(conn);
                        } else if (action == '6') {
                            propertyManager.addAmen(conn);
                        } else if (action == '7') {
                            propertyManager.removeAmen(conn);
                        } else if (action == '8') {
                            propertyManager.seeActive(conn);
                        } else if (action == '9') {
                            propertyManager.seePast(conn);
                        }
                    }

                } else if (action == 'T' || action == 't') {
                    int[] info = tenantManager.welcomeTenant(conn);
                    int check = 0;
                    while (true){
                        tenantMenu();
                        String tenantMenu = "1234BbQq";
                        action = prompt(in, tenantMenu);
                        if (action == 'Q' || action == 'q' ) {
                            try {
                                conn.close();
                                System.out.println("\n== Thank you for using the program! ==\n");
                            } catch(SQLException se) {
                                se.printStackTrace();
                                System.out.println("Error closing connection");
                            }
                            return;
                        } else if (action == 'B' || action == 'b' ) {
                            break;
                        } else if (action == '1') {
                            check = tenantManager.paymentStatus(conn, info[0]);
                        } else if (action == '2') {
                            tenantManager.rentalPayment(conn, info[0], check);
                        } else if (action == '3') {
                            tenantMenuThree();
                            while (true){
                                String tenantThreeMenu = "1234TtQq";
                                action = prompt(in, tenantThreeMenu);
                                if (action == 'Q' || action == 'q' ) {
                                    try {
                                        conn.close();
                                        System.out.println("\n== Thank you for using the program! ==\n");
                                    } catch(SQLException se) {
                                        se.printStackTrace();
                                        System.out.println("Error closing connection");
                                    }
                                    return;
                                } else if (action == 'T' || action == 't' ) {
                                    tenantMenu();
                                    break;
                                } else if (action == '1') {
                                    tenantManager.updateFirstName(conn, info[1]);
                                    tenantManager.getUpdatedInfo(conn, info[1]);
                                    break;
                                } else if (action == '2') {
                                    tenantManager.updateLastName(conn, info[1]);
                                    tenantManager.getUpdatedInfo(conn, info[1]);
                                    break;
                                } else if (action == '3') {
                                    tenantManager.updateEmail(conn, info[1]);
                                    tenantManager.getUpdatedInfo(conn, info[1]);
                                    break;
                                } else if (action == '4') {
                                    tenantManager.updatePhone(conn, info[1]);
                                    tenantManager.getUpdatedInfo(conn, info[1]);
                                    break;
                                } 
                            }                        
                        } else if (action == '4') {
                            tenantManager.showAmen(conn, info[2]);
                        } 
                    }

                } else if (action == 'F' || action == 'f') {
                    financialManager.welcomeFM(conn);
                    while(true){
                        financialManagerMenu();
                        String fmMenu = "12345BbQq";
                        action = prompt(in, fmMenu);
                        if (action == 'Q' || action == 'q' ) {
                            try {
                                conn.close();
                                System.out.println("\n== Thank you for using the program! ==\n");
                            } catch(SQLException se) {
                                se.printStackTrace();
                                System.out.println("Error closing connection");
                            }
                            return;
                        } else if (action == 'B' || action == 'b' ) {
                            break;
                        } else if (action == '1') {
                            financialManager.reportYear(conn);
                        } else if (action == '2') {
                            financialManager.reportPayments(conn);
                        } else if (action == '3') {
                            financialManager.reportAmenities(conn);
                        } else if (action == '4') {
                            financialManager.reportPayMethod(conn);
                        } else if (action == '5') {
                            financialManager.reportProperty(conn);
                        }
                    }
                } 
            }
        } catch (Exception e) {
            System.err.println("An exception occurred: " + e.getMessage());
            e.printStackTrace();
        }

  
        try {
            conn.close();
        } catch(SQLException se) {
            se.printStackTrace();
            System.out.println("Error closing connection");
        }
    }

    public static void mainMenu() {
        System.out.println("\n == Enter a corresponding symbol to pick the option. The case does not matter ==");
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("What is your role?");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("  [P] Property manager");
        System.out.println("  [T] Tenant");
        System.out.println("  [F] Financial manager");
        System.out.println("  [Q] Quit Program");
    }

    public static void tenantMenu() {
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("What do you want to do?");  
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("  [1] Check payment status");
        System.out.println("  [2] Make rental payment");
        System.out.println("  [3] Update personal data");
        System.out.println("  [4] See amenitites available for your property");
        System.out.println("  [B] Go back to role selection");
        System.out.println("  [Q] Quit Program");
    }

    public static void tenantMenuThree() {
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("What data you want to update?");  
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("  [1] First name");
        System.out.println("  [2] Last name");
        System.out.println("  [3] Email");
        System.out.println("  [4] Phone number");
        System.out.println("  [T] Go back to tenant menu");
        System.out.println("  [Q] Quit Program");
    }

    public static void financialManagerMenu() {
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("What do you want to do?");      
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");  
        System.out.println("  [1] Get a full financial report by year, month, day");
        System.out.println("  [2] See all the payments");
        System.out.println("  [3] See revenue made by shared amenities");
        System.out.println("  [4] Get a financial report for payment methods");
        System.out.println("  [5] Get a financial report for properties");
        System.out.println("  [B] Go back to role selection");
        System.out.println("  [Q] Quit Program");
    }

    public static void propertyManagerMenu() {
        System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("What do you want to do?");
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println("  [1] Record visit data");
        System.out.println("  [2] Record lease data");
        System.out.println("  [3] Record move-out");
        System.out.println("  [4] Add a roommate to a lease");
        System.out.println("  [5] Add a pet to a lease");     
        System.out.println("  [6] Add a shared amenity to a lease");     
        System.out.println("  [7] Remove a shared amenity from a lease");     
        System.out.println("  [8] See all active leases");     
        System.out.println("  [9] See all past leases");     
        System.out.println("  [B] Go back to role selection");
        System.out.println("  [Q] Quit Program");
    }


    // Citation: Method created following a tutorial for admin role in CSE216
    // Prof. Stephen Lee-Urban 
    // validating commands for main menu
    static char prompt(BufferedReader in, String actions) {

        while (true) {
            System.out.print("\n[*"  + "] :> ");
            String action;
            try {
                action = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (action.length() != 1){
                System.out.println("  Invalid Command");
                continue;
            }
            if (actions.contains(action)) {
                return action.charAt(0);
            }
            else {
                System.out.println("  Invalid Command");                
            }
        }
    }
}