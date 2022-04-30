# Copyright 2020-2022 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache-2.0

require 'time'
# reformats timestamp
module TimestampFormatter
  def parse(timestamp)
    ts_epoch = Time.parse(timestamp).to_i
    ts_epoch * (10**3)
  end
  module_function :parse
end
