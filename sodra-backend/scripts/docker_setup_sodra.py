#!/usr/bin/env python

import sys
import os
import argparse
import shutil
import stat
import yaml
import logging

# Docker setup
# # cassandra conf : /etc/cassandra/
# # cassandra lib : /usr/share/cassandra/
# # cassandra data dir : /var/lib/cassandra/data
# # sodra install dir : /sodra_install

def setup_sodra_lib(cassandra_lib_dir, sodra_install_dir):
    sodra_lib_dir = os.path.join(cassandra_lib_dir, 'sodra_lib')
    if os.path.exists(sodra_lib_dir):
        raise Exception('Remove "sodra_lib" directory from cassandra lib directory')
    logging.info('Creating sodra lib dir : ' + sodra_lib_dir)
    os.makedirs(sodra_lib_dir)
    
    # copy the sodra jar to the sodra lib directory
    sodra_install_contents = set(os.listdir(sodra_install_dir))
    sodra_jar = None
    for file in sodra_install_contents:
        if file.startswith('sodra') and file.endswith('.jar'):
            sodra_jar = file
    if sodra_jar is None:
        raise Exception('Could not find sodra jar file in the current directory')
    logging.info('Copying sodra jar from ' + sodra_jar + ' to ' + sodra_lib_dir)
    shutil.copy(sodra_jar, sodra_lib_dir)

def setup_sodra_conf(cassandra_conf_dir, sodra_install_dir):
    sodra_conf_install_dir = os.path.join(sodra_install_dir, 'sodra_conf')
    sodra_conf_dir = os.path.join(cassandra_conf_dir, 'sodra_conf')
    if os.path.exists(sodra_conf_dir):
        raise Exception('Remove "sodra_conf" directory from cassandra conf directory')
    logging.info('Copying sodra conf from ' + sodra_conf_install_dir + ' to ' + sodra_conf_dir)
    shutil.copytree(sodra_conf_install_dir, sodra_conf_dir)

def setup_sodra_data_dir(cassandra_data_dir, sodra_install_dir):
    sodra_data_dir = os.path.join(cassandra_data_dir, 'sodra')
    if os.path.exists(sodra_data_dir):
        raise Exception('Previous stale "sodra" directory exists inside : ' + cassandra_data_dir + ' . Delete that directory')
    logging.info('Creating sodra data dir : ' + sodra_data_dir)
    os.makedirs(sodra_data_dir)
            
    solr_home_dir = os.path.join(cassandra_data_dir, 'sodra', 'solr')
    logging.info('Creating solr home dir : ' + solr_home_dir)
    os.makedirs(solr_home_dir)
    
    src_config_dir = os.path.join(sodra_install_dir, 'index_template_config')
    dest_config_dir = os.path.join(sodra_data_dir, 'index_template_config')
    logging.info('Copying configs from ' + src_config_dir + ' to ' + dest_config_dir)
    shutil.copytree(src_config_dir, dest_config_dir)
    
    src_solr_xml_file = os.path.join(sodra_install_dir, 'solr.xml')
    dest_solr_xml_file = os.path.join(solr_home_dir, 'solr.xml')
    logging.info('Copying solr.xml from ' + src_solr_xml_file + ' to ' + dest_solr_xml_file)
    shutil.copyfile(src_solr_xml_file, dest_solr_xml_file)

def setup_sodra(options):
    cassandra_lib_dir = options.cassandra_lib
    cassandra_conf_dir = options.cassandra_conf
    cassandra_data_dir = options.cassandra_data
    sodra_install_dir = options.sodra_install_dir

    # setup sodra script
    logging.info('Copying sodra script from ' + os.path.join(sodra_install_dir, 'sodra_docker') + ' to /usr/sbin/sodra_docker')
    shutil.copyfile(os.path.join(sodra_install_dir, 'sodra_docker'), '/usr/sbin/sodra_docker')
    st = os.stat('/usr/sbin/sodra_docker')
    os.chmod('/usr/sbin/sodra_docker', st.st_mode | stat.S_IEXEC)
    
    # setup sodra lib 
    setup_sodra_lib(cassandra_lib_dir, sodra_install_dir)
    
    # setup sodra conf
    setup_sodra_conf(cassandra_conf_dir, sodra_install_dir)
    
    # setup sodra data directory
    setup_sodra_data_dir(cassandra_data_dir, sodra_install_dir)

def log_options(options):
    cassandra_lib_dir = options.cassandra_lib
    cassandra_conf_dir = options.cassandra_conf
    cassandra_data_dir = options.cassandra_data
    sodra_install_dir = options.sodra_install_dir
    logging.info('Cassandra lib dir : ' + cassandra_lib_dir)
    logging.info('Cassandra conf dir : ' + cassandra_conf_dir)
    logging.info('Cassandra data dir : ' + cassandra_data_dir)
    logging.info('Sodra install dir : ' + sodra_install_dir)
    
def delete_sodra(options):
    cassandra_lib_dir = options.cassandra_lib
    cassandra_conf_dir = options.cassandra_conf
    cassandra_data_dir = options.cassandra_data
    
    sodra_data_dir = os.path.join(cassandra_data_dir, 'sodra')
    logging.info('Deleting sodra data dir ' + sodra_data_dir)
    shutil.rmtree(sodra_data_dir)

    sodra_script = os.path.join('/usr/sbin/sodra_docker')
    logging.info('Deleting sodra script' + sodra_script)
    os.remove(sodra_script)
    
    sodra_conf_dir = os.path.join(cassandra_conf_dir, 'sodra_conf')
    logging.info('Deleting sodra conf dir ' + sodra_conf_dir)
    shutil.rmtree(sodra_conf_dir)
    
    sodra_lib_dir = os.path.join(cassandra_lib_dir, 'sodra_lib')
    logging.info('Deleting sodra lib dir ' + sodra_lib_dir)
    shutil.rmtree(sodra_lib_dir)

def parseargs(args):
    parser = argparse.ArgumentParser(description='Sodra setup arguments')
    parser.add_argument('-l', '--cassandra_lib', dest='cassandra_lib', type=str,
                        help='Path to cassandra lib directory', default='/usr/share/cassandra/')
    parser.add_argument('-f', '--cassandra_conf', dest='cassandra_conf', type=str,
                        help='Path to cassandra conf directory', default='/etc/cassandra/')
    parser.add_argument('-d', '--cassandra_data', dest='cassandra_data', type=str,
                        help='Path to cassandra data directory', default='/var/lib/cassandra/data/')
    parser.add_argument('-s', '--sodra_install_dir', dest='sodra_install_dir', type=str,
                        help='Path to sodra install dir', default='/sodra_install')
    parser.add_argument('-r', '--remove', dest='remove', action='store_true',
                        help='un-install sodra')
    return parser.parse_args(args)
    
if __name__ == '__main__':
    options = parseargs(sys.argv[1:])
    logging.basicConfig(filename='/docker.setup.log', level=logging.INFO)
    log_options(options)
    if options.remove:
        delete_sodra(options)
    else:
        setup_sodra(options)
    
