"""Launcher and pipeline implementation module."""

from __future__ import division

import multiprocessing
import os
import sys

from pyaloha.protocol import WorkerResults, str2date
from pyaloha.settings import DEFAULT_WORKER_NUM, DEFAULT_ALOHA_DATA_DIR
from pyaloha.worker import invoke_cmd_worker, load_plugin, setup_logs


def cmd_run(plugin_dir,
            data_dir=DEFAULT_ALOHA_DATA_DIR,
            worker_num=DEFAULT_WORKER_NUM):
    """Main command line interface to pyaloha system."""
    # TODO: argparse
    plugin_name = sys.argv[1]
    start_date, end_date = map(str2date, sys.argv[2:4])
    try:
        events_limit = int(sys.argv[4])
    except IndexError:
        events_limit = 0

    main_script(
        plugin_name,
        start_date, end_date,
        plugin_dir=plugin_dir, events_limit=events_limit,
        data_dir=data_dir,
        worker_num=worker_num
    )


def main_script(plugin_name, start_date, end_date, plugin_dir, data_dir,
                worker_num,
                results_dir='./stats',
                events_limit=0):
    """
    Running pyAloha stats processing pipeline.

    0. Load worker, aggregator, processor classes from a specified plugin
    1. Run workers (data preprocessors) on aloha files within specified range
    2. Accumulate [and postprocess] worker results with an aggregator instance
    3. Run stats processor and print results to stdout
    """
    aggregator = aggregate_raw_data(
        data_dir, results_dir, plugin_dir, plugin_name,
        start_date, end_date, events_limit,
        worker_num=worker_num
    )

    stats = load_plugin(
        plugin_name, plugin_dir=plugin_dir
    ).StatsProcessor(aggregator)

    logger = multiprocessing.get_logger()

    logger.info('Stats: processing')
    stats.process_stats()

    logger.info('Stats: outputting')
    stats.print_stats()

    logger.info('Stats: done')


def aggregate_raw_data(
        data_dir, results_dir, plugin_dir, plugin,
        start_date=None, end_date=None,
        events_limit=0,
        worker_num=DEFAULT_WORKER_NUM):
    """Workers-aggregator subpipeline.

    0. Load worker, aggregator classes from a specified plugin
    1. Run workers in parallel (basing on server stats files)
    2. Accumulate results by an aggregator
    3. Run aggregator post processing
    """
    setup_logs()
    logger = multiprocessing.get_logger()

    files = [
        os.path.join(data_dir, fname)
        for fname in sorted(os.listdir(data_dir))
        if check_fname(fname, start_date, end_date)
    ]

    tasks = [
        (plugin_dir, plugin, fpath, events_limit)
        for fpath in files
    ]

    aggregator = load_plugin(
        plugin, plugin_dir=plugin_dir
    ).DataAggregator(results_dir)

    logger.info('Aggregator: start workers')

    # Let us create pools before main process will consume more memory
    # and let workers live forever (default) to exclude spontaneous forking
    worker_pool = multiprocessing.Pool(worker_num)
    # Just to be 100% safe we have no leaks, let us use separate pools
    # for work-aggregate phase and post aggregation.
    # Also, most of the post aggregation tasks heavily depend on the disk IO
    # so we do not need so much workers.
    post_aggregator_pool = multiprocessing.Pool(worker_num // 2)
    try:
        engine = worker_pool.imap_unordered
        batch_size = 2 * worker_num
        batch_number = len(tasks) // batch_size + 1
        for batch_no in range(batch_number):
            batch_start = batch_no * batch_size
            batch_tasks = tasks[batch_start: batch_start + batch_size]
            logger.info(
                'Aggregator: batch %d is being aggregated: %s' % (
                    batch_no, batch_tasks
                )
            )
            for file_name, results in engine(invoke_cmd_worker, batch_tasks):
                try:
                    results = WorkerResults.loads_object(results)
                    logger.info(
                        'Aggregator: task %s is being aggregated' % file_name
                    )
                    aggregator.aggregate(results)
                    logger.info('Aggregator: task %s done' % file_name)
                except Exception as e:
                    logger.exception(
                        'Aggregator: task %s failed:\n%s', file_name, e
                    )
    finally:
        worker_pool.terminate()
        worker_pool.join()

    logger.info('Aggregator: post_aggregate')

    try:
        aggregator.post_aggregate(pool=post_aggregator_pool)
    finally:
        post_aggregator_pool.terminate()
        post_aggregator_pool.join()

    logger.info('Aggregator: done')

    return aggregator


def check_fname(filename, start_date, end_date):
    """Checking if a server stats file is within dates range.

    NOTE: using server dates, not clients.
    """
    if filename[0] == '.':
        return False
    return _check_date(filename, start_date, end_date)


def _check_date(filename, start_date, end_date):
    try:
        fdate = str2date(filename[-11:-3])
    except (ValueError, IndexError):
        raise Exception(
            'Unidentified alohalytics stats filename: %s' % filename
        )

    if start_date and fdate < start_date:
        return False
    if end_date and fdate > end_date:
        return False
    return True
