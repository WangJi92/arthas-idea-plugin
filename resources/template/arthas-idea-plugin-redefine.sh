#!/usr/bin/env bash

TARGET_PID=
JAVA_HOME=
# reset arthas work environment
# reset some options for env
reset_for_env()
{
    # if env define the JAVA_HOME, use it first
    # if is alibaba opts, use alibaba ops's default JAVA_HOME
    [ -z "${JAVA_HOME}" ] && [ -d /opt/taobao/java ] && JAVA_HOME=/opt/taobao/java

    if [[ (-z "${JAVA_HOME}") && ( -e "/usr/libexec/java_home") ]]; then
        # for mac
        JAVA_HOME=`/usr/libexec/java_home`
    fi

    # iterater throught candidates to find a proper JAVA_HOME at least contains tools.jar which is required by arthas.
    if [ ! -d "${JAVA_HOME}" ]; then
        JAVA_HOME_CANDIDATES=($(ps aux | grep java | grep -v 'grep java' | awk '{print $11}' | sed -n 's/\/bin\/java$//p'))
        for JAVA_HOME_TEMP in ${JAVA_HOME_CANDIDATES[@]}; do
            if [ -f "${JAVA_HOME_TEMP}/lib/tools.jar" ]; then
                JAVA_HOME=`rreadlink "${JAVA_HOME_TEMP}"`
                break
            fi
        done
    fi

    if [ -z "${JAVA_HOME}" ]; then
        exit_on_err 1 "Can not find JAVA_HOME, please set \$JAVA_HOME bash env first."
    fi

    echo "[INFO] JAVA_HOME: ${JAVA_HOME}"

    # reset CHARSET for alibaba opts, we use GBK
    [[ -x /opt/taobao/java ]] && JVM_OPTS="-Dinput.encoding=GBK ${JVM_OPTS} "

}
select_pid(){
    local IFS=$'\n'
    CANDIDATES=($(${JAVA_HOME}/bin/jps -l | grep -v sun.tools.jps.Jps | awk '{print $0}'))

    index=0
    suggest=1
    # auto select tomcat/pandora-boot process
    for process in "${CANDIDATES[@]}"; do
        index=$(($index+1))
        if [ $(echo ${process} | grep -c org.apache.catalina.startup.Bootstrap) -eq 1 ] \
            || [ $(echo ${process} | grep -c com.taobao.pandora.boot.loader.SarLauncher) -eq 1 ]
        then
           suggest=${index}
           break
        fi
    done

    index=0
    for process in "${CANDIDATES[@]}"; do
        index=$(($index+1))
        if [ ${index} -eq ${suggest} ]; then
            echo "* [$index]: ${process}"
        else
            echo "  [$index]: ${process}"
        fi
    done

    read choice

    if [ -z ${choice} ]; then
        choice=${suggest}
    fi

    TARGET_PID=`echo ${CANDIDATES[$(($choice-1))]} | cut -d ' ' -f 1`
}

# Usage: banner_simple "my title"
banner_simple() {
  local msg="* $* *"
  local edge=$(echo "$msg" | sed 's/./*/g')
  echo "$edge"
  echo "$(tput bold)$msg$(tput sgr0)"
  echo "$edge"
  echo
}
# Usage exit shell with err_code
# $1 : err_code
# $2 : err_msg
exit_on_err() {
  [[ ! -z "${2}" ]] && banner_simple $(echo $(tput setaf 1)${2} $(tput sgr0)) 1>&2
  exit ${1}
}

# check arthas permission
check_permission() {
  [ ! -w "$HOME" ] &&
    exit_on_err 1 "permission denied, $HOME is not writable."
}

createFile() {
  if [ ! -f $1 ]; then
    mkdir -p "$(dirname "$1")" && touch $1 >/dev/null 2>&1 && echo "File $1 created." || return 1
  fi
}

installArthas() {
  if [ ! -d "$HOME/opt/arthas" ]; then
    banner_simple "arthas idea plugin make dir $HOME/opt/arthas"
    mkdir -p $HOME/opt/arthas || return 1
  fi
  # download arthas
  if [ ! -f "$HOME/opt/arthas/as.sh" ]; then
    banner_simple "arthas idea plugin download arthas $HOME/opt/arthas/as.sh"
    curl -Lk https://arthas.aliyun.com/as.sh  -o $HOME/opt/arthas/as.sh || return 1
    chmod +x $HOME/opt/arthas/as.sh || return 1
  fi
}
# xxxClassBase64Str|xxxClassPath,xxxClass2Base64Str|xxxClass2Path
decodebase64CLassFile() {
  bash64FileAndPathList="${arthasIdeaPluginBase64AndPathCommand}"
  commaArraybash64FilePath=(${bash64FileAndPathList//\,/ })
  for i in "${!commaArraybash64FilePath[@]}"; do
    verticalArraySingleBash64FileAndPath=(${commaArraybash64FilePath[i]//\|/ })
    createFile ${verticalArraySingleBash64FileAndPath[1]}
    echo ${verticalArraySingleBash64FileAndPath[0]} | base64 --decode >${verticalArraySingleBash64FileAndPath[1]} || return 1
    echo "base64Class decode to path ${verticalArraySingleBash64FileAndPath[1]}"
  done
}

# Usage: doStarteRedefine
doStarteRedefine() {
  createFile $HOME/opt/arthas/redefine/redefineArthas.out
  echo $(tput bold)"arthas start command :$HOME/opt/arthas/as.sh --select ${TARGET_PID}  -c \"${arthasIdeaPluginRedefineCommand}\"  >$HOME/opt/arthas/redefine/redefineArthas.out"$(tput sgr0)
  $HOME/opt/arthas/as.sh --select ${TARGET_PID} -c "${arthasIdeaPluginRedefineCommand}" >$HOME/opt/arthas/redefine/redefineArthas.out
}

redefineResult() {
  cat $HOME/opt/arthas/redefine/redefineArthas.out
  redefineResult=$(cat $HOME/opt/arthas/redefine/redefineArthas.out | grep "redefine success")
  if [ -z "$redefineResult" ]; then
    banner_simple $(echo $(tput setaf 1)arthas idea plugin redefine error $(tput sgr0))
  else
    banner_simple "arthas idea plugin redefine success"
  fi
}

#delete file
doClenFile() {
  if [ ! -z "${deleteClassFile}" ]; then
    rm -rf $HOME/opt/arthas/redefine
    echo "arthas idea plugin delete class file $HOME/opt/arthas/redefine ok"
  fi
}

# the main function
main() {

  banner_simple "arthas idea plugin redefine begin;start script path: $(pwd)/arthas-idea-plugin-redefine.sh"

  check_permission

  installArthas
  if [ $? -ne 0 ]; then
    exit_on_err 1 "arthas install as.sh script error"
  fi

  decodebase64CLassFile
  if [ $? -ne 0 ]; then
    exit_on_err 1 "arthas idea plugin decodebase64CLass error"
  fi

  reset_for_env

  select_pid

  doStarteRedefine

  redefineResult

  doClenFile
}

main "${@}"
