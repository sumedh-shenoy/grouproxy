# grouproxy
A text messaging proxy that uses Twilio, AWS Lambda and DynamoDB to create a low barrier ephemeral group-based text messaging service. It provides a messaging service in which key user information (ie: phone number) is not displayed in any front-end interactions (nor is not easily accessible by admins), and messages sent by users are not permanently stored. 

Currently, grouproxy supports the following capabilities:
1. Signing Up: Users may sign up for an instantiation of grouproxy, with each phone number corresponding to a single user, and vice versa.
    - Each user must provide a unique username, which is then used for all front end displays/interactions
2. Creating Events: Users may create new event groups, which contain a name, event description, and event time, as well as time at which the event will automatically be deleted.
    - Event creation may only be open to admins, based on values set in the configuration file.
    - Events may also be deleted by an admin or the creator of the event.
3. Broadcast Creation of Event Groups: broadcasts the creation of events to all registered users
    - This feature can be disabled on an event by event basis; it is a parameter that needs to be specified during event creation.
    - This feature can be disabled in the configuration file.
4. Message Event Groups: users may message events they are in—and these messasges are then pushed to all users in the event.
    - This feature can be disabled on an event by event basis; it is a parameter that needs to be specified during event creation.
5. Invite/Join Event Groups: users may join event groups, as well as invite others to event groups
    - Again, this feature may be enabled or disabled on an event by event basis.
6. Automatically Generated Welcome Messages and Closing Remarks: these are sent to individual users when they join a group event, and to all users when an event group is deleted.

# Setup
This setup assumes that you have an AWS Root User account, as well as a Twilio account. To sign up for an AWS account or a Twilio account, see [here](https://portal.aws.amazon.com/billing/signup#/start) and [here](https://www.twilio.com/try-twilio), respectively.

NOTE: all of the DynamoDB tables and Lambda functions should be in the same region. In my code, I used US-West-1 (California), but if you want to use a different region, make sure to modify the code by changing all instances of US-West-1 to whatever region you are using.

## Purchasing Twilio Phone numbers
Since our SMS messaging proxy service is, well, SMS based, we'll need a few phone numbers from Twilio to get started. Purchase however many phone numbers (making sure they all have SMS capabilities!) as you see fit—but remember that United States phone numbers are rate limited to 1 message per second, so you may want to buy a larger amount of phone numbers to scale with a larger number of users. Additionally, make sure that you are buying phone numbers in the same region as you are operating (search for Twilio numbers with the local area code), as most U.S. Twilio phone numbers only support local capabilities.

In order to ensure that we're able to load balance users across different phone numbers, we will assign each user to a phone number which they will solely interact with after completing the signup process with whatever phone number is designated for handling signups.

## Setting Up DynamoDB Tables
Our DynamoDB tables will be where all of our data will be stored. The text messaging proxy uses a total of 5 tables, which are:
1. **UserList:** stores a list of users, indexed by username (which each user will provide upon signing up, and is unique), and stores phone number the user is assocaited with, whatever custom fields you have users enter, what type of member a user is (admin or member), and what phone number (out of the ones purchased) the user will interact with.
    - To create **UserList**, create a new table, set the table name to "UserList", and the partition key to "username" (string), which looks like: <img width="1080" alt="Screen Shot 2021-01-10 at 1 23 49 PM" src="https://user-images.githubusercontent.com/30962399/104135955-f13ca900-5347-11eb-97c5-0084d2d55deb.png">
  
2. **EventList:** stores a list of event groups, indexed by event group name. It contains a list of parameters (ie: whether members can be invited to a group, whether members can join the group with just the name, and whether members other than the admins can send messages to the group), as well as the time the event is at (expressed in milliseconds, following Java Time conventions), and the number of group members in the event.
    -  To create **EventList**, create a new table, set the table name to "EventList", and partition key to "id". Then, add a global secondary index named "constantIndex-index", which has partition key "constantIndex" (a number), and sort key "eventTime" (a number), and set projected values to "include", and add "deleteTime". This should look like: <img width="1072" alt="Screen Shot 2021-01-11 at 12 33 55 AM" src="https://user-images.githubusercontent.com/30962399/104159739-b87eed00-53a4-11eb-98cd-9143de2eb715.png">
  
3. **EventMemberList:** stores a list of all members of events—using a composite key of (username, event name). It also stores the user's role in the event (admin or member), and has a global secondary index of event name, allowing for efficient queries to yield all members of a certain event.
    - To create **EventMemberList**, create a new table, set the table name to "EventMemberList", partition key to "username" (string), and sort key to "eventID" (string). Then, add a global secondary index named "eventID-index", which has partition key "eventID", and project all attributes. This should look like: <img width="1085" alt="Screen Shot 2021-01-10 at 1 27 41 PM" src="https://user-images.githubusercontent.com/30962399/104135953-f0a41280-5347-11eb-8c3c-b2b0ef446a8f.png">
  
4. **MessageQueue:** a list of all messages sent across all event groups, indexed with a composite key of event group to which the message was sent to, and what number message it was (ie: first, second, third message sent in the event group). 
    - To create **MessageQueue**, create a new table, set the table name to "MessageQueue", partition key to "groupID" (string) and sort key to "number" (a number). This should look like: <img width="1093" alt="Screen Shot 2021-01-10 at 1 28 42 PM" src="https://user-images.githubusercontent.com/30962399/104135952-ef72e580-5347-11eb-9f72-dbea12a2d124.png">
  
5. **SignedupUsers:** a list that stores all phone numbers that are registered for the service, along with their associated usernames.
Create these tables in the AWS region of your preference—but make sure you are consistent, and use this region across all the AWS services we use henceforth.
   - To create **SignedupUsers**, create a new table, set the table name to "SignedupUsers", and partition key to "phoneNumber". This should look like: <img width="1073" alt="Screen Shot 2021-01-10 at 1 29 17 PM" src="https://user-images.githubusercontent.com/30962399/104135951-ec77f500-5347-11eb-8872-f40345469ad7.png">

For all these tables, provision read and write capacity units as you see fit, or set the capacity to be on-demand (which I suggest, as it is much easier to deal with). 

### Role Creation
We want all three of our functions to have access to both Lambda resources, as well as DynamoDB, so we will create a new role for our lambda functions. To do so, first go to the AWS IAM console (making sure you are logged in), and go to the Roles panel, and create a new role, selecting Lambda as the use case. Attach the **AWSLambda_FullAccess** and **AmazonDynamoDBFullAcess** policies to our role, a skip the tags section—we won't need any for the scope of this setup. Then, add a name and description to this role, and make sure that we assign our functions to this IAM role when you create them. 

## Setting Up The Functions
The process behind our text messaging proxy can be broken up into three components:

1. Processing incoming messages
2. Sending outgoing messages to other group members
3. Sending out alerts to remind group members of upcoming events.

Thus, we have a lambda function to handle each one of these events—called textMessagingProxyHandler, PushMessage, and EventAlert, respectively. To set up each of these functions, we will be using the AWS Eclipse SDK for Java, and will be closely following the tutorial found [here](https://docs.aws.amazon.com/toolkit-for-eclipse/v1/user-guide/lambda-tutorial.html), the only exception being role creation.

Note: The handler code for textMessagingProxyHandler, PushMessage, and EventAlert are in the `textMessagingProxy.java`, `PushMessage.java`, and `eventAlertHandler.java` files, respectively.

### Setting Credentials
To configure your AWS credentials with the Eclipse AWS SDK, see [here](https://docs.aws.amazon.com/toolkit-for-eclipse/v1/user-guide/setup-credentials.html). To configure your Twilio credentials, either
1. Go to the `GroupMessage.java` file, and replace the corresponding account SID and auth token values with the ones from your account
2. OR go to the `GroupMessage.java class`, and replace the corresponding account SID and auth token values with environmental variables, which you should then set in the Lambda function page for PushMessage. A tutorial for doing so can be found [here](https://docs.aws.amazon.com/lambda/latest/dg/configuration-envvars.html)

### The Rest of the Function Setup
Follow the tutorial, and upload the code to your AWS Lambda functions! Make sure that you have the Twilio SDK for Java included in your project.

Here are some errors you might run into (well, at least *I* did, maybe you will be more fortunate) include:
- **PushMessageHandler not working, and giving an error message outlined [here](https://github.com/twilio/twilio-java/issues/603):**
  This issue, as stated in the issue report thread, may boil down to incompatibilities with certain features of the Twilio and AWS SDKs. I encountered this issue when I used Twilio SDK version 7.55.3, and switching to 7.55.2 fixed the issue.
- **Issues with Jar compilation failing when you try to upload your function, as outlined [here](https://forums.aws.amazon.com/thread.jspa?messageID=925111)**: In addition to following the fix outlined in the thread, also make sure that your project is using the Java SE 8 System Library.

## Configuring Our Functions
To configure our functions, we will be working with the following four files included in the textMessagingHandler project:
- **admins:** This will be a list of the phonenumbers of the users to be designated as admins, with each phone number on a different line; there should be at least one phone number
  - Make sure to add a +, as well an area code to the numbers—for example, the phone number 1-(123)-456-7891 would be entered as +11234567891.
- **phoneNumbers:** This will be a list of the purchased Twilio phone numbers, and again, there must be at least one phone number.
  - Again, make sure to add a +, as well as an area code.
- **config:** This file contains a list of preferences which are, as follows:
  - **memberEventCreation**: a boolean, to be set to "true" or "false", that indicates whether members (who are not admins) can make groups of their own.
  - **memberEventGlobalNotification** a boolean, to be set to "true" or "false", that indicates whether a member created event is able to broadcast (ie: message all users) a text containing the event name and description. Suggested value is false: global notifications ought to be avoided, as it is a fairly costly and slow process.
  - **alertTime** an integer, designating the amount of time before an event that users will get a notification being reminded about the event. For example, setting alertTime to 60 would mean that users would recieve an alert 60 to 120 minutes before each event. Suggested value is 1440, as all times are in Central Time–so keeping alertTime high will combat any precision errors created by time zone differences.
- **customFields:** a list that contains the names of custom fields, as well as the questions to be asked in the signup process. For example, the customFields file for a signup process that asks the users for their first name, and then job title would look like:
```
First Name
What is your first name?
Job Title
What is your job title?
```
For each custom field, the name of the field appears first, then the question to be asked—with each appearing on a new line. It is not necessary to have any custom fields. Once these files have been filled with their corresponding entries, run "Configure.java", and follow the instructions printed in the system console. Once you've finished, upload all three of our functions to AWS Lambda, making sure to use the role that we created earlier.

## Putting It All Together
Now that all of our functions are configured, it's time to start putting the pieces together for our final product! 

First, go to DynamoDB, and select our MessageQueue table. Then, under "Triggers", hit "Create New Trigger", and choose to create a trigger from an existing function. Select "Existing Lambda Function", choose the function pushMessageHandler (which we have already created), and select BatchSize to be 1—as we want to call pushMessageHandler every time a message is sent, in order to ensure that no message is left undelivered for long stretches of time. 

Then, follow the instructions [here](https://www.twilio.com/docs/sms/tutorials/how-to-receive-and-reply-python-amazon-lambda), ignoring the section on creating a new Lambda function (we already have done that), and make sure to select textMessagingProxyHandler as our function of choice to integrate with our API. Set the webhook generated by our newly created API to be the webhook to handle incoming SMS messages for all of the phone numbers you've purchased. 

Finally, to set up our alert system go to AWS Cloudwatch, and under the Rules tab, select "Create Rule". Select "Schedule", and choose a fixed rate of that you set in the function configurations. Then, select "Add target", and choose "Lambda Function"–making sure to choose the function eventAlert. Then, under "configure input", select "Constant (JSON text)", and enter the following:
```JSON
{"duration": "duration_time"}
```
where `duration_time` is the fixed rate you set in the function configurations. For example, you should have something like this:
<img width="1460" alt="Screen Shot 2021-01-09 at 7 44 12 PM" src="https://user-images.githubusercontent.com/30962399/104113896-3236ae00-52b3-11eb-978e-764063682121.png">

Now, our messaging proxy is set up, and ready for use!

# To Do List
Here are some features that are currently a WIP, but may be added eventually:
- Configurations for difference time zones
- More admin-related commands: being able to remove users from a group, ban users from a group, etc.
