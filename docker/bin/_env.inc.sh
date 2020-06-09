export BDL_DIR="/opt/bdl"
export BDL_HOME=$BDL_DIR
export BDL_DATA_DIR="$BDL_DIR/data"
export BDL_LOG_DIR="$BDL_DIR/log"
export BDL_CONF_DIR="$BDL_DIR/etc"
export JAVA_HOME=$JDK8_HOME
export PATH=$JAVA_HOME/bin:$PATH

# Metatron
export METATRON_HOME="$BDL_DIR/lib/discovery"
export METATRON_CONF_DIR="$BDL_CONF_DIR/discovery"
export METATRON_ENV_MODE=local
export METATRON_PID_DIR="$BDL_DIR/data"
export METATRON_LOG_DIR="$BDL_LOG_DIR/discovery"
export METATRON_INDEX_DIR="$BDL_DATA_DIR/discovery/indexes"

# Raise default ulimits to more reasonable values
ulimit -Sn hard
ulimit -Su hard

# create data and log folders if they don't exist
data_folders=("discovery")
for df in "${data_folders[@]}"
do
	if [ ! -d $BDL_DIR/data/$df ] ; then
		mkdir -p $BDL_DIR/data/$df
	fi
done

log_folders=("discovery")
for lf in "${log_folders[@]}"
do
	if [ ! -d $BDL_DIR/log/$lf ] ; then
		mkdir -p $BDL_DIR/log/$lf
	fi
done
