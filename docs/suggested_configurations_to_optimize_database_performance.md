# Suggested configurations to optimize database performance


These configurations are not set automatically, and they are not supported via GIM. They optimize the database performance, though you might have other considerations in your database configuration.

## Network

  Minimum 2 NIC with 1 Gbit or10Gbit per second card.
  
Filebeat General section parameters (configured in filebeat.yml file):

        filebeat.spool_size: 20480
        queue.mem:
            events: 20480
            flush.min_events: 64
            flush.timeout: 50 ms

Logstash Output section parameters (configured in filebeat.yml file):
    output.logstash:

    # The Logstash hostshosts:
    ["10.XX.XXX.XX:5044", "10.XX.XXX.XXX:5044"]

    loadbalance: true
    compression_level: 5
    worker: 5
    ttl: 50
    msbulk_max_size: 409
