# AWS Aurora-MySQL

1. Go to https://console.aws.amazon.com/.
2. Click Services.
3. In the Database section, click RDS.
4. Select the region in the top right corner.
5. In the central panel of the Amazon RDS Dashboard, click Create database.
6. Choose a database creation method.
7. In the Engine options, select Amazon Aurora, and then select the Amazon Aurora MySQL-Compatible Edition.
8. Select an Capacity type(Provisioned).
9. Select template (Production,Dev/Test)
10. In the Settings section, type the database instance name and create the master account with the username and password to log in to the database.
11. Select the database instance size according to your requirements.
12. Select appropriate storage options (for example, you may want to enable auto scaling).
13. Select the Availability and durability options.
14. Select the connectivity settings that are appropriate for your environment. To make the database accessible, set the Public access option to Publicly Accessible within Additional Configuration.
15. Select the type of Authentication for the database (choose from Password Authentication, Password and IAM database authentication, and Password and Kerberos authentication).
16. Expand the Additional Configuration options:
		a. Configure the database options.
		b. Select DB cluster parameter group.
		b. Select options for Backup.
		c. If desired, enable Encryption on the database instances.
		d. In Log exports, select the log types to publish to Amazon CloudWatch (Audit log).
		e. Select the options for Deletion protection.
17. Click Create Database.
18. To view the database, click Databases under Amazon RDS in the left panel.
19. To authorize inbound traffic, edit the security group:
		a. In the database summary page, select the Connectivity and Security tab. Under Security, click VPC security group.
		b. Click the group name that you selected while creating database (each database has one active group).
		c. In the Inbound rule section, choose to edit the inbound rules.
		d. Set this rule:
			• Type: MYSQL/Aurora
			• Protocol: TCP
			• Port Range: 3306
			(depending on your requirements, the Source can be set to a specific IP address or it can be opened to all hosts)
		e. Click Add Rule and then click Save changes.
		The database may need to be restarted.
