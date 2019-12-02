import warnings

from multiprocessing import cpu_count

DEFAULT_WORKER_NUM = cpu_count() - 1

if DEFAULT_WORKER_NUM == 0:
    raise RuntimeError('PyAloha will not work with only one cpu core')

if DEFAULT_WORKER_NUM < 3:
    warnings.warn(
        'PyAloha works very slowly on the small amount of cpu cores',
        RuntimeWarning
    )

DEFAULT_ALOHA_DATA_DIR = '/mnt/disk1/alohalytics/by_date'
