#!/usr/bin/env python

import sys
import os
import argparse
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

    sodra_conf_dir = os.path.join(cassandra_home_dir, 'sodra_conf')
    if os.path.exists(sodra_conf_dir):
        raise Exception('Remove "sodra_conf" directory from cassandra home')
    # copy all the sodra conf
    shutil.copytree('config/sodra_conf', sodra_conf_dir)
    
    sodra_lib_dir = os.path.join(cassandra_home_dir, 'sodra_lib')
    if os.path.exists(sodra_lib_dir):
        raise Exception('Remove "sodra_lib" directory from cassandra home')
    os.makedirs(sodra_lib_dir)
    
    # copy all the sodra dependency lib
    for sodra_lib_file in lib_contents:
        sodra_lib_file = os.path.join('lib', sodra_lib_file)
        shutil.copy(sodra_lib_file, sodra_lib_dir)
    
    # copy the sodra jar file
    shutil.copy(sodra_jar, sodra_lib_dir)
    
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

def setup_sodra_files(cassandra_home_dir):
    
    if cassandra_home_dir is None:
        raise Exception('cassandra home cannot be None')

    sodra_dist_contents = set(os.listdir('.'))
    if 'lib' not in sodra_dist_contents:
        raise Exception('Usage : ./scripts/setup_sodra.py')

    copy_sodra_files(cassandra_home_dir, sodra_dist_contents)
    
    create_solr_home(cassandra_home_dir, sodra_dist_contents)

def setup_sodra(cassandra_home_dir=None):
    
    if cassandra_home_dir is None:
        cassandra_home_dir = os.getenv('CASSANDRA_HOME')
        
    if cassandra_home_dir is None:
        raise Exception('You should define "CASSANDRA_HOME" environment variable')
    
    setup_sodra_files(cassandra_home_dir)
    
def delete_sodra_files(cassandra_home_dir):
    
    if cassandra_home_dir is None:
        raise Exception('cassandra home cannot be None')
    
    cassandra_conf_file = os.path.join(cassandra_home_dir, 'conf', 'cassandra.yaml')
    config = yaml.load(open(cassandra_conf_file))
    cassandra_data_dir = os.path.join(cassandra_home_dir, 'data')
    if 'data_file_directories' in config:
        data_dirs = config['data_file_directories']
        if data_dirs is not None and len(data_dirs) > 0:
            cassandra_data_dir = data_dirs[0]
    sodra_dir = os.path.join(cassandra_data_dir, 'sodra')
    shutil.rmtree(sodra_dir)
    
    sodra_script = os.path.join(cassandra_home_dir, 'bin', 'sodra')
    os.remove(sodra_script)
    
    sodra_conf_dir = os.path.join(cassandra_home_dir, 'sodra_conf')
    shutil.rmtree(sodra_conf_dir)

    sodra_lib_dir = os.path.join(cassandra_home_dir, 'sodra_lib')
    shutil.rmtree(sodra_lib_dir)
    
def delete_sodra(cassandra_home_dir=None):
    
    if cassandra_home_dir is None:
        cassandra_home_dir = os.getenv('CASSANDRA_HOME')
        
    if cassandra_home_dir is None:
        raise Exception('You should define "CASSANDRA_HOME" environment variable')
    
    delete_sodra_files(cassandra_home_dir)

def parseargs(args):
    parser = argparse.ArgumentParser(description='Sodra setup arguments')
    parser.add_argument('-c', '--cassandra_home', dest='cassandra_home', type=str,
                      help='Path to cassandra home directory')
    parser.add_argument('-d', '--delete', dest='delete', action='store_true',
                      help='Delete sodra installation')
    parser.add_argument('-p', '--pseudo', dest='pseudo', action='store_true',
                      help='Pseduo distribution mode (single host)')
    parser.add_argument('-a', '--all_homes', dest='all_homes', type=file,
                      help='All cassandra home locations for pseudo mode in a file - one location per line')
    
    return parser.parse_args(args)
    
def pseudo_setup(options):
    if options.all_homes is None:
        raise Exception('all_homes location argument is missing')
    for cassandra_home_dir in options.all_homes:
        cassandra_home_dir = cassandra_home_dir.strip()
        setup_sodra_files(cassandra_home_dir)

def pseudo_delete(options):
    if options.all_homes is None:
        raise Exception('all_homes location argument is missing')
    for cassandra_home_dir in options.all_homes:
        cassandra_home_dir = cassandra_home_dir.strip()
        delete_sodra_files(cassandra_home_dir)
    
if __name__ == '__main__':
    options = parseargs(sys.argv[1:])
    if options.pseudo:
        if options.delete:
            pseudo_delete(options)
        else:
            pseudo_setup(options)
    elif options.delete:
        if options.cassandra_home is not None:
            delete_sodra(options.cassandra_home)
        else:
            delete_sodra()
    else:
        if options.cassandra_home is not None:
            setup_sodra(options.cassandra_home)
        else:
            setup_sodra()
    
