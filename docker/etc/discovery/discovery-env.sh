#!/bin/bash

export METATRON_ENV_MODE=local
export METATRON_PID_DIR="$BDL_DIR/data"
export METATRON_INDEX_DIR="$BDL_DATA_DIR/discovery/indexes"
export METATRON_DB_TYPE=h2
export METATRON_H2_DATA_DIR="$BDL_DIR/data/discovery/h2db"
export METATRON_LOG_DIR="$BDL_LOG_DIR/discovery"