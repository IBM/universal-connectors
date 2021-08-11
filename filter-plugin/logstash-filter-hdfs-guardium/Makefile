#
# Copyright 2020-2021 IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#
logstash_dir=../../../logstash-?.?.?
java_home_dir=/opt/ibm/java-x86_64-80
package_file=guardium-hdfs-uc.zip
filter_name=logstash-filter-hdfs_guardium_filter
version=$(shell cat VERSION)

all: jar gem

jar:
	JAVA_HOME=${java_home_dir} JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1.2 ./gradlew assemble

gem: jar
	./gradlew gem

clean:
	./gradlew clean
	\rm -f ${package_file}

test:
	JAVA_HOME=${java_home_dir} JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1.2 ./gradlew test

package: gem
	  ${logstash_dir}/bin/logstash-plugin install ${filter_name}-${version}.gem
	  ${logstash_dir}/bin/logstash-plugin prepare-offline-pack --output ${package_file} --overwrite ${filter_name}

