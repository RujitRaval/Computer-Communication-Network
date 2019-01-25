"""
A traceroute output parser, structuring the traceroute into a
sequence of hops, each containing individual probe results.

Courtesy of the Netalyzr project: http://netalyzr.icsi.berkeley.edu
"""
# ChangeLog
# ---------
#
# 1.0:  Initial release, tested on Linux/Android traceroute inputs only.
#       Also Python 2 only, most likely. (Send patches!)
#
# Copyright 2013 Christian Kreibich. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are
# met:
#
#    1. Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#
#    2. Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#
# THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
# WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY DIRECT,
# INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
# STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
# IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

import io
import re
import json

class Probe(object):
    """
    Abstraction of an individual probe in a traceroute.
    """
    def __init__(self):
        self.ipaddr = None
        self.name = None
        self.rtt = None # RTT in ms
        self.anno = None # Annotation, such as !H, !N, !X, etc

    def clone(self):
        """
        Return a copy of this probe, conveying the same endpoint.
        """
        copy = Probe()
        copy.ipaddr = self.ipaddr
        copy.name = self.name
        return copy

class Hop(object):
    """
    A traceroute hop consists of a number of probes.
    """
    def __init__(self):
        self.idx = None # Hop count, starting at 1
        self.probes = [] # Series of Probe instances

    def add_probe(self, probe):
        """Adds a Probe instance to this hop's results."""
        self.probes.append(probe)

    def __str__(self):
        res = []
        last_probe = None
        for probe in self.probes:
            if probe.name is None:
                res.append('*')
                continue
            anno = '' if probe.anno is None else ' ' + probe.anno
            if last_probe is None or last_probe.name != probe.name:
                res.append('%s (%s) %1.3f ms%s' % (probe.name, probe.ipaddr,
                                                   probe.rtt, anno))
            else:
                res.append('%1.3f ms%s' % (probe.rtt, anno))
            last_probe = probe
        return '  '.join(res)

class TracerouteParser(object):
    """
    A parser for traceroute text. A traceroute consists of a sequence of
    hops, each of which has at least one probe. Each probe records IP,
    hostname and timing information.
    """
    HEADER_RE = re.compile(r'traceroute to (\S+) \((\d+\.\d+\.\d+\.\d+)\)')

    def __init__(self):
        self.dest_ip = None
        self.dest_name = None
        self.hops = []

    def __str__(self):
        res = ['traceroute to %s (%s)' % (self.dest_name, self.dest_ip) ]
        ctr = 1
        for hop in self.hops:
            res.append('%2d  %s' % (ctr, str(hop)))
            ctr += 1
        return '\n'.join(res)

    def parse_data(self, data):
        """Parser entry point, given string of the whole traceroute output."""
        self.parse_hdl(io.StringIO(data))

    def parse_hdl(self, hdl):
        """Parser entry point, given readable file handle."""
        self.dest_ip = None
        self.dest_name = None
        self.hops = []

        for line in hdl:
            line = line.strip()
            if line == '':
                continue
            if line.lower().startswith('traceroute'):
                # It's the header line at the beginning of the traceroute.
                mob = self.HEADER_RE.match(line)
                if mob:
                    self.dest_ip = mob.group(2)
                    self.dest_name = mob.group(1)
            else:
                hop = self._parse_hop(line)
                self.hops.append(hop)

    def _parse_hop(self, line):
        """Internal helper, parses a single line in the output."""
        parts = line.split()
        parts.pop(0) # Drop hop number, implicit in resulting sequence
        hop = Hop()
        probe = None

        while len(parts) > 0:
            probe = self._parse_probe(parts, probe)
            if probe:
                hop.add_probe(probe)

        return hop

    def _parse_probe(self, parts, last_probe=None):
        """Internal helper, parses the next probe's results from a line."""
        try:
            probe = Probe() if last_probe is None else last_probe.clone()

            tok1 = parts.pop(0)
            if tok1 == '*':
                return probe

            tok2 = parts.pop(0)
            if tok2 == 'ms':
                # This is an additional RTT for the same endpoint we
                # saw before.
                probe.rtt = float(tok1)
                if len(parts) > 0 and parts[0].startswith('!'):
                    probe.anno = parts.pop(0)
            else:
                # This is a probe result from a different endpoint
                probe.name = tok1
                probe.ipaddr = tok2[1:][:-1]
                probe.rtt = float(parts.pop(0))
                parts.pop(0) # Drop "ms"
                if len(parts) > 0 and parts[0].startswith('!'):
                    probe.anno = parts.pop(0)

            return probe

        except (IndexError, ValueError):
            return None

def demo():
    """A simple example."""

    tr_data = """
traceroute to cs.ucsb.edu (128.111.27.13), 30 hops max, 60 byte packets
 1  141.22.213.33 (141.22.213.33)  0.386 ms  0.294 ms  0.350 ms
 2  141.22.4.148 (141.22.4.148)  0.420 ms  0.273 ms  0.286 ms
 3  gr-ham1-te1-3.x-win.dfn.de (188.1.231.165)  0.764 ms  0.509 ms  0.438 ms
 4  cr-ham1-be2.x-win.dfn.de (188.1.144.214)  0.619 ms  0.618 ms  0.623 ms
 5  cr-tub2-hundredgige0-0-0-5.x-win.dfn.de (188.1.144.58)  5.165 ms  5.191 ms  5.236 ms
 6  dfn.mx1.ham.de.geant.net (62.40.112.145)  14.762 ms  14.622 ms  14.654 ms
 7  ae3.mx1.ams.nl.geant.net (62.40.98.60)  21.092 ms  21.061 ms  21.100 ms
 8  ae2.mx1.lon.uk.geant.net (62.40.98.80)  28.595 ms  28.629 ms  28.507 ms
 9  internet2-gw.mx1.lon.uk.geant.net (62.40.124.45)  104.653 ms  104.215 ms  103.778 ms
 10  et-7-1-0.4070.rtsw.chic.net.internet2.edu (198.71.45.56)  121.078 ms  120.246 ms  120.328 ms
 11  et-5-0-0.4070.rtsw.kans.net.internet2.edu (198.71.45.15)  131.281 ms  *  *

"""
    # Create parser instance:
    trp = TracerouteParser()

    # Give it some data:
    trp.parse_data(tr_data)


    # Built-up data structures as string. Should look effectively
    # identical to the above input string.
    print(trp)
    print(type(trp))
    #json.dumps(trp)

if __name__ == '__main__':
    demo()
