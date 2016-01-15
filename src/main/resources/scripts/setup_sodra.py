#!/usr/bin/env python

import sys
import os
from optparse import OptionParser
import shutil
import stat
import yaml

def copy_sodra_files(cassandra_home_dir, sodra_dist_contents):
    sodra_jar = None
    for file in sodra_dist_contents:
        if file.startswith('sodra') and file.endswith('.jar'):
            sodra_jar = file
    if sodra_jar is None:
        raise Exception('Could not find sodra jar file in the current directory')
    lib_contents = set(os.listdir('lib'))
    cassandra_lib_dir = os.path.join(cassandra_home_dir, 'lib')
    
    # copy all the sodra dependency lib
    for sodra_lib_file in lib_contents:
        sodra_lib_file = os.path.join('lib', sodra_lib_file)
        shutil.copy(sodra_lib_file, cassandra_lib_dir)
    
    # copy the sodra jar file
    shutil.copy(sodra_jar, cassandra_lib_dir)
    
    # copy the sodra shell script to cassandra "bin" dir
    shutil.copyfile(os.path.join('scripts', 'sodra'), os.path.join(cassandra_home_dir, 'bin', 'sodra'))
    
    # make the sodra script executable
    os.chmod(os.path.join(cassandra_home_dir, 'bin', 'sodra'), stat.S_IRUSR | stat.S_IWUSR | stat.S_IXUSR)

def create_solr_home(cassandra_home_dir, sodra_dist_contents):
    cassandra_conf_file = os.path.join(cassandra_home_dir, 'conf', 'cassandra.yaml')
    config = yaml.load(open(cassandra_conf_file))
    cassandra_data_dir = os.path.join(cassandra_home_dir, 'data')
    if 'data_file_directories' in config:
        data_dirs = config['data_file_directories']
        if data_dirs is not None and len(data_dirs) > 0:
            cassandra_data_dir = data_dirs[0]
    
    sodra_dir = os.path.join(cassandra_data_dir, 'sodra')
    if os.path.exists(sodra_dir):
        raise Exception('Previous stale "sodra" directory exists inside : ' + cassandra_data_dir + ' . Delete that directory')
    os.makedirs(sodra_dir)
            
    solr_home_dir = os.path.join(cassandra_data_dir, 'sodra', 'solr')
    os.makedirs(solr_home_dir)
    
    shutil.copytree('config/index_template_config', os.path.join(sodra_dir, 'index_template_config'))
    
    shutil.copyfile('config/solr.xml', os.path.join(solr_home_dir, 'solr.xml'))
    
def setup_sodra(cassandra_home_dir=None):
    
    if cassandra_home_dir is None:
        cassandra_home_dir = os.getenv('CASSANDRA_HOME')
        
    if cassandra_home_dir is None:
        raise Exception('You should define "CASSANDRA_HOME" environment variable')
    
    sodra_dist_contents = set(os.listdir('.'))
    if 'lib' not in sodra_dist_contents:
        raise Exception('Usage : ./scripts/setup_sodra.py')

    copy_sodra_files(cassandra_home_dir, sodra_dist_contents)
    
    create_solr_home(cassandra_home_dir, sodra_dist_contents)
    
if __name__ == '__main__':
    setup_sodra()
    
