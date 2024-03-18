- Student name: Tran Nguyen Ha Khanh
- Student ID: s3877707

- Instruction to run and install the app
    + Add MAPs_API_KEY: under the root folder, open the file `local.properties` and add this line:
        MAPS_API_KEY=AIzaSyBYSnvzSw7F69MPM1FjUffipuLFFdKrKtQ
    + Configure/Select an AVD profile from Device Manager and run the device (I use Pixel 4 API 30)
    + Run 'app'

- Available accounts:
    + Super User:
        email: hakhanhne@gmail.com
        password: 22092002
    + Normal User:
        email: hakhanh2292002@gmail.com
        password: 22092002

- Functionality:
    - User authentication and user profile:
        + Register
        + Login
        + Logout
        + Update profile
    - Register Account:
        + Register using email and password.
        + After registering successfully, an verification email will be sent to the registered email.
        + User is required to open the verification email to activate their account.
        + IMPORTANT: To register a Super User account, the email must be one of the qualified emails for Super User.
        The qualified emails for Super User are defined in the array `SUPER_USERS` in Register.java (java/rmit/aad/cleanup/views/account/Register.java)
        You can use the Super User account `hakhanhne@gmail.com` above to login as Super User.
        Or you can add your email to the array `SUPER_USERS` above, and register new account. Your account will be automatically marked as Super User.
    - Login:
        + Login using email and password.
        + Save login credentials for auto-login next time when selecting `Remember me` at Login.
        + In case you cannot login after having registered successfully, please make sure if you have CHECK THE VERIFICATION EMAIL.
    - Logout: when a user logout, the app will execute 2 tasks below
        + Logout from FirebaseAuth
        + Logout from app
    - Users authorization:
        + Including: Not registered user, registered user, super user.
        + Different access level for each user group.
        + Only super user can view all sites (including normal site and private site).
    - Home page:
        + At home page, user can click on the cards to quickly navigate.
    - Side Navigation Menu:
        + Side Navigation Menu have different menu items for users, depending on their roles.
    - Maps:
        + Maps views that show available clean up sites.
        + Search and filter includes:
            Search sites by the site name
            Filter by Target (Community/Organization)
            Filter by Location Type (Campus/Beach/Park/ v.v...)
        + The map will update and only show matching sites for the query.
        + Locate "My location":
            Tap the icon `My Location` (on the top right corner of the map)
        + Zoom in/out:
            Tap `+` to zoom in, `-` to zoom out
        + Find rout:
            By clicking on the markers on page Explore Clean Up Site, user can find routes from their current location to the site.
            (please pay attention to the current location of the AVD, there could be no route available depending on the current location)
        + User can click on the site's name on the cards below the map to view details of a site.
        + User can click on the `Join!` text on the cards below the map to quickly join a site.
    - Register a Clean Up Site:
        + User will be asked to login when registering a Clean Up Site.
        + Go to Register Clean Up -> Fill in the form -> Submit
        + Regarding to input location address, user only need to enter the address.
         Then, the system will automatically find and get the latitude and longitude.
        + After creating new site, user will be redirected to Dashboard to view.
    - Join a Clean Up Site
        + User will be asked to login when joining a Clean Up site
        + Go to `Join Clean Up`
                -> Select Site from the map
                -> Click `Join` on the popup card below the map
                -> Enrolment information will be auto-filled on the Confirmation card
                -> Check the checkbox at the bottom of the enrolment form
                -> Click `Join this Site!`
        + User can click the marker on the map to select a site. Then click Join,
         and scroll down to check information. And finally, Confirm Join.
        + After joining site, user will be redirected to Dashboard to view.
    - Dashboard:
        + Registered user can go to Dashboard to view the sites they own.
        + Registered user can go to Dashboard to view the the sites they join.
        + User can click on the site's name on the cards to view details of a site.
    - Others:
        + Input validation for forms
        + Date pickers
        + About Us/ Privacy Policy/ Copyright


- Technology: Firebase and Google Maps Platform are the core technology solution of this app
    - Database: the app uses Firebase, including
        + Cloud Firestore: to store data models (User, Clean Up Site, Enrolment)
        + Firebase Storage: to store and retrieve generated content (e.g., photos).
    - Authentication:
        + The app use FirebaseAuth to authenticate user using email and password.
    - Maps:
        + The app use Google Maps Platform and Google Services to implement map features because this is a powerful built-in resource for map usage.
        + The app is registered to use an Maps API Key and enable some services, such as  Maps SDK for Android, Distance Matrix API.


- Drawback:
    + App is slow when running on Android emulator. When clicking a component, please wait a second for the device to process, avoid multiple clicks.