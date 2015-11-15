#!/usr/bin/env python

import sys
import os
from optparse import OptionParser
import shutil

def parseargs(args):
    parser = OptionParser()
    parser.add_option("-c", "--cassandra_home", dest="cassandra_home", type='string',
                      help="Path to cassandra home directory")
    
    (options, args) = parser.parse_args(args)
    return (options, args)

def setup_sodra(cassandra_home_dir):
    contents = set(os.listdir('.'))
    if 'lib' not in contents:
        raise Exception('Usage : ./scripts/setup_sodra.py -c <cassandra_home>')
    sodra_jar = None
    for file in contents:
        if file.startswith('sodra'):
            sodra_jar = file
    if sodra_jar is None:
        raise Exception('Could not find sodra jar file in the current directory')
    lib_contents = set(os.listdir('lib'))
    cassandra_lib_dir = os.path.join(cassandra_home_dir, 'lib')
    
    # copy all the sodra dependency lib
    for sodra_lib_file in lib_contents:
        sodra_lib_file = os.path.join('lib', sodra_lib_file)
        shutil.copy(sodra_lib_file, cassandra_lib_dir)
    
    # finally copy the sodra jar file
    shutil.copy(sodra_jar, cassandra_lib_dir)
    
if __name__ == '__main__':
    (options, args) = parseargs(sys.argv[1:])
    if options.cassandra_home is None:
        raise Exception('Usage : ./scripts/setup_sodra.py -c <cassandra_home>')
    setup_sodra(options.cassandra_home)
    
