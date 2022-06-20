<h1>Configuring a univer***REMOVED***l connector in Guardium Insights</h1>
<h2 id="before-you-begin">Before you begin</h2>
<p>Each data source type has a specific plug-in that filters and parses the events from the data source, and converts them to a standard Guardium format.
The first time that you add a connector of a specific data source type, you need to upload its plug-in. On the <a href="https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/available_plugins.md">available_plugins.md</a> page, refer to the "Downloads" column for the list of plug-ins available for GI. Click the "GI" hyperlink for the plug-in you wish to download.</p>
<h2 id="about-this-task">About this task</h2>
<p>The univer***REMOVED***l connector configuration has a few parts, all described in this task:</p>
<ul>
<li>Uploading a plug-in that configures the filter or parser to convert the data source events to a standard Guardium format</li>
<li>Configuring the connection between Guardium Insights and the data source</li>
<li>Downloading the certificate (when using Filebeat input type)</li>
</ul>
<h2 id="procedure">Procedure</h2>
<ol>
<li><p>Click <strong>Connections</strong> in the <strong>Settings</strong> menu.</p>
<li><p>Click <strong>Manage &gt; UC plug-ins</strong>.</p>
</li>
<li><p>If the plug-in is already installed, proceed to <a href="#Navigate_back_to_the_Connections_page_(**Settings_%3E_Connections**)">step 3</a>. If the plug-in for the specific data source type is not installed, click <strong>Add plug-in</strong>. Go to the directory with the plug-ins you already downloaded, select the .zip file for this data source type, and click <strong>Open</strong>.</p>
</li>
<li><h6 id="navigate-back-to-the-connections-page-settings--connections">Navigate back to the Connections page (<strong>Settings &gt; Connections</strong>).</h6>
</li>
<li><p>Click <strong>Add connection.</strong><br>
The Connect to new data source page opens, with a card for each data source type whose plug-in is already installed</p>
</li>
<li><p>Select the data source type. This opens a panel that aids you in initiating the connection.</p>
<p>a <strong>Select data source environment</strong>: Select the environment that hosts your data source.</p>
<p>b. <strong>Select connection method</strong>: Select <strong>Univer***REMOVED***l Connector.</strong></p>
<p>c. The remainder of the panel provides <strong>Additional information</strong> about the connection type that you are creating.</p>
<p>d. After reading the <strong>Additional information</strong>, click <strong>Configure</strong>.</p>
</li>
<li><p>Enter the details for this connection.</p>
<p>a. In the Name and description page, enter this information:</p>
<p>   &emsp; i. <strong>Name</strong>: A unique name for the connection. This name distinguishes this connection from all other Guardium Insights connections.</p>
<p>   &emsp; ii. <strong>Description</strong>: Enter a description for the connection.</p>
<p>b. Click <strong>Next</strong>.</p>
<p>c. In the Build pipeline page, use the <strong>Choose input plug-in</strong> menu to select your input plug-in. Then select a filter plug-in by using the <strong>Choose a filter plug-in</strong> menu.</p>
<p>d. Click <strong>Next</strong>.</p>
<p>e. Follow the instructions according to the input plug-in type you selected in <a href="#Navigate_back_to_the_Connections_page_(**Settings_%3E_Connections**)">step 3</a>.</p>
<h2 id="filebeat">Filebeat</h2>
<ol>
<li><p>In the Additional info page, enter a <strong>Data source tag</strong>: This tag identifies the plug-in that is associated with this connector.
Use this tag in the filebeat.yml configuration of the data sources whose type matches this plug-in step 2 of <a href="#Configuring_Filebeat_to_forward_audit_logs_to_Guardium">this section</a>. </p>
<p>The data source sends the tag with every event. For example, specify any-mongodb in this field, and configure Filebeat with the ***REMOVED***me tag for MongoDB activity logs coming from your MongoDB data source</p>
</li>
<li><p>Click <strong>Configure</strong>.</p>
</li>
<li><p>In the Configuration notes page, click Download certificate to <strong>download the certificate</strong> to your local system. Copy the certificate to the data source (step 4 in <a href="#Configuring_Filebeat_to_forward_audit_logs_to_Guardium">this section</a>). </p>
<p>All data sources of any one specific type use the ***REMOVED***me certificate.</p>
</li>
<li><p>Click <strong>Done</strong> .</p>
</li>
<li><p>Follow the instructions in <a href="#Configuring_Filebeat_to_forward_audit_logs_to_Guardium">this section</a> to configure the data source to communicate with Guardium Insights. Copy the hostname in the Configuration Notes to configure the host in the filebeat.yml file on your datasource.</p>
<h2 id="cloudwatch_logs">Cloudwatch_logs</h2>
</li>
</ol>
<ol>
<li>In the Additional info page, specify the details of the connection you want to create:</li>
</ol>
</li>
</ol>
<ul>
<li><p><strong>AWS Role ARN (optional)</strong> - this is used to generate temporary credentials, typically for cross-account access. See the <a href="https://docs.aws.amazon.com/STS/latest/APIReference/API_AssumeRole.html">AssumeRole API documentation</a> for more information.</p>
</li>
<li><p><strong>AWS access key id</strong> and <strong>AWS secret access key</strong> - your AWS user account access key and the secret access key (for more information, click <a href="https://docs.aws.amazon.com/powershell/latest/userguide/pstools-appendix-sign-up.html">here</a>).</p>
</li>
<li><p><strong>AWS account region</strong> - for example, "us-east-1".</p>
</li>
<li><p><strong>Event filter</strong> (optional) - specify the filters to apply when fetching resources. For example, for filtering an S3 events based on bucket name: '{$.eventSource="s3.amazonaws.com" &amp;&amp; $.requestParameters.bucketName= ""}'.</p>
</li>
<li><p><strong>Account id</strong> (optional) - your AWS account ID (For more information, click <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/console_account-alias.html#FindingYourAWSId">here</a>).</p>
</li>
<li><p><strong>Cloudwatch Log Group name</strong> - specify the log group that is created for your data instance.</p>
<p>For example "/aws/rds/instance/any_instance/any_log_group" .</p>
<h2 id="configuring-filebeat-to-forward-audit-logs-to-guardium">Configuring Filebeat to forward audit logs to Guardium</h2>
<h2 id="procedure-1">Procedure</h2>
</li>
</ul>
<ol>
<li><p>Open the file filebeat.yml, usually located in /etc/filebeat/filebeat.yml .</p>
</li>
<li><p>In the tags section,  enter the value of the Data source tag that you defined when you added a connection in <a href="#Filebeat">this section</a>.
For example,
  <code style="font-family: Menlo, Consolas, &quot;DejaVu Sans Mono&quot;, monospace;">tags: ["any-tag-name"]</code></p>
</li>
<li><p>In the Logstash Output section, enter the hostname URL from the Configuration Notes popup from when you <a href="#Procedure">configured the univer***REMOVED***l connector</a> .</p>
</li>
</ol>
<p>For example,</p>
<p><code style="font-family: Menlo, Consolas, &quot;DejaVu Sans Mono&quot;, monospace;">hosts: ["&lt;hostname-URL&gt;:5044"]</code></p>
<p>  <strong>NOTE</strong>: In GI, whenever using plug-ins that are based on Filebeat as a data shipper, the configured port should be 5044. Guardium Insights will map this to an internal port    </p>
<ol start="4">
<li><p>Copy the location of the downloaded certificate, in the ssl.certificate_authorities row from [here] ()
For example,
<code style="font-family: Menlo, Consolas, &quot;DejaVu Sans Mono&quot;, monospace;">ssl.certificate_authorities: ["/etc/pki/ca-trust/GuardiumInsightsCA.pem"]</code></p>
</li>
<li><p>Restart Filebeat to effect these changes</p>
<p>Linux: Enter the command:
<code style="font-family: Menlo, Consolas, &quot;DejaVu Sans Mono&quot;, monospace;">sudo service filebeat restart</code></p>
</li>
<li><p>Windows: Restart in the Services window</p>
</li>
</ol></body>
      </html>
