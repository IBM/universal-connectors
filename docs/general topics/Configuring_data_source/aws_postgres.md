# Configuring the AWS Postgres service


1. Go to https://console.aws.amazon.com/.
2. Click **Services**.
3. In the Database section, click **RDS**.
4. Select the region.
5. Click **Create database**.
6. Choose a database creation method.
7. In the Engine options, select **PostgreSQL**, and then select the appropropriate version.
8. Select an appropriate template (**Production**, **Dev/Test**, or **Free Tier**).
9. In the **Settings** section, type the database instance name and create the master account with the username and password to log in to the database.
10. Select the database instance size according to your requirements.
11. Select appropriate storage options (for example, you may want to enable auto-scaling).
12. Select the availability and durability options.
13. Select the connectivity settings that are appropriate for your environment. To make the database accessible, set the **Public access** option to **Publicly Accessible within Additional Configuration**.
14. Select the type of Authentication for the database (choose from **Password Authentication**, **Password and IAM database authentication**, and **Password and Kerberos authentication**).
15. Expand the Additional Configuration options:

	a. Configure the **Database options**.

	b. Select options for **Backup**.

	c. If desired, enable encryption on the database instances.

	d. In **Log exports**, select the Postgresql log type to publish to Amazon CloudWatch.

	e. Select the options for **Deletion protection**.
16. Click **Create Database**.
17. To view the database, click **Databases** under Amazon RDS in the left panel.
18. To authorize inbound traffic, edit the security group:

	a. In the database summary page, select the Connectivity and Security tab. Under Security, click VPC security group.

	b. Click the group name that you selected while creating a database (each database has one active group).

	c. In the **Inbound rule** section, choose to edit the inbound rules.

	d. Set this rule:

	• Type: PostgreSQL

	• Protocol: TCP

	• Port Range: 5432
	(depending on your requirements, the source can be set to a specific IP address or it can be opened to all hosts)

	e. Click **Add Rule** and then click **Save changes**.

	The database may need to be restarted.
