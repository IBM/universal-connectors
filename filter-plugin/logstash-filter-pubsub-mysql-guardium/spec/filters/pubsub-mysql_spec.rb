# encoding: utf-8
require_relative '../spec_helper'
require "logstash/filters/pubsub-mysql-guardium"

describe LogStash::Filters::PubsubMysql do
  describe "Set to GuardRecord" do
    let(:config) do <<-CONFIG
      filter {
        pubsub-mysql-guardium {
          target => "GuardRecord"
        }
      }
    CONFIG
    end

    sample("target" => "GuardRecord") do
      expect(subject).to include("target")
      expect(subject.get('target')).to eq('GuardRecord')
    end
  end
end
