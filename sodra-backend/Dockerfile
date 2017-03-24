FROM cassandra:3

# install necessary components
RUN apt-get -qq update \
        && apt-get install -y python-yaml

# add sodra artifacts to the docker image
ADD sodra-backend-0.1-all.jar /sodra-backend-0.1-all.jar
ADD setup /setup
ADD docker_setup_sodra.py /docker_setup_sodra.py

RUN chmod u+x /setup
RUN chmod u+x /setup_sodra.py

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
