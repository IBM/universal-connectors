## [Export logs to S3 bucket](https://aws.amazon.com/blogs/database/automate-postgresql-log-exports-to-amazon-s3-using-extensions/)

### Note :
This implementation leverages PostgreSQL extensions such as `log_fdw`, `aws_s3`, and `pg_cron`, and closely follows the approach outlined in the official AWS blog post: [Automate PostgreSQL log exports to Amazon S3 using extensions](https://aws.amazon.com/blogs/database/automate-postgresql-log-exports-to-amazon-s3-using-extensions/).
Please note that this solution is based entirely on the methodology provided by AWS. IBM does not assume responsibility for any future updates, enhancements, or fixes that may be required due to changes in the AWS implementation or related extensions.

### Limitation :
Client HostName is not available, will be seen as N.A. in Full SQL Report. 

### Create an IAM Role and Policy and Attach the Role to Your RDS for PostgreSQL Instance

To allow Amazon RDS to export logs or data to Amazon S3, follow these steps:

---

## 1. Create an S3 Bucket

1. Sign in to the **AWS Management Console**.
2. Navigate to **S3** (Services → Storage → S3).
3. Click **[Create bucket]**.
4. Enter a unique **Bucket name** (e.g., `my-postgres-export-bucket`).
5. Select the same **Region** as your RDS instance.
6. Leave default settings or configure according to your requirements.
7. Click **[Create bucket]**.

---

## 2. Create a Custom IAM Policy for S3 Access

1. Go to **IAM** (Services → Security, Identity, & Compliance → IAM).
2. In the left navigation, click **Policies**.
3. Click **[Create policy]**.
4. Select the **JSON** tab and paste the following policy:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:AbortMultipartUpload",
                "s3:DeleteObject",
                "s3:ListMultipartUploadParts",
                "s3:PutObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::<S3_Bucket_Name>",
                "arn:aws:s3:::<S3_Bucket_Name>/*"
            ]
        }
    ]
}
```

>  Replace `<S3_Bucket_Name>` with your actual bucket name.

5. Click **[Next]**, give the policy a name (e.g., `RDSExportToS3Policy`), then click **[Create policy]**.

---

## 3. Create an IAM Role for RDS

1. In the IAM console, go to **Roles**.
2. Click **[Create role]**.
3. Select **AWS service** as the trusted entity type.
4. Choose **RDS** as the use case.
5. Click **[Next]**.
6. Skip attaching policies for now → Click **[Next]**.
7. Enter a **Role name** (e.g., `RDSExportToS3Role`), then click **[Create role]**.

---

## 4. Modify the Trust Relationship

1. Click on the newly created role (e.g., `RDSExportToS3Role`).
2. Go to the **Trust relationships** tab.
3. Click **[Edit trust policy]**.
4. Replace the contents with the following:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Principal": {
                "Service": "rds.amazonaws.com"
            },
            "Action": "sts:AssumeRole"
        }
    ]
}
```

5. Click **[Update policy]**.

---

## 5. Attach the Policy to the Role

1. While on the role details page, go to the **Permissions** tab.
2. Click **[Add permissions]** → **Attach policies**.
3. Find and select the policy you created (e.g., `RDSExportToS3Policy`).
4. Click **[Add permissions]**.

---

## 6. Associate the IAM Role with Your RDS Instance

### Using AWS Console

1. Go to **RDS** in the AWS Console.
2. Select your **PostgreSQL DB instance**.
3. Scroll to **Manage IAM roles**.
4. Under **Feature name**, choose `s3Export`.
5. Under **IAM role**, select the IAM role you created (`RDSExportToS3Role`).
6. Click **[Continue]**, then **[Modify DB instance]**.
7. Wait for the instance to return to the **Available** state.

### Using AWS CLI

```bash
aws rds add-role-to-db-instance \
    --db-instance-identifier <your-db-instance-name> \
    --feature-name s3Export \
    --role-arn arn:aws:iam::<your-account-id>:role/RDSExportToS3Role
```

Replace `<your-db-instance-name>` with your actual RDS instance identifier and `<your-account-id>` with your AWS account ID.

To verify that the role has been associated correctly, you can use:

```bash
aws rds describe-db-instances \
    --db-instance-identifier <your-db-instance-name> \
    --query "DBInstances[*].AssociatedRoles"
```

---
For more information, follow **[Step 4 in the official AWS documentation](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/postgresql-s3-export-access-bucket.html)**. 

### Import PostgreSQL logs into the table using extension log_fdw
To use the `log_fdw` functions, we must first create the extension on the database instance. Connect to the database using psql and run the following command.
```bash
    postgres=> CREATE EXTENSION log_fdw;
    CREATE EXTENSION
```
With the extension loaded, we can create a function that loads all the available PostgreSQL DB log files as a table within the database. The definition of the function is available on [GitHub](https://github.com/aws-samples/amazon-rds-and-amazon-aurora-logging-blog/blob/master/scripts/pg_log_fdw_management.sql).
```bash
-- Yaser Raja
-- AWS Professional Services
--
-- This function uses log_fdw to load all the available RDS / Aurora PostgreSQL DB log files as a table.
--
-- Usage:
--    1) Create this function
--    2) Run the following to load all the log files
--          SELECT public.load_postgres_log_files();
--    3) Start looking at the logs
--          SELECT * FROM logs.postgres_logs;
--
-- Here are the key features:
--   - By default, a table named "postgres_logs" is created in schema "logs".
--   - The schema name and table name can be changed via arguments.
--   - If the table already exists, it will be DROPPED
--   - If the schema 'logs' does not exist, it will be created.
--   - Each log file is loaded as a foreign table and then made child of table logs.postgres_logs
--   - By default, CSV file format is preferred, it can be changed via argument v_prefer_csv
--   - Daily, hourly and minute-based log file name formats are supported for CSV and non-CSV output files
--       - postgresql.log.YYYY-MM-DD-HHMI
--       - postgresql.log.YYYY-MM-DD-HH
--       - postgresql.log.YYYY-MM-DD
--   - Supports the scenario where log files list consist of both the file name formats
--   - When CSV format is used, a check-constraint is added to the child table created for each log file
--
CREATE OR REPLACE FUNCTION public.load_postgres_log_files(v_schema_name TEXT DEFAULT 'logs', v_table_name TEXT DEFAULT 'postgres_logs', v_prefer_csv BOOLEAN DEFAULT TRUE)
RETURNS TEXT
AS
$BODY$
DECLARE
    v_csv_supported INT := 0;
    v_hour_pattern_used INT := 0;
    v_filename TEXT;
    v_dt timestamptz;
    v_dt_max timestamptz;
    v_partition_name TEXT;
    v_ext_exists INT := 0;
    v_server_exists INT := 0;
    v_table_exists INT := 0;
    v_server_name TEXT := 'log_server';
    v_filelist_sql TEXT;
    v_enable_csv BOOLEAN := TRUE;
BEGIN
    EXECUTE FORMAT('SELECT count(1) FROM pg_catalog.pg_extension WHERE extname=%L', 'log_fdw') INTO v_ext_exists;
    IF v_ext_exists = 0 THEN
        CREATE EXTENSION log_fdw;
    END IF;

    EXECUTE 'SELECT count(1) FROM pg_catalog.pg_foreign_server WHERE srvname=$1' INTO v_server_exists USING v_server_name;
    IF v_server_exists = 0 THEN
        EXECUTE FORMAT('CREATE SERVER %s FOREIGN DATA WRAPPER log_fdw', v_server_name);
    END IF;

    EXECUTE FORMAT('CREATE SCHEMA IF NOT EXISTS %I', v_schema_name);

    -- Set the search path to make sure the tables are created in dblogs schema
    EXECUTE FORMAT('SELECT set_config(%L, %L, TRUE)', 'search_path', v_schema_name);

    -- The db log files are in UTC timezone so that date extracted from filename will also be UTC.
    --    Setting timezone to get correct table constraints.
    EXECUTE FORMAT('SELECT set_config(%L, %L, TRUE)', 'timezone', 'UTC');

    -- Check the parent table exists
    EXECUTE 'SELECT count(1) FROM information_schema.tables WHERE table_schema=$1 AND table_name=$2' INTO v_table_exists USING v_schema_name, v_table_name;
    IF v_table_exists = 1 THEN
        RAISE NOTICE 'Table % already exists. It will be dropped.', v_table_name;
        EXECUTE FORMAT('SELECT set_config(%L, %L, TRUE)', 'client_min_messages', 'WARNING');
        EXECUTE FORMAT('DROP TABLE %I CASCADE', v_table_name);
        EXECUTE FORMAT('SELECT set_config(%L, %L, TRUE)', 'client_min_messages', 'NOTICE');
        v_table_exists = 0;
    END IF;

    -- Check the pg log format
    SELECT 1 INTO v_csv_supported FROM pg_catalog.pg_settings WHERE name='log_destination' AND setting LIKE '%csvlog%';
    IF v_csv_supported = 1 AND v_prefer_csv = TRUE THEN
        RAISE NOTICE 'CSV log format will be used.';
        v_filelist_sql = FORMAT('SELECT file_name FROM public.list_postgres_log_files() WHERE file_name LIKE %L ORDER BY 1 DESC', '%.csv');
    ELSE
        RAISE NOTICE 'Default log format will be used.';
        v_filelist_sql = FORMAT('SELECT file_name FROM public.list_postgres_log_files() WHERE file_name NOT LIKE %L ORDER BY 1 DESC', '%.csv');
        v_enable_csv = FALSE;
    END IF;

    FOR v_filename IN EXECUTE (v_filelist_sql)
    LOOP
        RAISE NOTICE 'Processing log file - %', v_filename;

        IF v_enable_csv = TRUE THEN
            -- Dynamically checking the file name pattern so that both allowed file names patters are parsed
            IF v_filename like 'postgresql.log.____-__-__-____.csv' THEN
                v_dt=substring(v_filename from 'postgresql.log.#"%#"-____.csv' for '#')::timestamp + INTERVAL '1 HOUR' * (substring(v_filename from 'postgresql.log.____-__-__-#"%#"__.csv' for '#')::int);
                v_dt_max = v_dt + INTERVAL '1 HOUR';
                v_dt=substring(v_filename from 'postgresql.log.#"%#"-____.csv' for '#')::timestamp + INTERVAL '1 HOUR' * (substring(v_filename from 'postgresql.log.____-__-__-#"%#"__.csv' for '#')::int) + INTERVAL '1 MINUTE' * (substring(v_filename from 'postgresql.log.____-__-__-__#"%#".csv' for '#')::int);
            ELSIF v_filename like 'postgresql.log.____-__-__-__.csv' THEN
                v_dt=substring(v_filename from 'postgresql.log.#"%#"-__.csv' for '#')::timestamp + INTERVAL '1 HOUR' * (substring(v_filename from 'postgresql.log.____-__-__-#"%#".csv' for '#')::int);
                v_dt_max = v_dt + INTERVAL '1 HOUR';
            ELSIF v_filename like 'postgresql.log.____-__-__.csv' THEN
                v_dt=substring(v_filename from 'postgresql.log.#"%#".csv' for '#')::timestamp;
                v_dt_max = v_dt + INTERVAL '1 DAY';
            ELSE
                RAISE NOTICE '        Skipping file';
                CONTINUE;
            END IF;
        ELSE
            IF v_filename like 'postgresql.log.____-__-__-____' THEN
                v_dt=substring(v_filename from 'postgresql.log.#"%#"-____' for '#')::timestamp + INTERVAL '1 HOUR' * (substring(v_filename from 'postgresql.log.____-__-__-#"%#"__' for '#')::int) + INTERVAL '1 MINUTE' * (substring(v_filename from 'postgresql.log.____-__-__-__#"%#"' for '#')::int);
            ELSIF v_filename like 'postgresql.log.____-__-__-__' THEN
                v_dt=substring(v_filename from 'postgresql.log.#"%#"-__' for '#')::timestamp + INTERVAL '1 HOUR' * (substring(v_filename from 'postgresql.log.____-__-__-#"%#"' for '#')::int);
            ELSIF v_filename like 'postgresql.log.____-__-__' THEN
                v_dt=substring(v_filename from 'postgresql.log.#"%#"' for '#')::timestamp;
            ELSE
                RAISE NOTICE '        Skipping file';
                CONTINUE;
            END IF;
        END IF;
        v_partition_name=CONCAT(v_table_name, '_', to_char(v_dt, 'YYYYMMDD_HH24MI'));
        EXECUTE FORMAT('SELECT public.create_foreign_table_for_log_file(%L, %L, %L)', v_partition_name, v_server_name, v_filename);

        IF v_table_exists = 0 THEN
            EXECUTE FORMAT('CREATE TABLE %I (LIKE %I INCLUDING ALL)', v_table_name, v_partition_name);
            v_table_exists = 1;
        END IF;

        EXECUTE FORMAT('ALTER TABLE %I INHERIT %I', v_partition_name, v_table_name);

        IF v_enable_csv = TRUE THEN
            EXECUTE FORMAT('ALTER TABLE %I ADD CONSTRAINT check_date_range CHECK (log_time>=%L and log_time < %L)', v_partition_name, v_dt, v_dt_max);
        END IF;

    END LOOP;

    RETURN FORMAT('Postgres logs loaded to table %I.%I', v_schema_name, v_table_name);
END;
$BODY$
LANGUAGE plpgsql;
```
With the function created, we can run the function to load the PostgreSQL logs into the database. Each time we run the following command, the logs.postgres_logs table is updated with the most recent engine logs.
```bash
postgres=> SELECT public.load_postgres_log_files();
```
### Export PostgreSQL logs from table into Amazon S3 using aws_s3
Now that we have a function to query for new log statements, we use aws_s3 to export the retrieved logs to Amazon S3. From the prerequisites, we should already have an S3 bucket created and we should have attached an IAM role to the DB instance that allows for writing to your S3 bucket.
Create the aws_s3 extension with the following code:
```bash
postgres=> CREATE EXTENSION aws_s3 CASCADE;
CREATE EXTENSION
```
### Automate the log exports using extension pg_cron
Now that we have the steps to perform log uploads to Amazon S3 using the log_fdw and aws_s3 extensions, we can automate these steps using pg_cron. With pg_cron, we can write database queries to be run on a schedule of our choosing.

As part of the prerequisites, you should have pg_cron added to the shared_preload_libraries parameter in your database instance's parameter group. After pg_cron is loaded into shared_preload_libraries, you can simply run the following command to create the extension:

```bash
postgres=> CREATE EXTENSION pg_cron;
CREATE EXTENSION
```
With pg_cron created, we can use the extension to perform the PostgreSQL log uploads on a cron defined schedule. To do this, we need to schedule a cron job, passing in a name, schedule, and the log export query we want to run. For example, to schedule log uploads every hour with the same query described earlier, we can run the following command:

Create a table logs.postgres_logs_export_tracker to track last exported log timestamp with `last_exported_log_time`.
```bash
    CREATE TABLE IF NOT EXISTS logs.postgres_logs_export_tracker (
        id SERIAL PRIMARY KEY,
        last_exported_log_time TIMESTAMPTZ NOT NULL DEFAULT '1970-01-01 00:00:00+00'
    );
    
    INSERT INTO logs.postgres_logs_export_tracker (last_exported_log_time) VALUES (NOW());
```

Create function `export_postgres_logs_to_s3` to export log to S3 bucket. Please replace parameter `delay_in_minutes`, `S3_bucket_name` and `region` with actual value.
```bash
    CREATE OR REPLACE FUNCTION public.export_postgres_logs_to_s3()
    RETURNS void LANGUAGE plpgsql SECURITY DEFINER
    AS $$
    DECLARE
        last_exported TIMESTAMPTZ;
        new_last_exported TIMESTAMPTZ;
        cutoff_time TIMESTAMPTZ;
        latest_tracker_id INT;
        export_query TEXT;
        export_filename TEXT;
        delay_interval INTERVAL := INTERVAL '<delay_in_minutes>';  -- Adjust delay window for example 5 minutes
    BEGIN
        -- 1. Read last exported timestamp from tracker
        SELECT id, last_exported_log_time
        INTO latest_tracker_id, last_exported
        FROM logs.postgres_logs_export_tracker
        ORDER BY id DESC
        LIMIT 1;
    
        IF last_exported IS NULL THEN
            last_exported := '1970-01-01 00:00:00+00';
        END IF;
    
        -- 2. Compute cutoff time
        cutoff_time := NOW() - delay_interval;
    
        IF cutoff_time <= last_exported THEN
            RAISE NOTICE 'Cutoff time <= last exported time (%), skipping.', last_exported;
            RETURN;
        END IF;
    
        -- 3. Load logs (your implementation)
        PERFORM public.load_postgres_log_files();
    
        -- 4. Build export query
        export_query := format($f$
            SELECT * FROM logs.postgres_logs
            WHERE message ~* '(AUDIT:)' OR sql_state_code IS DISTINCT FROM '00000'
              AND log_time > %L
              AND log_time <= %L
            ORDER BY log_time, session_id, session_line_num
        $f$, last_exported::TEXT, cutoff_time::TEXT);
    
        export_filename := to_char(NOW(), '"postgres-log-"YYYYMMDD_HH24MI".csv"');
    
        -- 5. Export to S3
        PERFORM aws_s3.query_export_to_s3(
            export_query,
            '<S3_bucket_name>',
            export_filename,
            '<region>',
            options := 'format csv, header true'
        );
    
        -- 6. Update tracker to cutoff time
        UPDATE logs.postgres_logs_export_tracker
        SET last_exported_log_time = cutoff_time
        WHERE id = latest_tracker_id;
    
        RAISE NOTICE 'Logs exported. Tracker updated to %', cutoff_time;
    END;
    $$;
```
Here we have used cron job which will run on every minute, you can customise it by updating the cron job schedule expression i.e. `* * * * *`
```bash
    SELECT cron.schedule(
        'postgres-s3-log-uploads-every-minute',
        '* * * * *',
        'SELECT public.export_postgres_logs_to_s3();'
    );
```
If you decide at any time that you want to cancel these automated log uploads, you can unschedule the associated cron job by passing in the job name specified previously. In the following example, the job name is `postgres-s3-log-uploads-every-minute`:
```bash
postgres=> SELECT cron.unschedule('postgres-s3-log-uploads-every-minute'); 
unschedule
------------
 t
(1 row)
```
### Creating the SQS queue
The SQS queue created in these steps will receive messages from the Event Notification (configured in the next section).
These messages, generated by monitoring the S3 bucket, will contain details of the recently added S3 log files.


#### Procedure
1. Go to https://console.aws.amazon.com/
2. Click **Services**
3. Search for SQS and click on **Simple Queue Services**
4. Click **Create Queue**.
5. Select the type as **Standard**.
6. Enter the name for the queue
7. Keep the rest of the default settings

### Creating a policy for the relevant IAM User
Perform the following steps for the IAM user who is accessing the SQS logs in Guardium:

#### Procedure
1. Go to https://console.aws.amazon.com/
2. Go to **IAM service** > **Policies** > **Create Policy**.
3. Select **service as SQS**.
4. Check the following checkboxes:
    * **ListQueues**
    * **DeleteMessage**
    * **DeleteMessageBatch**
    * **GetQueueAttributes**
    *  **GetQueueUrl**
    * **ReceiveMessage**
    * **ChangeMessageVisibility**
    * **ChangeMessageVisibilityBatch**
5. In the resources, specify the ARN of the queue created in the above step.
6. Click **Review policy** and specify the policy name.
7. Click **Create policy**.
8. Assign the policy to the user
    1. Log in to the IAM console as an IAM user (https://console.aws.amazon.com/iam/).
    2. Go to **Users** on the console and select the relevant IAM user to whom you want to give permissions.
       Click the **username**.
    3. In the **Permissions tab**, click **Add permissions**.
    4. Click **Attach existing policies directly**.
    5. Search for the policy created and check the checkbox next to it.
    6. Click **Next: Review**
    7. Click **Add permissions**

### Creating the Event Notification
The Event Notification will get triggered when a new Object is added to S3 bucket and will send the events to the SQS queue.
Follow the steps below to configure the Event Notification

#### Creating Access Policy to allow Notifications
Update the Access Policy of the SQS queue to allow the Notification Service to send messages to the Queue

__*Procedure*__
1. Go to https://console.aws.amazon.com/
2. Go to **SQS** -> **Queues**
3. Click on the Queue that was created in the above step
4. Go to **Access Policy**
5. Click on **Edit**
6. Add the below details to the existing policy

```
{
      "Sid": "example-statement-ID",
      "Effect": "Allow",
      "Principal": {
        "Service": "s3.amazonaws.com"
      },
      "Action": "SQS:SendMessage",
      "Resource": "<Queue_ARN which is being edited>",
      "Condition": {
        "StringEquals": {
          "aws:SourceAccount": "<AccountID>"
        },
        "ArnLike": {
          "aws:SourceArn": "<ARN of the S3 bucket>"
        }
      }
}
```


7. Click on **Save**


#### Create the Event Notification
__*Procedure*__
1. Go to https://console.aws.amazon.com/
2. Go to **Services**. Search for **S3**.
3. Click on the S3 bucket that is associated with the CloudTrail.
4. Click **Properties**
5. Navigate to **Event Notifications**
6. Click on **Create event notification**.
7. Enter **Event name**
8. Enter the **Prefix** though this is optional, this can be set to capture the specific traffic.
9. In **Event Types** Select **All object create events**.
10. In **Destination** Select **SQS queue**.
11. In **Specify SQS Queue** either **Choose from your SQS queues** option select the Queue name from drop down list or **Enter SQS queue ARN** enter the Queue ARN manually.
12. Click on **Save Changes**