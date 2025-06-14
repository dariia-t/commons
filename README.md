# EU Property Management
  
## Description of the interfaces 

  ### Property Manager 
This interface allows the property manager to record visit data, add a new lease, record a move-out, add a roommate or pet to a lease, add/remove amenity from the lease, and see active and past leases.

  ### Tenant
This interface allows tenant to check payment status for their lease, make a payment, update personal data (name, email, phone number), and see shared amenities available for their property. Upon login into the interface, the full information about personal data, lease, apartment, property, and amenities will be shown. 

  ### Financial Manager
This interface allows the financial manager to get a full financial report by each year, month, and day, see all the payments made in the system, analyze the revenues made by specific amenities or properties, and see statistics about the use of different payment methods. 

  Refer to the "Testing the flow" and "Assumptions I make" sections to see the specific functionality and requirements for each interface. 


## Assumptions I make 

  - Every tenant/visitor knows their ID. The Property Manager can see every tenant's ID. In case the tenant forgets the ID, they can contact the Property Manager to retrieve it by confirming their identity and phone number, but ideally, these situations should be avoided. 

  - Tenant can modify the personal information but can't edit the information associated with the lease, such as add/drop amenities or record move-out. If they want to modify information, associated with the lease, they need to contact their Property Manager providing their ID and the ID of amenities they want to add/drop.

  - This is a young enterprise that started operating in July 2022. That means that the majority of leases started in the past year and are still ongoing, however, there are 10 tenants whose leases ended and they have moved out. Those 10 apartments are now vacant, as well as an additional 5 apartments that have never been leased. That leaves 15 apartments available for visiting. Overall, there are 5 properties 12 apartments each in the greater Philadelphia area. 

  - I assume that the EU started using the database on October 1st, 2023. That means, that all the visits past that date have been recorded in the database. Also, all the payments past that date have been recorded. That will be reflected in a financial report that will have payments only for October and November, and December payments made at the time of testing the program by graders. I made this assumption because I have around 50 leases, and even with just October and November payments, it is around 100 inserts. The due dates for payments depend on the start date of the lease, so it would be a lot of manual editing of the payment dates if I were to add more payments for previous months.

  - All the other data which is not payments or visits, was successfully transferred by the enterprise to the database. That means that even if the lease ended before October 1st, 2023, you can still view the information about it. 

  - Monthly rate for the property amenity can be null. This applies mostly to amenities such as outdoor barbecue areas or community lounges which every tenant should be able to enjoy "free of charge". The costs associated with the maintenance of those amenities are already factored into the monthly rent amount. 

  - The property manager can't add a shared amenity that does not belong to the tenant's property.

  - Private amenity prices are already included in the monthly rent because if the apartment comes with a balcony, we don't want to have to take it down. 

  - Any person in the system (tenant, visitor, roommate) can't be over 120 years old. Prospective tenants must be over 18 years old to sign a lease. Visitors or roommates can be under 18 though, to account for tenants with families and children.

  - All the transactions that have date attributes are relative to the current date of the system. 

  - The expiration date of the card used for payment can't be in the past and must be within the next 20 years. 

  - The lease start date can't be in the past and must be within the next 10 years.

  - The lease is automatically signed for a year. It can't be signed for less than a year, however, the tenant can move out before the end date of the lease. If the tenant wants to extend the lease for more than a year, they should contact the property manager to sign "another lease" using the same tenant and property ID. 

  - The visitor can't visit a property that is currently occupied by a tenant.

  - The prospective tenant can't lease the property they have not visited. 

  - The date of the visit/move-out is recorded as the current date of the system at the moment of adding the visit/move-out. For both of those transactions, the property manager has to be present, because they either guide the visitors or asses the apartment conditions to decide on the refund amount for the move-out. I was imagining a property manager with a tablet walking around apartments and recording those transactions in real-time. I also made this decision to avoid adding transactions in the past or future if they did not happen. 

  - The tenant has to pay the exact amount of rent once per month. They can't pay a fraction of the rent. Also, if the payment was made before the due date and they want to pay in advance for the next month, they still have to wait until after the due date for this month to pay for the next month.

  - The only case where payment is allowed more than one time per month is if their first payment was overdue, so they are paying for this and the previous month. 


## Triggers 

  - set_initital_rent:
    Before adding a lease, this trigger sets the initial rent amount in the lease to the monthly rent of the apartment.
  - update_rent:
    After adding an amenity to a lease, this trigger adds the monthly rate of the shared amenity that was just added to the rent amount stated in the lease.
  - update_rent_after_delete:
    After deleting an amenity from a lease, this trigger substracts the monthly rate of the shared amenity that was just removed from the rent amount stated in the lease.


## Testing the flow 

  This is a suggested flow to test the program. I think it shows well all the updates to the system, however, you don't have to follow it. For some steps, I will give you more than one option to proceed in case you want to save some time due to the large number of projects to grade. 
  
  Also, I added the disclaimer for the Tenant interface, but I will reiterate it here. At the beginning of the Tenant interface, you are given the list of current tenants and their IDs so it is easier for you to test. However, in a real program that section would be omitted since I don't want tenants to be able to see other tenant IDs. The list contains only tenants whose lease is ongoing and not in the future or past, however, you can use the tenant ID you just used for creating a new lease as well. I would advise against that just because the system will prevent you from accessing check payment status or making rental payment functionalities since the added lease is in the future, so you can't pay rent for it until the start of the lease. Nevertheless, you can use that ID in the tenant interface to see the personal information. 

  All the information about names, phone numbers, dates of birth, etc. is just an inspiration but you are more than welcome to come up with your input. The case for the names does not matter since the program will automatically capitalize the first letter after the space before adding it to the database. 

  Let's begin!

  - Navigate to the property manager interface

    - Choose "record visit data"
      - First name: Katerine
      - Last name: Ferdinand
      - Phone number: 392-948-8540  
      - Email: kferdinand27@usnews.com
      - Pick any property you want to visit
      - Pick the apartment that you like 

    - Choose "record lease data"
      - Pick the ID of the visitor you just created (should be 80)
      - DOB: 25-Feb-1990  
      - SSN: 152-89-2336
      - Pick the apartment that has been visited
      - If you accidentally enter the one that has not been visited, the program will take you back to the Property Manager menu after displaying the "Please schedule a visit" message
      - Start date: 01-Jan-2024 

    - Choose "record move-out"
      - You will notice that lease 25 with Tenant ID 78 ends in January 2024
      - So Tenant ID 78 would be a good one to pick for move-out, but you can pick anyone you like
      - Please memorize the ID or the name of the person who is moving out
      - Decide on the refund. The system will prevent you from refusing less than 0 and more than the security deposit amount.

    - Check that the move-out was processed by choosing "see all past leases"
      - Notice that the tenant who moved out is now on that list

    - Choose "add a roommate"
      - I'd advise using Tenant ID 46 because I made sure to add a bunch of amenities to their lease.
      - So when you go later to the Tenant interface, you'll be able to see the roommate you added, and all the amenities that we will add/remove later in the testing flow.
      - But if you choose a different ID, you can always verify that the roommate was added by going to the Tenant interface, entering the respective tenant ID, and seeing their roommate in the Lease information for the tenant. 
      - First name: Olly
      - Last name: Gilfether
      - Date of birth: 23-Mar-1998

    - Choose "add a pet"
      - Same idea as "add a roommate" part
      - Either use tenant ID 46 or memorize the ID so you can verify the successful update later in the tenant interface. 
      - Name: Duke
      - Species: Ferret

    - I will ask you to remove an amenity from tenant 46 later in the program so it is up to you if you wanna test it once or twice. 
    - At this time you can try to remove the shared amenity from any lease. If there are no amenities to remove, the system will navigate back to the Property Manager menu with an appropriate message.
      - Tenants with shared amenities in their leases: 72, 23, 55, 36, 61, 13, 16, 46, 57, 39, 9, 48, 71, 53, 17, 44, 64


  - Navigate to the tenant interface

    - Enter Tenant ID 46 
    - Take a look at the property amenities that are added to their lease

    - Choose "update personal data"
      - Update anything you want, for example, email to kpercy19@gmail.com

    - Choose "check payment status"
      - Notice how much they need to pay 

    - Before they pay they want to add an amenity and remove an amenity from their lease

    - Choose "see amenities available for your property"
      - let's say they want to remove the business center (ID 24) from their lease (- $20)
      - And they want to add a fitness center (ID 21) (+ $80)

    - They contact the property manager providing their ID and the ID of the amenity they want to remove/add


  - Navigate to the property manager interface

    - Choose "add a share amenity to a lease"
      - Tenant ID 46
      - Amenity ID 21 or any other one you like
      - Notice how the value for rent will update (+ $80 if you chose the fitness center)

    - Choose "remove a shared amenity from a lease"
      - Tenant ID 46
      - Notice that the amenity you just added is now on the list
      - Amenity ID 24 or any other one you like
      - Notice how the value for rent will update (- $20 if you choose business center)


  - Navigate to the financial manager interface
  
    - Choose "Get a full financial report by year, month, day"
      - Take a look at the grand total at the very bottom of the report


  - Navigate to the tenant interface

    - Notice how the email you updated changed in the Personal Information section

    - Tenant ID 46 once again

    - Choose "make rental payment"
      - Notice how the number changed depending on the amenities you added/removed
      - Proceed with making the payment
        - If you choose to use the same payment method, information about it will be saved for this payment
        - If you choose no, it will prompt you for new credit, debit, or crypto payment information. 

    - Choose "check payment status"
      - Notice the update

    - If you choose to "make rental payment" again, the system won't let you until after the deadline for this month


  - Navigate to the financial manager interface

    - Choose "Get a full financial report by year, month, day"
      - Take a look at the updated grand total at the very bottom of the report
    
    - If you choose "See all payments" your recent payment should be the last one

    - You can also check out other cool stuff like which amenity or property brings the most revenue, and the statistics about the use of different payment methods. 

  - Check out any other functionality you want or quit the program. 


## Sources of data 

  - Most of the data was generated using Mackaroo
  - Data for property addresses was generated using ChatGPT
  - Some of the data was generated by Mockaroo and then edited by ChatGPT (for example, if I wanted to change the dates of payment for all the insert statements at once)


## How to compile 

  - move ojdbc11.jar to dat225 folder
  - cd into dat225
  - javac -cp .:ojdbc11.jar Tenant.java
  - javac -cp .:ojdbc11.jar PropertyManager.java
  - javac -cp .:ojdbc11.jar PropertyManager.java
  - javac -cp .:ojdbc11.jar EU.java
  - java -cp .:ojdbc11.jar EU
  - jar cfmv dat225.jar Manifest.txt *.class
  - java -jar dat225.jar
