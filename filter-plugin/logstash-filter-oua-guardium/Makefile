logstash_dir=/home/marockar/oua_uc/logstash-7.5.2
java_home_dir=/opt/ibm/java-x86_64-80
version=$(shell cat VERSION)

all: jar gem

jar:
	JAVA_HOME=${java_home_dir} JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1.2 ./gradlew assemble

gem: jar
	./gradlew gem

clean:
	./gradlew clean

test:
	JAVA_HOME=${java_home_dir} JAVA_TOOL_OPTIONS=-Dhttps.protocols=TLSv1.2 ./gradlew test

package:
	  ${logstash_dir}/bin/logstash-plugin install logstash-filter-oua_filter-${version}.gem
	  ${logstash_dir}/bin/logstash-plugin prepare-offline-pack --output guardium-oua-uc.zip --overwrite logstash-filter-oua_filter

