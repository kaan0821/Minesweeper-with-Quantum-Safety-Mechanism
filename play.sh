#!/bin/sh

DIRM=`pwd`

DIRM_L="$DIRM/../../libs"
DIRM_SRC="$DIRM/.."

LOGICNG_DIR="$DIRM_L/logicng-2.4.1.jar"
ANTLR_DIR="$DIRM_L/antlr-runtime-4.9.3.jar"

CLASSPATH=".:$CLASSPATH:$DIRM:$DIRM_SRC:$DIRM_L:$LOGICNG_DIR:$ANTLR_DIR"

export CLASSPATH

javac $DIRM_SRC/Main/*.java $DIRM_SRC/model/*.java $DIRM_SRC/guiDelegate/*.java

java Main.Pmain $*
