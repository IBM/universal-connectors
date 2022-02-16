# Copyright 2020-2022 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

# encoding: utf-8

require_relative '../spec_helper'
require 'logstash/filters/pubsub-mysql-guardium'

describe LogStash::Filters::PubsubMysql do
  describe 'Basic setup' do
    let(:config) do <<-CONFIG
      filter {
        pubsub-mysql-guardium {
          cloudsqlproxy_enabled => false
        }
      }
    CONFIG
    end
    event_str = '{"data":{"construct":null,"originalSqlCommand":"UPDATE testy.shop SET price=0.00 WHERE article=1"},
                                  "appUserName":"cloudSQL_service","sessionId":"12479","time":{"minOffsetFromGMT":0,
                                  "minDst":0,"timestamp":1643398168000},"dbName":"charged-mind-281913:mysql-test",
                                  "sessionLocator":{"clientIp":"34.141.130.33","serverPort":"0","clientPort":0,
                                  "serverIp":"0.0.0.0","clientIpv6":null,"serverIpv6":null,"isIpv6":false},"exception"
                                  :null,"accessor":{"commProtocol":"","clientHostName":"","sourceProgram":"Google Cloud Platform",
                                  "clientMac":"","clientOs":"","language":"MYSQL","dbUser":"root","dataType":"TEXT",
                                  "serverDescription":"","serverHostName":"us-central:charged-mind-281913:mysql-test",
                                  "serviceName":"cloudsql_database","dbProtocol":"MYSQL","osUser":"",
                                   "dbProtocolVersion":"","serverOS":"","serverType":"MySQL"}}'
    event1 = LogStash::Event.new(event_str)
    plugin = described_class.new(:config)
    plugin.register
    plugin.filter(event_str)
    result = event_str.get('GuardRecord')
    expect(result).to eq(event_str)
  end

  describe 'Validate config var' do
    let(:config) do <<-CONFIG
      filter {
        pubsub-mysql-guardium {}
      }
    CONFIG
    end
    ***REMOVED***mple("cloudsqlproxy_enabled") do
      expect(subject).to include("cloudsqlproxy_enabled")
      expect(subject.get('cloudsqlproxy_enabled')).to eq(false)
    end
  end

  describe 'Cloud sql proxy dbeaver setup' do
    let(:config) do <<-CONFIG
      filter {
        pubsub-mysql-guardium {
          cloudsqlproxy_enabled => true
        }
      }
    CONFIG
    end
    event_str = '{"data":{"construct":null,"originalSqlCommand":"select * from testy.shop where price>4"},
                                  "appUserName":"cloudSQL_service","sessionId":"12479","time":{"minOffsetFromGMT":0,
                                  "minDst":0,"timestamp":1634318623},"dbName":"charged-mind-281913:mysql-test",
                                  "sessionLocator":{"clientIp":"147.236.146.233","serverPort":"0","clientPort":0,
                                  "serverIp":"0.0.0.0","clientIpv6":null,"serverIpv6":null,"isIpv6":false},"exception"
                                  :null,"accessor":{"commProtocol":"","clientHostName":"","sourceProgram":"DBeaver",
                                  "clientMac":"","clientOs":"","language":"MYSQL","dbUser":"root","dataType":"TEXT",
                                  "serverDescription":"","serverHostName":"us-central:charged-mind-281913:mysql-test",
                                  "serviceName":"cloudsql_database","dbProtocol":"MYSQL","osUser":"",
                                   "dbProtocolVersion":"","serverOS":"","serverType":"MySQL"}}'
    event1 = LogStash::Event.new(event_str)
    plugin = described_class.new(:config)
    plugin.register
    plugin.filter(event_str)
    result = event_str.get('GuardRecord')
    expect(result).to eq(event_str)
  end
end
