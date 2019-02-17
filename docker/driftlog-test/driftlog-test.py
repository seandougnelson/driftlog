#!/usr/bin/env python3

import logging
import time

logging.getLogger().setLevel(logging.INFO)

for x in range(1, 4):
    logging.info('Line ' + str(x))

logging.info('Sleeping...')
while True:
    time.sleep(1)
