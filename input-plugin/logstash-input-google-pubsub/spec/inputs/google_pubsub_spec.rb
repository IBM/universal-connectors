# encoding: utf-8
# Copyright 2016 Google Inc.                                                    
#                                                                               
# Licensed under the Apache License, Version 2.0 (the "License");               
# you may not use this file except in compliance with the License.              
# You may obtain a copy of the License at                                       
#                                                                               
#      http://www.apache.org/licenses/LICENSE-2.0                               
#                                                                               
# Unless required by applicable law or agreed to in writing, software           
# distributed under the License is distributed on an "AS IS" BASIS,             
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      
# See the License for the specific language governing permissions and           
# limitations under the License.                                                

require "logstash/devutils/rspec/spec_helper"
require "logstash/inputs/google_pubsub"

describe LogStash::Inputs::GooglePubSub do

  let(:bad1) { { 'topic' => 'foo', 'subscription' => 'bar' } }
  let(:bad2) { { 'project_id' => 'foo', 'subscription' => 'bar' } }
  let(:bad3) { { 'project_id' => 'foo', 'topic' => 'bar' } }
  let(:config) { { 'project_id' => 'myproj', 'subscription' => 'foo', 'topic' => 'bar' } }

  it "ensures required config options are present" do
    expect {
      plugin = LogStash::Inputs::GooglePubSub.new(bad1)
    }.to raise_error(LogStash::ConfigurationError)
    expect {
      plugin = LogStash::Inputs::GooglePubSub.new(bad2)
    }.to raise_error(LogStash::ConfigurationError)
    expect {
      plugin = LogStash::Inputs::GooglePubSub.new(bad3)
    }.to raise_error(LogStash::ConfigurationError)
  end
end
