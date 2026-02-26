## Configuring Key Pair Authentication.

Some specific steps are required to enable the Key Pair Authentication in the Snowflake database
and Guardium. The detailed procedure is described in the official Snowflake documentation,
1. To generate a key pair and update the Snowflake user, https://docs.snowflake.com/en/user-guide/key-pair-auth
2. To configure JDBC to use the key pair, https://docs.snowflake.com/en/developer-guide/jdbc/jdbc

The following steps are required to enable Key Pair authentication.

### Procedure

1. Generate a private key using the command,
    ```shell
    openssl genrsa 2048 | openssl pkcs8 -topk8 -inform PEM -out <file_name> -nocrypt
    ```
   The key is created in PEM format.
2. From the created private key, use the following command to generate a public key.
    ```shell
    openssl rsa -in rsa_key.p8 -pubout -out <file_name>
    ```
   The public key is created in the PEM format.
3. Now you need to assign the generated public key to the Snowflake user who is connecting to the database.
    1. Copy the content of the key from the public key file (step 2).
    2. Run the following SQL statement in the database.
   ```shell
   ALTER USER <username> SET RSA_PUBLIC_KEY='MIIBIjANBgkqh...';
   ```
4. In Guardium, Go to **Configure universal connector** > click on **Upload File**. Select the
   generated private key file (step 1) and upload.

### Limitations

1. You cannot encrypt the generated private key. The key must stay in the same format in which it was created.
