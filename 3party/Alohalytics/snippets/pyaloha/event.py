"""Base event classes are defined in this module."""


def get_event(key, event_time, user_info,
              data_list=None, data_list_len=0):
    """Event factory function."""
    if data_list_len > 1:
        return DictEvent(
            key, event_time, user_info,
            data_list, data_list_len
        )
    elif data_list_len > 0:
        return ValueEvent(key, event_time, user_info, data_list[0])
    return Event(key, event_time, user_info)


class Event(object):
    """
    Base event class.

    Have only basic event info.
    """

    __slots__ = ('key', 'event_time', 'user_info')

    def __init__(self, key, event_time, user_info):
        """Copy ctypes structures into a pure Python ones."""
        self.event_time = event_time.make_object()
        self.user_info = user_info.make_object()
        self.key = key

    def process_me(self, processor):
        processor.process_unspecified(self)

    def __basic_dumpdict__(self):
        return self.__dumpdict__()

    def __dumpdict__(self):
        return {
            'key': self.key,
            'user_info': self.user_info,
            'event_time': self.event_time
        }


class ValueEvent(Event):
    """Add a single value to a base event."""

    __slots__ = ('key', 'event_time', 'user_info', 'value')

    def __init__(self, key, event_time, user_info, value):
        """Add self.value property to a base event info."""
        # No super constructor is used for performance reasons.
        self.event_time = event_time.make_object()
        self.user_info = user_info.make_object()
        self.key = key
        self.value = value

    def __dumpdict__(self):
        d = super(ValueEvent, self).__dumpdict__()
        d['value'] = self.value
        return d


class DictEvent(Event):
    """
    This is a simplified form of any Alohalytics pairs event.

    All event params (except datetime and user/device identification)
    are accumulated into a dict.
    """

    __slots__ = ('data',)

    def __init__(self,
                 key, event_time, user_info,
                 data_list, data_list_len):
        """Add event pairs to a dict self.data."""
        # No super constructor is used for performance reasons.
        self.event_time = event_time.make_object()
        self.user_info = user_info.make_object()
        self.key = key

        try:
            self.data = {
                data_list[i]: data_list[i + 1]
                for i in range(0, data_list_len, +2)
            }
        except IndexError:
            raise ValueError('Incorrect data_list')

    def __basic_dumpdict__(self):
        return super(DictEvent, self).__dumpdict__()

    def __dumpdict__(self):
        d = self.__basic_dumpdict__()
        d.update(self.data)
        return d
