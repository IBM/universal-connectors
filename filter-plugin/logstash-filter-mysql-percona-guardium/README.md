# MySql-Percona-Guardium Logstash filter plug-in
### Meet MySql-Percona
* Tested versions: 5.7.31-34
* Environment: On-premise, Iaas
* Supported inputs: Filebeat (push)
* Supported versions:
    * GDP: 11.3 and above
    * GI: 3.2 and above

This is a [Logstash](https://github.com/elastic/logstash) filter plug-in for the univer***REMOVED***l connector that is featured in IBM Security Guardium. 
It is an extension of MySql-Guardium Logstash filter plug-in. see [MySql-Guardium Logstash filter plug-in](https://github.ibm.com/Activity-Insights/univer***REMOVED***l-connectors/blob/master/filter-plugin/logstash-filter-mysql-guardium/README.md)
<div>
<section class="section" role="region" aria-labelledby="d27740e34" id="concept_unt_dlb_2nb__filebeat_cfg"><h2 class="sectiontitle bx--type-expressive-heading-04" id="d27740e34">Filebeat configuration</h2>

<ol class="bx--list--ordered--temporary">
<li class="bx--list__item">On the database, configure the Filebeat data shipper to forward the audit logs to the Guardium
univer***REMOVED***l connector. In the file <span class="ph filepath">filebeat.yml</span>, usually located in
<span class="ph filepath">/etc/filebeat/filebeat.yml</span>, modify the <code class="ph codeph">filebeat.inputs</code> section.<ol type="a" class="bx--list--ordered--temporary bx--list--nested">
<li class="bx--list__item">Change the <code class="ph codeph">enabled</code> field to <code class="ph codeph">true</code>, update the Logstash host,
and add the path of the audit logs. For
example:<div class="bx--snippet bx--snippet--multi bx--snippet--wraptext bx--snippet--expand"><div class="bx--snippet-container" tabindex="0" aria-label="Code snippet"><pre class="codeblock"><code class="language-plaintext hljs">#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj; filebeat.inputs:
    type: log
    #&zwnj; Change to true to enable this input configuration.
    enabled: true 
    paths:
    #&zwnj;- c:\programdata\elasticsearch\logs*
    C:\downloads\docker_volumes*
    output.logstash:
    #&zwnj; The Logstash hosts
    hosts: ["&lt;Guardium IP&gt;:5045"] #&zwnj; just to ip/host name of the gmachine
    # Paths that should be crawled and fetched. Glob based paths.
    paths: 
    - /var/lib/mysql/audit.log
    tags: ["mysqlpercona"] 
    #&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#&zwnj;#
</code></pre></div><button data-copy-btn="" class="bx--copy-btn" aria-label="Copy to clipboard" title="Copy to clipboard" type="button" tabindex="0"><span class="bx--assistive-text bx--copy-btn__feedback">Copied!</span><svg focu***REMOVED***ble="false" preserveAspectRatio="xMidYMid meet" style="will-change: transform;" xmlns="http://www.w3.org/2000/svg" class="bx--snippet__icon" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true"><path d="M14,5v9H5V5h9m0-1H5A1,1,0,0,0,4,5v9a1,1,0,0,0,1,1h9a1,1,0,0,0,1-1V5a1,1,0,0,0-1-1Z"></path><path d="M2,9H1V2A1,1,0,0,1,2,1H9V2H2Z"></path></svg>
      </button></div></li>

<li class="bx--list__item">Filebeat communicates with the Guardium univer***REMOVED***l connector via port 5045. Verify that port 5045
is open.</li>

</ol>
</li>

<li class="bx--list__item">Restart the filebeat service by
entering:<div class="bx--snippet bx--snippet--multi bx--snippet--wraptext bx--snippet--expand"><div class="bx--snippet-container" tabindex="0" aria-label="Code snippet"><pre class="codeblock"><code class="language-plaintext hljs">sudo service filebeat restart</code></pre></div><button data-copy-btn="" class="bx--copy-btn" aria-label="Copy to clipboard" title="Copy to clipboard" type="button" tabindex="0"><svg focu***REMOVED***ble="false" preserveAspectRatio="xMidYMid meet" style="will-change: transform;" xmlns="http://www.w3.org/2000/svg" class="bx--snippet__icon" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true"><path d="M14,5v9H5V5h9m0-1H5A1,1,0,0,0,4,5v9a1,1,0,0,0,1,1h9a1,1,0,0,0,1-1V5a1,1,0,0,0-1-1Z"></path><path d="M2,9H1V2A1,1,0,0,1,2,1H9V2H2Z"></path></svg>
      </button></div></li>

</ol>

</section>
<section class="section" role="region" aria-labelledby="d27740e91"><h2 class="sectiontitle bx--type-expressive-heading-04" id="d27740e91">Percona configuration</h2>

<ol class="bx--list--ordered--temporary">
<li class="bx--list__item">On the database, update the file:
<span class="ph filepath">/etc/percona-server.conf.d/mysqld.cnf</span><div class="bx--snippet bx--snippet--multi bx--snippet--wraptext bx--snippet--expand"><div class="bx--snippet-container" tabindex="0" aria-label="Code snippet"><pre class="codeblock"><code class="language-plaintext hljs">symbolic-links=0
    bind_address=0.0.0.0
    log-error=/var/log/mysqld.log
    pid-file=/var/run/mysqld/mysqld.pid
    audit_log_format=JSON
    audit_log_handler=FILE
    audit_log_file=/var/lib/mysql/audit.log</code></pre></div><button data-copy-btn="" class="bx--copy-btn" aria-label="Copy to clipboard" title="Copy to clipboard" type="button" tabindex="0"><svg focu***REMOVED***ble="false" preserveAspectRatio="xMidYMid meet" style="will-change: transform;" xmlns="http://www.w3.org/2000/svg" class="bx--snippet__icon" width="16" height="16" viewBox="0 0 16 16" aria-hidden="true"><path d="M14,5v9H5V5h9m0-1H5A1,1,0,0,0,4,5v9a1,1,0,0,0,1,1h9a1,1,0,0,0,1-1V5a1,1,0,0,0-1-1Z"></path><path d="M2,9H1V2A1,1,0,0,1,2,1H9V2H2Z"></path></svg>
      </button></div></li>

<li class="bx--list__item">Restart the mysql service.</li>

</ol>

</section>
</div>

## Configuring the MySQL filters in Guardium Insights
To configure this plug-in for Guardium Insights, follow [this guide.](https://github.com/IBM/univer***REMOVED***l-connectors/blob/main/docs/UC_Configuration_GI.md)
In the input configuration section, refer to the Filebeat section.

## Contribute
You can enhance this filter and open a pull request with suggested changes - or you can use the project to create a different filter plug-in for Guardium that supports other data sources.

## References
See [documentation for Logstash Java plug-ins](https://www.elastic.co/guide/en/logstash/current/contributing-java-plugin.html).

See [Guardium Univer***REMOVED***l connector commons](https://www.github.com/IBM/guardium-univer***REMOVED***lconnector-commons) library for more details regarding the standard Guardium Record object.

