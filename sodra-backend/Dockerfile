FROM cassandra:2.2

LABEL maintainer "Phani Chaitanya Vempaty"

# install necessary components
RUN apt-get -qq update \
        && apt-get install -y python-yaml \
        && apt-get install -y vim \
        && apt-get install -y curl

# add sodra artifacts to the docker image
RUN mkdir -p /sodra_install
ADD sodra-backend-0.1-all.jar /sodra_install/sodra-backend-0.1-all.jar
ADD docker_setup_sodra.py /sodra_install/docker_setup_sodra.py
ADD sodra_conf /sodra_install/sodra_conf
ADD index_template_config /sodra_install/index_template_config
ADD solr.xml /sodra_install/solr.xml
ADD sodra_docker /sodra_install/sodra_docker
ADD examples /sodra_install/examples
ADD sodra_entrypoint.sh /sodra_entrypoint.sh

RUN chmod +x /sodra_install/docker_setup_sodra.py
RUN chmod +x /sodra_install/sodra_docker

ENV SODRA_CONFIG_DIR="/etc/cassandra/sodra_conf" \
        SODRA_DATA_DIR="/var/lib/cassandra/data/sodra"

ENTRYPOINT ["/sodra_entrypoint.sh"]

# solr port
EXPOSE 7983

# start sodra in background mode
CMD ["bash"]
