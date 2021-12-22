# encoding: utf-8
require_relative '../spec_helper'
require "logstash/filters/pubsub-postgresql-guardium"

describe LogStash::Filters::PubsubPostgresqlGuardium do
  describe "Set to GuradRecord" do
    let(:config) do <<-CONFIG
      filter {
        pubsub-postgresql-guardium {
          target => "GuardRecord"
        }
      }
    CONFIG
    end

    ***REMOVED***mple("target" => "GuardRecord") do
      expect(subject).to include("target")
      expect(subject.get('target')).to eq('GuardRecord')
    end
  end
end
