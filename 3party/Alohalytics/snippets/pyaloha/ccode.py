import ctypes
import itertools
import os

from pyaloha.protocol import SerializableDatetime

try:
    from ctypes import set_conversion_mode
except ImportError:
    pass
else:
    set_conversion_mode('utf8', 'strict')


def c_unicode(c_data):
    return unicode(ctypes.string_at(c_data), 'utf8')


def c_string(c_data):
    return c_data


class CEVENTTIME(ctypes.Structure):
    _fields_ = [
        ('client_creation', ctypes.c_uint64),
        ('server_upload', ctypes.c_uint64)
    ]

    def make_object(self):
        return PythonEventTime(self.server_upload, self.client_creation)


class PythonEventTime(object):
    __slots__ = (
        'client_creation', 'server_upload',
        'dtime'
    )

    msec_in_a_day = 24 * 3600 * 1000
    delta_past = 6 * 30 * msec_in_a_day
    delta_future = 1 * msec_in_a_day

    def __init__(self, server_dtime, client_dtime):
        if client_dtime < server_dtime - self.delta_past or\
                client_dtime > server_dtime + self.delta_future:
            dtime = server_dtime
        else:
            dtime = client_dtime

        self.client_creation = client_dtime
        self.server_upload = server_dtime
        self.dtime = SerializableDatetime.utcfromtimestamp(
            dtime / 1000.  # timestamp is in millisecs
        )

    @property
    def is_accurate(self):
        return self.dtime == self.client_creation

    def __dumpdict__(self):
        return {
            'dtime': self.dtime,
            'is_accurate': self.is_accurate
        }


class CUSERINFO(ctypes.Structure):
    _fields_ = [
        ('os', ctypes.c_byte),
        ('lat', ctypes.c_float),
        ('lon', ctypes.c_float),
        ('raw_uid', (ctypes.c_char * 32)),
    ]

    _os_valid_range = range(3)

    def make_object(self):
        if self.os not in self._os_valid_range:
            raise ValueError('Incorrect os value: %s' % self.os)

        lat = round(self.lat, 6)
        lon = round(self.lon, 6)
        if not lat and not lon:
            return PythonUserInfo(
                int(self.raw_uid, 16), self.os
            )

        return PythonGeoUserInfo(
            int(self.raw_uid, 16),
            self.os,
            lat, lon
        )


class PythonUserInfo(object):
    __slots__ = ('uid', 'os')

    def __init__(self, uid, os):
        self.uid = uid
        self.os = os

    @property
    def has_geo(self):
        return False

    def is_on_android(self):
        return self.os == 1

    def is_on_ios(self):
        return self.os == 2

    def is_on_unknown_os(self):
        return self.os == 0

    def __dumpdict__(self):
        return {
            'os': self.os,
            'uid': self.uid
        }


class PythonGeoUserInfo(PythonUserInfo):
    __slots__ = ('lat', 'lon')

    def __init__(self, uid, os, lat, lon):
        self.uid = uid
        self.os = os
        self.lat = lat
        self.lon = lon

    @property
    def has_geo(self):
        return True

    def __dumpdict__(self):
        return {
            'os': self.os,
            'uid': self.uid,
            'lat': self.lat,
            'lon': self.lon
        }


CCALLBACK = ctypes.CFUNCTYPE(
    None,
    ctypes.c_char_p,
    ctypes.POINTER(CEVENTTIME),
    ctypes.POINTER(CUSERINFO),
    ctypes.POINTER(ctypes.c_char_p),
    ctypes.c_int
)  # key, event_time, user_info, data, data_len


def iterate_events(stream_processor, events_limit):
    base_path = os.path.dirname(os.path.abspath(__file__))
    c_module = ctypes.cdll.LoadLibrary(
        os.path.join(base_path, 'iterate_events.so')
    )
    use_keys = tuple(itertools.chain.from_iterable(
        e.keys for e in stream_processor.__events__
    ))
    keylist_type = ctypes.c_char_p * len(use_keys)
    c_module.Iterate.argtypes = [
        CCALLBACK, keylist_type, ctypes.c_int, ctypes.c_int
    ]
    c_module.Iterate(
        CCALLBACK(
            stream_processor.process_event
        ),
        keylist_type(*use_keys),
        len(use_keys),
        events_limit
    )
