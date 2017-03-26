FROM cassandra:2.2

# install necessary components
RUN apt-get -qq update \
        && apt-get install -y python-yaml \
        && apt-get install -y vim

# add sodra artifacts to the docker image
RUN mkdir -p /sodra_install
ADD sodra-backend-0.1-all.jar /sodra_install/sodra-backend-0.1-all.jar
ADD docker_setup_sodra.py /sodra_install/docker_setup_sodra.py
ADD sodra_conf /sodra_install/sodra_conf
ADD index_template_config /sodra_install/index_template_config
ADD solr.xml /sodra_install/solr.xml
ADD sodra_docker /sodra_install/sodra_docker

RUN chmod u+x /sodra_install/docker_setup_sodra.py

# install python yaml module
#RUN apt-get install -y  \
#        apt-transport-https \
#        ca-certificates \
#        curl \
#        software-properties-common 

#RUN add-apt-repository main \
#        && add-apt-repository universe \
#        && add-apt-repository restricted \
#        && add-apt-repository multiverse \
#        && apt install python-yaml

# Run all the commands
# start cassandra
CMD bash
