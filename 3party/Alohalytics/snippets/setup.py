#!/usr/bin/env python
# -*- coding: utf-8 -*-
import io
import os
import sys
import re
from setuptools import setup, find_packages
from distutils.command.build_clib import build_clib
from distutils.command.install_lib import install_lib
from distutils import log

NAME = "pyaloha"
DESCRIPTION = "Alohalytics statistics engine python parser"
URL = "https://github.com/mapsme/Alohalytics"
EMAIL = "me@alex.bio"
AUTHOR = "Alexander Zolotarev"

here = os.path.abspath(os.path.dirname(__file__))
about = {}
project_slug = NAME.lower().replace("-", "_").replace(" ", "_")
with open(os.path.join(here, project_slug, '__init__.py')) as f:
    exec(f.read(), about)

class build_clib_shared(build_clib, object):
    def build_libraries(self, libraries):
        for (lib_name, build_info) in libraries:
            sources = build_info.get('sources')
            if sources is None or not isinstance(sources, (list, tuple)):
                raise DistutilsSetupError(
                       "in 'libraries' option (library '%s'), "
                       "'sources' must be present and must be "
                       "a list of source filenames" % lib_name)
            sources = list(sources)

            log.info("building '%s' library", lib_name)

            # First, compile the source code to object files in the library
            # directory.  (This should probably change to putting object
            # files in a temporary build directory.)
            macros = build_info.get('macros')
            include_dirs = build_info.get('include_dirs')
            extra_preargs = build_info.get('extra_preargs')
            extra_postargs = build_info.get('extra_postargs')
            target_lang = build_info.get('target_lang')
            objects = self.compiler.compile(sources,
                                            output_dir=self.build_temp,
                                            macros=macros,
                                            include_dirs=include_dirs,
                                            extra_preargs=extra_preargs,
                                            extra_postargs=extra_postargs,
                                            debug=self.debug)

            # Now "link" the object files together into a static library.
            # (On Unix at least, this isn't really linking -- it just
            # builds an archive.  Whatever.)
            self.compiler.link_shared_object(objects, lib_name,
                                            output_dir=self.build_clib,
                                            target_lang=target_lang,
                                            debug=self.debug)


class install_lib_custom(install_lib, object):
    def install(self):
        outfiles = super(install_lib_custom, self).install()

        log.info("copying additional libraries")
        build_clib = self.get_finalized_command('build_clib')
        for lib in build_clib.libraries:
            libname = lib[0]
            ext = build_clib.compiler.shared_lib_extension
            target_libname = os.path.join(
                self.install_dir, *libname.split(".")
            ) + ext

            self.copy_file(
                os.path.join(build_clib.build_clib, libname),
                target_libname
            )
            if outfiles is None:
                outfiles = []
            outfiles.append(target_libname)
        return outfiles


c_api = (
    'pyaloha.iterate_events',
    {
        'sources': ['c_api/iterate_events.cc'],
        'include_dirs': ['../Alohalytics/src'],
        'extra_preargs': ['-std=c++11'],
        'target_lang': 'c++',
    }
)

setup(
    name=NAME,
    version=about['__version__'],
    description=DESCRIPTION,
    author=AUTHOR,
    author_email=EMAIL,
    url=URL,
    license='MIT',
    libraries=[c_api],
    packages=find_packages(),
    cmdclass={
        'build_clib': build_clib_shared,
        'install_lib': install_lib_custom,
    },
    classifiers=[
        # Trove classifiers
        # Full list: https://pypi.python.org/pypi?%3Aaction=list_classifiers
        'License :: OSI Approved :: MIT License',
        'Programming Language :: Python',
        'Programming Language :: Python :: 2',
        'Programming Language :: Python :: 2.7',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.5',
        'Programming Language :: Python :: 3.6',
        'Programming Language :: Python :: Implementation :: CPython',
    ],
)

