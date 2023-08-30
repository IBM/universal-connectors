# MariaDB

## Installing MariaDB on Linux

### Procedure:
1. Install MariaDB on the server with the yum command
```
Yum install mariadb* -y
```
2. Start the MariaDB service on the server with the systemctl command.
```
systemctl start mariadb
```
3. Enable the MariaDB service to start at boot on the server.
```
systemctl enable mariadb
```
4. Generate Password​ by executing "mysql_secure_installation"​ command After providing the new password user will be asked for following,  
- Remove test Database and access to it? [y/n].​User need to type ‘y’​,
- Reload privilege tables now?[y/n]​.User need to type ‘y’​.
- Enter password for connecting to MariaDB server

  [Click Here](https://computingforgeeks.com/how-to-install-mariadb-on-kali-linux/) for learn more about MariaDB configuration
