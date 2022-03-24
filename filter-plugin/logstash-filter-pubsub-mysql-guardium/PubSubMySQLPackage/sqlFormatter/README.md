# SQL Formatter for DBeaver
This package contains OS compatible scripts for reformatting the text in the SQL editor on DBeaver in order to automatically trim-off line feeds.

## Instructions:
### Prerequisites 
Make sure you have Python 3 installed on your OS
1. Download `sqlFormatter.py` script
2. In case you're using Windows, use the following steps in order to convert the `.py` script to a Windows executable:
   1. Add Python to Windows Path. An easy way to add Python to the path is by downloading a recent version of Python, and then checking the box to **Add Python to PATH** at the beginning of the installation:
   2. Open the Windows Command Prompt
   3. Install the Pyinstaller Package in the Windows Command Prompt:
      ```pip install pyinstaller```
   4. Save your Python Script downloaded at your desired location
   5. Create the Executable using Pyinstaller: 
      
         type in the command prompt:
         
      ```
      cd /path/to/your/script
      pyinstaller --onefile sqlFormatter.py
      ```
   

6. Your executable should now get created at the location that you specified in dist folder
3. On DBeaver from the script file from which you're running your queries, go to _File > Properties > Editors > SQL Editor > Formatting_
4. Mark the first checkbox (i.e., _Datasource [...] settings_)
5. In the _Formatter_ dropdown menu, select _External formatter_
6. In _Settings > Command line_, insert `/path/to/your/script/sqlFormatter.[ext]`, where `[ext]` is `exe` for Windows OS,
   and `py` for the other OSs listed in (1).
   1. **Note:** if you're using Windows, the path to your executable should be `/path/to/your/script/dist/sqlFormatter.exe` where 
   `/path/to/your/script/` should be the path from which you ran `pyinstaller` in (2)
7. Click _Apply > Apply and Close_ 
8. To format an SQL text, select it and press `Ctrl+Shift+F` or right-click the selected text and click _Format > Format_ SQL on the context menu.