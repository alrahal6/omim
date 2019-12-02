from pyaloha.base import DataAggregator as BaseDataAggregator
from pyaloha.base import DataStreamWorker as BaseDataStreamWorker
from pyaloha.base import StatsProcessor as BaseStatsProcessor


class DataStreamWorker(BaseDataStreamWorker):
    def __init__(self):
        super(DataStreamWorker, self).__init__()

        self.events = []

    def process_unspecified(self, event):
        self.events.append(event)


class DataAggregator(BaseDataAggregator):
    def aggregate(self, results):
        for event in results.events:
            print(event)


class StatsProcessor(BaseStatsProcessor):
    def gen_stats(self, *args):
        return []
